package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.common.BankMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentProfileMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Management hub for the viewer's own nation. */
class NationMenu(player: Player, back: Menu?) : Menu(player, "Your Nation", 6, back) {

    private val nation: Nation?
        get() = resident?.nationOrNull

    override fun build() {
        val nation = nation
        val viewer = resident
        if (nation == null || viewer == null) {
            button(22, Icons.icon(Material.BARRIER, "<red>Your town is not in a nation"))
            return backButton(49)
        }

        button(4, Icons.nation(nation, *buildList {
            add("<gray>Founded: <white>${TownyUtil.date(nation.registered)}")
            if (TownyUtil.economy) {
                add("<gray>Daily town tax: <white>${if (nation.isTaxPercentage) "${nation.taxes}%" else TownyUtil.money(nation.taxes)}")
            }
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(Icons.icon(Material.BELL, "<gold>Towns", "View member towns, invite towns, and remove towns.", "<gray>Towns: <white>${nation.numTowns}")) {
            NationTownsMenu(player, nation, this).open()
        }
        grid.add(Icons.icon(Material.PLAYER_HEAD, "<green>Residents", "View every resident and manage nation ranks.", "<gray>Residents: <white>${nation.numResidents}")) {
            residents(nation).open()
        }
        if (TownyUtil.economy) {
            grid.add(Icons.icon(Material.GOLD_INGOT, "<gold>Nation Bank", "Deposit, withdraw, and view bank history.", "<gray>Balance: <white>${TownyUtil.balance(nation)}")) {
                BankMenu(player, nation, this).open()
            }
        }
        grid.add(Icons.icon(Material.SHIELD, "<aqua>Allies & Enemies", "Manage alliances and declare enemies.")) {
            NationRelationsMenu(player, nation, this).open()
        }
        grid.add(Icons.icon(Material.LEVER, "<yellow>Nation Settings", "Peaceful, open, public, and percentage taxes.")) {
            toggles().open()
        }
        grid.add(Icons.icon(Material.WRITABLE_BOOK, "<yellow>Nation Details", "Name, board, tag, taxes, capital, leader, and spawn.")) {
            NationSettingsMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.ENDER_PEARL, "<light_purple>Nation Spawn", "Teleport to your nation's spawn.")) {
            runAndClose("towny:nation spawn")
        }
        if (viewer.isKing) {
            grid.add(PermissionNodes.TOWNY_COMMAND_NATION_DELETE,
                Icons.icon(Material.TNT, "<dark_red>Delete Nation", "Disband your nation permanently. You will be asked to confirm.")) {
                run("towny:nation delete", { viewer.hasNation() }, returnTo = MainMenu(player))
            }
        } else if (viewer.isMayor) {
            grid.add(PermissionNodes.TOWNY_COMMAND_NATION_LEAVE,
                Icons.icon(Material.OAK_DOOR, "<red>Leave Nation", "Take your town out of the nation. You will be asked to confirm.")) {
                run("towny:nation leave", { viewer.hasNation() }, returnTo = MainMenu(player))
            }
        }

        backButton(49)
    }

    private fun residents(nation: Nation): Menu =
        ListMenu(player, "Residents: ${TownyUtil.name(nation.name)}", this) { menu ->
            nation.residents
                .sortedWith(compareByDescending<Resident> { it.isKing }
                    .thenByDescending { it.isOnline }
                    .thenBy { it.name.lowercase() })
                .map { member -> MenuEntry({ Icons.resident(member, "", "<yellow>Click to view") }) { ResidentProfileMenu(player, member, menu).open() } }
        }

    private fun toggles(): Menu {
        fun toggle(material: Material, key: String, label: String, description: String, node: PermissionNodes, read: (Nation) -> Boolean) =
            Toggle(material, label, description, node, "towny:nation toggle $key") { nation?.let(read) }

        return ToggleMenu(player, "Nation Settings", this, listOf(
            toggle(Material.WHITE_BANNER, "peaceful", "<white>Peaceful", "Stay out of wars; PvP is disabled in member towns.", PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_NEUTRAL) { it.isNeutral },
            toggle(Material.OAK_DOOR, "open", "<green>Open", "Let any town join without an invite.", PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_OPEN) { it.isOpen },
            toggle(Material.ENDER_EYE, "public", "<aqua>Public", "Let outsiders teleport to your nation spawn.", PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_PUBLIC) { it.isPublic },
            toggle(Material.GOLD_NUGGET, "taxpercent", "<gold>Percentage Taxes", "Charge towns a percentage of their balance instead of a flat amount.", PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_TAXPERCENT) { it.isTaxPercentage },
        ))
    }
}
