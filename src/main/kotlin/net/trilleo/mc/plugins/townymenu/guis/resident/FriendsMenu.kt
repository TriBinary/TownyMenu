package net.trilleo.mc.plugins.townymenu.guis.resident

import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Lists the viewer's friends, with adding and removing. */
class FriendsMenu(player: Player, back: Menu) : PagedMenu(player, "Friends", back) {

    private fun friendNames(): List<String> = resident?.friends?.map { it.name }.orEmpty()

    override fun entries(): List<MenuEntry> =
        resident?.friends.orEmpty().sortedBy { it.name.lowercase() }.map { friend ->
            MenuEntry({ Icons.resident(friend, "", "<yellow>Left-click to view", "<red>Right-click to remove") }) { click ->
                if (click.isRightClick) {
                    run("towny:resident friend remove ${friend.name}", ::friendNames)
                } else {
                    ResidentProfileMenu(player, friend, this).open()
                }
            }
        }

    override fun controls() {
        button(47, Icons.icon(Material.PLAYER_HEAD, "<green>Add Online Player", "Pick a player who is online right now.")) {
            Pickers.resident(this, "Add Friend", { candidate -> resident?.hasFriend(candidate) == false }) { picked ->
                run("towny:resident friend add ${picked.name}", ::friendNames)
            }.open()
        }
        button(51, Icons.icon(Material.NAME_TAG, "<green>Add by Name", "Type the name of any player Towny knows.")) {
            prompt("Add Friend", "Player name") { name ->
                run("towny:resident friend add ${TownyUtil.argument(name)}", ::friendNames)
            }
        }
    }
}
