package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.utils.TownRuinUtil
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** A town's level, limits, and daily upkeep, with a warning for every problem Towny would act on. */
class TownStatusMenu(player: Player, private val town: Town, back: Menu) :
    Menu(player, player.tr("town-status.title", "town" to TownyUtil.name(town.name)), 6, back) {

    override fun build() {
        if (!town.exists()) {
            button(22, Icons.icon(Material.BARRIER, tr("town-info.gone")))
            return backButton(49)
        }
        button(4, Icons.town(player, town))

        val info = layout(19, 20, 21, 22, 23, 24, 25)
        info.add(level())
        info.add(claims())
        if (TownySettings.isAllowingOutposts()) {
            info.add(
                Icons.icon(
                    Material.COMPASS, tr("town-status.outposts"), tr("town-status.outposts-description"),
                    tr("claims.outposts", "outposts" to town.maxOutpostSpawn, "limit" to town.outpostLimit)
                )
            )
        }
        if (TownyUtil.economy) info.add(upkeep())
        if (town.hasNation()) {
            info.add(
                Icons.icon(
                    Material.BEACON, tr("town-status.nation-zone"), tr("town-status.nation-zone-description"),
                    tr("town-status.nation-zone-size", "size" to town.nationZoneSize),
                    tr("icon.currently", "value" to TownyUtil.onOff(player, town.isNationZoneEnabled))
                )
            )
        }

        val problems = warnings(this, town)
        if (problems.isEmpty()) {
            button(
                31,
                Icons.icon(Material.LIME_DYE, tr("town-status.all-good"), tr("town-status.all-good-description"))
            )
        } else {
            val row = layout(28, 29, 30, 31, 32, 33, 34)
            problems.forEach { row.add(it.icon) }
        }

        tutorialButton(53, Tutorial.TOWN)
        backButton(49)
    }

    private fun level(): ItemStack {
        val number = TownySettings.getTownLevelNumber(town)
        val thresholds = TownySettings.getConfigTownLevel().keys.sorted()
        val byClaims = TownySettings.isTownLevelDeterminedByTownBlockCount()
        val progress = if (byClaims) town.numTownBlocks else town.numResidents
        val next = thresholds.getOrNull(number + 1)
        return Icons.icon(
            Material.EXPERIENCE_BOTTLE, tr("town-status.level"), tr("town-status.level-description"), *buildList {
                add(tr("town-status.level-number", "level" to number, "max" to TownySettings.getTownLevelMax()))
                add(tr("town-status.level-name", "name" to TownyUtil.name(town.formattedName)))
                add(
                    when {
                        town.manualTownLevel > -1 -> tr("town-status.level-manual")
                        next == null -> tr("town-status.level-highest")
                        byClaims -> tr("town-status.next-level-claims", "current" to progress, "needed" to next)
                        else -> tr("town-status.next-level-residents", "current" to progress, "needed" to next)
                    }
                )
            }.toTypedArray()
        )
    }

    private fun claims(): ItemStack = Icons.icon(
        Material.GRASS_BLOCK, tr("claims.info"), tr("town-status.claims-description"), *buildList {
            add(tr("claims.claimed", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString))
            add(
                tr(
                    "claims.available",
                    "available" to if (town.hasUnlimitedClaims()) tr("claims.unlimited") else town.availableTownBlocks()
                )
            )
            TownySettings.getTownBlockRatio().takeIf { it > 0 }
                ?.let { add(tr("tutorial.fact.claims-per-resident", "count" to it)) }
            add(tr("claims.bonus", "bonus" to town.bonusBlocks, "purchased" to town.purchasedBlocks))
            if (TownyUtil.economy) {
                add(tr("town-status.max-purchased", "count" to TownySettings.getMaxPurchasedBlocks(town)))
            }
        }.toTypedArray()
    )

    private fun upkeep(): ItemStack {
        val upkeep = if (town.hasUpkeep()) TownySettings.getTownUpkeepCost(town) else 0.0
        val penalty = if (town.isOverClaimed) TownySettings.getTownPenaltyUpkeepCost(town) else 0.0
        val peaceful = if (town.isNeutral) TownySettings.getTownNeutralityCost(town) else 0.0
        return Icons.icon(
            Material.HOPPER, tr("town-status.upkeep"), tr("town-status.upkeep-description"), *buildList {
                if (!TownySettings.isTaxingDaily()) add(
                    tr(
                        "tutorial.fact.daily-taxes",
                        "value" to TownyUtil.onOff(player, false)
                    )
                )
                if (!town.hasUpkeep()) add(tr("town-status.upkeep-exempt"))
                add(tr("town-status.upkeep-daily", "cost" to TownyUtil.money(upkeep)))
                if (penalty > 0) add(tr("town-status.upkeep-penalty", "cost" to TownyUtil.money(penalty)))
                if (peaceful > 0) add(tr("town-status.upkeep-peaceful", "cost" to TownyUtil.money(peaceful)))
                add(tr("town-status.upkeep-total", "cost" to TownyUtil.money(upkeep + penalty + peaceful)))
                add(tr("bank.balance", "balance" to TownyUtil.balance(town)))
                add(tr("town-status.new-day", "time" to TownyUtil.duration(player, TownyUtil.secondsUntilNewDay())))
            }.toTypedArray()
        )
    }

    /** One problem with a town, as a short [line] for summaries and a detailed [icon]. */
    class Warning(val line: String, val icon: ItemStack)

    companion object {
        /** Every problem Towny would act on for [town]: ruins, debt, conquest, overclaiming, and unpaid upkeep. */
        fun warnings(menu: Menu, town: Town): List<Warning> = buildList {
            fun warn(material: Material, title: String, vararg lines: String) =
                add(Warning(title, Icons.icon(material, title, null, *lines)))

            if (town.isRuined) {
                val left = TownySettings.getTownRuinsMaxDurationHours() - TownRuinUtil.getTimeSinceRuining(town)
                warn(
                    Material.CRACKED_STONE_BRICKS, menu.tr("town-status.ruined"),
                    menu.tr("town-status.ruined-hours", "hours" to left.coerceAtLeast(0))
                )
            }
            if (TownyUtil.economy && town.isBankrupt) {
                warn(
                    Material.BARRIER, menu.tr("town-status.bankrupt"),
                    menu.tr("tutorial.fact.debt", "debt" to TownyUtil.money(town.debtBalance)),
                    menu.tr("town-status.debt-cap", "cap" to TownyUtil.money(town.account.debtCap)),
                    menu.tr("town-status.bankrupt-description")
                )
            }
            if (town.isConquered) {
                warn(
                    Material.IRON_SWORD, menu.tr("town-status.conquered"),
                    menu.tr("town-status.conquered-days", "days" to town.conqueredDays)
                )
            }
            if (town.isOverClaimed) {
                warn(
                    Material.TNT, menu.tr("town-status.overclaimed"),
                    menu.tr("claims.claimed", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString),
                    menu.tr(
                        if (TownySettings.isOverClaimingAllowingStolenLand()) "town-status.overclaimed-stealable"
                        else "town-status.overclaimed-description"
                    )
                )
            }
            if (TownyUtil.economy && TownySettings.isTaxingDaily() && !town.isRuined && !town.isBankrupt) {
                val due = (if (town.hasUpkeep()) TownySettings.getTownUpkeepCost(town) else 0.0) +
                        (if (town.isOverClaimed) TownySettings.getTownPenaltyUpkeepCost(town) else 0.0) +
                        (if (town.isNeutral) TownySettings.getTownNeutralityCost(town) else 0.0)
                if (due > town.account.cachedBalance) {
                    warn(
                        Material.HOPPER, menu.tr("town-status.upkeep-short"),
                        menu.tr("town-status.upkeep-total", "cost" to TownyUtil.money(due)),
                        menu.tr("bank.balance", "balance" to TownyUtil.balance(town)),
                        menu.tr("town-status.upkeep-short-description")
                    )
                }
            }
        }
    }
}

