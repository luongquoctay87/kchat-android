package com.kchat.core.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kchat.core.design.KChatAppearance
import com.kchat.core.model.MediaDownloadResult
import com.kchat.core.ui.KChatScaffold
import com.kchat.core.ui.KChatTab
import com.kchat.core.ui.components.SheetCancelRow
import com.kchat.core.ui.components.SheetHandle
import com.kchat.core.ui.components.SheetOptionRow
import com.kchat.core.model.ContactSummary
import com.kchat.core.model.RoomSummary
import com.kchat.core.navigation.viewmodel.CallViewModel
import com.kchat.core.navigation.viewmodel.ChatListViewModel
import com.kchat.core.navigation.viewmodel.ChatRoomViewModel
import com.kchat.core.navigation.viewmodel.ContactsViewModel
import com.kchat.core.navigation.viewmodel.CreateGroupViewModel
import com.kchat.core.navigation.viewmodel.DevicesViewModel
import com.kchat.core.navigation.viewmodel.VerifyResetOtpViewModel
import com.kchat.core.navigation.viewmodel.ForgotPasswordViewModel
import com.kchat.core.navigation.viewmodel.GroupInfoViewModel
import com.kchat.core.navigation.viewmodel.IncomingCallViewModel
import com.kchat.core.navigation.viewmodel.LoginAction
import com.kchat.core.navigation.viewmodel.LoginViewModel
import com.kchat.core.navigation.viewmodel.RegisterViewModel
import com.kchat.core.navigation.viewmodel.ResetPasswordViewModel
import com.kchat.core.navigation.viewmodel.PinSettingsViewModel
import com.kchat.core.navigation.viewmodel.SessionViewModel
import com.kchat.core.navigation.settings.toAppearance
import com.kchat.core.navigation.settings.toDmPrivacyPolicy
import com.kchat.core.navigation.settings.toMessageStorage
import com.kchat.core.navigation.settings.toQuietHours
import com.kchat.core.navigation.viewmodel.SettingsViewModel
import com.kchat.core.ui.screens.ChatListScreen
import com.kchat.core.ui.screens.ChatRoomScreen
import com.kchat.core.ui.screens.ContactDetailScreen
import com.kchat.core.ui.screens.ContactsScreen
import com.kchat.core.ui.screens.CreateGroupScreen
import com.kchat.core.ui.screens.GroupInfoScreen
import com.kchat.core.ui.screens.ImageViewerScreen
import com.kchat.core.ui.screens.CallScreen
import com.kchat.core.ui.screens.CallType
import com.kchat.core.ui.screens.settings.DevicesScreen
import com.kchat.core.ui.screens.InChatSearchScreen
import com.kchat.core.ui.screens.SettingsScreen
import com.kchat.core.ui.screens.settings.MessageStorageSettings
import com.kchat.core.ui.screens.settings.StorageScreen
import com.kchat.core.ui.screens.auth.VerifyResetOtpScreen
import com.kchat.core.ui.screens.auth.ForgotPasswordScreen
import com.kchat.core.ui.screens.auth.LoginScreen
import com.kchat.core.ui.screens.auth.RegisterScreen
import com.kchat.core.ui.screens.auth.ResetPasswordScreen
import com.kchat.core.ui.screens.settings.AppearanceScreen
import com.kchat.core.ui.screens.settings.ChangePasswordScreen
import com.kchat.core.ui.screens.settings.NotificationsScreen
import com.kchat.core.ui.screens.settings.PinSettingsScreen
import com.kchat.core.ui.screens.settings.PrivacyDmScreen
import com.kchat.core.ui.screens.settings.PrivacyQuietHoursScreen
import com.kchat.core.ui.screens.settings.PrivacyScreen
import com.kchat.core.ui.screens.settings.DmPrivacyPolicy
import com.kchat.core.ui.screens.settings.QuietHours
import com.kchat.core.ui.screens.settings.ProfileScreen
import com.kchat.data.repository.PushNavigationStore

