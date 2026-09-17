package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownyAPI
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
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** The entry point opened by `/townymenu` or sneak + swap-hand. */
class MainMenu(player: Player) : Menu(player, "Towny", 5) {

    override fun build() {
        val resident = resident
        if (resident == null) {
            button(22, Icons.icon(Material.BARRIER, "<red>Towny hasn't registered you yet", "Try again in a moment."))
            return backButton(40)
        }
        val town = resident.townOrNull
        val nation = resident.nationOrNull

        button(4, Icons.resident(resident, "", "<yellow>Click to open your profile")) {
            ResidentMenu(player, this).open()
        }

        if (town != null) {
            button(20, Icons.town(town, "", "<yellow>Click to manage your town")) { TownMenu(player, this).open() }
        } else {
            button(20, Icons.icon(Material.BELL, "<gold>Town", "You are not in a town. Found one, join one, or answer invites.")) {
                NoTownMenu(player, this).open()
            }
        }

        when {
            nation != null -> button(22, Icons.nation(nation, "", "<yellow>Click to manage your nation")) { NationMenu(player, this).open() }
            town != null -> button(22, Icons.icon(Material.BEACON, "<aqua>Nation", "Your town is not in a nation. Found one or join one.")) {
                NoNationMenu(player, this).open()
            }
            else -> button(22, Icons.icon(Material.BEACON, "<gray>Nation", "Join a town before joining a nation."))
        }

        val plot = TownyAPI.getInstance().getTownBlock(player)
        button(24, Icons.icon(Material.GRASS_BLOCK, "<green>This Plot",
            "Buy, sell, and configure the plot you are standing in.",
            "<gray>Here: <white>${plot?.townOrNull?.let { TownyUtil.name(it.name) } ?: "Wilderness"}")) {
            PlotMenu(player, this).open()
        }

        button(29, Icons.icon(Material.FILLED_MAP, "<aqua>Map", "See who owns the land around you.")) { MapMenu(player, this).open() }
        button(30, Icons.icon(Material.BELL, "<gold>Towns", "Browse and rank every town.")) { TownListMenu(player, this).open() }
        button(32, Icons.icon(Material.BEACON, "<aqua>Nations", "Browse and rank every nation.")) { NationListMenu(player, this).open() }

        val invites = resident.receivedInvites.size +
            (town?.receivedInvites?.size ?: 0) + (nation?.receivedInvites?.size ?: 0)
        button(33, Icons.icon(Material.PAPER, if (invites > 0) "<yellow>Invites <white>($invites)" else "<yellow>Invites",
            "Town invites, nation invites, and alliance requests.")) {
            InvitesMenu(player, this).open()
        }

        backButton(40)
    }
}
