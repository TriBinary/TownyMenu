package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Nation
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** A directory of every nation, sortable, doubling as the nation leaderboard. */
class NationListMenu(player: Player, back: Menu) : PagedMenu(player, player.tr("nation-list.title"), back) {

    private enum class Sort(val label: String, val comparator: Comparator<Nation>) {
        RESIDENTS("sort.residents", compareByDescending { it.numResidents }),
        TOWNS("sort.towns", compareByDescending { it.numTowns }),
        CLAIMS("sort.claims", compareByDescending { it.numTownblocks }),
        BALANCE("sort.balance", compareByDescending { it.account.cachedBalance }),
        OPEN("sort.open", compareByDescending<Nation> { it.isOpen }.thenByDescending { it.numResidents }),
        NAME("sort.name", compareBy { it.name.lowercase() }),
    }

    private var sort = Sort.RESIDENTS

    override fun entries(): List<MenuEntry> =
        TownyAPI.getInstance().nations
            .sortedWith(sort.comparator)
            .map { nation -> MenuEntry({ Icons.nation(player, nation, "", tr("common.click-details")) }) { NationInfoMenu(player, nation, this).open() } }

    override fun controls() {
        val sorts = Sort.entries.filter { it != Sort.BALANCE || TownyUtil.economy }
        button(47, itemStack(Material.HOPPER) {
            name(tr("sort.current", "sort" to tr(sort.label)))
            lore(sorts.map { (if (it == sort) "<green>▶ " else "<gray>  ") + tr(it.label) })
            lore("", tr("common.click-change"))
        }) {
            sort = sorts[(sorts.indexOf(sort) + 1) % sorts.size]
            render()
        }
    }
}
