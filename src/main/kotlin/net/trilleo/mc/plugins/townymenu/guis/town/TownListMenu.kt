package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** A directory of every town, sortable, doubling as the town leaderboard. */
class TownListMenu(player: Player, back: Menu) : PagedMenu(player, player.tr("town-list.title"), back) {

    private enum class Sort(val label: String, val comparator: (Player) -> Comparator<Town>) {
        RESIDENTS("sort.residents", { compareByDescending { it.numResidents } }),
        CLAIMS("sort.claims", { compareByDescending { it.numTownBlocks } }),
        BALANCE("sort.balance", { compareByDescending { it.account.cachedBalance } }),
        ONLINE("sort.online", { compareByDescending { town -> town.residents.count { it.isOnline } } }),
        OPEN("sort.open", { compareByDescending<Town> { it.isOpen }.thenByDescending { it.numResidents } }),
        NEARBY("sort.nearby", { player -> compareBy { distance(player, it) ?: Double.MAX_VALUE } }),
        NAME("sort.name", { compareBy { it.name.lowercase() } }),
    }

    private var sort = Sort.RESIDENTS

    override fun entries(): List<MenuEntry> =
        TownyAPI.getInstance().towns
            .sortedWith(sort.comparator(player))
            .map { town ->
                val lines = listOfNotNull(
                    distance(player, town)?.takeIf { sort == Sort.NEARBY }
                        ?.let { tr("town-list.distance", "blocks" to it.toLong()) },
                )
                MenuEntry({
                    Icons.town(player, town, *lines.toTypedArray(), actions = listOf(tr("common.click-details")))
                }) {
                    TownInfoMenu(player, town, this).open()
                }
            }

    override fun controls() {
        tutorialButton(52, Tutorial.FINDING_A_TOWN)
        val sorts = Sort.entries.filter {
            (it != Sort.BALANCE || TownyUtil.economy) &&
                    (it != Sort.NEARBY || TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_NEARBY))
        }
        button(47, itemStack(Material.HOPPER) {
            name(tr("sort.current", "sort" to tr(sort.label)))
            lore(sorts.map { (if (it == sort) "<green>▶ " else "<gray>  ") + tr(it.label) })
            loreActions(listOf(tr("common.click-change")))
        }) {
            sort = sorts[(sorts.indexOf(sort) + 1) % sorts.size]
            render()
        }
    }

    private companion object {
        /** Blocks between [player] and [town]'s spawn, or `null` when the town has no spawn in this world. */
        fun distance(player: Player, town: Town): Double? = town.spawnOrNull
            ?.takeIf { it.world == player.world }
            ?.distance(player.location)
    }
}
