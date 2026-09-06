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
        // Block GET rooms/messages/contacts before clearing so wiped history cannot refill.
        // New inbound WS/FCM still flows and can recreate a room stub.
        emergencyWipeStore.activate()
        chatRepository.emergencyWipeAllMessages()
        contactsRepository.clearForEmergencyWipe()
        emergencyWipeStore.allowRealtime()
    }

    override suspend fun clearAfterReengage() {
        if (!emergencyWipeStore.isRealtimeMuted()) return
        emergencyWipeStore.allowRealtime()
    }
}
