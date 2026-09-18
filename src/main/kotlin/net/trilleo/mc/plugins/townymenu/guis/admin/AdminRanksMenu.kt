package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import com.palmergames.bukkit.towny.permissions.TownyPerms
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The town or nation ranks a mayor or nation leader can hand out
 * (`/townyadmin townyperms townrank|nationrank`), and the permissions each one grants.
 */
class AdminRanksMenu(player: Player, private val nation: Boolean, back: Menu) : PagedMenu(
    player,
    player.tr(if (nation) "admin-perms.nation-ranks" else "admin-perms.town-ranks"),
    back,
) {

    private val command = "towny:townyadmin townyperms ${if (nation) "nationrank" else "townrank"}"

    private val group = if (nation) "nations.ranks" else "towns.ranks"

    private fun ranks(): List<String> = if (nation) TownyPerms.getNationRanks() else TownyPerms.getTownRanks()

    override fun entries(): List<MenuEntry> = ranks().sorted().map { rank ->
        val nodes =
            if (nation) TownyPerms.getNationRankPermissions(rank) else TownyPerms.getTownRankPermissions(rank)
        MenuEntry({
            itemStack(Material.NAME_TAG) {
                name("<gold>${TownyUtil.name(rank)}")
                lore(tr("admin-perms.node-count", "count" to nodes.size))
                loreActions(listOf(tr("common.left-details"), tr("admin-perms.right-remove")))
            }
        }) { click ->
            if (click.isRightClick) {
                run("$command removerank $rank", { ranks().size })
            } else {
                AdminPermGroupMenu(player, "$group.$rank", this).open()
            }
        }
    }

    override fun controls() {
        guarded(
            47, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(Material.PAPER, tr("admin-perms.add-rank"), tr("admin-perms.add-rank-description"))
        ) {
            prompt(tr("admin-perms.add-rank"), tr("admin-perms.rank-label")) { rank ->
                run("$command addrank ${TownyUtil.nameArgument(rank)}", { ranks().size })
            }
        }
        guarded(
            48, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWNYPERMS,
            Icons.icon(Material.WRITABLE_BOOK, tr("admin-perms.rename-rank"), tr("admin-perms.rename-rank-description"))
        ) {
            val options = ranks().sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, tr("admin-perms.rename-rank"), options, null, Material.NAME_TAG) { rank ->
                prompt(tr("admin-perms.rename-rank"), tr("common.new-name"), initial = rank) { renamed ->
                    run("$command renamerank $rank ${TownyUtil.nameArgument(renamed)}", { ranks().sorted() })
                }
            }.open()
        }
    }
}
