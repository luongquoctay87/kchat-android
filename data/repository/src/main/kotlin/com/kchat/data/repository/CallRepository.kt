package com.kchat.data.repository

import com.kchat.core.model.CallInfo
import com.kchat.core.model.RtcIceServer

interface CallRepository {
    suspend fun initiate(roomId: String, callType: String): Result<CallInfo>

    suspend fun listIncoming(): Result<List<CallInfo>> = Result.success(emptyList())

    suspend fun getIceServers(): Result<List<RtcIceServer>> = Result.success(emptyList())

    suspend fun accept(callId: String): Result<CallInfo>

    suspend fun decline(callId: String): Result<CallInfo>

    suspend fun end(callId: String): Result<CallInfo>
}
