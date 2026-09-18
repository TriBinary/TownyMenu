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

/** Shown to players without a town: found one, browse towns to join, answer invites, or reclaim a ruined town. */
class NoTownMenu(player: Player, back: Menu) : Menu(player, player.tr("no-town.title"), 4, back) {

    override fun build() {
        val reclaimable =
            TownySettings.getTownRuinsReclaimEnabled() && TownySettings.canRuinsBeReclaimedByTownlessPlayers()
        val row = if (reclaimable) layout(10, 12, 14, 16) else layout(11, 13, 15)
        val price = costLine(TownySettings.getNewTownPrice())
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_NEW,
            Icons.icon(
                Material.BELL,
                tr("no-town.found"),
                tr("no-town.found-description"),
                *listOfNotNull(price).toTypedArray()
            )
        ) {
            prompt(tr("no-town.found-title"), tr("no-town.town-name")) { name ->
                run(
                    "towny:town new ${TownyUtil.nameArgument(name)}",
                    { resident?.hasTown() },
                    returnTo = back ?: MainMenu(player)
                )
            }
        }
        row.add(Icons.icon(Material.OAK_DOOR, tr("no-town.browse"), tr("no-town.browse-description"))) {
            TownListMenu(player, this).open()
        }
        val invites = resident?.receivedInvites?.size ?: 0
        row.add(
            Icons.icon(
                Material.PAPER,
                tr("main.invites"),
                tr("no-town.invites-description"),
                tr("common.pending", "count" to invites)
            )
        ) {
            InvitesMenu(player, this).open()
        }
        if (reclaimable) {
            val cost = costLine(TownySettings.getEcoPriceReclaimTown())
            row.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_RECLAIM,
                Icons.icon(
                    Material.MOSSY_STONE_BRICKS, tr("no-town.reclaim"), tr("no-town.reclaim-description"),
                    *listOfNotNull(cost).toTypedArray()
                )
            ) {
                run("towny:town reclaim", { resident?.hasTown() }, returnTo = back ?: MainMenu(player))
            }
        }
        tutorialButton(35, Tutorial.FINDING_A_TOWN)
        backButton(31)
    }
}
