package com.kchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

internal sealed class MessageTextSegment {
    data class Text(val value: String) : MessageTextSegment()
    data class CodeBlock(val language: String?, val code: String) : MessageTextSegment()
}

/**
 * Splits message text into plain segments and fenced ```code``` blocks.
 * Incomplete fences stay as plain text.
 */
internal fun parseMessageTextSegments(text: String): List<MessageTextSegment> {
    if (text.isEmpty()) return listOf(MessageTextSegment.Text(""))
    val matcher = FENCE_PATTERN.matcher(text)
    val out = ArrayList<MessageTextSegment>()
    var last = 0
    while (matcher.find()) {
        val start = matcher.start()
        if (start > last) {
            out += MessageTextSegment.Text(text.substring(last, start))
        }
        val language = matcher.group(1)?.trim()?.takeIf { it.isNotEmpty() }
        val code = matcher.group(2).orEmpty().trimEnd('\n')
        out += MessageTextSegment.CodeBlock(language = language, code = code)
        last = matcher.end()
    }
    if (last < text.length) {
        out += MessageTextSegment.Text(text.substring(last))
    }
    if (out.isEmpty()) {
        out += MessageTextSegment.Text(text)
    }
    return out
}

@Composable
internal fun MessageFormattedBody(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val segments = remember(text) { parseMessageTextSegments(text) }
    val mentionColor = MaterialTheme.colorScheme.primary
    val inlineCodeBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    val inlineCodeFg = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is MessageTextSegment.Text -> {
                    if (segment.value.isNotEmpty()) {
                        Text(
                            text = remember(segment.value, mentionColor, inlineCodeBg, inlineCodeFg) {
                                buildRichMessageAnnotatedString(
                                    text = segment.value,
                                    mentionColor = mentionColor,
                                    inlineCodeBackground = inlineCodeBg,
                                    inlineCodeColor = inlineCodeFg,
                                )
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                        )
                    }
                }
                is MessageTextSegment.CodeBlock -> {
                    CodeBlockBubble(
                        language = segment.language,
                        code = segment.code,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 0.dp else 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockBubble(
    language: String?,
    code: String,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (!language.isNullOrBlank()) {
            Text(
                text = language,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Text(
            text = code.ifEmpty { " " },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            softWrap = false,
            modifier = Modifier.horizontalScroll(scroll),
        )
    }
}

internal fun buildRichMessageAnnotatedString(
    text: String,
    mentionColor: Color,
    inlineCodeBackground: Color,
    inlineCodeColor: Color,
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    return buildAnnotatedString {
        val inlineMatcher = INLINE_CODE_PATTERN.matcher(text)
        var last = 0
        while (inlineMatcher.find()) {
            val start = inlineMatcher.start()
            val end = inlineMatcher.end()
            if (start > last) {
                appendWithMentions(text.substring(last, start), mentionColor)
            }
            val code = inlineMatcher.group(1).orEmpty()
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = inlineCodeBackground,
                    color = inlineCodeColor,
                    fontSize = 13.sp,
                ),
            ) {
                append(code)
            }
            last = end
        }
        if (last < text.length) {
            appendWithMentions(text.substring(last), mentionColor)
        }
    }
}

private fun AnnotatedString.Builder.appendWithMentions(text: String, mentionColor: Color) {
    if (text.isEmpty()) return
    val matcher = MENTION_PATTERN.matcher(text)
    var last = 0
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        if (start > last) {
            append(text.substring(last, start))
        }
        withStyle(
            SpanStyle(
                color = mentionColor,
                fontWeight = FontWeight.SemiBold,
            ),
        ) {
            append(text.substring(start, end))
        }
        last = end
    }
    if (last < text.length) {
        append(text.substring(last))
    }
}

/** ```lang?\n...\n``` — closing fence required. */
private val FENCE_PATTERN: Pattern =
    Pattern.compile("```([a-zA-Z0-9_+-]*)[ \\t]*\\r?\\n([\\s\\S]*?)```")

private val INLINE_CODE_PATTERN: Pattern =
    Pattern.compile("(?<!`)`([^`\\n]+)`(?!`)")

private val MENTION_PATTERN: Pattern =
    Pattern.compile("(?<![\\w.])@[A-Za-z0-9_.]{1,64}\\b")

/** True when the whole message is a single fenced code block. */
internal fun isWholeMessageCodeSnippet(text: String): Boolean =
    unwrapCodeSnippet(text) != null

/** Returns inner code if [text] is exactly one fenced block; otherwise null. */
internal fun unwrapCodeSnippet(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null
    val segments = parseMessageTextSegments(trimmed)
    val only = segments.singleOrNull() as? MessageTextSegment.CodeBlock ?: return null
    return only.code
}

/** Wraps plain text in a fenced code block; leaves an existing whole-message fence unchanged. */
internal fun wrapAsCodeSnippet(text: String): String {
    if (isWholeMessageCodeSnippet(text)) return text.trimEnd()
    val body = text.trimEnd()
    return "```\n$body\n```"
}

/** Toggle fence wrap for a message body. */
internal fun toggleCodeSnippet(text: String): String {
    unwrapCodeSnippet(text)?.let { return it }
    return wrapAsCodeSnippet(text)
}
