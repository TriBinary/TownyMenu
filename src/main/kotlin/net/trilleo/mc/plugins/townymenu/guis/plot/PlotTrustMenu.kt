package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.`object`.Resident
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
 * Players trusted on one plot, or on every plot of its group when it has one.
 * Towny's `/plot trust` acts on the plot the player stands in, so this menu is
 * tied to the plot it was opened for.
 */
class PlotTrustMenu(player: Player, private val plot: TownBlock, back: Menu) :
    PagedMenu(player, player.tr("plot-trust.title"), back) {

    private val grouped: Boolean
        get() = plot.hasPlotObjectGroup()

    private val command: String
        get() = if (grouped) "towny:plot group trust" else "towny:plot trust"

    private val node: PermissionNodes
        get() = if (grouped) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_TRUST else PermissionNodes.TOWNY_COMMAND_PLOT_TRUST

    private fun trusted(): Set<Resident> = plot.plotObjectGroup?.trustedResidents ?: plot.trustedResidents

    private fun count() = trusted().size

    override fun entries(): List<MenuEntry> =
        trusted().sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.resident(player, trusted, actions = listOf(tr("click.untrust"))) }) {
                run("$command remove ${trusted.name}", ::count)
            }
        }

    override fun controls() {
        tutorialButton(52, Tutorial.PLOTS)
        guarded(
            47, node,
            Icons.icon(
                Material.PLAYER_HEAD, tr("common.trust-online"), tr("plot-trust.trust-online-description"),
                actions = hints("click.choose-player")
            )
        ) {
            Pickers.resident(this, tr("plot-trust.trust-title"), { it !in trusted() }) { picked ->
                run("$command add ${picked.name}", ::count)
            }.open()
        }
        guarded(
            48, node,
            Icons.icon(
                Material.NAME_TAG, tr("common.trust-name"), tr("common.trust-name-description"),
                actions = hints("click.type-name")
            )
        ) {
            prompt(
                tr("plot-trust.trust-title"),
                tr("common.player-name")
            ) { name -> run("$command add ${TownyUtil.argument(name)}", ::count) }
        }
        plot.plotObjectGroup?.let { group ->
            button(
                51, Icons.icon(
                    Material.CHEST, tr("plot-trust.group", "group" to TownyUtil.name(group.name)),
                    tr("plot-trust.group-description", "count" to group.townBlocks.size)
                )
            )
        }
    }
}
