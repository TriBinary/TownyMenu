package net.trilleo.mc.plugins.townymenu.guis.framework

import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * One clickable item in a [PagedMenu]. The icon is built lazily, so only the
 * entries on the visible page cost anything to render.
 */
class MenuEntry(icon: () -> ItemStack, val action: ((ClickType) -> Unit)? = null) {
    val item: ItemStack by lazy(LazyThreadSafetyMode.NONE, icon)
}

/**
 * A six-row menu that pages through [entries]. The top four rows hold entries and the fifth stays empty;
 * the bottom row holds previous/next arrows, the back button (slot 49), and
 * any extra buttons placed by [controls] in slots 46–48 and 50–52.
 */
abstract class PagedMenu(player: Player, title: String, back: Menu?) : Menu(player, title, 6, back) {

    private var page = 0

    /** The full, ordered list of entries to page through, read fresh on every render. */
    protected abstract fun entries(): List<MenuEntry>

    /** Places extra bottom-row buttons (slots 46–48 and 50–52). */
    protected open fun controls() {}

    /** Item shown in the middle of an empty page. */
    protected open val emptyItem: ItemStack = itemStack(Material.STRUCTURE_VOID) { name(tr("menu.empty")) }

    final override fun build() {
        val all = entries()
        val pages = maxOf(1, (all.size + PAGE_SIZE - 1) / PAGE_SIZE)
        page = page.coerceIn(0, pages - 1)

        all.drop(page * PAGE_SIZE).take(PAGE_SIZE)
            .forEachIndexed { slot, entry -> button(slot, entry.item, entry.action) }
        if (all.isEmpty()) button(22, emptyItem)

        if (page > 0) {
            button(45, pageArrow(true, page, pages)) {
                page--
                render()
            }
        }
        if (page < pages - 1) {
            button(53, pageArrow(false, page + 2, pages)) {
                page++
                render()
            }
        }
        backButton(49)
        controls()
    }

    companion object {
        private const val PAGE_SIZE = 36
    }
}

/** A [PagedMenu] whose entries come from a lambda, used for simple pickers and lists. */
class ListMenu(
    player: Player,
    title: String,
    back: Menu?,
    private val source: (ListMenu) -> List<MenuEntry>,
) : PagedMenu(player, title, back) {
    override fun entries(): List<MenuEntry> = source(this)
}
