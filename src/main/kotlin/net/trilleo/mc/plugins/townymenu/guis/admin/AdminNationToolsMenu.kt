package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.`object`.Nation
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

/** The rest of `/townyadmin nation`: level, merging, transfers, sanctions, ranks, and the town proximity check. */
class AdminNationToolsMenu(player: Player, private val nation: Nation, back: Menu) :
    Menu(player, player.tr("admin-nation-tools.title", "nation" to TownyUtil.name(nation.name)), 6, back) {

    private val command: String
        get() = "towny:townyadmin nation ${nation.name}"

    override fun build() {
        if (!nation.exists()) {
            button(22, Icons.icon(Material.BARRIER, tr("admin.gone")))
            return backButton(49)
        }
        button(4, Icons.nation(player, nation))

        val grid = layout(19, 20, 21, 22, 23, 24, 25)
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_SETNATIONLEVEL,
            Icons.icon(
                Material.EXPERIENCE_BOTTLE, tr("admin-nation-tools.level"),
                tr("admin-nation-tools.level-description"),
                tr("common.current", "value" to nation.levelNumber),
                actions = hints("click.set")
            )
        ) {
            prompt(
                tr("admin-nation-tools.level-title"),
                tr("admin-town-tools.level-label"),
                tr("admin-town-tools.level-hint"),
                maxLength = 5
            ) { level ->
                run("$command setnationlevel ${TownyUtil.argument(level)}", { nation.levelNumber })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_MERGE,
            Icons.icon(
                Material.STRUCTURE_VOID, tr("admin-nation-tools.merge"),
                tr("admin-nation-tools.merge-description", "nation" to TownyUtil.name(nation.name)),
                actions = hints("click.choose-nation", "admin-town-tools.merge-force")
            )
        ) { click ->
            val force = click.isRightClick
            Pickers.nation(this, tr("admin-nation-tools.merge-title"), { it != nation }) { other ->
                run("$command ${if (force) "forcemerge" else "merge"} ${other.name}", { nation.numTowns })
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_TRANSFER,
            Icons.icon(
                Material.BELL, tr("admin-nation-tools.transfer"),
                tr("admin-nation-tools.transfer-description", "nation" to TownyUtil.name(nation.name)),
                actions = hints("click.choose-town")
            )
        ) {
            Pickers.town(this, tr("admin-nation-tools.transfer-title"), { !nation.hasTown(it) }) { town ->
                run("$command transfer ${town.name}", { nation.numTowns })
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_RECHECK,
            Icons.icon(
                Material.RECOVERY_COMPASS, tr("admin-nation-tools.recheck"),
                tr("admin-nation-tools.recheck-description"),
                actions = hints("click.run")
            )
        ) {
            runAndClose("$command recheck")
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_SANCTIONTOWN,
            Icons.icon(
                Material.RED_BANNER, tr("nation-towns.sanctions"), tr("admin-nation-tools.sanctions-description"),
                tr("nation-towns.sanctions-count", "count" to nation.sanctionedTowns.size),
                actions = hints("click.view")
            )
        ) {
            AdminNamesMenu(
                player, tr("nation-sanctions.title", "nation" to TownyUtil.name(nation.name)), this,
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_SANCTIONTOWN, "$command sanctiontown", Material.BELL,
                tr("nation-sanctions.add-title"), tr("no-town.town-name")
            ) { nation.sanctionedTowns.map { it.name } }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_RANK,
            Icons.icon(
                Material.NAME_TAG, tr("admin-nation-tools.ranks"), tr("admin-nation-tools.ranks-description"),
                actions = hints("click.choose-player")
            )
        ) {
            lateinit var list: Menu
            list = ListMenu(player, tr("admin-town-tools.ranks-title"), this) { menu ->
                nation.residents.sortedBy { it.name.lowercase() }.map { resident ->
                    MenuEntry({ Icons.resident(player, resident, actions = listOf(tr("picker.select"))) }) {
                        RankMenu.create(player, resident, nation = true, back = menu, admin = nation.name).open()
                    }
                }
            }
            list.open()
        }

        backButton(49)
    }
}
