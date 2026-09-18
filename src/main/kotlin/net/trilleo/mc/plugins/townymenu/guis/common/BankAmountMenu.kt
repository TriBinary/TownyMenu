package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Government
import com.palmergames.bukkit.towny.`object`.Nation
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.floor

/**
 * Picks how much to move in or out of a bank: one of the server's preset amounts
 * (`bank-amounts` in `config.yml`), everything the source holds, or a typed figure.
 *
 * Presets the transaction would fail on — too little money, or outside Towny's own
 * minimum and maximum — are shown with the reason instead of a click hint, so a
 * button that cannot work is never offered.
 */
class BankAmountMenu(
    player: Player,
    private val government: Government,
    private val withdraw: Boolean,
    private val bank: Menu,
) : Menu(
    player,
    player.tr(
        if (withdraw) "bank.withdraw-menu-title" else "bank.deposit-menu-title",
        "name" to TownyUtil.name(government.name)
    ),
    4,
    bank,
) {

    private val isNation = government is Nation
    private val command = if (isNation) "towny:nation" else "towny:town"

    /**
     * The balance the money comes out of: the bank when withdrawing, the player's own when depositing.
     * Read once per render, because every amount is measured against it and the economy plugin answers each query.
     */
    private var available = 0.0

    override fun build() {
        val own = resident?.account?.holdingBalance ?: 0.0
        available = if (withdraw) balance() else own

        button(
            4, Icons.icon(
                if (withdraw) Material.REDSTONE else Material.EMERALD,
                tr(if (withdraw) "bank.withdraw" else "bank.deposit"),
                tr(if (withdraw) "bank.withdraw-description" else "bank.deposit-description"),
                *infoLines(own).toTypedArray()
            )
        )

        val amounts = Main.instance.pluginConfig.bankAmounts
        val row = layout(*centered(2, amounts.size + 2))
        amounts.forEach { amount ->
            add(row, Material.GOLD_NUGGET, tr("bank.amount", "amount" to TownyUtil.money(amount.toDouble())), amount)
        }
        val everything = floor(available).toInt()
        add(
            row, Material.GOLD_BLOCK, tr("bank.all"), everything, "all",
            tr("bank.amount-line", "amount" to TownyUtil.money(everything.toDouble()))
        )
        row.add(
            Icons.icon(
                Material.NAME_TAG, tr("bank.custom"), tr("bank.custom-description"),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            prompt(
                tr(if (withdraw) "bank.withdraw-title" else "bank.deposit-title"),
                tr("common.amount"),
                tr("bank.prompt-balance", "balance" to TownyUtil.balance(government))
            ) { amount ->
                transact(TownyUtil.argument(amount))
            }
        }

        backButton(31)
    }

    /** Adds an amount button, or, when Towny would refuse [amount], an unclickable icon saying why. */
    private fun add(
        row: Layout,
        material: Material,
        name: String,
        amount: Int,
        argument: String = "$amount",
        line: String? = null,
    ) {
        val problem = problem(amount)
        val icon = Icons.icon(
            material, name, null,
            *listOfNotNull(line, problem).toTypedArray(),
            actions = if (problem == null) listOf(tr(if (withdraw) "bank.click-withdraw" else "bank.click-deposit"))
            else emptyList()
        )
        if (problem == null) row.add(icon) { transact(argument) } else row.add(icon)
    }

    /** Why Towny would refuse [amount], or `null` when it would go through. */
    private fun problem(amount: Int): String? = when {
        amount <= 0 -> tr("bank.nothing-to-move")
        amount > available -> tr("bank.not-enough")
        amount < minimum -> tr("bank.below-minimum", "amount" to TownyUtil.money(minimum.toDouble()))
        maximum > -1 && amount > maximum -> tr("bank.above-maximum", "amount" to TownyUtil.money(maximum.toDouble()))
        else -> null
    }

    private fun transact(argument: String) {
        run("$command ${if (withdraw) "withdraw" else "deposit"} $argument", ::balance, returnTo = bank)
    }

    private fun infoLines(own: Double): List<String> = buildList {
        add(tr("bank.balance", "balance" to TownyUtil.balance(government)))
        add(tr("bank.your-balance", "balance" to TownyUtil.money(own)))
        if (minimum > 0) add(tr("bank.minimum", "amount" to TownyUtil.money(minimum.toDouble())))
        if (maximum > -1) add(tr("bank.maximum", "amount" to TownyUtil.money(maximum.toDouble())))
    }

    private val minimum: Int
        get() = when {
            isNation && withdraw -> TownySettings.getNationMinWithdraw()
            isNation -> TownySettings.getNationMinDeposit()
            withdraw -> TownySettings.getTownMinWithdraw()
            else -> TownySettings.getTownMinDeposit()
        }

    private val maximum: Int
        get() = when {
            isNation && withdraw -> TownySettings.getNationMaxWithdraw()
            isNation -> TownySettings.getNationMaxDeposit()
            withdraw -> TownySettings.getTownMaxWithdraw()
            else -> TownySettings.getTownMaxDeposit()
        }

    private fun balance(): Double = government.account.holdingBalance

    /** Up to nine slots in [row], centred, so a short list of presets sits under the icon above it. */
    private fun centered(row: Int, count: Int): IntArray {
        val size = count.coerceIn(1, 9)
        val start = row * 9 + (9 - size) / 2
        return IntArray(size) { start + it }
    }
}
