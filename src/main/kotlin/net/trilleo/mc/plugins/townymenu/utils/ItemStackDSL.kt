package net.trilleo.mc.plugins.townymenu.utils

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
 *     loreWrapped("<gray>Move money from your balance into the town bank.")
 *     loreBreak()
 *     lore("<gray>Balance: <white>$120")
 *     loreActions(listOf("<yellow>Click to deposit"))
 * }
 * ```
 *
 * Lore reads as blocks rather than one dense wall of text: [loreBreak] starts a
 * new block and [loreActions] closes with the hints for what clicking does, set
 * off by a divider rule. [LoreBlocks] documents how the blocks are laid out.
 *
 * Use [loreWrapped] for long descriptions (wrapped with [LoreUtil]), [head]
 * for player heads, and the [meta] escape hatch for anything else.
 */
class ItemStackBuilder(private val material: Material) {

    private val miniMessage = MiniMessage.miniMessage()

    private var displayName: String? = null
    private val loreBlocks = LoreBlocks()
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
        lines.forEach { loreBlocks.add(miniMessage.deserialize("<!i>$it")) }
    }

    /** Appends [text] to the lore, word-wrapped by [LoreUtil.wrapLore]. */
    fun loreWrapped(text: String, maxWidth: Int = 40) {
        LoreUtil.wrapLore(text, maxWidth).forEach(loreBlocks::add)
    }

    /** Starts a new lore block, separated from the one above by a blank line. */
    fun loreBreak() {
        loreBlocks.separator()
    }

    /**
     * Appends the icon's action hints — what a left- or right-click does — as the
     * closing lore block, set off from the data above by a divider rule.
     */
    fun loreActions(lines: Iterable<String>) {
        if (!lines.iterator().hasNext()) return
        loreBlocks.divider()
        lore(lines)
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
        val lore = loreBlocks.render()
        val item = ItemStack(material)
        item.editMeta { meta ->
            displayName?.let { meta.displayName(miniMessage.deserialize("<!i>$it")) }
            if (lore.isNotEmpty()) meta.lore(lore)
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
