package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
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

/** A nation's member towns. Members with permission can invite and remove towns. */
class NationTownsMenu(player: Player, private val nation: Nation, back: Menu) :
    PagedMenu(player, player.tr("nation-towns.title", "nation" to TownyUtil.name(nation.name)), back) {

    private val isMember: Boolean
        get() = resident?.nationOrNull == nation

    override fun entries(): List<MenuEntry> {
        val canKick = isMember && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_NATION_KICK)
        return nation.towns
            .sortedWith(compareByDescending<Town> { nation.isCapital(it) }.thenBy { it.name.lowercase() })
            .map { town ->
                val lines = listOfNotNull(if (nation.isCapital(town)) tr("nation-towns.capital") else null)
                val actions = listOfNotNull(
                    tr("click.left-details"),
                    if (canKick && !nation.isCapital(town)) tr("nation-towns.right-remove") else null
                )
                MenuEntry({ Icons.town(player, town, *lines.toTypedArray(), actions = actions) }) { click ->
                    if (click.isRightClick && canKick && !nation.isCapital(town)) {
                        run("towny:nation kick ${town.name}", { nation.numTowns })
                    } else {
                        TownInfoMenu(player, town, this).open()
                    }
                }
            }
    }

    override fun controls() {
        tutorialButton(52, Tutorial.NATIONS)
        if (!isMember) return
        val sent = { nation.sentInvites.size }
        guarded(
            47, PermissionNodes.TOWNY_COMMAND_NATION_INVITE_ADD,
            Icons.icon(
                Material.BELL, tr("nation-towns.invite"), tr("nation-towns.invite-description"),
                actions = hints("click.choose-town")
            )
        ) {
            Pickers.town(this, tr("nation-towns.invite-title"), { !it.hasNation() }) { picked ->
                run("towny:nation add ${picked.name}", sent)
            }.open()
        }
        button(
            48, Icons.icon(
                Material.RED_BANNER, tr("nation-towns.sanctions"), tr("nation-towns.sanctions-description"),
                tr("nation-towns.sanctions-count", "count" to nation.sanctionedTowns.size),
                actions = hints("click.view")
            )
        ) {
            NationSanctionsMenu(player, nation, this).open()
        }
        val pending = nation.sentInvites
        button(
            51, Icons.icon(
                Material.PAPER, tr("common.sent-invites"), null,
                tr("common.pending", "count" to pending.size), "",
                *pending.take(10)
                    .map { tr("common.list-entry", "entry" to TownyUtil.name(it.receiver.name)) }.toTypedArray()
            )
        )
    }
}
