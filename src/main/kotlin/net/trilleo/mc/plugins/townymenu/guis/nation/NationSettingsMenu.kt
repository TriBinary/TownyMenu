package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Edits the viewer's nation details: name, board, tag, taxes, capital, leader, spawn, and map colour. */
class NationSettingsMenu(player: Player, back: Menu) : Menu(player, "Nation Details", 5, back) {

    private val nation: Nation?
        get() = resident?.nationOrNull

    override fun build() {
        val nation = nation ?: return backButton(40)
        val grid = layout(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_NAME,
            Icons.icon(Material.NAME_TAG, "<aqua>Rename Nation", null, "<gray>Current: <white>${TownyUtil.name(nation.name)}")) {
            prompt("Rename Nation", "New name", initial = nation.name) { name ->
                run("towny:nation set name ${TownyUtil.nameArgument(name)}", { nation.name })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_BOARD,
            Icons.icon(Material.OAK_SIGN, "<yellow>Nation Board", "Left-click to edit the message residents see on login. Right-click to clear it.")) { click ->
            if (click.isRightClick) {
                run("towny:nation set board none", { nation.board })
            } else {
                prompt("Nation Board", "Message", initial = nation.board, maxLength = 256, multiline = true) { board ->
                    run("towny:nation set board ${board.replace('\n', ' ')}", { nation.board })
                }
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_TAG,
            Icons.icon(Material.PAPER, "<yellow>Nation Tag", "A short tag shown in chat.", "<gray>Current: <white>${TownyUtil.text(nation.tag)}")) {
            prompt("Nation Tag", "Tag", initial = nation.tag, maxLength = 16) { tag ->
                run("towny:nation set tag ${TownyUtil.argument(tag)}", { nation.tag })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_MAPCOLOR,
            Icons.icon(Material.LIGHT_BLUE_DYE, "<aqua>Map Color", "The colour of your nation on web maps.")) {
            val colors = TownySettings.getNationColorsMap().keys.sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, "Nation Map Color", colors, null, Material.LIGHT_BLUE_DYE) { color ->
                run("towny:nation set mapcolor $color", { nation.mapColorHexCode })
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_CAPITAL,
            Icons.icon(Material.GOLDEN_HELMET, "<gold>Change Capital", null, "<gray>Current: <white>${nation.capital?.let { TownyUtil.name(it.name) } ?: "-"}")) {
            ListMenu(player, "Choose Capital", this) {
                nation.towns.filter { !nation.isCapital(it) }.sortedBy { it.name.lowercase() }.map { town ->
                    MenuEntry({ Icons.town(town, "", "<yellow>Click to make capital") }) {
                        run("towny:nation set capital ${town.name}", { nation.capital }, returnTo = this)
                    }
                }
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_KING,
            Icons.icon(Material.PLAYER_HEAD, "<gold>Change Leader", "The leader must live in the capital.", "<gray>Current: <white>${nation.king?.let { TownyUtil.name(it.name) } ?: "-"}")) {
            ListMenu(player, "Choose Leader", this) {
                nation.capital?.residents.orEmpty().filter { !it.isKing }.sortedBy { it.name.lowercase() }.map { candidate ->
                    MenuEntry({ Icons.resident(candidate, "", "<yellow>Click to make leader") }) {
                        run("towny:nation set king ${candidate.name}", { nation.king }, returnTo = this)
                    }
                }
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWN,
            Icons.icon(Material.RESPAWN_ANCHOR, "<light_purple>Set Spawn Here", "Move the nation spawn to where you are standing.")) {
            run("towny:nation set spawn", { runCatching { nation.spawn }.getOrNull() })
        }

        if (TownyUtil.economy) {
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAXES, "taxes", "Daily Town Tax",
                if (nation.isTaxPercentage) "${nation.taxes}%" else TownyUtil.money(nation.taxes)) { nation.taxes }
            if (nation.isTaxPercentage) {
                money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAXPERCENTCAP, "taxpercentcap", "Percentage Tax Cap",
                    TownyUtil.money(nation.maxPercentTaxAmount)) { nation.maxPercentTaxAmount }
            }
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_CONQUEREDTAX, "conqueredtax", "Conquered Town Tax",
                TownyUtil.money(nation.conqueredTax)) { nation.conqueredTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWNCOST, "spawncost", "Spawn Cost for Visitors",
                TownyUtil.money(nation.spawnCost)) { nation.spawnCost }
        }

        backButton(40)
    }

    private fun money(grid: Layout, node: PermissionNodes, key: String, label: String, current: String, read: () -> Double) {
        grid.add(node, Icons.icon(Material.GOLD_NUGGET, "<gold>$label", null, "<gray>Current: <white>$current", "<yellow>Click to change")) {
            prompt(label, "Amount", initial = read().toString()) { amount ->
                run("towny:nation set $key ${TownyUtil.argument(amount)}", read)
            }
        }
    }
}
