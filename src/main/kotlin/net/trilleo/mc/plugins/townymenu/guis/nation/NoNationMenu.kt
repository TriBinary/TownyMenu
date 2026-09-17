package net.trilleo.mc.plugins.townymenu.guis.nation

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

/** Shown when the viewer's town has no nation: found one, browse nations, or answer invites. */
class NoNationMenu(player: Player, back: Menu) : Menu(player, player.tr("no-nation.title"), 3, back) {

    override fun build() {
        val price = if (TownyUtil.economy) tr("common.cost", "cost" to TownyUtil.money(TownySettings.getNewNationPrice())) else ""
        guarded(11, PermissionNodes.TOWNY_COMMAND_NATION_NEW,
            Icons.icon(Material.BEACON, tr("no-nation.found"), tr("no-nation.found-description"), price)) {
            prompt(tr("no-nation.found-title"), tr("no-nation.nation-name")) { name ->
                run("towny:nation new ${TownyUtil.nameArgument(name)}", { resident?.hasNation() }, returnTo = back ?: MainMenu(player))
            }
        }
        button(13, Icons.icon(Material.OAK_DOOR, tr("no-nation.browse"), tr("no-nation.browse-description"))) {
            NationListMenu(player, this).open()
        }
        val invites = resident?.townOrNull?.receivedInvites?.size ?: 0
        button(15, Icons.icon(Material.PAPER, tr("main.invites"), tr("no-nation.invites-description"), tr("common.pending", "count" to invites))) {
            InvitesMenu(player, this).open()
        }
        tutorialButton(26, Tutorial.NATIONS)
        backButton(22)
    }
}
