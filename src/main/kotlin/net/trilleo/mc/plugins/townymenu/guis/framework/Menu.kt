package net.trilleo.mc.plugins.townymenu.guis.framework

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Chapter
import net.trilleo.mc.plugins.townymenu.guis.tutorial.TutorialChapterMenu
import net.trilleo.mc.plugins.townymenu.utils.DialogUtil
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

/**
 * Base class for every chest menu.
 *
 * A menu is a short-lived object bound to one [player]: create it with the
 * context it needs (a town, a resident, …) and call [open]. The menu is its
 * own [InventoryHolder], so [net.trilleo.mc.plugins.townymenu.listeners.MenuListener]
 * routes clicks without any global registry.
 *
 * [build] is called on every [render] and must read fresh data from Towny —
 * menus never cache Towny state. Every text is translated for [player] with [tr],
 * including the [title] the subclass passes in.
 *
 * @param back the menu the back button returns to; `null` shows a close button
 */
abstract class Menu(
    val player: Player,
    val title: String,
    rows: Int,
    val back: Menu? = null,
) : InventoryHolder {

    private val inventory: Inventory =
        Bukkit.createInventory(this, rows * 9, MiniMessage.miniMessage().deserialize(title))
    private val actions = HashMap<Int, (ClickType) -> Unit>()

    /** The viewer's Towny resident record, or `null` if Towny has not registered them. */
    protected val resident: Resident?
        get() = TownyAPI.getInstance().getResident(player)

    override fun getInventory(): Inventory = inventory

    /** Translates [key] into the viewer's language. See [net.trilleo.mc.plugins.townymenu.utils.Lang]. */
    fun tr(key: String, vararg args: Pair<String, Any?>): String = player.tr(key, *args)

    /** Places this menu's items and click actions. Called on every [render]. */
    protected abstract fun build()

    /** Opens the menu, or re-renders it in place when it is already open. */
    fun open() {
        render()
        if (player.openInventory.topInventory !== inventory) player.openInventory(inventory)
    }

    /** Clears and rebuilds the menu contents from current Towny data. */
    fun render() {
        actions.clear()
        inventory.clear()
        repeat(inventory.size) { inventory.setItem(it, FILLER) }
        build()
    }

    internal fun click(slot: Int, click: ClickType) {
        // A double click also fires as a regular click first; acting on both would run commands twice.
        if (click == ClickType.DOUBLE_CLICK) return
        // The menu shows the old state until the last command settles, so a second click would repeat it.
        if (MenuActions.isBusy(player)) return
        val action = actions[slot] ?: return
        player.playSound(CLICK_SOUND)
        action(click)
    }

    /** Places [item] in [slot] and runs [action] when it is clicked. */
    protected fun button(slot: Int, item: ItemStack, action: ((ClickType) -> Unit)? = null) {
        inventory.setItem(slot, item)
        if (action != null) actions[slot] = action
    }

    /**
     * Places a button that is only usable with [node]. Without the permission the
     * icon is replaced by a grey "no permission" item carrying the same name.
     */
    protected fun guarded(slot: Int, node: PermissionNodes, item: ItemStack, action: (ClickType) -> Unit) {
        if (TownyUtil.can(player, node)) button(slot, item, action) else button(slot, locked(item))
    }

    /** Places buttons one after another in [slots], so menus with optional buttons stay compact. */
    protected inner class Layout(private val slots: IntArray) {
        private var next = 0

        fun add(item: ItemStack, action: ((ClickType) -> Unit)? = null) {
            nextSlot()?.let { button(it, item, action) }
        }

        fun add(node: PermissionNodes, item: ItemStack, action: (ClickType) -> Unit) {
            nextSlot()?.let { guarded(it, node, item, action) }
        }

        /** The next free slot, or `null` (logged) when the layout is full, so an extra button never breaks the menu. */
        private fun nextSlot(): Int? {
            if (next < slots.size) return slots[next++]
            Main.instance.logger.warning("${this@Menu::class.simpleName} has more buttons than its ${slots.size} layout slots")
            return null
        }
    }

    protected fun layout(vararg slots: Int) = Layout(slots)

    /** Places the back button (or a close button for top-level menus). */
    protected fun backButton(slot: Int) {
        val target = back
        if (target == null) {
            button(slot, itemStack(Material.BARRIER) { name(tr("menu.close")) }) { player.closeInventory() }
        } else {
            button(slot, itemStack(Material.ARROW) {
                name(tr("menu.back"))
                lore(tr("menu.back-to", "menu" to target.title))
            }) { target.open() }
        }
    }

    /** A page-turning arrow whose lore names the [target] page of [pages]. */
    protected fun pageArrow(previous: Boolean, target: Int, pages: Int): ItemStack = itemStack(Material.ARROW) {
        name(tr(if (previous) "menu.previous-page" else "menu.next-page"))
        lore(tr("menu.page", "page" to target, "pages" to pages))
        loreActions(hints("click.turn-page"))
    }

    /** The translated click hints for [keys], ready for an icon's `actions`. */
    fun hints(vararg keys: String): List<String> = keys.map { tr(it) }

    /**
     * A progress bar lore line for [current] out of [max], with the percentage after it. A [max] of zero or less
     * reads as full, so an unlimited allowance never shows as empty.
     */
    fun progressBar(current: Number, max: Number): String {
        val ratio = if (max.toDouble() <= 0) 1.0 else (current.toDouble() / max.toDouble()).coerceIn(0.0, 1.0)
        val filled = (ratio * BAR_WIDTH).roundToInt()
        return tr(
            "common.progress-bar",
            "filled" to " ".repeat(filled),
            "empty" to " ".repeat(BAR_WIDTH - filled),
            "percent" to (ratio * 100).roundToInt()
        )
    }

    /**
     * A `Cost: …` lore line for a button that charges [amount], or `null` when the server has no
     * economy or the action is free. Splat it into an icon with
     * `*listOfNotNull(costLine(x)).toTypedArray()`; see [net.trilleo.mc.plugins.townymenu.utils.Prices].
     */
    fun costLine(amount: Double): String? =
        if (TownyUtil.economy && amount > 0) tr("common.cost", "cost" to TownyUtil.money(amount)) else null

    /**
     * A `Price: …` lore line for something on sale at [amount], or `null` without an economy.
     * Unlike [costLine] a price of zero is still shown, because a free plot is an offer, not a missing fee.
     */
    fun priceLine(amount: Double): String? =
        if (TownyUtil.economy) tr("common.price", "price" to TownyUtil.money(amount)) else null

    /**
     * The option after [current] in this list on a left click, or the one before it on a right click, wrapping at
     * either end, for buttons that step through a list of choices such as a sort order.
     */
    protected fun <T> List<T>.cycle(current: T, click: ClickType): T {
        val step = if (click.isRightClick) size - 1 else 1
        return this[(indexOf(current).coerceAtLeast(0) + step) % size]
    }

    /** Places a help button opening the tutorial [chapter] that explains this menu. */
    protected fun tutorialButton(slot: Int, chapter: Chapter) {
        button(
            slot, Icons.icon(
                Material.KNOWLEDGE_BOOK, tr("tutorial.help"), tr("tutorial.help-description"),
                tr("tutorial.chapter-line", "chapter" to tr(chapter.title)),
                tr("tutorial.progress", "read" to chapter.readCount(player), "total" to chapter.lessons.size),
                progressBar(chapter.readCount(player), chapter.lessons.size),
                actions = hints("click.open")
            )
        ) {
            TutorialChapterMenu(player, chapter, this).open()
        }
    }

    /**
     * Runs a Towny [command] as the player. See [MenuActions.perform] for how the
     * menu reacts; [probe] reads the state the command is expected to change.
     */
    fun run(
        command: String,
        probe: (() -> Any?)? = null,
        returnTo: Menu = this,
        delayTicks: Long = MenuActions.DEFAULT_DELAY,
    ) {
        MenuActions.perform(returnTo, command, probe, delayTicks)
    }

    /** Runs [command] and closes the menu, for actions whose result is a teleport, book, or chat output. */
    fun runAndClose(command: String) {
        player.closeInventory()
        MenuActions.perform(this, command, null, MenuActions.DEFAULT_DELAY)
    }

    /**
     * Shows a text-input dialog. Submitting passes the text to [onSubmit];
     * cancelling reopens this menu.
     */
    fun prompt(
        title: String,
        label: String,
        body: String? = null,
        initial: String = "",
        maxLength: Int = 64,
        multiline: Boolean = false,
        onSubmit: (String) -> Unit,
    ) {
        DialogUtil.input(player, title, label, onSubmit, ::open, body, initial, maxLength, multiline)
    }

    companion object {
        private val CLICK_SOUND = Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.UI, 0.5f, 1f)

        private val FILLER = itemStack(Material.BLACK_STAINED_GLASS_PANE) { hideTooltip(true) }

        private const val BAR_WIDTH = 20
    }

    private fun locked(item: ItemStack): ItemStack = itemStack(Material.GRAY_DYE) {
        lore(tr("menu.no-permission"))
        meta { displayName(item.itemMeta?.displayName()) }
    }
}
