package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** A nation's level, the bonuses it gives member towns, and its daily upkeep. */
class NationStatusMenu(player: Player, private val nation: Nation, back: Menu) :
    Menu(player, player.tr("nation-status.title", "nation" to TownyUtil.name(nation.name)), 6, back) {

    override fun build() {
        if (!nation.exists()) {
            button(22, Icons.icon(Material.BARRIER, tr("nation-info.gone")))
            return backButton(49)
        }
        button(4, Icons.nation(player, nation))

        val row = layout(20, 21, 22, 23, 24)
        row.add(level())
        row.add(bonuses())
        if (TownyUtil.economy) row.add(upkeep())

        tutorialButton(53, Tutorial.NATIONS)
        backButton(49)
    }

    private fun level(): ItemStack {
        val number = TownySettings.getNationLevelNumber(nation)
        val thresholds = TownySettings.getConfigNationLevel().keys.sorted()
        val byTowns = TownySettings.isNationLevelDeterminedByTownCount()
        val progress = if (byTowns) nation.numTowns else nation.numResidents
        val next = thresholds.getOrNull(number + 1)
        return Icons.icon(
            Material.EXPERIENCE_BOTTLE, tr("nation-status.level"), tr("nation-status.level-description"),
            tr("town-status.level-number", "level" to number, "max" to TownySettings.getNationLevelMax()),
            tr("town-status.level-name", "name" to TownyUtil.name(nation.formattedName)),
            when {
                nation.manualNationLevel > -1 -> tr("town-status.level-manual")
                next == null -> tr("town-status.level-highest")
                byTowns -> tr("nation-status.next-level-towns", "current" to progress, "needed" to next)
                else -> tr("town-status.next-level-residents", "current" to progress, "needed" to next)
            }
        )
    }

    private fun bonuses(): ItemStack {
        val level = TownySettings.getNationLevel(nation)
        return Icons.icon(
            Material.GOLDEN_HELMET, tr("nation-status.bonuses"), tr("nation-status.bonuses-description"), *buildList {
                add(tr("nation-status.bonus-claims", "count" to level.townBlockLimitBonus()))
                add(tr("town-status.nation-zone-size", "size" to level.nationZonesSize()))
                if (TownySettings.isAllowingOutposts()) {
                    add(tr("nation-status.bonus-outposts", "count" to level.nationBonusOutpostLimit()))
                    add(tr("nation-status.bonus-capital-outposts", "count" to level.nationCapitalBonusOutpostLimit()))
                }
            }.toTypedArray()
        )
    }

    private fun upkeep(): ItemStack {
        val upkeep = TownySettings.getNationUpkeepCost(nation)
        val peaceful = if (nation.isNeutral) TownySettings.getNationNeutralityCost(nation) else 0.0
        val total = upkeep + peaceful
        return Icons.icon(
            Material.HOPPER, tr("town-status.upkeep"), tr("nation-status.upkeep-description"), *buildList {
                if (!TownySettings.isTaxingDaily()) add(tr("tutorial.fact.daily-taxes", "value" to TownyUtil.onOff(player, false)))
                add(tr("town-status.upkeep-daily", "cost" to TownyUtil.money(upkeep)))
                if (peaceful > 0) add(tr("town-status.upkeep-peaceful", "cost" to TownyUtil.money(peaceful)))
                add(tr("town-status.upkeep-total", "cost" to TownyUtil.money(total)))
                add(tr("bank.balance", "balance" to TownyUtil.balance(nation)))
                if (TownySettings.isTaxingDaily() && total > nation.account.cachedBalance) add(tr("nation-status.upkeep-short"))
                add(tr("town-status.new-day", "time" to TownyUtil.duration(player, TownyUtil.secondsUntilNewDay())))
            }.toTypedArray()
        )
    }
}
