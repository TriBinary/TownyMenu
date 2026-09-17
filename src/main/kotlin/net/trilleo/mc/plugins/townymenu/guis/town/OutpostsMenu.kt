package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import org.bukkit.Material
import org.bukkit.entity.Player

/** Teleport targets for each of a town's outpost spawns. */
class OutpostsMenu(player: Player, private val town: Town, back: Menu) : PagedMenu(player, "Outposts", back) {

    override fun entries(): List<MenuEntry> =
        town.allOutpostSpawns.mapIndexed { index, location ->
            val icon = {
                Icons.icon(
                    Material.COMPASS, "<light_purple>Outpost #${index + 1}", null,
                    "<gray>World: <white>${location.world?.name ?: "-"}",
                    "<gray>Location: <white>${location.blockX}, ${location.blockY}, ${location.blockZ}",
                    "", "<yellow>Click to teleport",
                )
            }
            MenuEntry(icon) { runAndClose("towny:town outpost ${index + 1}") }
        }
}
