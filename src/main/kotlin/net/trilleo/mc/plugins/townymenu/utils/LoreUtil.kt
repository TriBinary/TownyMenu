package net.trilleo.mc.plugins.townymenu.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Utility for wrapping MiniMessage-formatted text into multiple lore-ready
 * [Component] lines with word-aware line breaking, configurable width, and
 * automatic style carry-over.
 *
 * Each output line is prefixed with a style reset (`<!i>`) to neutralize
 * Minecraft's default purple italic lore styling.
 *
 * ### Usage
 *
 * ```kotlin
 * import net.trilleo.mc.plugins.townymenu.utils.LoreUtil
 *
 * val lines = LoreUtil.wrapLore("<gray>A long description text that will be wrapped.")
 * val narrow = LoreUtil.wrapLore("<red>Warning: dangerous item!", maxWidth = 30)
 * ```
 *
 * Width is measured in columns: Chinese, Japanese, and Korean characters count
 * as two, and lines may break between them.
 *
 * Explicit newlines (`\n` or `<newline>`) force a line break:
 * ```kotlin
 * val lines = LoreUtil.wrapLore("<gray>Line one\nLine two")
 * ```
 */
object LoreUtil {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Wraps a MiniMessage-formatted string into multiple lore-ready [Component] lines.
     *
     * @param text the MiniMessage-formatted input string
     * @param maxWidth maximum number of columns per line (default 40); wide CJK characters take two
     * @return a list of [Component] lines suitable for use as item lore
     */
    fun wrapLore(text: String, maxWidth: Int = 40): List<Component> {
        if (text.isEmpty()) return emptyList()

        // Pre-process: replace <newline> tag with \n for uniform handling
        val normalized = text.replace("<newline>", "\n")

        // Split on explicit newlines; each segment is wrapped independently
        val segments = normalized.split("\n")

        val result = mutableListOf<Component>()
        var carryOverStyle = Style.empty()

        for (segment in segments) {
            if (segment.isEmpty()) {
                result.add(buildLoreLine(emptyList()))
                continue
            }

            // Parse the segment with any carry-over style context
            val component = miniMessage.deserialize(segment)
            val styledChars = flattenComponent(component, carryOverStyle)

            val wrappedLines = wrapStyledChars(styledChars, maxWidth)

            for (line in wrappedLines) {
                result.add(buildLoreLine(line))
                if (line.isNotEmpty()) {
                    carryOverStyle = line.last().style
                }
            }

            // Update carry-over from the last character of this segment
            if (styledChars.isNotEmpty()) {
                carryOverStyle = styledChars.last().style
            }
        }

        return result
    }

    /**
     * Represents a single visible character paired with its resolved style.
     */
    private data class StyledChar(val char: Char, val style: Style)

    /**
     * Recursively flattens a [Component] tree into a list of [StyledChar],
     * resolving style inheritance from parent to child.
     */
    private fun flattenComponent(component: Component, parentStyle: Style): List<StyledChar> {
        val result = mutableListOf<StyledChar>()
        val resolvedStyle = parentStyle.merge(component.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)

        // Extract text content from TextComponent nodes
        if (component is TextComponent) {
            for (char in component.content()) {
                result.add(StyledChar(char, resolvedStyle))
            }
        }

        // Recurse into children
        for (child in component.children()) {
            result.addAll(flattenComponent(child, resolvedStyle))
        }

        return result
    }

