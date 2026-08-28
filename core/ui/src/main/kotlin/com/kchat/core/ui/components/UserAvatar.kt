package com.kchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = KChatDimens.avatarSmall,
    imageUrl: String? = null,
    isOnline: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val context = LocalContext.current

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (!imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        AvatarInitial(
                            initial = initial,
                            contentColor = contentColor,
                        )
                    },
                    error = {
                        AvatarInitial(
                            initial = initial,
                            contentColor = contentColor,
                        )
                    },
                )
            } else {
                AvatarInitial(initial = initial, contentColor = contentColor)
            }
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(KChatColors.online)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

@Composable
private fun AvatarInitial(
    initial: String,
    contentColor: Color,
) {
    Text(
        text = initial,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = contentColor,
    )
}
