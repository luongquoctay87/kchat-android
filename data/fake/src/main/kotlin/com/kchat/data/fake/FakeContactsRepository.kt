package com.kchat.data.fake

import com.kchat.core.model.ContactSummary
import com.kchat.data.repository.ContactsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeContactsRepository @Inject constructor() : ContactsRepository {
    private val contacts = MutableStateFlow(FakeSampleData.contacts)

    override fun observeContacts(): Flow<List<ContactSummary>> = contacts.asStateFlow()

    override suspend fun clearForEmergencyWipe() {
        contacts.value = emptyList()
    }

    override fun restoreAfterLogout() {
        contacts.value = FakeSampleData.contacts
    }

    override suspend fun refreshContacts() = Unit

    override suspend fun openDirectChat(contactId: String): Result<String> {
        val roomId = when (contactId) {
            "u-1" -> "room-1"
            else -> "room-$contactId"
        }
        return Result.success(roomId)
    }
}
