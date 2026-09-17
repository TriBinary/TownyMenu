package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.Government
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Deposit, withdraw, and bank history for a town or nation bank. */
class BankMenu(
    player: Player,
    private val government: Government,
    back: Menu,
) : Menu(player, "Bank: ${TownyUtil.name(government.name)}", 3, back) {

    private val isNation = government is Nation
    private val command = if (isNation) "towny:nation" else "towny:town"

    override fun build() {
        button(4, Icons.icon(
            Material.GOLD_BLOCK, "<gold>${TownyUtil.name(government.name)} Bank", null,
            "<gray>Balance: <white>${TownyUtil.balance(government)}",
            "<gray>Daily tax: <white>${if (isNation) TownyUtil.money(government.taxes) else taxLabel()}",
        ))

        guarded(11, if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_DEPOSIT else PermissionNodes.TOWNY_COMMAND_TOWN_DEPOSIT,
            Icons.icon(Material.EMERALD, "<green>Deposit", "Move money from your balance into the bank.")) {
            prompt("Deposit", "Amount", "<gray>Bank balance: ${TownyUtil.balance(government)}") { amount ->
                run("$command deposit ${TownyUtil.argument(amount)}", ::balance)
            }
        }
        guarded(13, if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_WITHDRAW else PermissionNodes.TOWNY_COMMAND_TOWN_WITHDRAW,
            Icons.icon(Material.REDSTONE, "<red>Withdraw", "Take money out of the bank.")) {
            prompt("Withdraw", "Amount", "<gray>Bank balance: ${TownyUtil.balance(government)}") { amount ->
                run("$command withdraw ${TownyUtil.argument(amount)}", ::balance)
            }
        }
        guarded(15, if (isNation) PermissionNodes.TOWNY_COMMAND_NATION_BANKHISTORY else PermissionNodes.TOWNY_COMMAND_TOWN_BANKHISTORY,
            Icons.icon(Material.WRITTEN_BOOK, "<yellow>Bank History", "Open a book with recent transactions.")) {
            runAndClose("$command bankhistory")
        }
        backButton(22)
    }

    private fun balance(): Double = government.account.holdingBalance

    private fun taxLabel(): String {
        val town = government as Town
        return if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)
    }
}
