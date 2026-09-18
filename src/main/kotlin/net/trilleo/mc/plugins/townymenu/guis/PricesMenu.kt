package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.TownBlockTypeHandler
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.roundToLong

/**
 * The time until Towny's next new day and the server's prices, like `/towny time` and `/towny prices`. Costs that
 * depend on a town, such as upkeep and plot prices, use the viewer's town when they have one.
 */
class PricesMenu(player: Player, back: Menu) : Menu(player, player.tr("prices.title"), 6, back) {

    override fun build() {
        val day = TownySettings.getNewDayTime()
        button(
            4, Icons.icon(Material.CLOCK, tr("prices.new-day"), tr("prices.new-day-description"), *buildList {
                if (TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWNY_TIME)) {
                    add(tr("town-status.new-day", "time" to TownyUtil.duration(player, TownyUtil.secondsUntilNewDay())))
                }
                add(tr("tutorial.fact.new-day", "time" to "%02d:%02d".format(day / 3600 % 24, day / 60 % 60)))
                add(tr("tutorial.fact.daily-taxes", "value" to TownyUtil.onOff(player, TownySettings.isTaxingDaily())))
            }.toTypedArray())
        )
        if (!TownyUtil.economy) {
            button(22, Icons.icon(Material.BARRIER, tr("prices.no-economy")))
            return backButton(49)
        }

        val town = resident?.townOrNull
        val nation = town?.nationOrNull
        val row = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        row.add(
            Icons.icon(
                Material.BELL, tr("prices.founding"), null,
                cost("prices.new-town", TownySettings.getNewTownPrice()),
                cost("prices.new-nation", TownySettings.getNewNationPrice()),
                cost("prices.reclaim", TownySettings.getEcoPriceReclaimTown()),
                cost("prices.merge", TownySettings.getBaseCostForTownMerge().toDouble()),
            )
        )
        row.add(Icons.icon(Material.GRASS_BLOCK, tr("prices.claims"), null, *buildList {
            add(cost("prices.claim", town?.townBlockCost ?: TownySettings.getClaimPrice()))
            val increase = TownySettings.getClaimPriceIncreaseValue()
            if (increase > 1) {
                add(tr("tutorial.fact.claim-increase", "percent" to ((increase - 1) * 100).roundToLong()))
            }
            add(cost("prices.claim-refund", TownySettings.getClaimRefundPrice()))
            add(cost("prices.bonus-claim", town?.bonusBlockCost ?: TownySettings.getPurchasedBonusBlocksCost()))
            if (TownySettings.isAllowingOutposts()) {
                add(cost("prices.outpost", TownySettings.getOutpostCost()))
                if (TownySettings.getPerOutpostUpkeepCost() > 0) {
                    add(cost("prices.outpost-upkeep", TownySettings.getPerOutpostUpkeepCost()))
                }
            }
        }.toTypedArray()))
        row.add(Icons.icon(Material.HOPPER, tr("prices.upkeep"), tr("prices.upkeep-description"), *buildList {
            add(
                cost(
                    "prices.town-upkeep",
                    town?.let { TownySettings.getTownUpkeepCost(it) } ?: TownySettings.getTownUpkeep()))
            add(
                cost(
                    "prices.nation-upkeep",
                    nation?.let { TownySettings.getNationUpkeepCost(it) } ?: TownySettings.getNationUpkeep()))
            if (town != null && town.isOverClaimed) {
                add(cost("prices.overclaimed-upkeep", TownySettings.getTownPenaltyUpkeepCost(town)))
            }
            add(
                cost(
                    "prices.town-peaceful",
                    town?.let { TownySettings.getTownNeutralityCost(it) } ?: TownySettings.getTownNeutralityCost()))
            add(
                cost(
                    "prices.nation-peaceful",
                    nation?.let { TownySettings.getNationNeutralityCost(it) }
                        ?: TownySettings.getNationNeutralityCost()))
        }.toTypedArray()))
        if (town != null) {
            row.add(
                Icons.icon(
                    Material.EMERALD, tr("prices.plot-prices", "town" to TownyUtil.name(town.name)), null,
                    cost("prices.plot", town.plotPrice),
                    cost("prices.shop", town.commercialPlotPrice),
                    cost("prices.embassy", town.embassyPlotPrice),
                )
            )
            row.add(
                Icons.icon(
                    Material.GOLD_NUGGET, tr("prices.taxes", "town" to TownyUtil.name(town.name)), null,
                    tr(
                        "prices.resident-tax",
                        "cost" to if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)
                    ),
                    cost("prices.plot-tax", town.plotTax),
                    cost("prices.shop-tax", town.commercialPlotTax),
                    cost("prices.embassy-tax", town.embassyPlotTax),
                )
            )
        }
        row.add(
            Icons.icon(
                Material.OAK_SIGN, tr("prices.plot-types"), tr("prices.plot-types-description"),
                *TownBlockTypeHandler.getTypes().values.sortedBy { it.name }.map {
                    tr(
                        "prices.plot-type",
                        "type" to TownyUtil.plotType(player, it.name),
                        "cost" to TownyUtil.money(it.cost)
                    )
                }.toTypedArray()
            )
        )

        tutorialButton(53, Tutorial.ECONOMY)
        backButton(49)
    }

    private fun cost(key: String, amount: Double): String = tr(key, "cost" to TownyUtil.money(maxOf(amount, 0.0)))
}
