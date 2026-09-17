package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.common.BankMenu
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.nation.NationMenu
import net.trilleo.mc.plugins.townymenu.guis.nation.NoNationMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Management hub for the viewer's own town. */
class TownMenu(player: Player, back: Menu?) : Menu(player, "Your Town", 6, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town
        if (town == null) {
            button(22, Icons.icon(Material.BARRIER, "<red>You are not in a town"))
            return backButton(49)
        }
        val viewer = resident ?: return backButton(49)

        button(4, Icons.town(town, *buildList {
            add("<gray>Founded: <white>${TownyUtil.date(town.registered)}")
            if (TownyUtil.economy) {
                add("<gray>Daily tax: <white>${if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)}")
                add("<gray>Plot tax: <white>${TownyUtil.money(town.plotTax)}")
            }
            add("<gray>Outposts: <white>${town.maxOutpostSpawn}")
            add("<gray>PvP: ${TownyUtil.onOff(town.isPVP)}  <gray>Mobs: ${TownyUtil.onOff(town.hasMobs())}")
            add("<gray>Fire: ${TownyUtil.onOff(town.isFire)}  <gray>Explosions: ${TownyUtil.onOff(town.isExplosion)}")
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)

        grid.add(Icons.icon(Material.PLAYER_HEAD, "<green>Residents", "View residents, manage ranks, and invite players.", "<gray>Residents: <white>${town.numResidents}")) {
            TownMembersMenu(player, town, this).open()
        }
        if (TownyUtil.economy) {
            grid.add(Icons.icon(Material.GOLD_INGOT, "<gold>Town Bank", "Deposit, withdraw, and view bank history.", "<gray>Balance: <white>${TownyUtil.balance(town)}")) {
                BankMenu(player, town, this).open()
            }
        }
        grid.add(Icons.icon(Material.GRASS_BLOCK, "<green>Claims", "Claim and unclaim land, create outposts, and buy bonus claims.", "<gray>Claims: <white>${town.numTownBlocks}/${town.maxTownBlocksAsAString}")) {
            TownClaimsMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.FILLED_MAP, "<aqua>Map", "See the land around you.")) {
            MapMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.LEVER, "<yellow>Town Settings", "PvP, mobs, fire, explosions, open, public, and peaceful.")) {
            toggles().open()
        }
        grid.add(Icons.icon(Material.WRITABLE_BOOK, "<yellow>Town Details", "Name, board, tag, taxes, prices, spawn, and sale.")) {
            TownSettingsMenu(player, this).open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_PERM,
            Icons.icon(Material.IRON_DOOR, "<aqua>Permissions", "Who can build, break, and interact on town-owned land.")) {
            PermissionMenu(player, "Town Permissions", this, "towny:town set perm",
                PermissionNodes.TOWNY_COMMAND_TOWN_SET_PERM, false) { TownyAPI.getInstance().getResident(player)?.townOrNull?.permissions }.open()
        }
        grid.add(Icons.icon(Material.TRIPWIRE_HOOK, "<green>Trusted", "Residents and towns trusted to build anywhere in town.")) {
            TownTrustMenu(player, town, this).open()
        }
        grid.add(Icons.icon(Material.IRON_BARS, "<red>Outlaws", "Players declared outlaws of your town.", "<gray>Outlaws: <white>${town.outlaws.size}")) {
            OutlawsMenu(player, town, this).open()
        }
        grid.add(Icons.icon(Material.ENDER_PEARL, "<light_purple>Town Spawn", "Teleport to your town spawn.")) {
            runAndClose("towny:town spawn")
        }
        if (town.hasOutpostSpawn()) {
            grid.add(Icons.icon(Material.COMPASS, "<light_purple>Outposts", "Teleport to one of your town's outposts.")) {
                OutpostsMenu(player, town, this).open()
            }
        }
        val nation = town.nationOrNull
        grid.add(Icons.icon(Material.BEACON, "<aqua>Nation", if (nation != null) "Open ${TownyUtil.name(nation.name)}." else "Found or join a nation.")) {
            if (town.hasNation()) NationMenu(player, this).open() else NoNationMenu(player, this).open()
        }

        if (viewer.isMayor) {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_DELETE,
                Icons.icon(Material.TNT, "<dark_red>Delete Town", "Disband your town permanently. You will be asked to confirm.")) {
                run("towny:town delete", { viewer.hasTown() }, returnTo = MainMenu(player))
            }
        } else {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_LEAVE,
                Icons.icon(Material.OAK_DOOR, "<red>Leave Town", "Leave your town. You will be asked to confirm.")) {
                run("towny:town leave", { viewer.hasTown() }, returnTo = MainMenu(player))
            }
        }

        backButton(49)
    }

    private fun toggles(): Menu {
        fun toggle(material: Material, key: String, label: String, description: String, node: PermissionNodes, read: (Town) -> Boolean) =
            Toggle(material, label, description, node, "towny:town toggle $key") { town?.let(read) }

        return ToggleMenu(player, "Town Settings", this, listOf(
            toggle(Material.IRON_SWORD, "pvp", "<red>PvP", "Allow players to fight inside the town.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_PVP) { it.isPVP },
            toggle(Material.ZOMBIE_HEAD, "mobs", "<dark_green>Hostile Mobs", "Allow hostile mobs to spawn in town.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_MOBS) { it.hasMobs() },
            toggle(Material.FLINT_AND_STEEL, "fire", "<gold>Fire Spread", "Allow fire to spread in town.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_FIRE) { it.isFire },
            toggle(Material.TNT, "explosion", "<dark_red>Explosions", "Allow explosions to damage the town.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_EXPLOSION) { it.isExplosion },
            toggle(Material.OAK_DOOR, "open", "<green>Open", "Let anyone join without an invite.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_OPEN) { it.isOpen },
            toggle(Material.ENDER_EYE, "public", "<aqua>Public", "Let outsiders teleport to your spawn and see your home block.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_PUBLIC) { it.isPublic },
            toggle(Material.WHITE_BANNER, "neutral", "<white>Peaceful", "Stay out of wars and PvP.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_NEUTRAL) { it.isNeutral },
            toggle(Material.GOLD_NUGGET, "taxpercent", "<gold>Percentage Taxes", "Charge taxes as a percentage of balance instead of a flat amount.", PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_TAXPERCENT) { it.isTaxPercentage },
        ))
    }
}