@Composable
fun KChatNavHost(
    navController: NavHostController,
    appearance: KChatAppearance,
    onAppearanceChange: (KChatAppearance) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: KChatRoute = KChatRoute.Main,
    debugBackendLabel: String? = null,
    pushNavigationStore: PushNavigationStore? = null,
    /** Navigate to room from push only when user session is ready (not login/loading). */
    enablePushNavigation: Boolean = false,
    /** False under PIN lock so a covered chat does not swallow notifications. */
    roomInteractive: Boolean = true,
) {
    val activity = LocalContext.current as ComponentActivity
    val settingsViewModel: SettingsViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val incomingCallViewModel: IncomingCallViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val profile by settingsViewModel.profile.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val profileSaving by settingsViewModel.profileSaving.collectAsStateWithLifecycle()
    val avatarUploading by settingsViewModel.avatarUploading.collectAsStateWithLifecycle()
    val profileError by settingsViewModel.profileError.collectAsStateWithLifecycle()
    val passwordSaving by settingsViewModel.passwordSaving.collectAsStateWithLifecycle()
    val passwordError by settingsViewModel.passwordError.collectAsStateWithLifecycle()
    val passwordSuccess by settingsViewModel.passwordSuccess.collectAsStateWithLifecycle()
    val settingsError by settingsViewModel.settingsError.collectAsStateWithLifecycle()

    LaunchedEffect(settings) {
        settings?.let { onAppearanceChange(it.toAppearance()) }
    }

    LaunchedEffect(pushNavigationStore, enablePushNavigation) {
        val store = pushNavigationStore ?: return@LaunchedEffect
        store.pending.collect { target ->
            if (target == null || !enablePushNavigation) return@collect
            navController.navigate(KChatRoute.Chat(target.roomId, target.title)) {
                launchSingleTop = true
            }
            store.consume()
        }
    }

    LaunchedEffect(pushNavigationStore, enablePushNavigation) {
        val store = pushNavigationStore ?: return@LaunchedEffect
        store.pendingCall.collect { target ->
            if (target == null || !enablePushNavigation) return@collect
            val activeId = incomingCallViewModel.activeCallId()
            if (activeId == target.callId || incomingCallViewModel.isBusy()) {
                store.consumeCall()
                return@collect
            }
            val callTypeStr = target.callType.ifBlank { CallType.IncomingVoice.name }
            val name = target.callerName.ifBlank { "Cuộc gọi đến" }
            store.consumeCall()
            navController.navigate(
                KChatRoute.Call(
                    roomId = target.roomId,
                    contactName = name,
                    callType = callTypeStr,
                    callId = target.callId,
                ),
            ) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        incomingCallViewModel.incoming.collect { call ->
            val activeId = incomingCallViewModel.activeCallId()
            // Already showing this call, or mid another real call — skip.
            if (activeId == call.id) return@collect
            if (incomingCallViewModel.isBusy()) return@collect
            if (!call.status.equals("ringing", ignoreCase = true)) return@collect
            val type = if (call.isVideo) CallType.IncomingVideo else CallType.IncomingVoice
            val name = call.initiatorName.ifBlank { "Cuộc gọi đến" }
            incomingCallViewModel.clearIncoming()
            navController.navigate(
                KChatRoute.Call(
                    roomId = call.roomId,
                    contactName = name,
                    callType = type.name,
                    callId = call.id,
                ),
            ) {
                launchSingleTop = true
            }
        }
    }

    fun handleAppearanceChange(newAppearance: KChatAppearance) {
        onAppearanceChange(newAppearance)
        settingsViewModel.updateAppearance(newAppearance)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<KChatRoute.Login> { entry ->
            val route = entry.toRoute<KChatRoute.Login>()
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate(KChatRoute.Main) {
                        popUpTo<KChatRoute.Login> { inclusive = true }
                    }
                }
            }
            LoginScreen(
                identifier = state.identifier,
                password = state.password,
                isLoading = state.isLoading,
                error = state.error,
                infoMessage = route.infoMessage,
                debugBackendLabel = debugBackendLabel,
                onIdentifierChange = { viewModel.onAction(LoginAction.IdentifierChanged(it)) },
                onPasswordChange = { viewModel.onAction(LoginAction.PasswordChanged(it)) },
                onLogin = { viewModel.onAction(LoginAction.Submit) },
                onLoginSuccess = {},
                onRegister = { navController.navigate(KChatRoute.Register) },
                onForgotPassword = { navController.navigate(KChatRoute.ForgotPassword) },
            )
        }
        composable<KChatRoute.Register> {
            val context = LocalContext.current
            val viewModel: RegisterViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isRegistered) {
                if (state.isRegistered) {
                    Toast.makeText(
                        context,
                        "Đăng ký thành công",
                        Toast.LENGTH_LONG,
                    ).show()
                    // Tokens already saved — SessionGate switches NavHost to Main.
                }
            }
            RegisterScreen(
                displayName = state.displayName,
                username = state.username,
                email = state.email,
                password = state.password,
                confirm = state.confirm,
                isSendingOtp = state.isSendingOtp,
                showOtpDialog = state.showOtpDialog,
                otp = state.otp,
                isVerifyingOtp = state.isVerifyingOtp,
                formError = state.formError,
                displayNameError = state.displayNameError,
                emailError = state.emailError,
                usernameError = state.usernameError,
                passwordError = state.passwordError,
                confirmError = state.confirmError,
                otpError = state.otpError,
                canSubmit = state.canSubmitForm,
                onDisplayNameChange = viewModel::onDisplayNameChange,
                onUsernameChange = viewModel::onUsernameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmChange = viewModel::onConfirmChange,
                onOtpChange = viewModel::onOtpChange,
                onSubmit = viewModel::submit,
                onVerifyOtp = viewModel::verifyOtp,
                onResendOtp = viewModel::resendOtp,
                onDismissOtp = viewModel::dismissOtpDialog,
                onLogin = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.ForgotPassword> {
            val viewModel: ForgotPasswordViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate(KChatRoute.VerifyResetOtp(state.email))
                }
            }
            ForgotPasswordScreen(
                email = state.email,
                isLoading = state.isLoading,
                error = state.error,
                onEmailChange = viewModel::onEmailChange,
                onSubmit = viewModel::submit,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.VerifyResetOtp> {
            val viewModel: VerifyResetOtpViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.verifiedResetToken) {
                state.verifiedResetToken?.let { token ->
                    navController.navigate(KChatRoute.ResetPassword(token))
                }
            }
            VerifyResetOtpScreen(
                email = state.email,
                otp = state.otp,
                isLoading = state.isLoading,
                isResending = state.isResending,
                error = state.error,
                resendMessage = state.resendMessage,
                resendError = state.resendError,
                onOtpChange = viewModel::onOtpChange,
                onVerify = viewModel::verify,
                onBack = { navController.popBackStack() },
                onBackToLogin = {
                    navController.navigate(KChatRoute.Login()) {
                        popUpTo<KChatRoute.Login> { inclusive = true }
                    }
                },
                onResend = viewModel::resend,
            )
        }
        composable<KChatRoute.ResetPassword> {
            val viewModel: ResetPasswordViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate(
                        KChatRoute.Login(
                            infoMessage = "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.",
                        ),
                    ) {
                        popUpTo<KChatRoute.Login> { inclusive = true }
                    }
                }
            }
            ResetPasswordScreen(
                password = state.password,
                confirm = state.confirm,
                isLoading = state.isLoading,
                error = state.error,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmChange = viewModel::onConfirmChange,
                onConfirm = viewModel::submit,
            )
        }
        composable<KChatRoute.Main> {
            MainTabsScreen(
                navController = navController,
                appearance = appearance,
                onAppearanceChange = ::handleAppearanceChange,
                username = profile?.username.orEmpty(),
                displayName = profile?.displayName.orEmpty(),
                avatarUrl = profile?.avatarUrl,
                onRefreshProfile = settingsViewModel::refresh,
            )
        }
        composable<KChatRoute.Chat> { entry ->
            val route = entry.toRoute<KChatRoute.Chat>()
            val viewModel: ChatRoomViewModel = hiltViewModel()
            // Nav 2.8 keeps Chat composed on the back stack (CREATED, not disposed).
            // Only RESUMED + unlocked counts as "viewing" — otherwise tray notify is swallowed.
            LifecycleResumeEffect(viewModel.roomId, roomInteractive) {
                if (roomInteractive) {
                    viewModel.onScreenVisible()
                }
                onPauseOrDispose {
                    viewModel.onScreenHidden()
                }
            }
            val messages by viewModel.messages.collectAsStateWithLifecycle()
            val roomMeta by viewModel.roomMeta.collectAsStateWithLifecycle()
            val pinned by viewModel.pinned.collectAsStateWithLifecycle()
            val readReceipts by viewModel.readReceipts.collectAsStateWithLifecycle()
            val actionError by viewModel.actionError.collectAsStateWithLifecycle()
            val infoMessage by viewModel.infoMessage.collectAsStateWithLifecycle()
            val canAddPeerToContacts by viewModel.canAddPeerToContacts.collectAsStateWithLifecycle()
            val restoreDraft by viewModel.restoreDraft.collectAsStateWithLifecycle()
            val restoreReply by viewModel.restoreReply.collectAsStateWithLifecycle()
            val fileDownloadStatuses by viewModel.fileDownloadStatuses.collectAsStateWithLifecycle()
            val scrollToMessageId by entry.savedStateHandle
                .getStateFlow<String?>("scrollToMessageId", null)
                .collectAsStateWithLifecycle()
            val context = LocalContext.current
            var pendingDownload by remember { mutableStateOf<Pair<String, String>?>(null) }
            val writePermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                val pending = pendingDownload
                pendingDownload = null
                if (pending == null) return@rememberLauncherForActivityResult
                if (granted) {
                    viewModel.openOrDownloadFile(pending.first, pending.second)
                } else {
                    Toast.makeText(context, "Cần quyền lưu file vào Downloads", Toast.LENGTH_SHORT).show()
                }
            }
            fun openDownloadedResult(result: MediaDownloadResult) {
                val uri = Uri.parse(result.contentUri)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, result.mimeType.ifBlank { "*/*" })
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                runCatching {
                    context.startActivity(Intent.createChooser(intent, result.fileName))
                }.onFailure {
                    Toast.makeText(context, "Không mở được file", Toast.LENGTH_SHORT).show()
                }
            }
            fun requestOpenOrDownload(url: String, fileName: String) {
                val needsWritePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ) != PackageManager.PERMISSION_GRANTED
                if (needsWritePermission) {
                    pendingDownload = url to fileName
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    viewModel.openOrDownloadFile(url, fileName)
                }
            }
            LaunchedEffect(actionError) {
                actionError?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    viewModel.clearActionError()
                }
            }
            LaunchedEffect(infoMessage) {
                infoMessage?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearInfoMessage()
                }
            }
            LaunchedEffect(viewModel) {
                viewModel.openDownloadedFile.collect { result ->
                    openDownloadedResult(result)
                }
            }
            ChatRoomScreen(
                roomId = route.roomId,
                roomTitle = route.title,
                enterToSend = appearance.enterToSend,
                roomMeta = roomMeta,
                messages = messages,
                pinnedMessages = pinned,
                scrollToMessageId = scrollToMessageId,
                onScrollToMessageConsumed = {
                    entry.savedStateHandle.remove<String>("scrollToMessageId")
                },
                onEnsureMessageLoaded = viewModel::ensureMessageLoadedForScroll,
                onSendMessage = { text, reply -> viewModel.sendMessage(text, reply) },
                restoreDraft = restoreDraft,
                restoreReply = restoreReply,
                onRestoreDraftConsumed = viewModel::clearRestoreDraft,
                onEditMessage = { id, text -> viewModel.editMessage(id, text) },
                onDeleteMessage = viewModel::deleteMessage,
                onReactMessage = { id, emoji -> viewModel.toggleReaction(id, emoji) },
                onSendMedia = { uri, mime, name -> viewModel.sendMedia(uri, mime, name) },
                onInputChanged = viewModel::onInputChanged,
                mentionUsersProvider = viewModel::mentionUsers,
                readReceipts = readReceipts,
                onLoadReadReceipts = viewModel::loadReadReceipts,
                onPinMessage = viewModel::pinMessage,
                onUnpinMessage = viewModel::unpinMessage,
                onBack = { navController.popBackStack() },
                onOpenGroupInfo = {
                    navController.navigate(
                        KChatRoute.GroupInfo(
                            roomId = route.roomId,
                            title = route.title,
                            isChannel = roomMeta.isChannel,
                        ),
                    )
                },
                onOpenImage = { url, title ->
                    navController.navigate(
                        KChatRoute.ImageViewer(
                            mediaUrl = android.net.Uri.encode(url),
                            title = android.net.Uri.encode(title),
                        ),
                    )
                },
                onOpenFile = { url, fileName -> requestOpenOrDownload(url, fileName) },
                fileDownloadStatuses = fileDownloadStatuses,
                onOpenSearch = {
                    navController.navigate(KChatRoute.InChatSearch(route.roomId, route.title))
                },
                onDisappearingChange = viewModel::setDisappearing,
                onMuteChange = viewModel::setRoomMuted,
                canAddPeerToContacts = canAddPeerToContacts,
                onAddPeerToContacts = viewModel::addPeerToContacts,
                onOpenCall = { type ->
                    navController.navigate(
                        KChatRoute.Call(
                            roomId = route.roomId,
                            contactName = route.title,
                            callType = type.name,
                        ),
                    ) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<KChatRoute.CreateGroup> {
            val viewModel: CreateGroupViewModel = hiltViewModel()
            val contacts by viewModel.contacts.collectAsStateWithLifecycle()
            val creating by viewModel.creating.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
            CreateGroupScreen(
                contacts = contacts,
                creating = creating,
                error = error,
                onBack = { navController.popBackStack() },
                onCreate = { name, ids -> viewModel.createGroup(name, ids) },
                onCreated = { roomId, title ->
                    navController.navigate(KChatRoute.Chat(roomId, title)) {
                        popUpTo(KChatRoute.CreateGroup) { inclusive = true }
                    }
                },
            )
        }
        composable<KChatRoute.GroupInfo> { entry ->
            val route = entry.toRoute<KChatRoute.GroupInfo>()
            val viewModel: GroupInfoViewModel = hiltViewModel()
            val members by viewModel.members.collectAsStateWithLifecycle()
            val contacts by viewModel.contacts.collectAsStateWithLifecycle()
            val room by viewModel.room.collectAsStateWithLifecycle()
            val loading by viewModel.loading.collectAsStateWithLifecycle()
            val busy by viewModel.busy.collectAsStateWithLifecycle()
            val avatarUploading by viewModel.avatarUploading.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
            GroupInfoScreen(
                groupName = room?.title
                    ?: route.title.ifBlank { viewModel.title }.ifBlank {
                        if (route.isChannel) "Kênh" else "Nhóm"
                    },
                avatarUrl = room?.avatarUrl,
                avatarUploading = avatarUploading,
                myRole = room?.myRole,
                members = members,
                contacts = contacts,
                loading = loading,
                busy = busy,
                error = error,
                isChannel = route.isChannel,
                onBack = { navController.popBackStack() },
                onAddMembers = { ids -> viewModel.addMembers(ids) },
                onRemoveMember = { userId -> viewModel.removeMember(userId) },
                onChangeAvatar = if (route.isChannel) {
                    null
                } else {
                    { uri, mime, name -> viewModel.updateAvatar(uri, mime, name) }
                },
                onLeave = { viewModel.leave() },
                onLeft = {
                    navController.popBackStack(KChatRoute.Main, inclusive = false)
                },
            )
        }
        composable<KChatRoute.Profile> {
            LaunchedEffect(Unit) { settingsViewModel.refresh() }
            ProfileScreen(
                displayName = profile?.displayName.orEmpty(),
                email = profile?.email.orEmpty(),
                username = profile?.username.orEmpty(),
                phone = profile?.phone.orEmpty(),
                avatarUrl = profile?.avatarUrl,
                isSaving = profileSaving,
                isUploadingAvatar = avatarUploading,
                error = profileError,
                onSave = { name, phone ->
                    settingsViewModel.updateProfile(name, phone) { success ->
                        if (success) navController.popBackStack()
                    }
                },
                onChangeAvatar = { uri, mime, name ->
                    settingsViewModel.updateAvatar(uri, mime, name)
                },
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(KChatRoute.ChangePassword) },
            )
        }
        composable<KChatRoute.ContactDetail> { entry ->
            val route = entry.toRoute<KChatRoute.ContactDetail>()
            val contact = ContactSummary(
                id = route.id,
                name = route.name,
                subtitle = route.subtitle,
                isOnline = route.isOnline,
                email = route.email,
                avatarUrl = route.avatarUrl,
                isContact = route.isContact,
                username = route.username,
                phone = route.phone,
            )
            val contactsViewModel: ContactsViewModel = hiltViewModel()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            ContactDetailScreen(
                contact = contact,
                onBack = { navController.popBackStack() },
                onMessage = {
                    scope.launch {
                        contactsViewModel.openDirectChat(contact.id)
                            .onSuccess { roomId ->
                                navController.navigate(KChatRoute.Chat(roomId, contact.name)) {
                                    popUpTo(KChatRoute.Main) { inclusive = false }
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(
                                    context,
                                    error.message ?: "Không mở được chat riêng",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                    }
                },
                onRemove = if (contact.isContact) {
                    {
                        contactsViewModel.removeContact(contact.id)
                        navController.popBackStack()
                    }
                } else {
                    null
                },
            )
        }
        composable<KChatRoute.ChangePassword> {
            val context = LocalContext.current
            val sessionViewModel: SessionViewModel = hiltViewModel(viewModelStoreOwner = activity)
            LaunchedEffect(passwordSuccess) {
                val message = passwordSuccess ?: return@LaunchedEffect
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                delay(1200)
                sessionViewModel.logout { }
            }
            ChangePasswordScreen(
                isSaving = passwordSaving,
                error = passwordError,
                success = passwordSuccess,
                onSave = { current, newPassword, confirm ->
                    settingsViewModel.changePassword(current, newPassword, confirm)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Appearance> {
            AppearanceScreen(
                appearance = appearance,
                onAppearanceChange = ::handleAppearanceChange,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Notifications> {
            NotificationsScreen(
                pushEnabled = settings?.pushEnabled ?: true,
                onPushEnabledChange = settingsViewModel::setPushEnabled,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.ImageViewer> { entry ->
            val route = entry.toRoute<KChatRoute.ImageViewer>()
            ImageViewerScreen(
                mediaUrl = android.net.Uri.decode(route.mediaUrl),
                title = android.net.Uri.decode(route.title).ifBlank { "Ảnh" },
                onClose = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.InChatSearch> { entry ->
            val route = entry.toRoute<KChatRoute.InChatSearch>()
            val viewModel: com.kchat.core.navigation.viewmodel.InChatSearchViewModel = hiltViewModel()
            val query by viewModel.query.collectAsStateWithLifecycle()
            val results by viewModel.results.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            InChatSearchScreen(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                results = results,
                isLoading = isLoading,
                onResultClick = { result ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scrollToMessageId", result.id)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Privacy> {
            val pinEnabled by settingsViewModel.pinEnabled.collectAsStateWithLifecycle()
            PrivacyScreen(
                showOnline = settings?.showOnline ?: true,
                onShowOnlineChange = settingsViewModel::setShowOnline,
                dmPolicy = settings?.toDmPrivacyPolicy() ?: DmPrivacyPolicy.Everyone,
                quietHours = settings?.toQuietHours() ?: QuietHours(),
                pinEnabled = pinEnabled,
                onDmPolicy = { navController.navigate(KChatRoute.PrivacyDm) },
                onQuietHours = { navController.navigate(KChatRoute.PrivacyQuietHours) },
                onAppPin = { navController.navigate(KChatRoute.PinSettings) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.PinSettings> {
            val pinSettingsViewModel: PinSettingsViewModel = hiltViewModel()
            val pinEnabled by pinSettingsViewModel.isEnabled.collectAsStateWithLifecycle()
            val emergencyEnabled by pinSettingsViewModel.isEmergencyEnabled.collectAsStateWithLifecycle()
            val pinUi by pinSettingsViewModel.uiState.collectAsStateWithLifecycle()
            PinSettingsScreen(
                enabled = pinEnabled,
                emergencyEnabled = emergencyEnabled,
                mode = pinUi.mode,
                currentPin = pinUi.currentPin,
                newPin = pinUi.newPin,
                confirmPin = pinUi.confirmPin,
                error = pinUi.error,
                isSaving = pinUi.isSaving,
                onBack = { navController.popBackStack() },
                onModeChange = pinSettingsViewModel::setMode,
                onCurrentPinChange = pinSettingsViewModel::onCurrentPinChange,
                onNewPinChange = pinSettingsViewModel::onNewPinChange,
                onConfirmPinChange = pinSettingsViewModel::onConfirmPinChange,
                onSubmit = pinSettingsViewModel::submit,
            )
        }
        composable<KChatRoute.PrivacyDm> {
            ObserveSettingsError(settingsError, settingsViewModel::clearSettingsError)
            PrivacyDmScreen(
                selected = settings?.toDmPrivacyPolicy() ?: DmPrivacyPolicy.Everyone,
                onSelectedChange = settingsViewModel::setDmPolicy,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.PrivacyQuietHours> {
            ObserveSettingsError(settingsError, settingsViewModel::clearSettingsError)
            PrivacyQuietHoursScreen(
                quietHours = settings?.toQuietHours() ?: QuietHours(),
                onQuietHoursChange = settingsViewModel::setQuietHours,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Devices> {
            val viewModel: DevicesViewModel = hiltViewModel()
            val devices by viewModel.devices.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
            val context = LocalContext.current
            LaunchedEffect(error) {
                error?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
            DevicesScreen(
                devices = devices,
                isLoading = isLoading,
                onRevokeDevice = viewModel::revokeDevice,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Storage> {
            ObserveSettingsError(settingsError, settingsViewModel::clearSettingsError)
            StorageScreen(
                storage = settings?.toMessageStorage() ?: MessageStorageSettings(),
                onStorageChange = settingsViewModel::setMessageStorage,
                onBack = { navController.popBackStack() },
            )
        }
        composable<KChatRoute.Call> { entry ->
            val route = entry.toRoute<KChatRoute.Call>()
            val viewModel: CallViewModel = hiltViewModel()
            val phase by viewModel.phase.collectAsStateWithLifecycle()
            val statusText by viewModel.statusText.collectAsStateWithLifecycle()
            val durationLabel by viewModel.durationLabel.collectAsStateWithLifecycle()
            val micMuted by viewModel.micMuted.collectAsStateWithLifecycle()
            val cameraOff by viewModel.cameraOff.collectAsStateWithLifecycle()
            val speakerOn by viewModel.speakerOn.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()
            val shouldClose by viewModel.shouldClose.collectAsStateWithLifecycle()

            LaunchedEffect(shouldClose) {
                if (shouldClose) {
                    navController.popBackStack()
                }
            }

            CallScreen(
                contactName = viewModel.contactName.ifBlank { route.contactName },
                isVideo = viewModel.isVideo,
                phase = phase,
                statusText = statusText,
                durationLabel = durationLabel,
                micMuted = micMuted,
                cameraOff = cameraOff,
                speakerOn = speakerOn,
                error = error,
                onAccept = viewModel::accept,
                onDecline = viewModel::decline,
                onEnd = viewModel::end,
                onToggleMic = viewModel::toggleMic,
                onToggleCamera = viewModel::toggleCamera,
                onToggleSpeaker = viewModel::toggleSpeaker,
                onBindLocalRenderer = viewModel::bindLocalRenderer,
                onBindRemoteRenderer = viewModel::bindRemoteRenderer,
                onDisposeRenderers = viewModel::disposeRenderers,
                onMediaReady = viewModel::onMediaReady,
                onMediaDenied = viewModel::onMediaDenied,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTabsScreen(
    navController: NavHostController,
    appearance: KChatAppearance,
    onAppearanceChange: (KChatAppearance) -> Unit,
    modifier: Modifier = Modifier,
    username: String = "",
    displayName: String = "",
    avatarUrl: String? = null,
    onRefreshProfile: () -> Unit = {},
) {
    var currentTab by rememberSaveable { mutableStateOf(KChatTab.Chat) }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

    val activity = LocalContext.current as ComponentActivity
    val sessionViewModel: SessionViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val rooms by chatListViewModel.rooms.collectAsStateWithLifecycle()
    val chatListItems by chatListViewModel.items.collectAsStateWithLifecycle()
    val chatUnreadCount = rooms.sumOf { it.unreadCount.coerceAtLeast(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(currentTab) {
        if (currentTab == KChatTab.Chat) {
            chatListViewModel.refresh()
        }
        if (currentTab == KChatTab.Settings) {
            onRefreshProfile()
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            SheetHandle()
            SheetOptionRow(
                label = "Nhắn tin mới",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    showCreateSheet = false
                    currentTab = KChatTab.Contacts
                },
            )
            SheetOptionRow(
                label = "Tạo nhóm",
                icon = Icons.Default.GroupAdd,
                onClick = {
                    showCreateSheet = false
                    navController.navigate(KChatRoute.CreateGroup)
                },
            )
            HorizontalDivider()
            SheetCancelRow(onClick = { showCreateSheet = false })
        }
    }

    KChatScaffold(
        modifier = modifier,
        currentTab = currentTab,
        onTabSelected = { currentTab = it },
        title = when (currentTab) {
            KChatTab.Chat -> null
            KChatTab.Contacts -> "Danh bạ"
            KChatTab.Settings -> "Cài đặt"
        },
        showWordmark = currentTab == KChatTab.Chat,
        chatUnreadCount = chatUnreadCount,
        actions = {
            if (currentTab == KChatTab.Chat) {
                IconButton(onClick = { showCreateSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo mới")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    KChatTab.Chat -> {
                        ChatListScreen(
                            rooms = chatListItems.map { it.room },
                            typingRoomIds = chatListItems
                                .filter { it.isTyping }
                                .map { it.room.id.trim().lowercase() }
                                .toSet(),
                            onRoomClick = { room ->
                                navController.navigate(KChatRoute.Chat(room.id, room.title))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    KChatTab.Contacts -> {
                        val contactsViewModel: ContactsViewModel = hiltViewModel()
                        val contacts by contactsViewModel.contacts.collectAsStateWithLifecycle()
                        val contactsUi by contactsViewModel.uiState.collectAsStateWithLifecycle()
                        ContactsScreen(
                            contacts = contacts,
                            query = contactsUi.query,
                            onQueryChange = contactsViewModel::onQueryChange,
                            searchResults = contactsUi.searchResults,
                            isSearching = contactsUi.isSearching,
                            searchError = contactsUi.searchError,
                            actionError = contactsUi.actionError,
                            actionMessage = contactsUi.actionMessage,
                            onContactClick = { contact ->
                                scope.launch {
                                    contactsViewModel.openDirectChat(contact.id)
                                        .onSuccess { roomId ->
                                            navController.navigate(KChatRoute.Chat(roomId, contact.name))
                                        }
                                        .onFailure { error ->
                                            Toast.makeText(
                                                context,
                                                error.message ?: "Không mở được chat riêng",
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                }
                            },
                            onAddContact = { contact ->
                                contactsViewModel.addContact(contact)
                            },
                            onRemoveContact = { contact ->
                                contactsViewModel.removeContact(contact.id)
                            },
                            onViewContactDetail = { contact ->
                                navController.navigate(
                                    KChatRoute.ContactDetail(
                                        id = contact.id,
                                        name = contact.name,
                                        subtitle = contact.subtitle,
                                        isOnline = contact.isOnline,
                                        email = contact.email,
                                        avatarUrl = contact.avatarUrl,
                                        username = contact.username,
                                        phone = contact.phone,
                                        isContact = true,
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    KChatTab.Settings -> SettingsScreen(
                        username = username,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        onProfile = { navController.navigate(KChatRoute.Profile) },
                        onNotifications = { navController.navigate(KChatRoute.Notifications) },
                        onAppearance = { navController.navigate(KChatRoute.Appearance) },
                        onPrivacy = { navController.navigate(KChatRoute.Privacy) },
                        onStorage = { navController.navigate(KChatRoute.Storage) },
                        onDevices = { navController.navigate(KChatRoute.Devices) },
                        onLogout = {
                            sessionViewModel.logout { /* NavHost resets via key(isLoggedIn) in MainActivity */ }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ObserveSettingsError(
    settingsError: String?,
    onClear: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(settingsError) {
        settingsError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onClear()
        }
    }
}

