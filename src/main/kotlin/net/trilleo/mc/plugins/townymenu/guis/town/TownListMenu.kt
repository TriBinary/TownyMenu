package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player

/** A directory of every town, sortable, doubling as the town leaderboard. */
class TownListMenu(player: Player, back: Menu) : PagedMenu(player, "Towns", back) {

    private enum class Sort(val label: String, val comparator: Comparator<Town>) {
        RESIDENTS("Residents", compareByDescending { it.numResidents }),
        CLAIMS("Claims", compareByDescending { it.numTownBlocks }),
        BALANCE("Bank balance", compareByDescending { it.account.cachedBalance }),
        ONLINE("Online players", compareByDescending { town -> town.residents.count { it.isOnline } }),
        OPEN("Open to join", compareByDescending<Town> { it.isOpen }.thenByDescending { it.numResidents }),
        NAME("Name", compareBy { it.name.lowercase() }),
    }

    private var sort = Sort.RESIDENTS

    override fun entries(): List<MenuEntry> =
        TownyAPI.getInstance().towns
            .sortedWith(sort.comparator)
            .map { town -> MenuEntry({ Icons.town(town, "", "<yellow>Click for details") }) { TownInfoMenu(player, town, this).open() } }

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
