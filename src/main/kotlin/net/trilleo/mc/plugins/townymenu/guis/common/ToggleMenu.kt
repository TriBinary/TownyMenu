package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/**
 * One on/off setting backed by a Towny toggle command. [name] and [description] are already translated.
 *
 * @param node the permission the toggle needs, or `null` when Towny checks none
 * @param value reads the current state; `null` when the target no longer exists
 */
class Toggle(
    val material: Material,
    val name: String,
    val description: String,
    val node: PermissionNodes?,
    val command: String,
    val value: () -> Boolean?,
)

/** A menu of [Toggle] switches, seven per row and up to three rows per page. */
class ToggleMenu(
    player: Player,
    title: String,
    back: Menu,
    private val toggles: List<Toggle>,
) : Menu(player, title, 3 + (minOf(toggles.size, PAGE_SIZE) + 6) / 7, back) {

    private var page = 0

    override fun build() {
        val pages = maxOf(1, (toggles.size + PAGE_SIZE - 1) / PAGE_SIZE)
        page = page.coerceIn(0, pages - 1)
        toggles.drop(page * PAGE_SIZE).take(PAGE_SIZE).forEachIndexed { index, toggle ->
            val slot = 10 + index / 7 * 9 + index % 7
            val icon = Icons.toggle(player, toggle.material, toggle.name, toggle.value() ?: false, toggle.description)
            val action = { _: ClickType -> run(toggle.command, toggle.value) }
            val node = toggle.node
            if (node == null) button(slot, icon, action) else guarded(slot, node, icon, action)
        }

        val bottom = inventory.size - 9
        if (page > 0) {
            button(bottom, pageArrow("menu.previous-page", page, pages)) {
                page--
                render()
            }
        }
        if (page < pages - 1) {
            button(bottom + 8, pageArrow("menu.next-page", page + 2, pages)) {
                page++
                render()
            }
        }
        backButton(bottom + 4)
    }

    private fun pageArrow(key: String, target: Int, pages: Int) = itemStack(Material.ARROW) {
        name(tr(key))
        lore(tr("menu.page", "page" to target, "pages" to pages))
    }

    private companion object {
        const val PAGE_SIZE = 21
    }
}
