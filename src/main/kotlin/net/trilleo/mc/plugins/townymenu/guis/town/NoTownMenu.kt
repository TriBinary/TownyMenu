package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.InvitesMenu
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Shown to players without a town: found one, browse towns to join, or answer invites. */
class NoTownMenu(player: Player, back: Menu) : Menu(player, player.tr("no-town.title"), 3, back) {

    override fun build() {
        val price = if (TownyUtil.economy) tr("common.cost", "cost" to TownyUtil.money(TownySettings.getNewTownPrice())) else ""
        guarded(11, PermissionNodes.TOWNY_COMMAND_TOWN_NEW,
            Icons.icon(Material.BELL, tr("no-town.found"), tr("no-town.found-description"), price)) {
            prompt(tr("no-town.found-title"), tr("no-town.town-name")) { name ->
                run("towny:town new ${TownyUtil.nameArgument(name)}", { resident?.hasTown() }, returnTo = back ?: MainMenu(player))
            }
        }
        button(13, Icons.icon(Material.OAK_DOOR, tr("no-town.browse"), tr("no-town.browse-description"))) {
            TownListMenu(player, this).open()
        }
        val invites = resident?.receivedInvites?.size ?: 0
        button(15, Icons.icon(Material.PAPER, tr("main.invites"), tr("no-town.invites-description"), tr("common.pending", "count" to invites))) {
            InvitesMenu(player, this).open()
        }
        tutorialButton(26, Tutorial.FINDING_A_TOWN)
        backButton(22)
    }
}
