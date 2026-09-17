package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Edits the viewer's nation details: name, board, tag, taxes, capital, leader, spawn, and map colour. */
class NationSettingsMenu(player: Player, back: Menu) : Menu(player, player.tr("nation-details.title"), 5, back) {

    private val nation: Nation?
        get() = resident?.nationOrNull

    override fun build() {
        val nation = nation ?: return backButton(40)
        val grid = layout(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_NAME,
            Icons.icon(Material.NAME_TAG, tr("nation-details.rename"), null, tr("common.current", "value" to TownyUtil.name(nation.name)))) {
            prompt(tr("nation-details.rename-title"), tr("common.new-name"), initial = nation.name) { name ->
                run("towny:nation set name ${TownyUtil.nameArgument(name)}", { nation.name })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_BOARD,
            Icons.icon(Material.OAK_SIGN, tr("nation-details.board"), tr("town-details.board-description"))) { click ->
            if (click.isRightClick) {
                run("towny:nation set board none", { nation.board })
            } else {
                prompt(tr("nation-details.board-title"), tr("common.message"), initial = nation.board, maxLength = 256, multiline = true) { board ->
                    run("towny:nation set board ${board.replace('\n', ' ')}", { nation.board })
                }
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_TAG,
            Icons.icon(Material.PAPER, tr("nation-details.tag"), tr("common.tag-description"), tr("common.current", "value" to TownyUtil.text(nation.tag)))) {
            prompt(tr("nation-details.tag-title"), tr("common.tag"), initial = nation.tag, maxLength = 16) { tag ->
                run("towny:nation set tag ${TownyUtil.argument(tag)}", { nation.tag })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_MAPCOLOR,
            Icons.icon(Material.LIGHT_BLUE_DYE, tr("nation-details.map-color"), tr("nation-details.map-color-description"))) {
            val colors = TownySettings.getNationColorsMap().keys.sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, tr("nation-details.map-color-title"), colors, null, Material.LIGHT_BLUE_DYE) { color ->
                run("towny:nation set mapcolor $color", { nation.mapColorHexCode })
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_CAPITAL,
            Icons.icon(Material.GOLDEN_HELMET, tr("nation-details.capital"), null,
                tr("common.current", "value" to (nation.capital?.let { TownyUtil.name(it.name) } ?: "-")))) {
            ListMenu(player, tr("nation-details.capital-title"), this) {
                nation.towns.filter { !nation.isCapital(it) }.sortedBy { it.name.lowercase() }.map { town ->
                    MenuEntry({ Icons.town(player, town, "", tr("nation-details.capital-click")) }) {
                        run("towny:nation set capital ${town.name}", { nation.capital }, returnTo = this)
                    }
                }
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_KING,
            Icons.icon(Material.PLAYER_HEAD, tr("nation-details.leader"), tr("nation-details.leader-description"),
                tr("common.current", "value" to (nation.king?.let { TownyUtil.name(it.name) } ?: "-")))) {
            ListMenu(player, tr("nation-details.leader-title"), this) {
                nation.capital?.residents.orEmpty().filter { !it.isKing }.sortedBy { it.name.lowercase() }.map { candidate ->
                    MenuEntry({ Icons.resident(player, candidate, "", tr("nation-details.leader-click")) }) {
                        run("towny:nation set king ${candidate.name}", { nation.king }, returnTo = this)
                    }
                }
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWN,
            Icons.icon(Material.RESPAWN_ANCHOR, tr("common.set-spawn"), tr("nation-details.set-spawn-description"))) {
            run("towny:nation set spawn", { runCatching { nation.spawn }.getOrNull() })
        }

        if (TownyUtil.economy) {
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAXES, "taxes", tr("nation-details.taxes"),
                if (nation.isTaxPercentage) "${nation.taxes}%" else TownyUtil.money(nation.taxes)) { nation.taxes }
            if (nation.isTaxPercentage) {
                money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_TAXPERCENTCAP, "taxpercentcap", tr("town-details.tax-cap"),
                    TownyUtil.money(nation.maxPercentTaxAmount)) { nation.maxPercentTaxAmount }
            }
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_CONQUEREDTAX, "conqueredtax", tr("nation-details.conquered-tax"),
                TownyUtil.money(nation.conqueredTax)) { nation.conqueredTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_NATION_SET_SPAWNCOST, "spawncost", tr("town-details.spawn-cost"),
                TownyUtil.money(nation.spawnCost)) { nation.spawnCost }
        }

        tutorialButton(44, Tutorial.NATIONS)
        backButton(40)
    }

    private fun money(grid: Layout, node: PermissionNodes, key: String, label: String, current: String, read: () -> Double) {
        grid.add(node, Icons.icon(Material.GOLD_NUGGET, "<gold>$label", null, tr("common.current", "value" to current), tr("common.click-change"))) {
            prompt(label, tr("common.amount"), initial = read().toString()) { amount ->
                run("towny:nation set $key ${TownyUtil.argument(amount)}", read)
            }
        }
    }
}
