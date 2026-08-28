package com.kchat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kchat.core.design.KChatAppearance
import com.kchat.core.design.KChatAppearanceSaver
import com.kchat.core.design.KChatTheme
import com.kchat.core.navigation.KChatNavHost
import com.kchat.core.navigation.KChatRoute
import com.kchat.core.navigation.viewmodel.PinLockViewModel
import com.kchat.core.navigation.viewmodel.SessionGate
import com.kchat.core.navigation.viewmodel.SessionViewModel
import com.kchat.core.ui.screens.auth.PinLockScreen
import com.kchat.data.repository.PushNavigationStore
import com.kchat.push.PushNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var pushNavigationStore: PushNavigationStore
    @Inject lateinit var pushNotificationHelper: PushNotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pushNotificationHelper.ensureChannel()
        handlePushIntent(intent)
        enableEdgeToEdge()
        setContent {
            var appearance by rememberSaveable(stateSaver = KChatAppearanceSaver) {
                mutableStateOf(KChatAppearance())
            }
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { _ -> }

            KChatTheme(appearance = appearance) {
                val sessionViewModel: SessionViewModel = hiltViewModel()
                val gate by sessionViewModel.gate.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                LaunchedEffect(gate) {
                    if (gate != SessionGate.Login && gate != SessionGate.Loading && BuildConfig.FCM_ENABLED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                DisposableEffect(sessionViewModel) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> sessionViewModel.onAppBackgrounded()
                            Lifecycle.Event.ON_START -> sessionViewModel.onAppForegrounded()
                            else -> Unit
                        }
                    }
                    ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
                    onDispose {
                        ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
                    }
                }

                when (val current = gate) {
                    SessionGate.Loading -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )
                    SessionGate.Login,
                    SessionGate.Main,
                    SessionGate.PinLock,
                    -> {
                        val isLoggedIn = current != SessionGate.Login
                        Box(modifier = Modifier.fillMaxSize()) {
                            key(isLoggedIn) {
                                KChatNavHost(
                                    navController = navController,
                                    appearance = appearance,
                                    onAppearanceChange = { appearance = it },
                                    startDestination = if (isLoggedIn) {
                                        KChatRoute.Main
                                    } else {
                                        KChatRoute.Login()
                                    },
                                    debugBackendLabel = if (BuildConfig.DEBUG) {
                                        if (BuildConfig.USE_FAKE_DATA) {
                                            "FAKE DATA (không gọi BE)"
                                        } else {
                                            "API ${BuildConfig.API_BASE_URL}"
                                        }
                                    } else {
                                        null
                                    },
                                    showMockChrome = BuildConfig.USE_FAKE_DATA,
                                    pushNavigationStore = pushNavigationStore,
                                    enablePushNavigation = current == SessionGate.Main,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            if (current is SessionGate.PinLock) {
                                val pinLockViewModel: PinLockViewModel = hiltViewModel()
                                val pinState by pinLockViewModel.uiState.collectAsStateWithLifecycle()
                                LaunchedEffect(Unit) {
                                    pinLockViewModel.onVisible()
                                }
                                PinLockScreen(
                                    pin = pinState.pin,
                                    onPinChange = pinLockViewModel::onPinChange,
                                    onLogout = pinLockViewModel::logout,
                                    isVerifying = pinState.isVerifying,
                                    error = pinState.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushIntent(intent)
    }

    private fun handlePushIntent(intent: Intent?) {
        val roomId = intent?.getStringExtra(PushNotificationHelper.EXTRA_ROOM_ID) ?: return
        val title = intent.getStringExtra(PushNotificationHelper.EXTRA_ROOM_TITLE).orEmpty()
        pushNotificationHelper.handleNotificationTap(roomId, title)
        intent.removeExtra(PushNotificationHelper.EXTRA_ROOM_ID)
        intent.removeExtra(PushNotificationHelper.EXTRA_ROOM_TITLE)
    }
}
