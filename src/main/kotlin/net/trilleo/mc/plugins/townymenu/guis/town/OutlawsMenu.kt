package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
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

/** Players declared outlaws of the viewer's town. */
class OutlawsMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, player.tr("outlaws.title"), back) {

    private fun count() = town.outlaws.size

    override fun entries(): List<MenuEntry> {
        val canEdit = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW)
        return town.outlaws.sortedBy { it.name.lowercase() }.map { outlaw ->
            MenuEntry({
                Icons.resident(
                    player, outlaw,
                    actions = listOf(tr(if (canEdit) "outlaws.click-pardon" else "outlaws.outlaw"))
                )
            }) {
                if (canEdit) run("towny:town outlaw remove ${outlaw.name}", ::count)
            }
        }
    }

    override fun controls() {
        tutorialButton(52, Tutorial.PROTECTION)
        guarded(
            47, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW,
            Icons.icon(Material.PLAYER_HEAD, tr("outlaws.add-online"), tr("outlaws.add-online-description"))
        ) {
            Pickers.resident(this, tr("outlaws.declare"), { it.townOrNull != town && !town.hasOutlaw(it) }) { picked ->
                run("towny:town outlaw add ${picked.name}", ::count)
            }.open()
        }
        guarded(
            48, PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW,
            Icons.icon(Material.NAME_TAG, tr("outlaws.add-name"), tr("outlaws.add-name-description"))
        ) {
            prompt(
                tr("outlaws.declare"),
                tr("common.player-name")
            ) { name -> run("towny:town outlaw add ${TownyUtil.argument(name)}", ::count) }
        }
    }
}
