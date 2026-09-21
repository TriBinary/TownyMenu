package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import com.palmergames.bukkit.towny.permissions.TownyPerms
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** The permission nodes of one `townyperms.yml` group, such as `towns.mayor` or `towns.ranks.assistant`. */
class AdminPermGroupMenu(player: Player, private val group: String, back: Menu) :
    PagedMenu(player, player.tr("admin-perms.group-title", "group" to TownyUtil.text(group)), back) {

    private val command = "towny:townyadmin townyperms group $group"

    private fun nodes(): List<String> = TownyPerms.getPermsOfGroup(group)

    override fun entries(): List<MenuEntry> = nodes().sorted().map { node ->
        MenuEntry({
            itemStack(Material.PAPER) {
                name("<white>${TownyUtil.text(node)}")
                loreActions(listOf(tr("admin-perms.click-remove")))
            }
        }) {
            run("$command removeperm $node", { nodes().size })
        }
    }

    override fun controls() {
        guarded(
            47, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(
                Material.NAME_TAG, tr("admin-perms.add-node"), tr("admin-perms.add-node-description"),
                actions = hints("click.type-node")
            )
        ) {
            prompt(tr("admin-perms.add-node"), tr("admin-perms.node-label"), maxLength = 128) { node ->
                run("$command addperm ${TownyUtil.argument(node)}", { nodes().size })
            }
        }
    }
}
