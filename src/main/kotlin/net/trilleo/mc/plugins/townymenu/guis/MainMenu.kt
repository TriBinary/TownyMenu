package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownyAPI
import net.trilleo.mc.plugins.townymenu.guis.admin.AdminMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.nation.NationListMenu
import net.trilleo.mc.plugins.townymenu.guis.nation.NationMenu
import net.trilleo.mc.plugins.townymenu.guis.nation.NoNationMenu
import net.trilleo.mc.plugins.townymenu.guis.plot.PlotMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentMenu
import net.trilleo.mc.plugins.townymenu.guis.town.NoTownMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownListMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.TutorialMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** The entry point opened by `/townymenu` or sneak + swap-hand. */
class MainMenu(player: Player) : Menu(player, player.tr("main.title"), 6) {

    override fun build() {
        val resident = resident
        if (resident == null) {
            button(22, Icons.icon(Material.BARRIER, tr("main.unregistered"), tr("main.unregistered-hint")))
            return backButton(49)
        }
        val town = resident.townOrNull
        val nation = resident.nationOrNull

        button(4, Icons.resident(player, resident, "", tr("main.profile-click"))) {
            ResidentMenu(player, this).open()
        }

        if (town != null) {
            button(20, Icons.town(player, town, "", tr("main.town-click"))) { TownMenu(player, this).open() }
        } else {
            button(20, Icons.icon(Material.BELL, tr("main.town"), tr("main.town-none"))) {
                NoTownMenu(player, this).open()
            }
        }

        when {
            nation != null -> button(22, Icons.nation(player, nation, "", tr("main.nation-click"))) {
                NationMenu(
                    player,
                    this
                ).open()
            }

            town != null -> button(22, Icons.icon(Material.BEACON, tr("main.nation"), tr("main.nation-none"))) {
                NoNationMenu(player, this).open()
            }

            else -> button(22, Icons.icon(Material.BEACON, tr("main.nation-locked"), tr("main.nation-no-town")))
        }

        val plot = TownyAPI.getInstance().getTownBlock(player)
        button(
            24, Icons.icon(
                Material.GRASS_BLOCK, tr("main.plot"), tr("main.plot-description"),
                tr(
                    "main.plot-here",
                    "town" to (plot?.townOrNull?.let { TownyUtil.name(it.name) } ?: tr("main.wilderness"))))) {
            PlotMenu(player, this).open()
        }

        button(
            28, Icons.icon(
                Material.CLOCK, tr("main.prices"), tr("main.prices-description"),
                tr("town-status.new-day", "time" to TownyUtil.duration(player, TownyUtil.secondsUntilNewDay()))
            )
        ) {
            PricesMenu(player, this).open()
        }
        button(29, Icons.icon(Material.FILLED_MAP, tr("main.map"), tr("main.map-description"))) {
            MapMenu(
                player,
                this
            ).open()
        }
        button(30, Icons.icon(Material.BELL, tr("main.towns"), tr("main.towns-description"))) {
            TownListMenu(
                player,
                this
            ).open()
        }
        button(31, Icons.icon(Material.KNOWLEDGE_BOOK, tr("main.tutorial"), tr("main.tutorial-description"))) {
            TutorialMenu(player, this).open()
        }
        button(32, Icons.icon(Material.BEACON, tr("main.nations"), tr("main.nations-description"))) {
            NationListMenu(
                player,
                this
            ).open()
        }

        button(34, Icons.icon(Material.LECTERN, tr("main.leaderboards"), tr("main.leaderboards-description"))) {
            LeaderboardMenu(player, this).open()
        }

        val invites = resident.receivedInvites.size +
                (town?.receivedInvites?.size ?: 0) + (nation?.receivedInvites?.size ?: 0)
        button(
            33, Icons.icon(
                Material.PAPER, if (invites > 0) tr("main.invites-count", "count" to invites) else tr("main.invites"),
                tr("main.invites-description")
            )
        ) {
            InvitesMenu(player, this).open()
        }

        if (player.hasPermission(AdminMenu.PERMISSION)) {
            button(53, Icons.icon(Material.COMMAND_BLOCK, tr("main.admin"), tr("main.admin-description"))) {
                AdminMenu(
                    player,
                    this
                ).open()
            }
        }

        backButton(49)
    }
}
