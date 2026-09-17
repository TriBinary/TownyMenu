package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.InvitesMenu
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Shown when the viewer's town has no nation: found one, browse nations, or answer invites. */
class NoNationMenu(player: Player, back: Menu) : Menu(player, "Find a Nation", 3, back) {

    override fun build() {
        val price = if (TownyUtil.economy) "<gray>Cost: <white>${TownyUtil.money(TownySettings.getNewNationPrice())}" else ""
        guarded(11, PermissionNodes.TOWNY_COMMAND_NATION_NEW,
            Icons.icon(Material.BEACON, "<aqua>Found a Nation", "Found a nation with your town as the capital.", price)) {
            prompt("Found a Nation", "Nation name") { name ->
                run("towny:nation new ${TownyUtil.nameArgument(name)}", { resident?.hasNation() }, returnTo = back ?: MainMenu(player))
            }
        }
        button(13, Icons.icon(Material.OAK_DOOR, "<green>Browse Nations", "Find an open nation for your town to join.")) {
            NationListMenu(player, this).open()
        }
        val invites = resident?.townOrNull?.receivedInvites?.size ?: 0
        button(15, Icons.icon(Material.PAPER, "<yellow>Invites", "Accept an invitation for your town to join a nation.", "<gray>Pending: <white>$invites")) {
            InvitesMenu(player, this).open()
        }
        backButton(22)
    }
}
