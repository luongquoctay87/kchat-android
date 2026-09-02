package com.kchat.data.network.emergency

import com.kchat.data.repository.ChatRepository
import com.kchat.data.repository.ContactsRepository
import com.kchat.data.repository.EmergencyWipeCoordinator
import com.kchat.data.repository.EmergencyWipeStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyWipeCoordinatorImpl @Inject constructor(
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val emergencyWipeStore: EmergencyWipeStore,
) : EmergencyWipeCoordinator {
    override suspend fun execute() {
        chatRepository.emergencyWipeAllMessages()
        contactsRepository.clearForEmergencyWipe()
        emergencyWipeStore.activate()
    }

    override suspend fun clearAfterReengage() {
        if (!emergencyWipeStore.isActiveNow()) return
        emergencyWipeStore.reset()
        runCatching { chatRepository.refreshRooms() }
        runCatching { contactsRepository.refreshContacts() }
    }
}
