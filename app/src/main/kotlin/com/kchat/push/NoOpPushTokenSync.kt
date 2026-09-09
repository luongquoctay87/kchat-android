package com.kchat.push

import com.kchat.data.repository.PushTokenSync
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpPushTokenSync @Inject constructor() : PushTokenSync {
    override suspend fun syncCurrentToken(): Boolean = false
}
