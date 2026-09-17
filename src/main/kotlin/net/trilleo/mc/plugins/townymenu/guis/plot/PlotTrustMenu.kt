package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Players trusted on one plot. Towny's `/plot trust` acts on the plot the player
 * stands in, so this menu is tied to the plot it was opened for.
 */
class PlotTrustMenu(player: Player, private val plot: TownBlock, back: Menu) : PagedMenu(player, "Plot Trust", back) {

    private fun count() = plot.trustedResidents.size

    override fun entries(): List<MenuEntry> =
        plot.trustedResidents.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.resident(trusted, "", "<red>Click to untrust") }) {
                run("towny:plot trust remove ${trusted.name}", ::count)
            }
        }

    override fun controls() {
        guarded(47, PermissionNodes.TOWNY_COMMAND_PLOT_TRUST,
            Icons.icon(Material.PLAYER_HEAD, "<green>Trust Online Player", "Let an online player build on this plot.")) {
            Pickers.resident(this, "Trust on Plot", { !plot.hasTrustedResident(it) }) { picked ->
                run("towny:plot trust add ${picked.name}", ::count)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_PLOT_TRUST,
            Icons.icon(Material.NAME_TAG, "<green>Trust by Name", "Trust any player Towny knows.")) {
            prompt("Trust on Plot", "Player name") { name -> run("towny:plot trust add ${TownyUtil.argument(name)}", ::count) }
        }
    }
}
