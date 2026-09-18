package net.trilleo.mc.plugins.townymenu.guis

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.nation.NationInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentProfileMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The same rankings as Towny's `/towny top`, `/town baltop`, and `/nation baltop`, as clickable lists.
 * Server-wide lists hide towns an admin removed from top lists and show at most Towny's `towny_top_size` entries.
 */
class LeaderboardMenu(player: Player, back: Menu) : Menu(player, player.tr("leaderboard.title"), 6, back) {

    private val topSize: Int
        get() = TownySettings.getTownyTopSize()

    private val towns: List<Town>
        get() = TownyAPI.getInstance().towns.filter { it.isVisibleOnTopLists }

    private val nations: List<Nation>
        get() = TownyAPI.getInstance().nations.toList()

    override fun build() {
        button(4, Icons.icon(Material.LECTERN, tr("leaderboard.info"), tr("leaderboard.info-description")))

        val row = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_RESIDENTS,
            Icons.icon(Material.BELL, tr("leaderboard.town-residents"), tr("leaderboard.town-residents-description"))
        ) {
            townList(tr("leaderboard.town-residents")) { it.numResidents to tr("icon.town.residents", "count" to it.numResidents) }
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_RESIDENTS,
            Icons.icon(Material.BEACON, tr("leaderboard.nation-residents"), tr("leaderboard.nation-residents-description"))
        ) {
            nationList(tr("leaderboard.nation-residents")) { it.numResidents to tr("icon.town.residents", "count" to it.numResidents) }
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_LAND,
            Icons.icon(Material.GRASS_BLOCK, tr("leaderboard.town-land"), tr("leaderboard.town-land-description"))
        ) {
            townList(tr("leaderboard.town-land")) {
                it.numTownBlocks to tr("claims.claimed", "claims" to it.numTownBlocks, "max" to it.maxTownBlocksAsAString)
            }
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_LAND,
            Icons.icon(Material.OAK_SIGN, tr("leaderboard.resident-land"), tr("leaderboard.resident-land-description"))
        ) {
            residentList(
                tr("leaderboard.resident-land"),
                TownyAPI.getInstance().residents.filter { it.townBlocks.isNotEmpty() }
            ) { it.townBlocks.size.toDouble() to tr("profile.plots-owned", "count" to it.townBlocks.size) }
        }
        if (TownyUtil.economy) {
            row.add(
                PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_BALANCE,
                Icons.icon(Material.GOLD_INGOT, tr("leaderboard.town-balance"), tr("leaderboard.town-balance-description"))
            ) {
                townList(tr("leaderboard.town-balance")) { it.account.cachedBalance to tr("icon.bank", "balance" to TownyUtil.balance(it)) }
            }
            row.add(
                PermissionNodes.TOWNY_COMMAND_TOWNY_TOP_BALANCE,
                Icons.icon(Material.GOLD_BLOCK, tr("leaderboard.nation-balance"), tr("leaderboard.nation-balance-description"))
            ) {
                nationList(tr("leaderboard.nation-balance")) { it.account.cachedBalance to tr("icon.bank", "balance" to TownyUtil.balance(it)) }
            }
            val town = resident?.townOrNull
            if (town != null) {
                row.add(
                    Icons.icon(
                        Material.EMERALD, tr("leaderboard.town-baltop"),
                        tr("leaderboard.town-baltop-description", "town" to TownyUtil.name(town.name))
                    )
                ) {
                    residentList(tr("leaderboard.town-baltop"), town.residents, limit = false)
                }
            }
            val nation = resident?.nationOrNull
            if (nation != null) {
                row.add(
                    PermissionNodes.TOWNY_COMMAND_NATION_BALTOP,
                    Icons.icon(
                        Material.EMERALD_BLOCK, tr("leaderboard.nation-baltop"),
                        tr("leaderboard.nation-baltop-description", "nation" to TownyUtil.name(nation.name))
                    )
                ) {
                    residentList(tr("leaderboard.nation-baltop"), nation.residents, limit = false)
                }
            }
        }

        tutorialButton(53, Tutorial.GETTING_STARTED)
        backButton(49)
    }

    private fun rank(index: Int) = tr("leaderboard.rank", "rank" to index + 1)

    /** Opens [towns] ranked by the first value of [score], showing its second value as a lore line. */
    private fun <T : Comparable<T>> townList(title: String, score: (Town) -> Pair<T, String>) =
        ListMenu(player, title, this) { menu ->
            towns.map { it to score(it) }.sortedByDescending { it.second.first }.take(topSize)
                .mapIndexed { index, (town, value) ->
                    MenuEntry({ Icons.town(player, town, "", rank(index), value.second, tr("common.click-details")) }) {
                        TownInfoMenu(player, town, menu).open()
                    }
                }
        }.open()

    private fun <T : Comparable<T>> nationList(title: String, score: (Nation) -> Pair<T, String>) =
        ListMenu(player, title, this) { menu ->
            nations.map { it to score(it) }.sortedByDescending { it.second.first }.take(topSize)
                .mapIndexed { index, (nation, value) ->
                    MenuEntry({ Icons.nation(player, nation, "", rank(index), value.second, tr("common.click-details")) }) {
                        NationInfoMenu(player, nation, menu).open()
                    }
                }
        }.open()

    /**
     * Opens [residents] ranked by [score], by bank balance unless given. Town and nation baltops pass `limit = false`
     * to list every member, like Towny's, instead of stopping at the top-list size.
     */
    private fun residentList(
        title: String,
        residents: Collection<Resident>,
        limit: Boolean = true,
        score: (Resident) -> Pair<Double, String> = { resident ->
            val balance = resident.accountOrNull?.cachedBalance ?: 0.0
            balance to tr("bank.balance", "balance" to TownyUtil.money(balance))
        },
    ) = ListMenu(player, title, this) { menu ->
        residents.map { it to score(it) }.sortedByDescending { it.second.first }
            .let { if (limit) it.take(topSize) else it }
            .mapIndexed { index, (target, value) ->
                MenuEntry({ Icons.resident(player, target, "", rank(index), value.second, tr("common.click-view")) }) {
                    ResidentProfileMenu(player, target, menu).open()
                }
            }
    }.open()
}
