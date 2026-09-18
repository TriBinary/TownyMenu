package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import com.palmergames.bukkit.towny.permissions.TownyPerms
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Towny's `townyperms.yml` (`/townyadmin townyperms`): the permission groups every resident, mayor, and nation
 * leader gets, and the town and nation ranks a mayor or leader may hand out.
 */
class AdminPermsMenu(player: Player, back: Menu) : Menu(player, player.tr("admin-perms.title"), 6, back) {

    override fun build() {
        button(4, Icons.icon(Material.IRON_DOOR, tr("admin-perms.info"), tr("admin-perms.info-description")))

        val grid = layout(20, 21, 22, 23, 24)
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(
                Material.BOOKSHELF, tr("admin-perms.groups"), tr("admin-perms.groups-description"),
                tr("admin-perms.groups-count", "count" to groups().size)
            )
        ) {
            groupList().open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(
                Material.NAME_TAG, tr("admin-perms.town-ranks"), tr("admin-perms.town-ranks-description"),
                tr("admin-perms.ranks-count", "count" to TownyPerms.getTownRanks().size)
            )
        ) {
            AdminRanksMenu(player, nation = false, back = this).open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(
                Material.BEACON, tr("admin-perms.nation-ranks"), tr("admin-perms.nation-ranks-description"),
                tr("admin-perms.ranks-count", "count" to TownyPerms.getNationRanks().size)
            )
        ) {
            AdminRanksMenu(player, nation = true, back = this).open()
        }

        backButton(49)
    }

    /** Groups that actually hold permissions; `townyperms.yml` also has empty parent sections such as `towns`. */
    private fun groups(): List<String> =
        TownyPerms.getGroupList().filter { TownyPerms.getPermsOfGroup(it).isNotEmpty() }.sorted()

    private fun groupList(): Menu = ListMenu(player, tr("admin-perms.groups"), this) { menu ->
        groups().map { group ->
            MenuEntry({
                itemStack(Material.BOOKSHELF) {
                    name("<gold>${TownyUtil.text(group)}")
                    lore(tr("admin-perms.node-count", "count" to TownyPerms.getPermsOfGroup(group).size))
                    loreActions(listOf(tr("common.click-details")))
                }
            }) { AdminPermGroupMenu(player, group, menu).open() }
        }
    }
}
