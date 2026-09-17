package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Players declared outlaws of the viewer's town. */
class OutlawsMenu(player: Player, private val town: Town, back: Menu) : PagedMenu(player, "Outlaws", back) {

    private fun count() = town.outlaws.size

    override fun entries(): List<MenuEntry> {
        val canEdit = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW)
        return town.outlaws.sortedBy { it.name.lowercase() }.map { outlaw ->
            MenuEntry({ Icons.resident(outlaw, "", if (canEdit) "<green>Click to pardon" else "<red>Outlaw") }) {
                if (canEdit) run("towny:town outlaw remove ${outlaw.name}", ::count)
            }
        }
    }

    override fun controls() {
        guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW,
            Icons.icon(Material.PLAYER_HEAD, "<red>Outlaw Online Player", "Declare an online player an outlaw.")) {
            Pickers.resident(this, "Declare Outlaw", { it.townOrNull != town && !town.hasOutlaw(it) }) { picked ->
                run("towny:town outlaw add ${picked.name}", ::count)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW,
            Icons.icon(Material.NAME_TAG, "<red>Outlaw by Name", "Declare any player Towny knows an outlaw.")) {
            prompt("Declare Outlaw", "Player name") { name -> run("towny:town outlaw add ${TownyUtil.argument(name)}", ::count) }
        }
    }
}
