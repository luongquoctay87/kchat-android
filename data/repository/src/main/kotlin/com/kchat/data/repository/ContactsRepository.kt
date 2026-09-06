package com.kchat.data.repository

import com.kchat.core.model.ContactSummary
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun observeContacts(): Flow<List<ContactSummary>>

    suspend fun refreshContacts()

    /** Clear cached contacts after emergency wipe. */
    suspend fun clearForEmergencyWipe()

    /** Restore defaults after logout (fake); network reloads on next refresh. */
    fun restoreAfterLogout()

    /** Returns room id for 1-1 chat (fake: deterministic; network: API). */
    suspend fun openDirectChat(contactId: String): Result<String>

    /** Search all active users by name / username / email (min 2 chars on server). */
    suspend fun searchUsers(query: String): Result<List<ContactSummary>>

    suspend fun addContact(contact: ContactSummary): Result<Unit>

    suspend fun removeContact(userId: String): Result<Unit>
}
