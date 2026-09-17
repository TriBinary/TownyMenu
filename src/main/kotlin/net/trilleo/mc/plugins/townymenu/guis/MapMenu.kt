package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.WorldCoord
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A 9×5 chunk map centred on the viewer (north is up). Clicking claimed land
 * opens that town; the bottom row claims or unclaims the viewer's own chunk.
 */
class MapMenu(player: Player, back: Menu?) : Menu(player, player.tr("map.title"), 6, back) {

    override fun build() {
        val center = WorldCoord.parseWorldCoord(player)
        val viewer = resident
        val api = TownyAPI.getInstance()

        for (row in 0 until 5) {
            for (column in 0 until 9) {
                val coord = WorldCoord(center.worldName, center.x + column - 4, center.z + row - 2)
                val plot = api.getTownBlock(coord)
                val here = column == 4 && row == 2
                val town = plot?.townOrNull
                button(row * 9 + column, cell(coord, plot, viewer, here)) {
                    if (town != null) TownInfoMenu(player, town, this).open()
                }
            }
        }

        button(45, Icons.icon(Material.PAPER, tr("map.legend"), null,
            tr("map.legend-wilderness"), tr("map.legend-town"), tr("map.legend-plot"),
            tr("map.legend-nation"), tr("map.legend-ally"), tr("map.legend-enemy"),
            tr("map.legend-other"), tr("map.legend-for-sale"), tr("map.legend-north")))

        val town = viewer?.townOrNull
        if (town != null) {
            val claims = { town.numTownBlocks }
            guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
                Icons.icon(Material.GRASS_BLOCK, tr("map.claim"), tr("map.claim-description"))) {
                run("towny:town claim", claims, delayTicks = 20)
            }
            guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
                Icons.icon(Material.COARSE_DIRT, tr("map.unclaim"), tr("map.unclaim-description"))) {
                run("towny:town unclaim", claims, delayTicks = 20)
            }
        }
        backButton(49)
        button(50, Icons.icon(Material.CLOCK, tr("map.refresh"), tr("map.refresh-description"))) { render() }
    }

    private fun cell(coord: WorldCoord, plot: TownBlock?, viewer: Resident?, here: Boolean): ItemStack {
        val town = plot?.townOrNull
        val nation = town?.nationOrNull
        val viewerNation = viewer?.nationOrNull
        val (material, color) = when {
            town == null -> Material.WHITE_STAINED_GLASS_PANE to "<white>"
            viewer != null && plot.residentOrNull == viewer -> Material.YELLOW_STAINED_GLASS_PANE to "<yellow>"
            town == viewer?.townOrNull -> Material.LIME_STAINED_GLASS_PANE to "<green>"
            viewerNation != null && nation == viewerNation -> Material.LIGHT_BLUE_STAINED_GLASS_PANE to "<aqua>"
            viewerNation != null && nation != null && viewerNation.hasAlly(nation) -> Material.BLUE_STAINED_GLASS_PANE to "<blue>"
            viewerNation != null && nation != null && viewerNation.hasEnemy(nation) -> Material.RED_STAINED_GLASS_PANE to "<red>"
            else -> Material.ORANGE_STAINED_GLASS_PANE to "<gold>"
        }
        return itemStack(if (here) Material.PLAYER_HEAD else material) {
            if (here) head(player)
            name(color + if (town == null) tr("main.wilderness") else TownyUtil.name(town.name))
            lore(buildList {
                if (here) add(tr("map.you-are-here"))
                add(tr("map.chunk", "x" to coord.x, "z" to coord.z))
                if (plot != null) {
                    nation?.let { add(tr("icon.town.nation", "nation" to TownyUtil.name(it.name))) }
                    add(tr("map.owner", "owner" to (plot.residentOrNull?.let { TownyUtil.name(it.name) } ?: tr("map.owner-town"))))
                    add(tr("map.type", "type" to TownyUtil.plotType(player, plot.type.name)))
                    if (plot.isHomeBlock) add(tr("map.home-block"))
                    if (plot.isOutpost) add(tr("map.outpost"))
                    if (plot.isForSale) add(tr("map.for-sale", "price" to TownyUtil.money(plot.plotPrice)))
                    add("")
                    add(tr("map.click-town"))
                }
            })
            glow(plot?.isForSale == true)
        }
    }
}
