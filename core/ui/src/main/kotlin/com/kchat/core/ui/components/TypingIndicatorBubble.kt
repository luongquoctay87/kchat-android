package com.kchat.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kchat.core.design.KChatColors
import com.kchat.core.design.KChatDimens

/**
 * Incoming-style typing bubble at the bottom of the message list (left-aligned).
 */
@Composable
fun TypingIndicatorBubble(
    visible: Boolean,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { fullHeight -> fullHeight / 3 },
        exit = fadeOut(tween(150)) + slideOutVertically(tween(180)) { fullHeight -> fullHeight / 3 },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = KChatDimens.bubbleRadius,
                            topEnd = KChatDimens.bubbleRadius,
                            bottomStart = 4.dp,
                            bottomEnd = KChatDimens.bubbleRadius,
                        ),
                    )
                    .background(KChatColors.bubbleReceived)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                TypingDots(
                    dotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun TypingDots(
    dotColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val scale by transition.animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 480),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 160, StartOffsetType.FastForward),
                ),
                label = "dotScale$index",
            )
            val dotSize = (7f * scale).dp
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = ((1f - scale) * 2f).dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.35f + 0.65f * scale)),
                )
            }
        }
    }
}
