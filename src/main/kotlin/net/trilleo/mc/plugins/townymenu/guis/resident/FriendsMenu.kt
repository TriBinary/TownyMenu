package net.trilleo.mc.plugins.townymenu.guis.resident

import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Lists the viewer's friends, with adding and removing. */
class FriendsMenu(player: Player, back: Menu) : PagedMenu(player, player.tr("friends.title"), back) {

    private fun friendNames(): List<String> = resident?.friends?.map { it.name }.orEmpty()

    override fun entries(): List<MenuEntry> =
        resident?.friends.orEmpty().sortedBy { it.name.lowercase() }.map { friend ->
            MenuEntry({ Icons.resident(player, friend, "", tr("friends.left-view"), tr("friends.right-remove")) }) { click ->
                if (click.isRightClick) {
                    run("towny:resident friend remove ${friend.name}", ::friendNames)
                } else {
                    ResidentProfileMenu(player, friend, this).open()
                }
            }
        }

    override fun controls() {
        tutorialButton(52, Tutorial.PROTECTION)
        button(47, Icons.icon(Material.PLAYER_HEAD, tr("friends.add-online"), tr("friends.add-online-description"))) {
            Pickers.resident(this, tr("friends.add-title"), { candidate -> resident?.hasFriend(candidate) == false }) { picked ->
                run("towny:resident friend add ${picked.name}", ::friendNames)
            }.open()
        }
        button(51, Icons.icon(Material.NAME_TAG, tr("friends.add-name"), tr("friends.add-name-description"))) {
            prompt(tr("friends.add-title"), tr("common.player-name")) { name ->
                run("towny:resident friend add ${TownyUtil.argument(name)}", ::friendNames)
            }
        }
    }
}
