package com.kchat.data.network.repository

import com.kchat.core.model.CallInfo
import com.kchat.core.model.RtcIceServer
import com.kchat.data.network.api.KChatApi
import com.kchat.data.network.apiMessage
import com.kchat.data.network.dto.CreateCallRequest
import com.kchat.data.network.mapper.toModel
import com.kchat.data.repository.CallRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkCallRepository @Inject constructor(
    private val api: KChatApi,
) : CallRepository {
    override suspend fun initiate(roomId: String, callType: String): Result<CallInfo> =
        runCatching {
            api.initiateCall(roomId, CreateCallRequest(callType = callType)).toModel()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không gọi được"))) },
        )

    override suspend fun listIncoming(): Result<List<CallInfo>> =
        runCatching { api.getIncomingCalls().map { it.toModel() } }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không tải cuộc gọi đến"))) },
        )

    override suspend fun getIceServers(): Result<List<RtcIceServer>> =
        runCatching { api.getIceServers().iceServers.map { it.toModel() } }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không tải ICE servers"))) },
        )

    override suspend fun accept(callId: String): Result<CallInfo> =
        runCatching { api.acceptCall(callId).toModel() }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không trả lời được"))) },
        )

    override suspend fun decline(callId: String): Result<CallInfo> =
        runCatching { api.declineCall(callId).toModel() }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không từ chối được"))) },
        )

    override suspend fun end(callId: String): Result<CallInfo> =
        runCatching { api.endCall(callId).toModel() }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Exception(it.apiMessage("Không kết thúc được"))) },
        )
}
