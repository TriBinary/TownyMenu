package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Every resident Towny knows, online players first, with a name filter. */
class AdminResidentListMenu(player: Player, back: Menu) : PagedMenu(player, player.tr("admin.residents-title"), back) {

    private var filter = ""

    override fun entries(): List<MenuEntry> =
        TownyAPI.getInstance().residents
            .filter { it.name.contains(filter, ignoreCase = true) }
            .sortedWith(compareBy({ !it.isOnline }, { it.name.lowercase() }))
            .map { resident ->
                MenuEntry({ Icons.resident(player, resident, "", tr("common.click-view")) }) {
                    AdminResidentMenu(player, resident, this).open()
                }
            }

    override fun controls() {
        button(47, Icons.icon(Material.COMPASS, tr("admin.search"), tr("admin.search-description"),
            tr("admin.filter", "filter" to if (filter.isEmpty()) tr("common.none") else TownyUtil.text(filter)))) { click ->
            if (click.isRightClick) {
                filter = ""
                render()
            } else {
                prompt(tr("admin.search"), tr("common.player-name"), initial = filter) { name ->
                    filter = name
                    open()
                }
            }
        }
    }
}
