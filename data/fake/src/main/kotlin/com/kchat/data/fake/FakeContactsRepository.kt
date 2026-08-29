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
    private val contacts = MutableStateFlow(
        FakeSampleData.contacts.map { it.copy(isContact = true) },
    )
    private val directory = FakeSampleData.contacts + listOf(
        ContactSummary(
            id = "u-5",
            name = "Nguyễn Văn F",
            subtitle = "offline",
            isOnline = false,
            email = "f@company.com",
            username = "nguyenvanf",
            isContact = false,
        ),
        ContactSummary(
            id = "u-6",
            name = "Đỗ Thị G",
            subtitle = "online",
            isOnline = true,
            email = "g@company.com",
            username = "dothig",
            phone = "0901000006",
            isContact = false,
        ),
    )

    override fun observeContacts(): Flow<List<ContactSummary>> = contacts.asStateFlow()

    override suspend fun clearForEmergencyWipe() {
        contacts.value = emptyList()
    }

    override fun restoreAfterLogout() {
        contacts.value = FakeSampleData.contacts.map { it.copy(isContact = true) }
    }

    override suspend fun refreshContacts() = Unit

    override suspend fun openDirectChat(contactId: String): Result<String> {
        val roomId = when (contactId) {
            "u-1" -> "room-1"
            else -> "room-$contactId"
        }
        return Result.success(roomId)
    }

    override suspend fun searchUsers(query: String): Result<List<ContactSummary>> {
        val q = query.trim()
        if (q.length < 2) return Result.success(emptyList())
        val contactIds = contacts.value.map { it.id }.toSet()
        return Result.success(
            directory.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.email.contains(q, ignoreCase = true)
            }.map { it.copy(isContact = it.id in contactIds) },
        )
    }

    override suspend fun addContact(userId: String): Result<Unit> {
        val user = directory.find { it.id == userId }
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy người dùng"))
        if (contacts.value.none { it.id == userId }) {
            contacts.value = (contacts.value + user.copy(isContact = true))
                .sortedBy { it.name.lowercase() }
        }
        return Result.success(Unit)
    }

    override suspend fun removeContact(userId: String): Result<Unit> {
        contacts.value = contacts.value.filterNot { it.id == userId }
        return Result.success(Unit)
    }
}
