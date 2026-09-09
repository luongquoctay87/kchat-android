package com.kchat.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kchat.core.design.KChatScriptFontFamily
import com.kchat.core.design.KChatTheme
import com.kchat.core.ui.R

enum class WordmarkSize {
    Compact,
    Hero,
}

@Composable
fun KChatWordmark(
    modifier: Modifier = Modifier,
    size: WordmarkSize = WordmarkSize.Compact,
    showText: Boolean = (size == WordmarkSize.Compact),
    tagline: String? = null,
) {
    when (size) {
        WordmarkSize.Compact -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.kchat_logo),
                    contentDescription = "K-Chat",
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit,
                )
                if (showText) {
                    Text(
                        text = "Chat",
                        fontFamily = KChatScriptFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        WordmarkSize.Hero -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .border(
                            width = 8.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = CircleShape,
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.kchat_logo),
                        contentDescription = "K-Chat",
                        modifier = Modifier.size(135.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                if (showText) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Chat",
                        fontFamily = KChatScriptFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (tagline != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordmarkCompactPreview() {
    KChatTheme {
        KChatWordmark(size = WordmarkSize.Compact, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun WordmarkHeroPreview() {
    KChatTheme {
        KChatWordmark(size = WordmarkSize.Hero, modifier = Modifier.padding(24.dp))
    }
}
