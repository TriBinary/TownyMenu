package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material

/**
 * Selection menus. Each picker returns to [Menu] `back` and calls `onPick` with
 * the chosen value; the caller usually runs a command on `back` so the result
 * shows there.
 */
object Pickers {

    /** Picks an online resident (other than the viewer) matching [filter]. */
    fun resident(back: Menu, title: String, filter: (Resident) -> Boolean, onPick: (Resident) -> Unit): Menu =
        ListMenu(back.player, title, back) {
            TownyUtil.otherOnlineResidents(back.player)
                .filter(filter)
                .map { resident -> MenuEntry({ Icons.resident(back.player, resident, "", back.tr("picker.select")) }) { onPick(resident) } }
        }

    /** Picks a town matching [filter]. */
    fun town(back: Menu, title: String, filter: (Town) -> Boolean, onPick: (Town) -> Unit): Menu =
        ListMenu(back.player, title, back) {
            TownyAPI.getInstance().towns
                .filter(filter)
                .sortedBy { it.name.lowercase() }
                .map { town -> MenuEntry({ Icons.town(back.player, town, "", back.tr("picker.select")) }) { onPick(town) } }
        }

    /** Picks a nation matching [filter]. */
    fun nation(back: Menu, title: String, filter: (Nation) -> Boolean, onPick: (Nation) -> Unit): Menu =
        ListMenu(back.player, title, back) {
            TownyAPI.getInstance().nations
                .filter(filter)
                .sortedBy { it.name.lowercase() }
                .map { nation -> MenuEntry({ Icons.nation(back.player, nation, "", back.tr("picker.select")) }) { onPick(nation) } }
        }

    /** Picks one of [options] (id to MiniMessage label), marking [current] as selected. */
    fun option(back: Menu, title: String, options: List<Pair<String, String>>, current: String?, material: Material, onPick: (String) -> Unit): Menu =
        ListMenu(back.player, title, back) {
            options.map { (id, label) ->
                val selected = id.equals(current, ignoreCase = true)
                val icon = {
                    itemStack(material) {
                        name(label)
                        lore(back.tr(if (selected) "picker.current" else "picker.select"))
                        glow(selected)
                    }
                }
                MenuEntry(icon) { onPick(id) }
            }
        }
}
