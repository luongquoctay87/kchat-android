package com.kchat.data.repository

import com.kchat.core.model.RoomSummary
import com.kchat.core.model.UserSettings
import java.util.Calendar

object PushNotificationPolicy {
    fun shouldNotify(settings: UserSettings?, room: RoomSummary?): Boolean {
        if (settings?.pushEnabled == false) return false
        if (room?.isMuted == true && isRoomMutedNow(room)) return false
        if (settings?.quietHoursEnabled == true) {
            val start = settings.quietHoursStartHour ?: 22
            val end = settings.quietHoursEndHour ?: 7
            val nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (isQuietHoursActive(start, end, nowHour)) return false
        }
        return true
    }

    private fun isRoomMutedNow(room: RoomSummary): Boolean {
        val until = room.mutedUntilEpochMs ?: return true
        if (until < 0L) return true
        return until > System.currentTimeMillis()
    }

    private fun isQuietHoursActive(startHour: Int, endHour: Int, nowHour: Int): Boolean {
        if (startHour == endHour) return false
        return if (startHour < endHour) {
            nowHour in startHour until endHour
        } else {
            nowHour >= startHour || nowHour < endHour
        }
    }
}
