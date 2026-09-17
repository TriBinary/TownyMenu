package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Edits the viewer's town details: name, board, tag, taxes, prices, spawn points, and sale. */
class TownSettingsMenu(player: Player, back: Menu) : Menu(player, player.tr("town-details.title"), 6, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town ?: return backButton(49)
        val grid = layout(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)

        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_NAME,
            Icons.icon(Material.NAME_TAG, tr("town-details.rename"), null, tr("common.current", "value" to TownyUtil.name(town.name)))) {
            prompt(tr("town-details.rename-title"), tr("common.new-name"), initial = town.name) { name ->
                run("towny:town set name ${TownyUtil.nameArgument(name)}", { town.name })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_BOARD,
            Icons.icon(Material.OAK_SIGN, tr("town-details.board"), tr("town-details.board-description"))) { click ->
            if (click.isRightClick) {
                run("towny:town set board none", { town.board })
            } else {
                prompt(tr("town-details.board-title"), tr("common.message"), initial = town.board, maxLength = 256, multiline = true) { board ->
                    run("towny:town set board ${board.replace('\n', ' ')}", { town.board })
                }
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAG,
            Icons.icon(Material.PAPER, tr("town-details.tag"), tr("common.tag-description"), tr("common.current", "value" to TownyUtil.text(town.tag)))) {
            prompt(tr("town-details.tag-title"), tr("common.tag"), initial = town.tag, maxLength = 16) { tag ->
                run("towny:town set tag ${TownyUtil.argument(tag)}", { town.tag })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_MAPCOLOR,
            Icons.icon(Material.LIME_DYE, tr("common.map-color"), tr("town-details.map-color-description"))) {
            val colors = TownySettings.getTownColorsMap().keys.sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, tr("town-details.map-color-title"), colors, null, Material.LIME_DYE) { color ->
                run("towny:town set mapcolor $color", { town.mapColorHexCode })
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_SPAWN,
            Icons.icon(Material.RESPAWN_ANCHOR, tr("common.set-spawn"), tr("town-details.set-spawn-description"))) {
            run("towny:town set spawn", { town.spawnOrNull })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_HOMEBLOCK,
            Icons.icon(Material.LODESTONE, tr("town-details.home-block"), tr("town-details.home-block-description"))) {
            run("towny:town set homeblock", { town.homeBlockOrNull })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_OUTPOST,
            Icons.icon(Material.COMPASS, tr("town-details.outpost"), tr("town-details.outpost-description"))) {
            run("towny:town set outpost", { town.allOutpostSpawns.toList() })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_PRIMARYJAIL,
            Icons.icon(Material.IRON_BARS, tr("town-details.jail"), tr("town-details.jail-description"))) {
            run("towny:town set primaryjail")
        }

        if (TownyUtil.economy) {
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAXES, Material.GOLD_NUGGET, "taxes", tr("town-details.taxes"),
                if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)) { town.taxes }
            if (town.isTaxPercentage) {
                money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAXPERCENTCAP, Material.GOLD_NUGGET, "taxpercentcap", tr("town-details.tax-cap"),
                    TownyUtil.money(town.maxPercentTaxAmount)) { town.maxPercentTaxAmount }
            }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_PLOTTAX, Material.IRON_NUGGET, "plottax", tr("town-details.plot-tax"), TownyUtil.money(town.plotTax)) { town.plotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SHOPTAX, Material.IRON_NUGGET, "shoptax", tr("town-details.shop-tax"), TownyUtil.money(town.commercialPlotTax)) { town.commercialPlotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_EMBASSYTAX, Material.IRON_NUGGET, "embassytax", tr("town-details.embassy-tax"), TownyUtil.money(town.embassyPlotTax)) { town.embassyPlotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_PLOTPRICE, Material.EMERALD, "plotprice", tr("town-details.plot-price"), TownyUtil.money(town.plotPrice)) { town.plotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SHOPPRICE, Material.EMERALD, "shopprice", tr("town-details.shop-price"), TownyUtil.money(town.commercialPlotPrice)) { town.commercialPlotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_EMBASSYPRICE, Material.EMERALD, "embassyprice", tr("town-details.embassy-price"), TownyUtil.money(town.embassyPlotPrice)) { town.embassyPlotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SPAWNCOST, Material.ENDER_PEARL, "spawncost", tr("town-details.spawn-cost"), TownyUtil.money(town.spawnCost)) { town.spawnCost }

            if (town.isForSale) {
                grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_NOTFORSALE,
                    Icons.icon(Material.RED_BANNER, tr("town-details.not-for-sale"), null, tr("common.price", "price" to TownyUtil.money(town.forSalePrice)))) {
                    run("towny:town notforsale", { town.isForSale })
                }
            } else {
                grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_FORSALE,
                    Icons.icon(Material.GREEN_BANNER, tr("town-details.for-sale"), tr("town-details.for-sale-description"))) {
                    prompt(tr("town-details.for-sale-title"), tr("common.price-label")) { price ->
                        run("towny:town forsale ${TownyUtil.argument(price)}", { town.isForSale })
                    }
                }
            }
        }

        backButton(49)
    }

    private fun money(grid: Layout, node: PermissionNodes, material: Material, key: String, label: String, current: String, read: () -> Double) {
        grid.add(node, Icons.icon(material, "<gold>$label", null, tr("common.current", "value" to current), tr("common.click-change"))) {
            prompt(label, tr("common.amount"), initial = read().toString()) { amount ->
                run("towny:town set $key ${TownyUtil.argument(amount)}", read)
            }
        }
    }
}
