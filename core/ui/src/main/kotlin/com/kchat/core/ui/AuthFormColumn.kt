package com.kchat.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Scrollable auth/form column that keeps focused inputs above the IME (edge-to-edge safe).
 *
 * [imePadding] must sit before [verticalScroll] so the scroll viewport shrinks when the keyboard
 * opens. Pair with [KChatAuthScaffold] / [KChatDetailScaffold] using zero content window insets.
 *
 * Use [verticalArrangement] = [Arrangement.Center] on short forms (e.g. login) so content sits
 * in the middle of the remaining space above the keyboard instead of sticking to the top.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthFormColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val bottomSpacer = if (imeBottom > 0.dp) 48.dp else 24.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imeNestedScroll()
            .imePadding()
            .verticalScroll(scrollState),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        content()
        Spacer(modifier = Modifier.height(bottomSpacer))
    }
}
