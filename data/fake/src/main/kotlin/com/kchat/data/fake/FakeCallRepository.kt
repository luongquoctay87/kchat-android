package com.kchat.data.fake

import com.kchat.core.model.CallInfo
import com.kchat.core.model.CallRealtimeEvent
import com.kchat.core.model.RtcIceServer
import com.kchat.data.repository.CallRepository
import com.kchat.data.repository.CallSignalBus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class FakeCallRepository @Inject constructor(
    private val callSignalBus: CallSignalBus,
) : CallRepository {
    override val usesSimulatedPeer: Boolean = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun getIceServers(): Result<List<RtcIceServer>> =
        Result.success(
            listOf(
                RtcIceServer(urls = listOf("stun:stun.l.google.com:19302")),
            ),
        )

    override suspend fun initiate(roomId: String, callType: String): Result<CallInfo> {
        delay(300)
        val info = CallInfo(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            initiatorId = "me",
            initiatorName = "Tôi",
            calleeId = "peer",
            calleeName = "Đối phương",
            callType = callType,
            status = "ringing",
        )
        scope.launch {
            delay(2_000)
            callSignalBus.publish(
                CallRealtimeEvent.Accepted(info.copy(status = "active")),
            )
        }
        return Result.success(info)
    }

    override suspend fun accept(callId: String): Result<CallInfo> {
        delay(200)
        val info = CallInfo(
            id = callId,
            roomId = "fake-room",
            initiatorId = "peer",
            initiatorName = "Đối phương",
            calleeId = "me",
            calleeName = "Tôi",
            callType = "voice",
            status = "active",
        )
        callSignalBus.publish(CallRealtimeEvent.Accepted(info))
        return Result.success(info)
    }

    override suspend fun decline(callId: String): Result<CallInfo> {
        delay(150)
        val info = CallInfo(
            id = callId,
            roomId = "fake-room",
            initiatorId = "peer",
            initiatorName = "Đối phương",
            calleeId = "me",
            calleeName = "Tôi",
            callType = "voice",
            status = "declined",
        )
        callSignalBus.publish(CallRealtimeEvent.Rejected(info))
        return Result.success(info)
    }

    override suspend fun end(callId: String): Result<CallInfo> {
        delay(150)
        val info = CallInfo(
            id = callId,
            roomId = "fake-room",
            initiatorId = "me",
            initiatorName = "Tôi",
            calleeId = "peer",
            calleeName = "Đối phương",
            callType = "voice",
            status = "ended",
        )
        callSignalBus.publish(CallRealtimeEvent.Ended(info))
        return Result.success(info)
    }
}
