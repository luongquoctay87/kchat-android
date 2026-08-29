package com.kchat.core.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val KeyboardScrollExtraBottom = 180.dp
private val KeyboardScrollExtraTop = 24.dp
private const val KeyboardScrollRetryDelayMs = 100L

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.keyboardScrollOnFocus(): Modifier = composed {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val extraBottomPx = with(density) { KeyboardScrollExtraBottom.toPx() }
    val extraTopPx = with(density) { KeyboardScrollExtraTop.toPx() }
    var isFocused by remember { mutableStateOf(false) }
    var widthPx by remember { mutableIntStateOf(0) }
    var heightPx by remember { mutableIntStateOf(0) }

    suspend fun scrollIntoView() {
        if (widthPx <= 0 || heightPx <= 0) return
        bringIntoViewRequester.bringIntoView(
            rect = Rect(
                left = 0f,
                top = -extraTopPx,
                right = widthPx.toFloat(),
                bottom = heightPx.toFloat() + extraBottomPx,
            ),
        )
    }

    LaunchedEffect(isFocused, imeBottom) {
        if (!isFocused || imeBottom <= 0.dp) return@LaunchedEffect
        delay(KeyboardScrollRetryDelayMs)
        scrollIntoView()
        delay(KeyboardScrollRetryDelayMs)
        scrollIntoView()
    }

    this
        .onSizeChanged {
            widthPx = it.width
            heightPx = it.height
        }
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { event ->
            isFocused = event.isFocused
            if (event.isFocused) {
                scope.launch {
                    delay(KeyboardScrollRetryDelayMs)
                    scrollIntoView()
                }
            }
        }
}
