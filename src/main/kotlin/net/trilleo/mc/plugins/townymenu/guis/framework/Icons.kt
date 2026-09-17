package net.trilleo.mc.plugins.townymenu.guis.framework

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/** Icon builders shared by the Towny menus. */
object Icons {

    /** A simple icon with a name and wrapped description. */
    fun icon(material: Material, name: String, description: String? = null, vararg extra: String): ItemStack =
        itemStack(material) {
            name(name)
            description?.let { loreWrapped("<gray>$it") }
            if (extra.isNotEmpty()) lore(extra.asList())
        }

    /** A toggle switch showing its current [value]; clicking should flip it. */
    fun toggle(material: Material, name: String, value: Boolean, description: String): ItemStack =
        itemStack(material) {
            name(name)
            loreWrapped("<gray>$description")
            lore("", "<gray>Currently: ${TownyUtil.onOff(value)}", "<yellow>Click to toggle")
            glow(value)
        }

    /** A player head for [resident], with extra lore [lines]. */
    fun resident(resident: Resident, vararg lines: String): ItemStack =
        itemStack(Material.PLAYER_HEAD) {
            resident.uuid?.let { head(Bukkit.getOfflinePlayer(it)) }
            name((if (resident.isOnline) "<green>" else "<gray>") + TownyUtil.name(resident.name))
            val town = resident.townOrNull
            lore(buildList {
                if (resident.hasTitle() || resident.hasSurname()) {
                    add("<gray>${TownyUtil.text(resident.title)} ${TownyUtil.name(resident.name)} ${TownyUtil.text(resident.surname)}".trim())
                }
                add("<gray>Town: <white>${town?.let { TownyUtil.name(it.name) } ?: "None"}")
                if (town != null) {
                    val ranks = buildList {
                        if (resident.isMayor) add("Mayor")
                        addAll(resident.townRanks)
                        addAll(resident.nationRanks)
                    }
                    if (ranks.isNotEmpty()) add("<gray>Ranks: <white>${TownyUtil.name(ranks.joinToString(", "))}")
                }
                add(if (resident.isOnline) "<green>Online" else "<gray>Last online: <white>${TownyUtil.date(resident.lastOnline)}")
                addAll(lines)
            })
        }

    /** A summary icon for [town], with extra lore [lines]. */
    fun town(town: Town, vararg lines: String): ItemStack =
        itemStack(if (town.isCapital) Material.GOLDEN_HELMET else Material.BELL) {
            name("<gold>${TownyUtil.name(town.name)}")
            lore(buildList {
                town.board.takeIf { it.isNotBlank() }?.let { add("<gray><i>${TownyUtil.text(it).take(60)}") }
                add("<gray>Mayor: <white>${town.mayor?.let { TownyUtil.name(it.name) } ?: "-"}")
                add("<gray>Residents: <white>${town.numResidents}")
                add("<gray>Claims: <white>${town.numTownBlocks}/${town.maxTownBlocksAsAString}")
                add("<gray>Nation: <white>${town.nationOrNull?.let { TownyUtil.name(it.name) } ?: "None"}")
                if (TownyUtil.economy) add("<gray>Bank: <white>${TownyUtil.balance(town)}")
                add("<gray>Open: ${yesNo(town.isOpen)}  <gray>Public: ${yesNo(town.isPublic)}")
                if (town.isForSale) add("<gold>For sale: ${TownyUtil.money(town.forSalePrice)}")
                if (town.isRuined) add("<red>Ruined")
                addAll(lines)
            })
            glow(town.isOpen)
        }

    /** A summary icon for [nation], with extra lore [lines]. */
    fun nation(nation: Nation, vararg lines: String): ItemStack =
        itemStack(Material.BEACON) {
            name("<aqua>${TownyUtil.name(nation.name)}")
            lore(buildList {
                nation.board.takeIf { it.isNotBlank() }?.let { add("<gray><i>${TownyUtil.text(it).take(60)}") }
                add("<gray>Leader: <white>${nation.king?.let { TownyUtil.name(it.name) } ?: "-"}")
                add("<gray>Capital: <white>${nation.capital?.let { TownyUtil.name(it.name) } ?: "-"}")
                add("<gray>Towns: <white>${nation.numTowns}  <gray>Residents: <white>${nation.numResidents}")
                if (TownyUtil.economy) add("<gray>Bank: <white>${TownyUtil.balance(nation)}")
                add("<gray>Allies: <white>${nation.allies.size}  <gray>Enemies: <white>${nation.enemies.size}")
                add("<gray>Open: ${yesNo(nation.isOpen)}  <gray>Public: ${yesNo(nation.isPublic)}  <gray>Peaceful: ${yesNo(nation.isNeutral)}")
                addAll(lines)
            })
        }

    private fun yesNo(value: Boolean) = if (value) "<green>Yes" else "<red>No"
}
