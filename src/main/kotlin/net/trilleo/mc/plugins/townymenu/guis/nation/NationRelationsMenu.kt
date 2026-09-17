package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** A nation's allies and enemies. Members with permission can change them. */
class NationRelationsMenu(player: Player, private val nation: Nation, back: Menu) :
    PagedMenu(player, "Allies & Enemies: ${TownyUtil.name(nation.name)}", back) {

    private val isMember: Boolean
        get() = resident?.nationOrNull == nation

    private fun snapshot() = Triple(nation.allies.size, nation.enemies.size, nation.sentAllyInvites.size)

    override fun entries(): List<MenuEntry> {
        val canAlly = isMember && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_REMOVE)
        val canEnemy = isMember && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_NATION_ENEMY)
        val allies = nation.allies.sortedBy { it.name.lowercase() }.map { ally ->
            relation(ally, "<green>Ally", canAlly, "towny:nation ally remove ${ally.name}")
        }
        val enemies = nation.enemies.sortedBy { it.name.lowercase() }.map { enemy ->
            relation(enemy, "<red>Enemy", canEnemy, "towny:nation enemy remove ${enemy.name}")
        }
        return allies + enemies
    }

    private fun relation(other: Nation, label: String, canRemove: Boolean, removeCommand: String): MenuEntry {
        val lines = listOfNotNull("", label, "<yellow>Left-click for details", if (canRemove) "<red>Right-click to remove" else null)
        return MenuEntry({ Icons.nation(other, *lines.toTypedArray()) }) { click ->
            if (click.isRightClick && canRemove) run(removeCommand, ::snapshot) else NationInfoMenu(player, other, this).open()
        }
    }

    override fun controls() {
        if (!isMember) return
        guarded(47, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ADD,
            Icons.icon(Material.SHIELD, "<green>Propose Alliance", "Send an alliance request to another nation.")) {
            Pickers.nation(this, "Propose Alliance", { it != nation && !nation.hasAlly(it) }) { picked ->
                run("towny:nation ally add ${picked.name}", ::snapshot)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_NATION_ENEMY,
            Icons.icon(Material.IRON_SWORD, "<red>Declare Enemy", "Mark another nation as an enemy.")) {
            Pickers.nation(this, "Declare Enemy", { it != nation && !nation.hasEnemy(it) }) { picked ->
                run("towny:nation enemy add ${picked.name}", ::snapshot)
            }.open()
        }
        val pending = nation.sentAllyInvites
        button(51, Icons.icon(
            Material.PAPER, "<yellow>Sent Alliance Requests", null,
            *pending.take(10).map { "<gray>- <white>${TownyUtil.name(it.receiver.name)}" }.toTypedArray(),
            "<gray>Pending: <white>${pending.size}",
        ))
    }
}
