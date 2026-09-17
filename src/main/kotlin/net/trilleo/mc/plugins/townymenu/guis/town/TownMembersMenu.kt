package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentProfileMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Lists a town's residents (mayor first, then online players). Members can also invite players. */
class TownMembersMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, "Residents: ${TownyUtil.name(town.name)}", back) {

    private val isMember: Boolean
        get() = resident?.townOrNull == town

    override fun entries(): List<MenuEntry> =
        town.residents
            .sortedWith(compareByDescending<Resident> { it.isMayor }
                .thenByDescending { it.isOnline }
                .thenBy { it.name.lowercase() })
            .map { member -> MenuEntry({ Icons.resident(member, "", "<yellow>Click to view") }) { ResidentProfileMenu(player, member, this).open() } }

    override fun controls() {
        if (!isMember) return
        val sent = { town.sentInvites.size }
        guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
            Icons.icon(Material.PLAYER_HEAD, "<green>Invite Online Player", "Invite a player who isn't in a town.")) {
            Pickers.resident(this, "Invite to Town", { !it.hasTown() }) { picked ->
                run("towny:town add ${picked.name}", sent)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
            Icons.icon(Material.NAME_TAG, "<green>Invite by Name", "Invite any player Towny knows, even if they are offline.")) {
            prompt("Invite to Town", "Player name") { name -> run("towny:town add ${TownyUtil.argument(name)}", sent) }
        }
        val pending = town.sentInvites
        guarded(51, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD, Icons.icon(
            Material.PAPER, "<yellow>Sent Invites", "Right-click to revoke every pending invite.",
            *pending.take(10).map { "<gray>- <white>${TownyUtil.name(it.receiver.name)}" }.toTypedArray(),
            "<gray>Pending: <white>${pending.size}",
        )) { click -> if (click.isRightClick) run("towny:town invite sent removeall", sent) }
    }
}
