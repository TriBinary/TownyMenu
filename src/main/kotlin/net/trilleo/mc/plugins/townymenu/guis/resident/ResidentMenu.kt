package net.trilleo.mc.plugins.townymenu.guis.resident

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** The viewer's own resident profile: friends, personal toggles, plot permissions, and more. */
class ResidentMenu(player: Player, back: Menu?) : Menu(player, "Your Profile", 5, back) {

    override fun build() {
        val resident = resident ?: return backButton(40)

        button(4, Icons.resident(resident, *buildList {
            add("<gray>Registered: <white>${TownyUtil.date(resident.registered)}")
            add("<gray>Friends: <white>${resident.friends.size}")
            add("<gray>Plots owned: <white>${resident.townBlocks.size}")
            if (TownyUtil.economy) {
                resident.accountOrNull?.let { add("<gray>Balance: <white>${TownyUtil.money(it.holdingBalance)}") }
                if (resident.hasTown()) add("<gray>Tax owed today: <white>${TownyUtil.money(resident.getTaxOwing(true))}")
            }
            if (resident.about.isNotBlank()) add("<gray>About: <white><i>${TownyUtil.text(resident.about)}")
            if (resident.isJailed) add("<red>Jailed in ${resident.jailTown?.let { TownyUtil.name(it.name) } ?: "a town"} (${resident.jailHours}h left)")
        }.toTypedArray()))

        val row = layout(19, 20, 21, 22, 23, 24, 25)
        row.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_FRIEND,
            Icons.icon(Material.POPPY, "<light_purple>Friends", "Friends count as residents in your personal plot permissions.")) {
            FriendsMenu(player, this).open()
        }
        row.add(Icons.icon(Material.LEVER, "<yellow>Personal Settings", "PvP, fire, and mobs on your plots, plus map and border display modes.")) {
            toggles(resident).open()
        }
        row.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_PERM,
            Icons.icon(Material.IRON_DOOR, "<aqua>Plot Permissions", "Who can build, break, and interact on plots you personally own.")) {
            PermissionMenu(player, "Your Plot Permissions", this, "towny:resident set perm",
                PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_PERM, true) { TownyAPI.getInstance().getResident(player)?.permissions }.open()
        }
        row.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_ABOUT,
            Icons.icon(Material.WRITABLE_BOOK, "<gold>About Me", "Left-click to write a short bio. Right-click to clear it.")) { click ->
            if (click.isRightClick) {
                run("towny:resident set about none", { resident.about })
            } else {
                prompt("About Me", "Bio", initial = resident.about, maxLength = 159) { text ->
                    run("towny:resident set about $text", { resident.about })
                }
            }
        }
        row.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_SPAWN,
            Icons.icon(Material.RED_BED, "<green>Go to Spawn", "Teleport to your bed or your town spawn.")) {
            runAndClose("towny:resident spawn")
        }
        if (resident.isJailed && TownyUtil.economy) {
            row.add(Icons.icon(Material.IRON_BARS, "<red>Pay Bail",
                "Pay ${TownyUtil.money(resident.jailBailCost)} to leave jail.")) {
                run("towny:resident jail paybail", { resident.isJailed })
            }
        }

        backButton(40)
    }

    private fun toggles(resident: Resident): Menu {
        fun perm(material: Material, key: String, label: String, description: String, node: PermissionNodes, read: (Resident) -> Boolean) =
            Toggle(material, label, description, node, "towny:resident toggle $key") { TownyAPI.getInstance().getResident(player)?.let(read) }

        fun mode(material: Material, key: String, label: String, description: String, node: PermissionNodes) =
            perm(material, key, label, description, node) { it.hasMode(key) }

        return ToggleMenu(player, "Personal Settings", this, listOf(
            perm(Material.IRON_SWORD, "pvp", "<red>PvP", "Allow PvP on plots you own.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PVP) { it.permissions.pvp },
            perm(Material.FLINT_AND_STEEL, "fire", "<gold>Fire Spread", "Allow fire to spread on plots you own.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_FIRE) { it.permissions.fire },
            perm(Material.TNT, "explosion", "<dark_red>Explosions", "Allow explosions on plots you own.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_EXPLOSION) { it.permissions.explosion },
            perm(Material.ZOMBIE_HEAD, "mobs", "<dark_green>Hostile Mobs", "Allow hostile mobs to spawn on plots you own.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_MOBS) { it.permissions.mobs },
            perm(Material.OAK_SIGN, "bordertitles", "<yellow>Border Titles", "Show a title when you cross into a town or the wilderness.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_BORDERTITLES) { it.isSeeingBorderTitles },
            mode(Material.FILLED_MAP, "map", "<aqua>Auto Map", "Print the Towny map in chat whenever you enter a new chunk.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_MAP),
            mode(Material.GRASS_BLOCK, "townclaim", "<green>Auto Claim", "Claim each chunk you walk into for your town.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNCLAIM),
            mode(Material.DEAD_BUSH, "townunclaim", "<red>Auto Unclaim", "Unclaim each chunk you walk into.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNUNCLAIM),
            mode(Material.GLOWSTONE_DUST, "plotborder", "<yellow>Plot Borders", "Show particle borders when you enter a plot.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PLOTBORDER),
            mode(Material.GLOWSTONE, "constantplotborder", "<yellow>Constant Plot Borders", "Always show particle borders of the plot you are in.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_CONSTANTPLOTBORDER),
            mode(Material.REDSTONE, "townborder", "<gold>Town Borders", "Show particle borders of the town you are in.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNBORDER),
            mode(Material.STICK, "infotool", "<white>Info Tool", "Right-click blocks with a stick to see their Towny info.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_INFOTOOL),
            mode(Material.MAP, "ignoreplots", "<gray>Ignore Plot Titles", "Hide plot name and owner messages while you walk.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_IGNOREPLOTS),
            mode(Material.PAPER, "ignoreinvites", "<gray>Ignore Invites", "Stop receiving town invites.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_IGNOREINVITES),
            mode(Material.WHITE_BED, "bedspawn", "<white>Bed Spawn", "Use your bed as your Towny spawn.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_BEDSPAWN),
            mode(Material.CHEST, "plotgroup", "<gold>Plot Group Mode", "Add each plot you walk into to your selected plot group.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PLOTGROUP),
            mode(Material.BARREL, "district", "<gold>District Mode", "Add each plot you walk into to your selected district.", PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_DISTRICT),
        ))
    }
}
