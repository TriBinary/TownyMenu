package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Nation
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player

/** A directory of every nation, sortable, doubling as the nation leaderboard. */
class NationListMenu(player: Player, back: Menu) : PagedMenu(player, "Nations", back) {

    private enum class Sort(val label: String, val comparator: Comparator<Nation>) {
        RESIDENTS("Residents", compareByDescending { it.numResidents }),
        TOWNS("Towns", compareByDescending { it.numTowns }),
        CLAIMS("Claims", compareByDescending { it.numTownblocks }),
        BALANCE("Bank balance", compareByDescending { it.account.cachedBalance }),
        OPEN("Open to join", compareByDescending<Nation> { it.isOpen }.thenByDescending { it.numResidents }),
        NAME("Name", compareBy { it.name.lowercase() }),
    }

    private var sort = Sort.RESIDENTS

    override fun entries(): List<MenuEntry> =
        TownyAPI.getInstance().nations
            .sortedWith(sort.comparator)
            .map { nation -> MenuEntry({ Icons.nation(nation, "", "<yellow>Click for details") }) { NationInfoMenu(player, nation, this).open() } }

    override fun controls() {
        val sorts = Sort.entries.filter { it != Sort.BALANCE || TownyUtil.economy }
        button(47, itemStack(Material.HOPPER) {
            name("<yellow>Sort: <white>${sort.label}")
            lore(sorts.map { (if (it == sort) "<green>▶ " else "<gray>  ") + it.label })
            lore("", "<yellow>Click to change")
        }) {
            sort = sorts[(sorts.indexOf(sort) + 1) % sorts.size]
            render()
        }
    }
}
