package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.WorldCoord
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** `/townyadmin plot` and the plot half of `/townyadmin set`, for the plot the admin is standing in. */
class AdminPlotMenu(player: Player, back: Menu) : Menu(player, player.tr("admin-plot.title"), 6, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        val coord = WorldCoord.parseWorldCoord(player)
        if (plot == null) {
            button(
                4, Icons.icon(
                    Material.OAK_SAPLING, "<green>${tr("main.wilderness")}", tr("admin-plot.wilderness-description"),
                    tr("map.chunk", "x" to coord.x, "z" to coord.z)
                )
            )
        } else {
            val owner = plot.residentOrNull
            button(
                4, Icons.icon(
                    Material.GRASS_BLOCK,
                    "<gold>${if (plot.name.isNullOrBlank()) tr("plot.unnamed") else TownyUtil.text(plot.name)}",
                    null,
                    tr("icon.resident.town", "town" to (plot.townOrNull?.let { TownyUtil.name(it.name) } ?: "-")),
                    tr("map.owner", "owner" to (owner?.let { TownyUtil.name(it.name) } ?: tr("map.owner-town"))),
                    tr("map.type", "type" to TownyUtil.plotType(player, plot.type.name)),
                    tr("map.chunk", "x" to plot.x, "z" to plot.z),
                    tr("plot.claimed", "date" to TownyUtil.date(plot.claimedAt)),
                )
            )
        }

        val grid = layout(20, 21, 22, 23, 24)
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PLOT_CLAIM,
            Icons.icon(
                Material.PLAYER_HEAD, tr("admin-plot.claim"), tr("admin-plot.claim-description"),
                actions = hints("click.choose-player")
            )
        ) {
            Pickers.resident(this, tr("admin-plot.claim-title"), { true }) { target ->
                run("towny:townyadmin plot claim ${target.name}", { this.plot?.residentOrNull })
            }.open()
        }
        if (plot != null) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PLOT_TRUST,
                Icons.icon(
                    Material.TRIPWIRE_HOOK, tr("plot.trusted"), tr("admin-plot.trust-description"),
                    tr("plot.trusted-count", "count" to plot.trustedResidents.size),
                    actions = hints("click.view")
                )
            ) {
                AdminNamesMenu(
                    player, tr("plot-trust.title"), this, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PLOT_TRUST,
                    "towny:townyadmin plot trust", Material.PLAYER_HEAD, tr("plot-trust.trust-title"),
                    tr("common.player-name")
                ) { this.plot?.trustedResidents?.map { it.name }.orEmpty() }.open()
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_PLOT,
                Icons.icon(
                    Material.BELL, tr("admin-plot.move"), tr("admin-plot.move-description"),
                    actions = hints("click.choose-town")
                )
            ) {
                Pickers.town(this, tr("admin-plot.move-title"), { it != this.plot?.townOrNull }) { town ->
                    run("towny:townyadmin set plot ${town.name}", { this.plot?.townOrNull })
                }.open()
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_UNCLAIM,
            Icons.icon(
                Material.COARSE_DIRT, tr("admin-plot.unclaim"), tr("admin-plot.unclaim-description"),
                actions = hints("admin-plot.left-unclaim", "admin-plot.right-unclaim-area")
            )
        ) { click ->
            if (click.isRightClick) {
                prompt(tr("admin-plot.unclaim-title"), tr("claims.radius"), initial = "1", maxLength = 3) { radius ->
                    run("towny:townyadmin unclaim rect ${TownyUtil.argument(radius)}", { this.plot?.townOrNull })
                }
            } else {
                run("towny:townyadmin unclaim", { this.plot?.townOrNull })
            }
        }
        button(
            50, Icons.icon(
                Material.CLOCK, tr("map.refresh"), tr("plot.refresh-description"),
                actions = hints("click.refresh")
            )
        ) { render() }

        backButton(49)
    }
}
