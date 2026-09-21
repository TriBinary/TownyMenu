package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.jail.Jail
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Chooses the sentence for jailing [target] — hours, bail, jail, and cell — then
 * runs `/town jail`. The choices live in this menu until the player confirms.
 */
class JailSentenceMenu(player: Player, private val town: Town, private val target: Resident, back: Menu) :
    Menu(player, player.tr("jail-sentence.title", "player" to TownyUtil.name(target.name)), 5, back) {

    private val jails: List<Jail>
        get() = TownJailMenu.jails(town)

    private var hours = "2"
    private var bail = TownySettings.getBailAmount().toBigDecimal().stripTrailingZeros().toPlainString()
    private var jailNumber = town.primaryJail?.let { jails.indexOf(it) + 1 }?.takeIf { it > 0 } ?: 1

    /** Towny's cell index starts at 0; it is shown to players starting at 1. */
    private var cell = 0

    private val bailEnabled: Boolean
        get() = TownySettings.isAllowingBail() && TownyUtil.economy

    override fun build() {
        button(4, Icons.resident(player, target))
        val jail = jails.elementAtOrNull(jailNumber - 1)
        if (jail == null) {
            button(22, Icons.icon(Material.BARRIER, tr("town-jail.no-jails")))
            return backButton(40)
        }
        if (cell >= jail.jailCellCount) cell = 0

        val row = layout(20, 21, 22, 23, 24)
        row.add(
            Icons.icon(
                Material.CLOCK, tr("jail-sentence.hours"), tr("jail-sentence.hours-description"),
                tr("common.current", "value" to hours),
                tr("jail-sentence.max-hours", "hours" to TownySettings.getJailedMaxHours()),
                actions = hints("click.set")
            )
        ) {
            prompt(tr("jail-sentence.hours-title"), tr("jail-sentence.hours-label"), initial = hours, maxLength = 5) {
                hours = TownyUtil.argument(it)
                open()
            }
        }
        if (bailEnabled) {
            row.add(
                Icons.icon(
                    Material.GOLD_NUGGET, tr("jail-sentence.bail"), tr("jail-sentence.bail-description"),
                    tr("common.current", "value" to bail),
                    tr("jail-sentence.max-bail", "bail" to TownyUtil.money(TownySettings.getBailMaxAmount())),
                    actions = hints("click.set")
                )
            ) {
                prompt(tr("jail-sentence.bail-title"), tr("common.amount"), initial = bail, maxLength = 12) {
                    bail = TownyUtil.argument(it)
                    open()
                }
            }
        }
        if (jails.size > 1) {
            row.add(
                Icons.icon(
                    Material.IRON_BARS, tr("jail-sentence.jail"), null,
                    tr("common.current", "value" to TownJailMenu.jailName(this, town, jail)),
                    actions = listOf(tr("click.change"))
                )
            ) {
                val options = jails.mapIndexed { index, option ->
                    (index + 1).toString() to "<white>${TownJailMenu.jailName(this, town, option)}"
                }
                Pickers.option(this, tr("jail-sentence.jail"), options, jailNumber.toString(), Material.IRON_BARS) {
                    jailNumber = it.toInt()
                    cell = 0
                    open()
                }.open()
            }
        }
        if (jail.jailCellCount > 1) {
            row.add(
                Icons.icon(
                    Material.IRON_CHAIN, tr("jail-sentence.cell"), null,
                    tr("common.current", "value" to cell + 1),
                    actions = listOf(tr("click.change"))
                )
            ) {
                val options = (0 until jail.jailCellCount).map {
                    it.toString() to tr("jail-sentence.cell-number", "number" to it + 1)
                }
                Pickers.option(this, tr("jail-sentence.cell"), options, cell.toString(), Material.IRON_CHAIN) {
                    cell = it.toInt()
                    open()
                }.open()
            }
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_JAIL,
            Icons.icon(
                Material.LIME_CONCRETE, tr("jail-sentence.confirm"), tr("jail-sentence.confirm-description"),
                actions = hints("click.jail")
            )
        ) {
            val bailArgument = if (bailEnabled) " $bail" else ""
            run(
                "towny:town jail ${target.name} $hours$bailArgument $jailNumber $cell",
                { target.isJailed },
                returnTo = back ?: this
            )
        }

        backButton(40)
    }
}
