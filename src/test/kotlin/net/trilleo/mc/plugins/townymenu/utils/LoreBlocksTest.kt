package net.trilleo.mc.plugins.townymenu.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoreBlocksTest {

    private val plain = PlainTextComponentSerializer.plainText()

    private fun blocks(build: LoreBlocks.() -> Unit): List<String> =
        LoreBlocks().apply(build).render().map { plain.serialize(it) }

    private fun LoreBlocks.text(value: String) = add(Component.text(value))

    @Test
    fun `nothing added renders nothing`() {
        assertEquals(emptyList(), blocks {})
    }

    @Test
    fun `a blank line always follows the display name`() {
        assertEquals(listOf("", "Residents: 12"), blocks { text("Residents: 12") })
    }

    @Test
    fun `separators divide blocks`() {
        val lore = blocks {
            text("A description.")
            separator()
            text("Residents: 12")
        }
        assertEquals(listOf("", "A description.", "", "Residents: 12"), lore)
    }

    @Test
    fun `repeated separators collapse into one`() {
        val lore = blocks {
            text("A description.")
            separator()
            separator()
            add(Component.empty())
            text("Residents: 12")
        }
        assertEquals(listOf("", "A description.", "", "Residents: 12"), lore)
    }

    @Test
    fun `separators at either end are dropped`() {
        val lore = blocks {
            separator()
            text("Residents: 12")
            separator()
        }
        assertEquals(listOf("", "Residents: 12"), lore)
    }

    @Test
    fun `a divider replaces the separator above it`() {
        val lore = blocks {
            text("A description.")
            separator()
            divider()
            text("Click to view")
        }
        assertEquals(4, lore.size)
        assertEquals(listOf("", "A description."), lore.take(2))
        assertTrue(lore[2].isNotEmpty() && lore[2].isBlank(), "expected a rule, got '${lore[2]}'")
        assertEquals("Click to view", lore[3])
    }

    @Test
    fun `a divider with nothing above it is dropped`() {
        val lore = blocks {
            divider()
            text("Click to view")
        }
        assertEquals(listOf("", "Click to view"), lore)
    }

    @Test
    fun `a trailing divider is dropped`() {
        val lore = blocks {
            text("Residents: 12")
            divider()
        }
        assertEquals(listOf("", "Residents: 12"), lore)
    }

    @Test
    fun `the rule is as wide as the widest line`() {
        val short = blocks {
            text("Cost: 25")
            divider()
            text("Go")
        }
        val long = blocks {
            text("A considerably longer line of lore text")
            divider()
            text("Go")
        }
        assertTrue(short[2].length < long[2].length, "the rule should grow with the lore")
    }
}
