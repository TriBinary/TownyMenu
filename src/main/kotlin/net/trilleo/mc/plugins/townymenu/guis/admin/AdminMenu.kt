package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyUniverse
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * The server admin hub, opened by `/townymenu admin` or the main menu. Only players with
 * [PERMISSION] may use it; each action still checks the Towny node of the command it runs.
 */
class AdminMenu(player: Player, back: Menu?) : Menu(player, player.tr("admin.title"), 4, back) {

    override fun build() {
        if (!player.hasPermission(PERMISSION)) {
            button(13, Icons.icon(Material.BARRIER, tr("menu.no-permission")))
            return backButton(31)
        }

        button(10, Icons.icon(Material.COMPARATOR, tr("admin.config"), tr("admin.config-description"))) {
            TownyConfigMenu(player, "", this).open()
        }
        button(
            12, Icons.icon(
                Material.GRASS_BLOCK, tr("admin.worlds"), tr("admin.worlds-description"),
                tr("admin.worlds-count", "count" to TownyUniverse.getInstance().townyWorlds.size)
            )
        ) {
            worlds().open()
        }
        button(14, Icons.icon(Material.COMMAND_BLOCK, tr("admin.server"), tr("admin.server-description"))) {
            AdminServerMenu(player, this).open()
        }
        button(16, Icons.icon(Material.WRITABLE_BOOK, tr("admin.plugin"), tr("admin.plugin-description"))) {
            PluginSettingsMenu(player, this).open()
        }

        val towny = TownyAPI.getInstance()
        button(
            20, Icons.icon(
                Material.BELL, tr("admin.towns"), tr("admin.towns-description"),
                tr("admin.count", "count" to towny.towns.size)
            )
        ) {
            lateinit var list: Menu
            list = Pickers.town(this, tr("admin.towns-title"), { true }) { town ->
                AdminTownMenu(
                    player,
                    town,
                    list
                ).open()
            }
            list.open()
        }
        button(
            22, Icons.icon(
                Material.BEACON, tr("admin.nations"), tr("admin.nations-description"),
                tr("admin.count", "count" to towny.nations.size)
            )
        ) {
            lateinit var list: Menu
            list = Pickers.nation(this, tr("admin.nations-title"), { true }) { nation ->
                AdminNationMenu(
                    player,
                    nation,
                    list
                ).open()
            }
            list.open()
        }
        button(
            24, Icons.icon(
                Material.PLAYER_HEAD, tr("admin.residents"), tr("admin.residents-description"),
                tr("admin.count", "count" to towny.residents.size)
            )
        ) {
            AdminResidentListMenu(player, this).open()
        }

        backButton(31)
    }

    private fun worlds(): Menu = ListMenu(player, tr("admin-world.list-title"), this) { list ->
        TownyUniverse.getInstance().townyWorlds.sortedBy { it.name.lowercase() }.map { world ->
            val icon = {
                itemStack(
                    when (world.bukkitWorld?.environment) {
                        World.Environment.NETHER -> Material.NETHERRACK
                        World.Environment.THE_END -> Material.END_STONE
                        else -> Material.GRASS_BLOCK
                    }
                ) {
                    name("<green>${TownyUtil.text(world.name)}")
                    lore(
                        tr("admin-world.using-towny", "value" to TownyUtil.yesNo(player, world.isUsingTowny)),
                        tr("admin-world.towns", "count" to world.townsInWorld.size),
                        "",
                        tr("common.click-view"),
                    )
                    glow(world.isUsingTowny)
                }
            }
            MenuEntry(icon) { AdminWorldMenu(player, world, list).open() }
        }
    }

    companion object {
        const val PERMISSION = "townymenu.admin"
    }
}
