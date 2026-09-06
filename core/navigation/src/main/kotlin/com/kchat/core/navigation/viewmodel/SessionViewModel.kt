package com.kchat.core.navigation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kchat.data.repository.ActiveRoomTracker
import com.kchat.data.repository.AppForegroundTracker
import com.kchat.data.repository.AuthRepository
import com.kchat.data.repository.PinLockStore
import com.kchat.data.repository.PinLockTransientLeave
import com.kchat.data.repository.RealtimeCoordinator
import com.kchat.data.repository.SessionCoordinator
import com.kchat.data.repository.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionGate {
    data object Loading : SessionGate
    data object Login : SessionGate
    data object Main : SessionGate
    data object PinLock : SessionGate
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val sessionCoordinator: SessionCoordinator,
    private val realtimeCoordinator: RealtimeCoordinator,
    private val pinLockStore: PinLockStore,
    private val pinLockTransientLeave: PinLockTransientLeave,
    private val appForegroundTracker: AppForegroundTracker,
    private val activeRoomTracker: ActiveRoomTracker,
) : ViewModel() {
    val gate: StateFlow<SessionGate> = combine(
        authRepository.isLoggedIn,
        pinLockStore.isEnabled,
        pinLockStore.isUnlocked,
    ) { loggedIn, pinEnabled, unlocked ->
        when {
            !loggedIn -> SessionGate.Login
            pinEnabled && !unlocked -> SessionGate.PinLock
            else -> SessionGate.Main
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionGate.Loading)

    init {
        viewModelScope.launch {
            if (tokenStore.getAccessToken() != null) {
                try {
                    sessionCoordinator.onAuthenticated()
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Startup bootstrap is best-effort; invalid session clears via authenticator.
                }
            }
        }
        viewModelScope.launch {
            authRepository.isLoggedIn.distinctUntilChanged().collect { loggedIn ->
                if (!loggedIn) {
                    runCatching { realtimeCoordinator.disconnect() }
                    pinLockStore.clear()
                }
            }
        }
    }

    fun onAppBackgrounded() {
        appForegroundTracker.onBackground()
        activeRoomTracker.clear()
        if (pinLockTransientLeave.isActive()) {
            Log.d(TAG, "skip PIN lock (transient system UI)")
            return
        }
        Log.d(TAG, "PIN lock on background")
        pinLockStore.lock()
    }

    fun onPinLockShown() {
        activeRoomTracker.clear()
    }

    fun onAppForegrounded() {
        appForegroundTracker.onForeground()
        if (gate.value != SessionGate.Login && gate.value != SessionGate.Loading) {
            viewModelScope.launch {
                runCatching { sessionCoordinator.onForeground() }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    private companion object {
        const val TAG = "KChatPinLock"
    }
}
