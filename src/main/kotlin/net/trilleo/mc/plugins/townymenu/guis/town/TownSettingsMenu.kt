package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Edits the viewer's town details: name, board, tag, taxes, prices, spawn points, and sale. */
class TownSettingsMenu(player: Player, back: Menu) : Menu(player, "Town Details", 6, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town ?: return backButton(49)
        val grid = layout(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)

        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_NAME,
            Icons.icon(Material.NAME_TAG, "<gold>Rename Town", null, "<gray>Current: <white>${TownyUtil.name(town.name)}")) {
            prompt("Rename Town", "New name", initial = town.name) { name ->
                run("towny:town set name ${TownyUtil.nameArgument(name)}", { town.name })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_BOARD,
            Icons.icon(Material.OAK_SIGN, "<yellow>Town Board", "Left-click to edit the message residents see on login. Right-click to clear it.")) { click ->
            if (click.isRightClick) {
                run("towny:town set board none", { town.board })
            } else {
                prompt("Town Board", "Message", initial = town.board, maxLength = 256, multiline = true) { board ->
                    run("towny:town set board ${board.replace('\n', ' ')}", { town.board })
                }
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAG,
            Icons.icon(Material.PAPER, "<yellow>Town Tag", "A short tag shown in chat.", "<gray>Current: <white>${TownyUtil.text(town.tag)}")) {
            prompt("Town Tag", "Tag", initial = town.tag, maxLength = 16) { tag ->
                run("towny:town set tag ${TownyUtil.argument(tag)}", { town.tag })
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_MAPCOLOR,
            Icons.icon(Material.LIME_DYE, "<green>Map Color", "The colour of your town on web maps.")) {
            val colors = TownySettings.getTownColorsMap().keys.sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, "Town Map Color", colors, null, Material.LIME_DYE) { color ->
                run("towny:town set mapcolor $color", { town.mapColorHexCode })
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_SPAWN,
            Icons.icon(Material.RESPAWN_ANCHOR, "<light_purple>Set Spawn Here", "Move the town spawn to where you are standing.")) {
            run("towny:town set spawn", { town.spawnOrNull })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_HOMEBLOCK,
            Icons.icon(Material.LODESTONE, "<light_purple>Set Home Block Here", "Make the chunk you are standing in the town's home block.")) {
            run("towny:town set homeblock", { town.homeBlockOrNull })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_OUTPOST,
            Icons.icon(Material.COMPASS, "<light_purple>Set Outpost Spawn Here", "Set the spawn of the outpost you are standing in.")) {
            run("towny:town set outpost", { town.allOutpostSpawns.toList() })
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_PRIMARYJAIL,
            Icons.icon(Material.IRON_BARS, "<gray>Set Primary Jail", "Make the jail plot you are standing in the town's primary jail.")) {
            run("towny:town set primaryjail")
        }

        if (TownyUtil.economy) {
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAXES, Material.GOLD_NUGGET, "taxes", "Daily Resident Tax",
                if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)) { town.taxes }
            if (town.isTaxPercentage) {
                money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_TAXPERCENTCAP, Material.GOLD_NUGGET, "taxpercentcap", "Percentage Tax Cap",
                    TownyUtil.money(town.maxPercentTaxAmount)) { town.maxPercentTaxAmount }
            }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_PLOTTAX, Material.IRON_NUGGET, "plottax", "Daily Plot Tax", TownyUtil.money(town.plotTax)) { town.plotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SHOPTAX, Material.IRON_NUGGET, "shoptax", "Daily Shop Plot Tax", TownyUtil.money(town.commercialPlotTax)) { town.commercialPlotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_EMBASSYTAX, Material.IRON_NUGGET, "embassytax", "Daily Embassy Plot Tax", TownyUtil.money(town.embassyPlotTax)) { town.embassyPlotTax }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_PLOTPRICE, Material.EMERALD, "plotprice", "Default Plot Price", TownyUtil.money(town.plotPrice)) { town.plotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SHOPPRICE, Material.EMERALD, "shopprice", "Default Shop Plot Price", TownyUtil.money(town.commercialPlotPrice)) { town.commercialPlotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_EMBASSYPRICE, Material.EMERALD, "embassyprice", "Default Embassy Plot Price", TownyUtil.money(town.embassyPlotPrice)) { town.embassyPlotPrice }
            money(grid, PermissionNodes.TOWNY_COMMAND_TOWN_SET_SPAWNCOST, Material.ENDER_PEARL, "spawncost", "Spawn Cost for Visitors", TownyUtil.money(town.spawnCost)) { town.spawnCost }

            if (town.isForSale) {
                grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_NOTFORSALE,
                    Icons.icon(Material.RED_BANNER, "<red>Take Town off Sale", null, "<gray>Price: <white>${TownyUtil.money(town.forSalePrice)}")) {
                    run("towny:town notforsale", { town.isForSale })
                }
            } else {
                grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_FORSALE,
                    Icons.icon(Material.GREEN_BANNER, "<gold>Put Town up for Sale", "Let another player buy your town.")) {
                    prompt("Sell Town", "Price") { price ->
                        run("towny:town forsale ${TownyUtil.argument(price)}", { town.isForSale })
                    }
                }
            }
        }

        backButton(49)
    }

    private fun money(grid: Layout, node: PermissionNodes, material: Material, key: String, label: String, current: String, read: () -> Double) {
        grid.add(node, Icons.icon(material, "<gold>$label", null, "<gray>Current: <white>$current", "<yellow>Click to change")) {
            prompt(label, "Amount", initial = read().toString()) { amount ->
                run("towny:town set $key ${TownyUtil.argument(amount)}", read)
            }
        }
    }
}
