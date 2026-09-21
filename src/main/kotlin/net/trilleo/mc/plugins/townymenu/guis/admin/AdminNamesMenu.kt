package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The names an admin `add`/`remove` subcommand holds — outlaws, trusted residents and towns, sanctioned towns.
 * Clicking an entry removes it, and the bottom row adds one by name.
 *
 * @param command the subcommand to add to and remove from, such as `towny:townyadmin town Springfield outlaw`
 * @param held    reads the current names, fresh on every render
 */
class AdminNamesMenu(
    player: Player,
    title: String,
    back: Menu,
    private val node: PermissionNodes,
    private val command: String,
    private val material: Material,
    private val addTitle: String,
    private val addLabel: String,
    private val held: () -> List<String>,
) : PagedMenu(player, title, back) {

    private fun count() = held().size

    override fun entries(): List<MenuEntry> = held().sortedBy { it.lowercase() }.map { name ->
        MenuEntry({
            Icons.icon(
                material, "<white>${TownyUtil.name(name)}",
                actions = listOf(tr("admin-tools.click-remove"))
            )
        }) {
            run("$command remove $name", ::count)
        }
    }

    override fun controls() {
        guarded(
            47, node,
            Icons.icon(
                Material.NAME_TAG, tr("admin-tools.add"), tr("admin-tools.add-description"),
                actions = hints("click.type-name")
            )
        ) {
            prompt(addTitle, addLabel) { name ->
                run("$command add ${TownyUtil.argument(name)}", ::count)
            }
        }
    }
}
