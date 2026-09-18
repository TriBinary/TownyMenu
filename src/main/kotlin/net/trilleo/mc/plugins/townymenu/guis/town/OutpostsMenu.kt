package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.Prices
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Teleport targets for each of a town's outpost spawns. */
class OutpostsMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, player.tr("outposts.title"), back) {

    override fun entries(): List<MenuEntry> {
        val cost = costLine(Prices.townSpawn(player, town, outpost = true))
        return town.allOutpostSpawns.mapIndexed { index, location ->
            val icon = {
                Icons.icon(
                    Material.COMPASS, tr("outposts.outpost", "number" to index + 1), null,
                    tr("outposts.world", "world" to (location.world?.name ?: "-")),
                    tr("outposts.location", "x" to location.blockX, "y" to location.blockY, "z" to location.blockZ),
                    *listOfNotNull(cost).toTypedArray(),
                    "", tr("common.click-teleport"),
                )
            }
            MenuEntry(icon) { runAndClose("towny:town outpost ${index + 1}") }
        }
    }
}
