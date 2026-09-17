package net.trilleo.mc.plugins.townymenu.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

/**
 * DSL builder for creating [ItemStack] instances in a concise, readable way.
 *
 * All text (display name and lore lines) is parsed through
 * [MiniMessage](https://docs.advntr.dev/minimessage/index.html). Italics are
 * disabled by default so menu icons read cleanly.
 *
 * ```kotlin
 * val icon = itemStack(Material.EMERALD) {
 *     name("<green>Deposit")
 *     lore("<gray>Add money to the town bank")
 *     glow(true)
 * }
 * ```
 *
 * Use [loreWrapped] for long descriptions (wrapped with [LoreUtil]), [head]
 * for player heads, and the [meta] escape hatch for anything else.
 */
class ItemStackBuilder(private val material: Material) {

    private val miniMessage = MiniMessage.miniMessage()

    private var displayName: String? = null
    private val loreLines = mutableListOf<Component>()
    private var glint: Boolean? = null
    private var isHideTooltip: Boolean = false
    private var headOwner: OfflinePlayer? = null
    private var metaBlock: (ItemMeta.() -> Unit)? = null

    /** Sets the display name (MiniMessage). */
    fun name(name: String) {
        displayName = name
    }

    /** Appends lore lines; each line is parsed with MiniMessage independently. */
    fun lore(vararg lines: String) {
        lore(lines.asList())
    }

    /** Appends lore lines; each line is parsed with MiniMessage independently. */
    fun lore(lines: Iterable<String>) {
        lines.forEach { loreLines += miniMessage.deserialize("<!i>$it") }
    }

    /** Appends [text] to the lore, word-wrapped by [LoreUtil.wrapLore]. */
    fun loreWrapped(text: String, maxWidth: Int = 40) {
        loreLines += LoreUtil.wrapLore(text, maxWidth)
    }

    /** Forces the enchantment glint on (`true`) or off (`false`). */
    fun glow(value: Boolean) {
        glint = value
    }

    /** Hides the whole tooltip, used for decorative filler items. */
    fun hideTooltip(value: Boolean) {
        isHideTooltip = value
    }

    /** Makes a [Material.PLAYER_HEAD] show [player]'s skin. */
    fun head(player: OfflinePlayer) {
        headOwner = player
    }

    /** Escape hatch for direct [ItemMeta] manipulation, applied last. */
    fun meta(block: ItemMeta.() -> Unit) {
        metaBlock = block
    }

    /** Builds the configured [ItemStack]. */
    fun build(): ItemStack {
        val item = ItemStack(material)
        item.editMeta { meta ->
            displayName?.let { meta.displayName(miniMessage.deserialize("<!i>$it")) }
            if (loreLines.isNotEmpty()) meta.lore(loreLines)
            glint?.let { meta.setEnchantmentGlintOverride(it) }
            meta.isHideTooltip = isHideTooltip
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            headOwner?.let { (meta as? SkullMeta)?.owningPlayer = it }
            metaBlock?.invoke(meta)
        }
        return item
    }
}

/**
 * Creates an [ItemStack] of the given [material] using a builder DSL.
 *
 * @param material the material type for the item
 * @param block    the builder configuration block
 */
fun itemStack(material: Material, block: ItemStackBuilder.() -> Unit): ItemStack =
    ItemStackBuilder(material).apply(block).build()
