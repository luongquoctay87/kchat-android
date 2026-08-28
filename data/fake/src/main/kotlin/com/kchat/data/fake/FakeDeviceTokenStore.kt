package com.kchat.data.fake

import com.kchat.data.repository.DeviceTokenStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDeviceTokenStore @Inject constructor() : DeviceTokenStore {
    override suspend fun getOrCreate(): String = "fake-device-token"

    override suspend fun savePendingFcmToken(token: String) = Unit

    override suspend fun consumePendingFcmToken(): String? = null
}
