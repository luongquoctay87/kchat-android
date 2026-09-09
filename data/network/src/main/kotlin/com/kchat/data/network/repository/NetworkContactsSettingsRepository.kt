package com.kchat.data.network.repository

import com.kchat.core.model.ContactSummary
import com.kchat.data.network.api.KChatApi
import com.kchat.data.network.apiMessage
import com.kchat.data.network.apiResult
import com.kchat.data.network.dto.CreateDirectRoomRequest
import com.kchat.data.network.mapper.toModel
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.EmergencyWipeStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkContactsRepository @Inject constructor(
    private val api: KChatApi,
    private val emergencyWipeStore: EmergencyWipeStore,
    @javax.inject.Named("apiBaseUrl") private val apiBaseUrl: String,
) : ContactsRepository {
    private val contacts = MutableStateFlow<List<ContactSummary>>(emptyList())

    override fun observeContacts(): Flow<List<ContactSummary>> = contacts.asStateFlow()

    override suspend fun clearForEmergencyWipe() {
        // Keep contacts intact during message wipe
    }

    override fun restoreAfterLogout() = Unit

    override suspend fun refreshContacts() {
        try {
            contacts.value = api.getContacts().map { dto ->
                dto.toModel().withAbsoluteAvatarUrl(apiBaseUrl).copy(isContact = true)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Keep last-known contacts.
        }
    }

    override suspend fun openDirectChat(contactId: String): Result<String> = apiResult("Không mở được chat riêng") {
        api.createDirectRoom(CreateDirectRoomRequest(userId = contactId)).id
    }

    override suspend fun searchUsers(query: String): Result<List<ContactSummary>> = apiResult("Không tìm được người dùng") {
        api.searchUsers(query.trim()).map { dto ->
            dto.toModel().withAbsoluteAvatarUrl(apiBaseUrl)
        }
    }

    override suspend fun addContact(contact: ContactSummary): Result<Unit> {
        val userId = contact.id.trim()
        if (userId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Không thêm được vào danh bạ"))
        }
        val optimistic = contact.copy(id = userId, isContact = true)
            .withAbsoluteAvatarUrl(apiBaseUrl)
        upsertLocal(optimistic)
        return runCatching {
            val response = api.addContact(userId)
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
            val added = response.body()?.toModel()
                ?.withAbsoluteAvatarUrl(apiBaseUrl)
                ?.copy(isContact = true)
            if (added != null) {
                upsertLocal(added)
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                if (error.isEmptyResponseBody()) {
                    Result.success(Unit)
                } else {
                    contacts.value = contacts.value.filterNot {
                        it.id.equals(userId, ignoreCase = true)
                    }
                    Result.failure(Exception(error.apiMessage("Không thêm được vào danh bạ")))
                }
            },
        )
    }

    override suspend fun removeContact(userId: String): Result<Unit> = apiResult("Không xóa được khỏi danh bạ") {
        api.removeContact(userId)
        contacts.value = contacts.value.filterNot { it.id.equals(userId, ignoreCase = true) }
    }

    private fun upsertLocal(contact: ContactSummary) {
        contacts.value = (contacts.value.filterNot { it.id.equals(contact.id, ignoreCase = true) } + contact)
            .sortedBy { it.name.lowercase() }
    }
}

private fun Throwable.isEmptyResponseBody(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is java.io.EOFException) return true
        val msg = current.message.orEmpty()
        if (msg.contains("EOF", ignoreCase = true) ||
            msg.contains("End of input", ignoreCase = true) ||
            msg.contains("Expected start of the object", ignoreCase = true)
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

private fun ContactSummary.withAbsoluteAvatarUrl(baseUrl: String): ContactSummary {
    val relative = avatarUrl ?: return this
    if (relative.startsWith("http://") || relative.startsWith("https://")) return this
    return copy(avatarUrl = baseUrl.trimEnd('/') + relative)
}
