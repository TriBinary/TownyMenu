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
) : Menu(player, player.tr("bank.title", "name" to TownyUtil.name(government.name)), 5, back) {

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
            20,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_DEPOSIT else PermissionNodes.TOWNY_COMMAND_TOWN_DEPOSIT,
            Icons.icon(
                Material.EMERALD, tr("bank.deposit"), tr("bank.deposit-description"),
                actions = listOf(tr("bank.click-choose"))
            )
        ) {
            BankAmountMenu(player, government, withdraw = false, bank = this).open()
        }
        guarded(
            22,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_WITHDRAW else PermissionNodes.TOWNY_COMMAND_TOWN_WITHDRAW,
            Icons.icon(
                Material.REDSTONE, tr("bank.withdraw"), tr("bank.withdraw-description"),
                actions = listOf(tr("bank.click-choose"))
            )
        ) {
            BankAmountMenu(player, government, withdraw = true, bank = this).open()
        }
        guarded(
            24,
            if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_BANKHISTORY else PermissionNodes.TOWNY_COMMAND_TOWN_BANKHISTORY,
            Icons.icon(
                Material.WRITTEN_BOOK, tr("bank.history"), tr("bank.history-description"),
                actions = listOf(tr("click.view"))
            )
        ) {
            runAndClose("$command bankhistory")
        }
        tutorialButton(44, Tutorial.ECONOMY)
        backButton(40)
    }

    private fun taxLabel(): String {
        val town = government as Town
        return if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)
    }
}
