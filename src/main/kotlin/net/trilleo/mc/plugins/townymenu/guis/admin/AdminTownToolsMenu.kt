package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.common.RankMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** The rest of `/townyadmin town`: level, merging, sale, outlaws, trust, and ranks. */
class AdminTownToolsMenu(player: Player, private val town: Town, back: Menu) :
    Menu(player, player.tr("admin-town-tools.title", "town" to TownyUtil.name(town.name)), 6, back) {

    private val command: String
        get() = "towny:townyadmin town ${town.name}"

    override fun build() {
        if (!town.exists()) {
            button(22, Icons.icon(Material.BARRIER, tr("admin.gone")))
            return backButton(49)
        }
        button(4, Icons.town(player, town))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_SETTOWNLEVEL,
            Icons.icon(
                Material.EXPERIENCE_BOTTLE, tr("admin-town-tools.level"), tr("admin-town-tools.level-description"),
                tr("common.current", "value" to town.levelNumber)
            )
        ) {
            prompt(
                tr("admin-town-tools.level-title"),
                tr("admin-town-tools.level-label"),
                tr("admin-town-tools.level-hint"),
                maxLength = 5
            ) { level ->
                run("$command settownlevel ${TownyUtil.argument(level)}", { town.levelNumber })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_MERGE,
            Icons.icon(
                Material.STRUCTURE_VOID, tr("admin-town-tools.merge"),
                tr("admin-town-tools.merge-description", "town" to TownyUtil.name(town.name)),
                tr("admin-town-tools.merge-force")
            )
        ) { click ->
            val force = click.isRightClick
            Pickers.town(this, tr("admin-town-tools.merge-title"), { it != town }) { other ->
                run("$command ${if (force) "forcemerge" else "merge"} ${other.name}", { town.numResidents })
            }.open()
        }
        if (TownyUtil.economy) {
            if (town.isForSale) {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_NOTFORSALE,
                    Icons.icon(
                        Material.RED_BANNER, tr("admin-town-tools.not-for-sale"),
                        tr("admin-town-tools.not-for-sale-description"),
                        tr("common.price", "price" to TownyUtil.money(town.forSalePrice))
                    )
                ) {
                    run("$command notforsale", { town.isForSale })
                }
            } else {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_FORSALE,
                    Icons.icon(
                        Material.GREEN_BANNER, tr("admin-town-tools.for-sale"),
                        tr("admin-town-tools.for-sale-description")
                    )
                ) {
                    prompt(tr("admin-town-tools.for-sale-title"), tr("common.price-label"), initial = "0") { price ->
                        run("$command forsale ${TownyUtil.argument(price)}", { town.isForSale })
                    }
                }
            }
        }
        if (town.isConquered) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TOGGLE,
                Icons.icon(
                    Material.IRON_SWORD, tr("admin-town-tools.unconquer"),
                    tr("admin-town-tools.unconquer-description"),
                    tr("town-status.conquered-days", "days" to town.conqueredDays)
                )
            ) {
                run("$command toggle conquered off", { town.isConquered })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_CHECKOUTPOSTS,
            Icons.icon(
                Material.RECOVERY_COMPASS, tr("admin-server.check-outposts"),
                tr("admin-town-tools.check-outposts-description")
            )
        ) {
            runAndClose("$command checkoutposts")
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_RANK,
            Icons.icon(Material.NAME_TAG, tr("admin-town-tools.ranks"), tr("admin-town-tools.ranks-description"))
        ) {
            members(town.residents.toList()) { target, list ->
                RankMenu.create(player, target, nation = false, back = list, admin = town.name).open()
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_OUTLAW,
            Icons.icon(
                Material.IRON_BARS, tr("town.outlaws"), tr("admin-town-tools.outlaws-description"),
                tr("town.outlaws-count", "count" to town.outlaws.size)
            )
        ) {
            AdminNamesMenu(
                player, tr("outlaws.title"), this, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_OUTLAW,
                "$command outlaw", Material.PLAYER_HEAD, tr("outlaws.declare"), tr("common.player-name")
            ) { town.outlaws.map { it.name } }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TRUST,
            Icons.icon(
                Material.TRIPWIRE_HOOK, tr("common.trusted"), tr("admin-town-tools.trust-description"),
                tr("plot.trusted-count", "count" to town.trustedResidents.size)
            )
        ) {
            AdminNamesMenu(
                player, tr("common.trusted-title"), this, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TRUST,
                "$command trust", Material.PLAYER_HEAD, tr("common.trust-title"), tr("common.player-name")
            ) { town.trustedResidents.map { it.name } }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TRUSTTOWN,
            Icons.icon(
                Material.BELL, tr("admin-town-tools.trusted-towns"), tr("admin-town-tools.trusted-towns-description"),
                tr("nation.towns-count", "count" to town.trustedTowns.size)
            )
        ) {
            AdminNamesMenu(
                player, tr("admin-town-tools.trusted-towns"), this,
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TRUSTTOWN, "$command trusttown", Material.BELL,
                tr("admin-town-tools.trust-town-title"), tr("no-town.town-name")
            ) { town.trustedTowns.map { it.name } }.open()
        }

        backButton(49)
    }

    /** Picks one of [residents], including those offline, and opens [onPick] with the list as its back button. */
    private fun members(residents: List<Resident>, onPick: (Resident, Menu) -> Unit) {
        lateinit var list: Menu
        list = ListMenu(player, tr("admin-town-tools.ranks-title"), this) { menu ->
            residents.sortedBy { it.name.lowercase() }.map { resident ->
                MenuEntry({ Icons.resident(player, resident, "", tr("picker.select")) }) { onPick(resident, menu) }
            }
        }
        list.open()
    }
}
