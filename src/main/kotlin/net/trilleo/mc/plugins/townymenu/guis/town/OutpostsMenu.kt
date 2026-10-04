package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.Prices
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * A town's outpost spawns: teleport targets for the viewer's own town, and a read-only list of
 * locations for any other town, since Towny only lets residents travel to their own outposts.
 */
class OutpostsMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, title(player, town), back) {

    override fun entries(): List<MenuEntry> {
        val own = resident?.townOrNull == town
        val cost = if (own) costLine(Prices.townSpawn(player, town, outpost = true)) else null
        return town.allOutpostSpawns.mapIndexed { index, location ->
            val icon = {
                Icons.icon(
                    Material.COMPASS, tr("outposts.outpost", "number" to index + 1), null,
                    tr("outposts.world", "world" to (location.world?.name ?: "-")),
                    tr("outposts.location", "x" to location.blockX, "y" to location.blockY, "z" to location.blockZ),
                    *listOfNotNull(cost).toTypedArray(),
                    actions = if (own) hints("click.teleport") else emptyList()
                )
            }
            if (own) MenuEntry(icon) { runAndClose("towny:town outpost ${index + 1}") } else MenuEntry(icon)
        }
    }

    private companion object {
        fun title(player: Player, town: Town): String =
            if (TownyAPI.getInstance().getResident(player)?.townOrNull == town) player.tr("outposts.title")
            else player.tr("outposts.title-other", "town" to TownyUtil.name(town.name))
    }
}