    /**
     * Splits a flat list of styled characters into word-aware wrapped lines,
     * each not exceeding [maxWidth] columns.
     *
     * Chinese, Japanese, and Korean characters count as two columns and may be
     * broken between any two characters, since those scripts don't separate
     * words with spaces. Closing punctuation stays on the line of the character
     * before it.
     */
    private fun wrapStyledChars(chars: List<StyledChar>, maxWidth: Int): List<List<StyledChar>> {
        if (chars.isEmpty()) return listOf(emptyList())

        val words = mutableListOf<List<StyledChar>>()
        var currentWord = mutableListOf<StyledChar>()
        for (sc in chars) {
            if (sc.char == ' ') {
                words.add(currentWord)
                currentWord = mutableListOf()
            } else {
                currentWord.add(sc)
            }
        }
        words.add(currentWord)

        val lines = mutableListOf<List<StyledChar>>()
        var currentLine = mutableListOf<StyledChar>()

        fun place(segment: List<StyledChar>, glued: Boolean) {
            val separator = if (currentLine.isEmpty() || glued) 0 else 1
            when {
                width(segment) > maxWidth -> {
                    if (currentLine.isNotEmpty()) lines.add(currentLine)
                    val broken = forceBreakWord(segment, maxWidth)
                    lines.addAll(broken.dropLast(1))
                    currentLine = broken.last().toMutableList()
                }
                currentLine.isEmpty() -> currentLine.addAll(segment)
                width(currentLine) + separator + width(segment) <= maxWidth -> {
                    if (separator == 1) currentLine.add(StyledChar(' ', segment.first().style))
                    currentLine.addAll(segment)
                }
                else -> {
                    lines.add(currentLine)
                    currentLine = segment.toMutableList()
                }
            }
        }

        for (word in words) {
            if (word.isEmpty()) {
                // Consecutive spaces: keep the extra space when it fits
                if (currentLine.isNotEmpty() && width(currentLine) < maxWidth) {
                    currentLine.add(StyledChar(' ', currentLine.last().style))
                }
                continue
            }
            segments(word).forEachIndexed { index, segment -> place(segment, glued = index > 0) }
        }

        if (currentLine.isNotEmpty() || lines.isEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    /** Splits a space-free word at every point a line may break: around each wide character. */
    private fun segments(word: List<StyledChar>): List<List<StyledChar>> {
        val result = mutableListOf<MutableList<StyledChar>>()
        var previousWide = false
        for (sc in word) {
            val wide = isWide(sc.char)
            val startNew = result.isEmpty() || ((wide || previousWide) && sc.char !in CLOSING_PUNCTUATION)
            if (startNew) result.add(mutableListOf(sc)) else result.last().add(sc)
            previousWide = wide
        }
        return result
    }

    /**
     * Force-breaks a word that exceeds [maxWidth] columns into multiple chunks.
     */
    private fun forceBreakWord(word: List<StyledChar>, maxWidth: Int): List<List<StyledChar>> {
        val result = mutableListOf<List<StyledChar>>()
        var chunk = mutableListOf<StyledChar>()
        var chunkWidth = 0
        for (sc in word) {
            val charWidth = width(sc.char)
            if (chunk.isNotEmpty() && chunkWidth + charWidth > maxWidth) {
                result.add(chunk)
                chunk = mutableListOf()
                chunkWidth = 0
            }
            chunk.add(sc)
            chunkWidth += charWidth
        }
        result.add(chunk)
        return result
    }

    private fun width(chars: List<StyledChar>): Int = chars.sumOf { width(it.char) }

    private fun width(char: Char): Int = if (isWide(char)) 2 else 1

    private fun isWide(char: Char): Boolean =
        char in '\u3000'..'\u303F' || char in '\uFF00'..'\uFFEF' ||
            Character.UnicodeScript.of(char.code) in WIDE_SCRIPTS

    private val WIDE_SCRIPTS = setOf(
        Character.UnicodeScript.HAN,
        Character.UnicodeScript.HIRAGANA,
        Character.UnicodeScript.KATAKANA,
        Character.UnicodeScript.HANGUL,
    )

    private const val CLOSING_PUNCTUATION = "，。、；：？！）」』》〉】,.;:?!)"

    /**
     * Builds a single lore line [Component] from styled characters,
     * prefixed with a reset to override Minecraft's default lore styling.
     */
    private fun buildLoreLine(chars: List<StyledChar>): Component {
        if (chars.isEmpty()) {
            return Component.empty().style(Style.style().decoration(TextDecoration.ITALIC, false).build())
        }

        // Group consecutive characters with the same style
        val builder = Component.text().style(
            Style.style().decoration(TextDecoration.ITALIC, false).build()
        )

        var currentStyle = chars.first().style
        val buffer = StringBuilder()

        for (sc in chars) {
            if (sc.style == currentStyle) {
                buffer.append(sc.char)
            } else {
                // Flush current group
                builder.append(Component.text(buffer.toString(), currentStyle))
                buffer.clear()
                currentStyle = sc.style
                buffer.append(sc.char)
            }
        }

        // Flush final group
        if (buffer.isNotEmpty()) {
            builder.append(Component.text(buffer.toString(), currentStyle))
        }

        return builder.build()
    }
}
