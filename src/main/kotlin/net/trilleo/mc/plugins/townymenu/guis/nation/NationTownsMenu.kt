package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** A nation's member towns. Members with permission can invite and remove towns. */
class NationTownsMenu(player: Player, private val nation: Nation, back: Menu) :
    PagedMenu(player, "Towns: ${TownyUtil.name(nation.name)}", back) {

    private val isMember: Boolean
        get() = resident?.nationOrNull == nation

    override fun entries(): List<MenuEntry> {
        val canKick = isMember && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_NATION_KICK)
        return nation.towns
            .sortedWith(compareByDescending<Town> { nation.isCapital(it) }.thenBy { it.name.lowercase() })
            .map { town ->
                val lines = buildList {
                    add("")
                    if (nation.isCapital(town)) add("<gold>Capital")
                    add("<yellow>Left-click for details")
                    if (canKick && !nation.isCapital(town)) add("<red>Right-click to remove from nation")
                }
                MenuEntry({ Icons.town(town, *lines.toTypedArray()) }) { click ->
                    if (click.isRightClick && canKick && !nation.isCapital(town)) {
                        run("towny:nation kick ${town.name}", { nation.numTowns })
                    } else {
                        TownInfoMenu(player, town, this).open()
                    }
                }
            }
    }

    override fun controls() {
        if (!isMember) return
        val sent = { nation.sentInvites.size }
        guarded(47, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_ADD,
            Icons.icon(Material.BELL, "<green>Invite a Town", "Invite a town that isn't in a nation.")) {
            Pickers.town(this, "Invite Town", { !it.hasNation() }) { picked ->
                run("towny:nation add ${picked.name}", sent)
            }.open()
        }
        val pending = nation.sentInvites
        button(51, Icons.icon(
            Material.PAPER, "<yellow>Sent Invites", null,
            *pending.take(10).map { "<gray>- <white>${TownyUtil.name(it.receiver.name)}" }.toTypedArray(),
            "<gray>Pending: <white>${pending.size}",
        ))
    }
}
