package net.trilleo.mc.plugins.townymenu.guis.framework

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** Icon builders shared by the Towny menus. Icons with built-in text take the [Player] whose language they use. */
object Icons {

    /** A simple icon with a name and wrapped description. */
    fun icon(material: Material, name: String, description: String? = null, vararg extra: String): ItemStack =
        itemStack(material) {
            name(name)
            description?.let { loreWrapped("<gray>$it") }
            if (extra.isNotEmpty()) lore(extra.asList())
        }

    /** A toggle switch showing its current [value]; clicking should flip it. */
    fun toggle(player: Player, material: Material, name: String, value: Boolean, description: String): ItemStack =
        itemStack(material) {
            name(name)
            loreWrapped("<gray>$description")
            lore(
                "",
                player.tr("icon.currently", "value" to TownyUtil.onOff(player, value)),
                player.tr("icon.click-toggle")
            )
            glow(value)
        }

    /** A player head for [resident], with extra lore [lines]. */
    fun resident(player: Player, resident: Resident, vararg lines: String): ItemStack =
        itemStack(Material.PLAYER_HEAD) {
            resident.uuid?.let { head(Bukkit.getOfflinePlayer(it)) }
            name((if (resident.isOnline) "<green>" else "<gray>") + TownyUtil.name(resident.name))
            val town = resident.townOrNull
            lore(buildList {
                if (resident.hasTitle() || resident.hasSurname()) {
                    add(
                        "<gray>${TownyUtil.text(resident.title)} ${TownyUtil.name(resident.name)} ${
                            TownyUtil.text(
                                resident.surname
                            )
                        }".trim()
                    )
                }
                add(
                    player.tr(
                        "icon.resident.town",
                        "town" to (town?.let { TownyUtil.name(it.name) } ?: player.tr("common.none"))))
                if (town != null) {
                    val ranks = buildList {
                        if (resident.isMayor) add(player.tr("icon.resident.mayor"))
                        addAll(resident.townRanks.map(TownyUtil::name))
                        addAll(resident.nationRanks.map(TownyUtil::name))
                    }
                    if (ranks.isNotEmpty()) add(player.tr("icon.resident.ranks", "ranks" to ranks.joinToString(", ")))
                }
                add(
                    if (resident.isOnline) player.tr("icon.resident.online")
                    else player.tr("icon.resident.last-online", "date" to TownyUtil.date(resident.lastOnline))
                )
                addAll(lines)
            })
        }

    /** A summary icon for [town], with extra lore [lines]. */
    fun town(player: Player, town: Town, vararg lines: String): ItemStack =
        itemStack(if (town.isCapital) Material.GOLDEN_HELMET else Material.BELL) {
            name("<gold>${TownyUtil.name(town.name)}")
            lore(buildList {
                town.board.takeIf { it.isNotBlank() }?.let { add("<gray><i>${TownyUtil.text(it).take(60)}") }
                add(player.tr("icon.town.mayor", "mayor" to (town.mayor?.let { TownyUtil.name(it.name) } ?: "-")))
                add(player.tr("icon.town.residents", "count" to town.numResidents))
                add(player.tr("icon.town.claims", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString))
                add(
                    player.tr(
                        "icon.town.nation",
                        "nation" to (town.nationOrNull?.let { TownyUtil.name(it.name) } ?: player.tr("common.none"))))
                if (TownyUtil.economy) add(player.tr("icon.bank", "balance" to TownyUtil.balance(town)))
                add(
                    player.tr(
                        "icon.open-public",
                        "open" to TownyUtil.yesNo(player, town.isOpen),
                        "public" to TownyUtil.yesNo(player, town.isPublic)
                    )
                )
                if (town.isForSale) add(player.tr("icon.town.for-sale", "price" to TownyUtil.money(town.forSalePrice)))
                if (town.isRuined) add(player.tr("icon.town.ruined"))
                addAll(lines)
            })
            glow(town.isOpen)
        }

    /** A summary icon for [nation], with extra lore [lines]. */
    fun nation(player: Player, nation: Nation, vararg lines: String): ItemStack =
        itemStack(Material.BEACON) {
            name("<aqua>${TownyUtil.name(nation.name)}")
            lore(buildList {
                nation.board.takeIf { it.isNotBlank() }?.let { add("<gray><i>${TownyUtil.text(it).take(60)}") }
                add(player.tr("icon.nation.leader", "leader" to (nation.king?.let { TownyUtil.name(it.name) } ?: "-")))
                add(
                    player.tr(
                        "icon.nation.capital",
                        "capital" to (nation.capital?.let { TownyUtil.name(it.name) } ?: "-")))
                add(player.tr("icon.nation.size", "towns" to nation.numTowns, "residents" to nation.numResidents))
                if (TownyUtil.economy) add(player.tr("icon.bank", "balance" to TownyUtil.balance(nation)))
                add(
                    player.tr(
                        "icon.nation.relations",
                        "allies" to nation.allies.size,
                        "enemies" to nation.enemies.size
                    )
                )
                add(
                    player.tr(
                        "icon.nation.flags",
                        "open" to TownyUtil.yesNo(player, nation.isOpen),
                        "public" to TownyUtil.yesNo(player, nation.isPublic),
                        "peaceful" to TownyUtil.yesNo(player, nation.isNeutral)
                    )
                )
                addAll(lines)
            })
        }
}
