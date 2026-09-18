package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Towns a nation has sanctioned, which keeps their residents from using its nation spawn. */
class NationSanctionsMenu(player: Player, private val nation: Nation, back: Menu) :
    PagedMenu(player, player.tr("nation-sanctions.title", "nation" to TownyUtil.name(nation.name)), back) {

    private val canEdit: Boolean
        get() = resident?.nationOrNull == nation && TownyUtil.can(
            player,
            PermissionNodes.TOWNY_COMMAND_NATION_SANCTIONTOWN
        )

    private fun count() = nation.sanctionedTowns.size

    override fun entries(): List<MenuEntry> {
        val canEdit = canEdit
        return nation.sanctionedTowns.sortedBy { it.name.lowercase() }.map { town ->
            val actions = listOfNotNull(
                tr("common.left-details"),
                if (canEdit) tr("nation-sanctions.right-lift") else null
            )
            MenuEntry({ Icons.town(player, town, actions = actions) }) { click ->
                if (click.isRightClick && canEdit) {
                    run("towny:nation sanctiontown remove ${town.name}", ::count)
                } else {
                    TownInfoMenu(player, town, this).open()
                }
            }
        }
    }

    override fun controls() {
        tutorialButton(52, Tutorial.NATIONS)
        if (resident?.nationOrNull != nation) return
        guarded(
            47, PermissionNodes.TOWNY_COMMAND_NATION_SANCTIONTOWN,
            Icons.icon(Material.RED_BANNER, tr("nation-sanctions.add"), tr("nation-sanctions.add-description"))
        ) {
            Pickers.town(
                this,
                tr("nation-sanctions.add-title"),
                { !nation.hasTown(it) && !nation.hasSanctionedTown(it) }) { picked ->
                run("towny:nation sanctiontown add ${picked.name}", ::count)
            }.open()
        }
    }
}
