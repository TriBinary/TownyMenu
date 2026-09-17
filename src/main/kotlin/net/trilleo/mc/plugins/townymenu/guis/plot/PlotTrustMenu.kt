package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Players trusted on one plot. Towny's `/plot trust` acts on the plot the player
 * stands in, so this menu is tied to the plot it was opened for.
 */
class PlotTrustMenu(player: Player, private val plot: TownBlock, back: Menu) : PagedMenu(player, player.tr("plot-trust.title"), back) {

    private fun count() = plot.trustedResidents.size

    override fun entries(): List<MenuEntry> =
        plot.trustedResidents.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.resident(player, trusted, "", tr("common.click-untrust")) }) {
                run("towny:plot trust remove ${trusted.name}", ::count)
            }
        }

    override fun controls() {
        tutorialButton(52, Tutorial.PLOTS)
        guarded(47, PermissionNodes.TOWNY_COMMAND_PLOT_TRUST,
            Icons.icon(Material.PLAYER_HEAD, tr("common.trust-online"), tr("plot-trust.trust-online-description"))) {
            Pickers.resident(this, tr("plot-trust.trust-title"), { !plot.hasTrustedResident(it) }) { picked ->
                run("towny:plot trust add ${picked.name}", ::count)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_PLOT_TRUST,
            Icons.icon(Material.NAME_TAG, tr("common.trust-name"), tr("common.trust-name-description"))) {
            prompt(tr("plot-trust.trust-title"), tr("common.player-name")) { name -> run("towny:plot trust add ${TownyUtil.argument(name)}", ::count) }
        }
    }
}
