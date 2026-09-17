package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.`object`.PermissionData
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
 * Per-player permission overrides on one plot, or on every plot of its group.
 * TownyMenu adds and removes players; the build/destroy/switch/item-use choices
 * for each player are edited in Towny's own override editor.
 */
class PlotOverridesMenu(player: Player, private val plot: TownBlock, back: Menu) :
    PagedMenu(player, player.tr("plot-overrides.title"), back) {

    private val grouped: Boolean
        get() = plot.hasPlotObjectGroup()

    private val command: String
        get() = if (grouped) "towny:plot group perm" else "towny:plot perm"

    private fun overrides(): Map<Resident, PermissionData> =
        plot.plotObjectGroup?.permissionOverrides ?: plot.permissionOverrides

    private fun snapshot() = overrides().keys.toSet()

    private fun node(grouped: PermissionNodes, single: PermissionNodes) = if (this.grouped) grouped else single

    override fun entries(): List<MenuEntry> {
        val canRemove = TownyUtil.can(
            player,
            node(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_PERM, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_REMOVE)
        )
        return overrides().entries.sortedBy { it.key.name.lowercase() }.map { (target, data) ->
            val lines = listOfNotNull(
                "",
                tr("plot-overrides.changed-by", "player" to TownyUtil.name(data.lastChangedBy ?: "-")),
                if (canRemove) tr("plot-overrides.click-remove") else null,
            )
            MenuEntry({ Icons.resident(player, target, *lines.toTypedArray()) }) {
                if (canRemove) run("$command remove ${target.name}", ::snapshot)
            }
        }
    }

    override fun controls() {
        tutorialButton(52, Tutorial.PROTECTION)
        val addNode = node(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_PERM, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_ADD)
        guarded(
            47, addNode,
            Icons.icon(Material.PLAYER_HEAD, tr("plot-overrides.add-online"), tr("plot-overrides.add-description"))
        ) {
            Pickers.resident(this, tr("plot-overrides.add-title"), { it !in overrides() }) { picked ->
                run("$command add ${picked.name}", ::snapshot)
            }.open()
        }
        guarded(
            48, addNode,
            Icons.icon(Material.NAME_TAG, tr("plot-overrides.add-name"), tr("plot-overrides.add-description"))
        ) {
            prompt(tr("plot-overrides.add-title"), tr("common.player-name")) { name ->
                run("$command add ${TownyUtil.argument(name)}", ::snapshot)
            }
        }
        guarded(
            51, node(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_PERM, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_GUI),
            Icons.icon(Material.WRITABLE_BOOK, tr("plot-overrides.edit"), tr("plot-overrides.edit-description"))
        ) {
            runAndClose("$command gui")
        }
    }
}
