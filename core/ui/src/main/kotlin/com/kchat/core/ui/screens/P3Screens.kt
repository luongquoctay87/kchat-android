package com.kchat.core.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens
import com.kchat.core.model.SearchResult
import com.kchat.core.ui.KChatDetailScaffold
import com.kchat.core.ui.components.KChatSearchField
import com.kchat.core.ui.components.UserAvatar
import org.webrtc.SurfaceViewRenderer

@Composable
fun InChatSearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    results: List<SearchResult> = emptyList(),
    isLoading: Boolean = false,
    onResultClick: (SearchResult) -> Unit = {},
) {
    val trimmed = query.trim()
    val summary = when {
        trimmed.isBlank() -> "Nhập từ khóa để tìm"
        isLoading -> "Đang tìm..."
        results.isEmpty() -> "Không có kết quả"
        else -> "${results.size} kết quả"
    }

    KChatDetailScaffold(modifier = modifier, title = "Tìm trong cuộc trò chuyện", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(KChatDimens.screenPadding),
        ) {
            KChatSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Tìm tin nhắn...",
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading && results.isEmpty() && trimmed.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Top,
                ) {
                    items(results, key = { it.id }) { result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onResultClick(result) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = "${result.author} · ${result.time}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(result.snippet, style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

enum class CallType { Voice, Video, IncomingVoice, IncomingVideo }

enum class CallPhase { OutgoingRinging, IncomingRinging, Connecting, Active, Ended }

@Composable
fun CallScreen(
    contactName: String,
    isVideo: Boolean,
    phase: CallPhase,
    statusText: String,
    durationLabel: String,
    micMuted: Boolean,
    cameraOff: Boolean,
    speakerOn: Boolean,
    error: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onEnd: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onBindLocalRenderer: (SurfaceViewRenderer) -> Unit,
    onBindRemoteRenderer: (SurfaceViewRenderer) -> Unit,
    onDisposeRenderers: () -> Unit,
    onMediaReady: () -> Unit,
    onMediaDenied: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var mediaReadyFired by remember { mutableStateOf(false) }
    var pendingAccept by remember { mutableStateOf(false) }

    fun requiredPermissions(): Array<String> =
        if (isVideo) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }

    fun hasAllPermissions(): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val ok = grants.values.all { it }
        if (ok) {
            if (!mediaReadyFired) {
                mediaReadyFired = true
                onMediaReady()
            }
            if (pendingAccept) {
                pendingAccept = false
                onAccept()
            }
        } else {
            pendingAccept = false
            onMediaDenied()
        }
    }

    fun ensurePermissionsThen(onGranted: () -> Unit) {
        if (hasAllPermissions()) {
            onGranted()
        } else {
            permissionLauncher.launch(requiredPermissions())
        }
    }

    LaunchedEffect(phase, isVideo) {
        if (phase == CallPhase.OutgoingRinging || phase == CallPhase.Connecting) {
            ensurePermissionsThen {
                if (!mediaReadyFired) {
                    mediaReadyFired = true
                    onMediaReady()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { onDisposeRenderers() }
    }

    BackHandler {
        when (phase) {
            CallPhase.IncomingRinging -> onDecline()
            CallPhase.OutgoingRinging, CallPhase.Connecting, CallPhase.Active -> onEnd()
            CallPhase.Ended -> Unit
        }
    }

    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .background(KChatColors.callBackground),
        containerColor = KChatColors.callBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (phase) {
                CallPhase.IncomingRinging -> IncomingCallContent(
                    name = contactName,
                    isVideo = isVideo,
                    onDecline = onDecline,
                    onAccept = {
                        pendingAccept = true
                        ensurePermissionsThen {
                            pendingAccept = false
                            if (!mediaReadyFired) {
                                mediaReadyFired = true
                                onMediaReady()
                            }
                            onAccept()
                        }
                    },
                )
                CallPhase.OutgoingRinging, CallPhase.Connecting, CallPhase.Ended ->
                    RingingOrConnectingContent(
                        name = contactName,
                        statusText = statusText,
                        error = error,
                        showEnd = phase != CallPhase.Ended,
                        onEnd = onEnd,
                    )
                CallPhase.Active -> {
                    if (isVideo) {
                        ActiveVideoCallContent(
                            durationLabel = durationLabel,
                            micMuted = micMuted,
                            cameraOff = cameraOff,
                            speakerOn = speakerOn,
                            error = error,
                            onToggleMic = onToggleMic,
                            onToggleCamera = onToggleCamera,
                            onToggleSpeaker = onToggleSpeaker,
                            onEnd = onEnd,
                            onBindLocalRenderer = onBindLocalRenderer,
                            onBindRemoteRenderer = onBindRemoteRenderer,
                        )
                    } else {
                        ActiveVoiceCallContent(
                            name = contactName,
                            statusText = durationLabel.ifBlank { statusText },
                            micMuted = micMuted,
                            speakerOn = speakerOn,
                            error = error,
                            onToggleMic = onToggleMic,
                            onToggleSpeaker = onToggleSpeaker,
                            onEnd = onEnd,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingCallContent(
    name: String,
    isVideo: Boolean,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(name = name, size = 112.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (isVideo) "Cuộc gọi video" else "Cuộc gọi thoại",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CallActionButton(label = "Từ chối", isDestructive = true, onClick = onDecline)
            CallActionButton(label = "Trả lời", onClick = onAccept)
        }
    }
}

@Composable
private fun RingingOrConnectingContent(
    name: String,
    statusText: String,
    error: String?,
    showEnd: Boolean,
    onEnd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(name = name, size = 112.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
        if (showEnd) {
            EndCallButton(onClick = onEnd)
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun ActiveVoiceCallContent(
    name: String,
    statusText: String,
    micMuted: Boolean,
    speakerOn: Boolean,
    error: String?,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEnd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(name = name, size = 112.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CallControlIcon(
                icon = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                description = if (micMuted) "Bật mic" else "Tắt mic",
                onClick = onToggleMic,
            )
            CallControlIcon(
                icon = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                description = if (speakerOn) "Tắt loa ngoài" else "Loa ngoài",
                onClick = onToggleSpeaker,
            )
            EndCallButton(onClick = onEnd, compact = true)
        }
    }
}

@Composable
private fun ActiveVideoCallContent(
    durationLabel: String,
    micMuted: Boolean,
    cameraOff: Boolean,
    speakerOn: Boolean,
    error: String?,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEnd: () -> Unit,
    onBindLocalRenderer: (SurfaceViewRenderer) -> Unit,
    onBindRemoteRenderer: (SurfaceViewRenderer) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).also(onBindRemoteRenderer)
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { renderer ->
                runCatching { renderer.release() }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    durationLabel.ifBlank { "00:00" },
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.titleMedium,
                )
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).also(onBindLocalRenderer)
                    },
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    onRelease = { renderer ->
                        runCatching { renderer.release() }
                    },
                )
            }
            Column {
                if (error != null) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CallControlIcon(
                        icon = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        description = if (micMuted) "Bật mic" else "Tắt mic",
                        onClick = onToggleMic,
                    )
                    CallControlIcon(
                        icon = if (cameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        description = if (cameraOff) "Bật camera" else "Tắt camera",
                        onClick = onToggleCamera,
                    )
                    CallControlIcon(
                        icon = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        description = if (speakerOn) "Tắt loa ngoài" else "Loa ngoài",
                        onClick = onToggleSpeaker,
                    )
                    EndCallButton(onClick = onEnd, compact = true)
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(label: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier.width(140.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun EndCallButton(onClick: () -> Unit, compact: Boolean = false) {
    if (compact) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = "Kết thúc", tint = Color.White)
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kết thúc")
        }
    }
}

@Composable
private fun CallControlIcon(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit = {},
) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
