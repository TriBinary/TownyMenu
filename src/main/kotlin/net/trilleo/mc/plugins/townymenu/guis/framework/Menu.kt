package net.trilleo.mc.plugins.townymenu.guis.framework

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
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
    title: String,
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

        fun add(item: ItemStack, action: ((ClickType) -> Unit)? = null) = button(slots[next++], item, action)

        fun add(node: PermissionNodes, item: ItemStack, action: (ClickType) -> Unit) =
            guarded(slots[next++], node, item, action)
    }

    protected fun layout(vararg slots: Int) = Layout(slots)

    /** Places the back button (or a close button for top-level menus). */
    protected fun backButton(slot: Int) {
        val target = back
        if (target == null) {
            button(slot, itemStack(Material.BARRIER) { name(tr("menu.close")) }) { player.closeInventory() }
        } else {
            button(slot, itemStack(Material.ARROW) { name(tr("menu.back")) }) { target.open() }
        }
    }

    /** Places a help button opening the tutorial [chapter] that explains this menu. */
    protected fun tutorialButton(slot: Int, chapter: Chapter) {
        button(
            slot, Icons.icon(
                Material.KNOWLEDGE_BOOK, tr("tutorial.help"), tr("tutorial.help-description"),
                tr("tutorial.chapter-line", "chapter" to tr(chapter.title)),
                tr("tutorial.progress", "read" to chapter.readCount(player), "total" to chapter.lessons.size)
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

        private val FILLER = itemStack(Material.GRAY_STAINED_GLASS_PANE) { hideTooltip(true) }

    }

    private fun locked(item: ItemStack): ItemStack = itemStack(Material.GRAY_DYE) {
        lore(tr("menu.no-permission"))
        meta { displayName(item.itemMeta?.displayName()) }
    }
}
