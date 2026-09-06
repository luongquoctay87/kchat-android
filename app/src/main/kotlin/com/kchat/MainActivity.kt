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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kchat.core.ui.LocalTransientAppLeave
import com.kchat.core.ui.TransientAppLeave
import com.kchat.core.ui.screens.auth.PinLockScreen
import com.kchat.data.repository.PinLockTransientLeave
import com.kchat.data.repository.PushNavigationStore
import com.kchat.push.PushNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var pushNavigationStore: PushNavigationStore
    @Inject lateinit var pushNotificationHelper: PushNotificationHelper
    @Inject lateinit var pinLockTransientLeave: PinLockTransientLeave

    private var sessionViewModel: SessionViewModel? = null

    private val processLifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> sessionViewModel?.onAppBackgrounded()
            Lifecycle.Event.ON_START -> sessionViewModel?.onAppForegrounded()
            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pushNotificationHelper.ensureChannel()
        handlePushIntent(intent)
        enableEdgeToEdge()
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
        setContent {
            var appearance by rememberSaveable(stateSaver = KChatAppearanceSaver) {
                mutableStateOf(KChatAppearance())
            }
                    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission(),
                    ) { granted ->
                        pinLockTransientLeave.end()
                        // After grant, re-sync FCM so the first message isn't lost to a late token.
                        if (granted) {
                            sessionViewModel?.onAppForegrounded()
                        }
                    }
                    val transientLeave = remember(pinLockTransientLeave) {
                        object : TransientAppLeave {
                            override fun begin() = pinLockTransientLeave.begin()
                            override fun end() = pinLockTransientLeave.end()
                        }
                    }

                    CompositionLocalProvider(LocalTransientAppLeave provides transientLeave) {
                        KChatTheme(appearance = appearance) {
                            val vm: SessionViewModel = hiltViewModel()
                            DisposableEffect(vm) {
                                sessionViewModel = vm
                                onDispose {
                                    if (sessionViewModel === vm) {
                                        sessionViewModel = null
                                    }
                                }
                            }
                            val gate by vm.gate.collectAsStateWithLifecycle()
                            val navController = rememberNavController()

                            // Ask on Login (fresh install) — not only after Main — so permission
                            // is decided before the first background message can arrive.
                            LaunchedEffect(gate, notificationPermissionRequested) {
                                if (gate == SessionGate.Loading) return@LaunchedEffect
                                if (notificationPermissionRequested) return@LaunchedEffect
                                if (!BuildConfig.FCM_ENABLED) return@LaunchedEffect
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    return@LaunchedEffect
                                }
                                val granted = ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) return@LaunchedEffect
                                notificationPermissionRequested = true
                                pinLockTransientLeave.begin()
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                                        debugBackendLabel = when (BuildConfig.APP_ENV) {
                                            "local" -> "Local"
                                            "staging" -> "Staging"
                                            else -> null // prod / release: ẩn
                                        },
                                        showMockChrome = false,
                                        pushNavigationStore = pushNavigationStore,
                                        enablePushNavigation = current == SessionGate.Main,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                if (current == SessionGate.PinLock) {
                                    val pinLockViewModel: PinLockViewModel = hiltViewModel()
                                    val pinState by pinLockViewModel.uiState.collectAsStateWithLifecycle()
                                    LaunchedEffect(Unit) {
                                        vm.onPinLockShown()
                                        pinLockViewModel.onVisible()
                                    }
                                    PinLockScreen(
                                        pin = pinState.pin,
                                        onPinChange = pinLockViewModel::onPinChange,
                                        onLogout = pinLockViewModel::logout,
                                        isVerifying = pinState.isVerifying,
                                        error = pinState.error,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(processLifecycleObserver)
        sessionViewModel = null
        super.onDestroy()
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
