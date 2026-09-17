package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.TownyPerms
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Builds the menu that grants or revokes town or nation ranks for [target]. */
object RankMenu {

    fun create(player: Player, target: Resident, nation: Boolean, back: Menu): Menu {
        val scope = if (nation) "nation" else "town"
        val title =
            player.tr(if (nation) "rank.nation-title" else "rank.town-title", "name" to TownyUtil.name(target.name))
        return ListMenu(player, title, back) { menu ->
            val ranks = if (nation) TownyPerms.getNationRanks() else TownyPerms.getTownRanks()
            ranks.map { rank ->
                val held = if (nation) target.hasNationRank(rank) else target.hasTownRank(rank)
                val allowed = TownyUtil.can(player, "towny.command.$scope.rank.$rank")
                val icon = {
                    itemStack(if (held) Material.NAME_TAG else Material.PAPER) {
                        name("${if (held) "<green>" else "<gray>"}${TownyUtil.name(rank)}")
                        lore(
                            menu.tr(if (held) "rank.assigned" else "rank.not-assigned"),
                            menu.tr(if (!allowed) "rank.not-allowed" else if (held) "rank.click-remove" else "rank.click-assign"),
                        )
                        glow(held)
                    }
                }
                MenuEntry(icon) {
                    if (allowed) menu.run(
                        "towny:$scope rank ${if (held) "remove" else "add"} ${target.name} $rank",
                        { if (nation) target.nationRanks.toList() else target.townRanks.toList() },
                    )
                }
            }
        }
    }
}
