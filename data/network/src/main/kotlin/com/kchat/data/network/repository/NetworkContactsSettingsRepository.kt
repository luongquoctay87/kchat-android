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
                dto.toModel().withAbsoluteAvatarUrl(apiBaseUrl)
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
}

private fun ContactSummary.withAbsoluteAvatarUrl(baseUrl: String): ContactSummary {
    val relative = avatarUrl ?: return this
    if (relative.startsWith("http://") || relative.startsWith("https://")) return this
    return copy(avatarUrl = baseUrl.trimEnd('/') + relative)
}
