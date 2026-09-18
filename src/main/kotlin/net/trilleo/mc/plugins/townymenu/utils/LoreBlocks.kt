package net.trilleo.mc.plugins.townymenu.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * Assembles an icon's lore out of blocks, so a tooltip reads as description,
 * data, and actions rather than one dense wall of text.
 *
 * Lines go in with [add], block boundaries with [separator], and the closing
 * actions block with [divider]. [render] lays them out:
 *
 * - a blank line always follows the display name, so the title has room;
 * - repeated separators collapse into one, and a line with no visible text
 *   counts as a separator, so callers may pass `""` between blocks;
 * - a divider swallows the separators above it and is drawn as a rule as wide
 *   as the widest line;
 * - separators left dangling at either end are dropped, so a caller may open a
 *   block that turns out to be empty without leaving a hole behind.
 *
 * @see ItemStackBuilder
 */
class LoreBlocks {

    private enum class Kind { TEXT, SEPARATOR, DIVIDER }

    private class Line(val component: Component, val kind: Kind)

    private val lines = mutableListOf<Line>()

    /** Appends [component], treating a line with no visible text as a [separator]. */
    fun add(component: Component) {
        lines += Line(component, if (PLAIN.serialize(component).isBlank()) Kind.SEPARATOR else Kind.TEXT)
    }

    /** Ends the current block; the next line starts a new one. */
    fun separator() {
        lines += Line(BLANK, Kind.SEPARATOR)
    }

    /** Ends the data blocks; what follows is the icon's actions, set off by a rule. */
    fun divider() {
        lines += Line(BLANK, Kind.DIVIDER)
    }

    /** The finished lore, or an empty list when nothing but separators was added. */
    fun render(): List<Component> {
        val result = mutableListOf<Line>()
        for (line in lines) {
            when (line.kind) {
                Kind.TEXT -> result += line
                Kind.SEPARATOR -> if (result.lastOrNull()?.kind == Kind.TEXT) result += line
                Kind.DIVIDER -> {
                    while (result.lastOrNull()?.kind == Kind.SEPARATOR) result.removeLast()
                    if (result.isNotEmpty()) result += line
                }
            }
        }
        while (result.isNotEmpty() && result.last().kind != Kind.TEXT) result.removeLast()
        if (result.isEmpty()) return emptyList()

        val rule = rule(result)
        return listOf(BLANK) + result.map { if (it.kind == Kind.DIVIDER) rule else it.component }
    }

    /**
     * The divider rule, as wide as the widest line. It is drawn as struck-through
     * spaces, and a space is narrower than an average character, so the count is
     * scaled up rather than matching the column count one to one.
     */
    private fun rule(lines: List<Line>): Component {
        val columns = lines.filter { it.kind == Kind.TEXT }
            .maxOf { LoreUtil.columns(PLAIN.serialize(it.component)) }
        val spaces = (columns * 3 / 2).coerceIn(MIN_RULE, MAX_RULE)
        return MiniMessage.miniMessage().deserialize("<!i><dark_gray><st>" + " ".repeat(spaces))
    }

    private companion object {
        val PLAIN: PlainTextComponentSerializer = PlainTextComponentSerializer.plainText()

        val BLANK: Component = Component.empty()
            .style(Style.style().decoration(TextDecoration.ITALIC, false).build())

        const val MIN_RULE = 16
        const val MAX_RULE = 60
    }
}
