package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.invites.Invite
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Every invitation waiting on the viewer: town invites to them, nation invites
 * to their town, and alliance requests to their nation.
 */
class InvitesMenu(player: Player, back: Menu?) : PagedMenu(player, "Invites", back) {

    override val emptyItem = itemStack(Material.PAPER) { name("<gray>No pending invites") }

    override fun entries(): List<MenuEntry> {
        val viewer = resident ?: return emptyList()
        val town = viewer.townOrNull
        val nation = viewer.nationOrNull
        val accept = TownySettings.getAcceptCommand()
        val deny = TownySettings.getDenyCommand()

        val personal = viewer.receivedInvites.mapNotNull { invite ->
            val from = invite.sender as? Town ?: return@mapNotNull null
            entry(invite, "Invitation to join ${TownyUtil.name(from.name)}", { Icons.town(from, *it) },
                "towny:invite $accept ${from.name}", "towny:invite $deny ${from.name}") { snapshot(viewer) }
        }
        val forTown = if (town != null && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ACCEPT)) {
            town.receivedInvites.mapNotNull { invite ->
                val from = invite.sender as? Nation ?: return@mapNotNull null
                entry(invite, "Invitation for your town to join ${TownyUtil.name(from.name)}", { Icons.nation(from, *it) },
                    "towny:town invite accept ${from.name}", "towny:town invite deny ${from.name}") { snapshot(viewer) }
            }
        } else {
            emptyList()
        }
        val forNation = if (nation != null && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ACCEPT)) {
            nation.receivedInvites.mapNotNull { invite ->
                val from = invite.sender as? Nation ?: return@mapNotNull null
                entry(invite, "Alliance request from ${TownyUtil.name(from.name)}", { Icons.nation(from, *it) },
                    "towny:nation ally accept ${from.name}", "towny:nation ally deny ${from.name}") { snapshot(viewer) }
            }
        } else {
            emptyList()
        }
        return personal + forTown + forNation
    }

    private fun entry(
        invite: Invite,
        title: String,
        icon: (Array<String>) -> ItemStack,
        acceptCommand: String,
        denyCommand: String,
        probe: () -> Any?,
    ): MenuEntry {
        val lines = arrayOf("", "<yellow>$title", "<gray>Sent by: <white>${TownyUtil.name(invite.senderName)}",
            "<green>Left-click to accept", "<red>Right-click to decline")
        return MenuEntry({ icon(lines) }) { click ->
            run(if (click.isRightClick) denyCommand else acceptCommand, probe)
        }
    }

    private fun snapshot(viewer: Resident) = listOf(
        viewer.receivedInvites.size,
        viewer.townOrNull?.receivedInvites?.size,
        viewer.nationOrNull?.receivedInvites?.size,
        viewer.townOrNull?.name,
        viewer.nationOrNull?.name,
    )
}
