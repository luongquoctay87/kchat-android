package com.kchat.data.network.repository

import com.kchat.core.model.ContactSummary
import com.kchat.data.network.api.KChatApi
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
        contacts.value = emptyList()
    }

    override fun restoreAfterLogout() = Unit

    override suspend fun refreshContacts() {
        if (emergencyWipeStore.isActiveNow()) return
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

    override suspend fun addContact(userId: String): Result<Unit> = apiResult("Không thêm được vào danh bạ") {
        api.addContact(userId)
        refreshContacts()
    }

    override suspend fun removeContact(userId: String): Result<Unit> = apiResult("Không xóa được khỏi danh bạ") {
        api.removeContact(userId)
        contacts.value = contacts.value.filterNot { it.id == userId }
    }
}

private fun ContactSummary.withAbsoluteAvatarUrl(baseUrl: String): ContactSummary {
    val relative = avatarUrl ?: return this
    if (relative.startsWith("http://") || relative.startsWith("https://")) return this
    return copy(avatarUrl = baseUrl.trimEnd('/') + relative)
}
