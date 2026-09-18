package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.Government
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Deposit, withdraw, and bank history for a town or nation bank. */
class BankMenu(
    player: Player,
    private val government: Government,
    back: Menu,
) : Menu(player, player.tr("bank.title", "name" to TownyUtil.name(government.name)), 4, back) {

    private val isNation = government is Nation
    private val command = if (isNation) "towny:nation" else "towny:town"

    override fun build() {
        button(
            4, Icons.icon(
                Material.GOLD_BLOCK, tr("bank.info", "name" to TownyUtil.name(government.name)), null,
                tr("bank.balance", "balance" to TownyUtil.balance(government)),
                tr("bank.daily-tax", "tax" to if (isNation) TownyUtil.money(government.taxes) else taxLabel()),
            )
        )

        guarded(
            11,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_DEPOSIT else PermissionNodes.TOWNY_COMMAND_TOWN_DEPOSIT,
            Icons.icon(Material.EMERALD, tr("bank.deposit"), tr("bank.deposit-description"))
        ) {
            prompt(
                tr("bank.deposit-title"),
                tr("common.amount"),
                tr("bank.prompt-balance", "balance" to TownyUtil.balance(government))
            ) { amount ->
                run("$command deposit ${TownyUtil.argument(amount)}", ::balance)
            }
        }
        guarded(
            13,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_WITHDRAW else PermissionNodes.TOWNY_COMMAND_TOWN_WITHDRAW,
            Icons.icon(Material.REDSTONE, tr("bank.withdraw"), tr("bank.withdraw-description"))
        ) {
            prompt(
                tr("bank.withdraw-title"),
                tr("common.amount"),
                tr("bank.prompt-balance", "balance" to TownyUtil.balance(government))
            ) { amount ->
                run("$command withdraw ${TownyUtil.argument(amount)}", ::balance)
            }
        }
        guarded(
            15,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_BANKHISTORY else PermissionNodes.TOWNY_COMMAND_TOWN_BANKHISTORY,
            Icons.icon(Material.WRITTEN_BOOK, tr("bank.history"), tr("bank.history-description"))
        ) {
            runAndClose("$command bankhistory")
        }
        tutorialButton(35, Tutorial.ECONOMY)
        backButton(31)
    }

    private fun balance(): Double = government.account.holdingBalance

    private fun taxLabel(): String {
        val town = government as Town
        return if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)
    }
}
