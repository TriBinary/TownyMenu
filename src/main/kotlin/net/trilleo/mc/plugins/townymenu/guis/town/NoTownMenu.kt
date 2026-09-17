package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.InvitesMenu
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Shown to players without a town: found one, browse towns to join, or answer invites. */
class NoTownMenu(player: Player, back: Menu) : Menu(player, "Find a Town", 3, back) {

    override fun build() {
        val price = if (TownyUtil.economy) "<gray>Cost: <white>${TownyUtil.money(TownySettings.getNewTownPrice())}" else ""
        guarded(11, PermissionNodes.TOWNY_COMMAND_TOWN_NEW,
            Icons.icon(Material.BELL, "<gold>Found a Town", "Start a new town on the chunk you are standing in.", price)) {
            prompt("Found a Town", "Town name") { name ->
                run("towny:town new ${TownyUtil.nameArgument(name)}", { resident?.hasTown() }, returnTo = back ?: MainMenu(player))
            }
        }
        button(13, Icons.icon(Material.OAK_DOOR, "<green>Browse Towns", "Find an open town to join.")) {
            TownListMenu(player, this).open()
        }
        val invites = resident?.receivedInvites?.size ?: 0
        button(15, Icons.icon(Material.PAPER, "<yellow>Invites", "Accept an invitation to a town.", "<gray>Pending: <white>$invites")) {
            InvitesMenu(player, this).open()
        }
        backButton(22)
    }
}
