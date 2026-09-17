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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A 9×5 chunk map centred on the viewer (north is up). Clicking claimed land
 * opens that town; the bottom row claims or unclaims the viewer's own chunk.
 */
class MapMenu(player: Player, back: Menu?) : Menu(player, "Map", 6, back) {

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

        button(45, Icons.icon(Material.PAPER, "<yellow>Legend", null,
            "<white>■ <gray>Wilderness", "<green>■ <gray>Your town", "<yellow>■ <gray>Your plot",
            "<aqua>■ <gray>Your nation", "<blue>■ <gray>Allied nation", "<red>■ <gray>Enemy nation",
            "<gold>■ <gray>Other town", "<gray>Glowing tiles are for sale.", "<gray>North is up."))

        val town = viewer?.townOrNull
        if (town != null) {
            val claims = { town.numTownBlocks }
            guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
                Icons.icon(Material.GRASS_BLOCK, "<green>Claim Your Chunk", "Claim the chunk you are standing in.")) {
                run("towny:town claim", claims, delayTicks = 20)
            }
            guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
                Icons.icon(Material.COARSE_DIRT, "<red>Unclaim Your Chunk", "Unclaim the chunk you are standing in.")) {
                run("towny:town unclaim", claims, delayTicks = 20)
            }
        }
        backButton(49)
        button(50, Icons.icon(Material.CLOCK, "<yellow>Refresh", "Redraw the map around your current position.")) { render() }
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
            name(if (town == null) "${color}Wilderness" else "$color${TownyUtil.name(town.name)}")
            lore(buildList {
                if (here) add("<yellow>You are here")
                add("<gray>Chunk: <white>${coord.x}, ${coord.z}")
                if (plot != null) {
                    nation?.let { add("<gray>Nation: <white>${TownyUtil.name(it.name)}") }
                    add("<gray>Owner: <white>${plot.residentOrNull?.let { TownyUtil.name(it.name) } ?: "The town"}")
                    add("<gray>Type: <white>${TownyUtil.name(plot.type.name)}")
                    if (plot.isHomeBlock) add("<gold>Home block")
                    if (plot.isOutpost) add("<light_purple>Outpost")
                    if (plot.isForSale) add("<green>For sale: <white>${TownyUtil.money(plot.plotPrice)}")
                    add("")
                    add("<yellow>Click to view town")
                }
            })
            glow(plot?.isForSale == true)
        }
    }
}
