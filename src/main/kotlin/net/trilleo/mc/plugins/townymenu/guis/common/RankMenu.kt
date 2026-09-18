package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
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

    /**
     * @param admin the town or nation name to change ranks in as an admin, instead of as one of its members
     */
    fun create(player: Player, target: Resident, nation: Boolean, back: Menu, admin: String? = null): Menu {
        val scope = if (nation) "nation" else "town"
        val command =
            if (admin == null) "towny:$scope rank" else "towny:townyadmin $scope $admin rank"
        val adminNode =
            if (nation) PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_RANK
            else PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_RANK
        val title =
            player.tr(if (nation) "rank.nation-title" else "rank.town-title", "name" to TownyUtil.name(target.name))
        return ListMenu(player, title, back) { menu ->
            val ranks = if (nation) TownyPerms.getNationRanks() else TownyPerms.getTownRanks()
            ranks.map { rank ->
                val held = if (nation) target.hasNationRank(rank) else target.hasTownRank(rank)
                val allowed =
                    if (admin == null) TownyUtil.can(player, "towny.command.$scope.rank.$rank")
                    else TownyUtil.can(player, adminNode)
                val icon = {
                    itemStack(if (held) Material.NAME_TAG else Material.PAPER) {
                        name("${if (held) "<green>" else "<gray>"}${TownyUtil.name(rank)}")
                        lore(menu.tr(if (held) "rank.assigned" else "rank.not-assigned"))
                        loreActions(
                            listOf(
                                menu.tr(
                                    if (!allowed) "rank.not-allowed"
                                    else if (held) "rank.click-remove" else "rank.click-assign"
                                )
                            )
                        )
                        glow(held)
                    }
                }
                MenuEntry(icon) {
                    if (allowed) menu.run(
                        "$command ${if (held) "remove" else "add"} ${target.name} $rank",
                        { if (nation) target.nationRanks.toList() else target.townRanks.toList() },
                    )
                }
            }
        }
    }
}
