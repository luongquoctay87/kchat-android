package com.kchat.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    @Suppress("UNUSED_PARAMETER") tagline: String? = null,
) {
    when (size) {
        WordmarkSize.Compact -> {
            Image(
                painter = painterResource(R.drawable.kchat_logo),
                contentDescription = "K-CHAT",
                modifier = modifier
                    .height(28.dp)
                    .widthIn(max = 140.dp),
                contentScale = ContentScale.Fit,
            )
        }
        WordmarkSize.Hero -> {
            Box(
                modifier = modifier
                    .size(220.dp)
                    .border(
                        width = 10.dp,
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape,
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.kchat_logo),
                    contentDescription = "K-CHAT",
                    // 85% of previous image size; outer circle stays 220.dp
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit,
                )
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
