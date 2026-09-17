package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentProfileMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Lists a town's residents (mayor first, then online players). Members can also invite players. */
class TownMembersMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, player.tr("town-members.title", "town" to TownyUtil.name(town.name)), back) {

    private val isMember: Boolean
        get() = resident?.townOrNull == town

    override fun entries(): List<MenuEntry> =
        town.residents
            .sortedWith(compareByDescending<Resident> { it.isMayor }
                .thenByDescending { it.isOnline }
                .thenBy { it.name.lowercase() })
            .map { member -> MenuEntry({ Icons.resident(player, member, "", tr("common.click-view")) }) { ResidentProfileMenu(player, member, this).open() } }

    override fun controls() {
        tutorialButton(52, Tutorial.TOWN)
        if (!isMember) return
        val sent = { town.sentInvites.size }
        guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
            Icons.icon(Material.PLAYER_HEAD, tr("town-members.invite-online"), tr("town-members.invite-online-description"))) {
            Pickers.resident(this, tr("town-members.invite-title"), { !it.hasTown() }) { picked ->
                run("towny:town add ${picked.name}", sent)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
            Icons.icon(Material.NAME_TAG, tr("town-members.invite-name"), tr("town-members.invite-name-description"))) {
            prompt(tr("town-members.invite-title"), tr("common.player-name")) { name -> run("towny:town add ${TownyUtil.argument(name)}", sent) }
        }
        val pending = town.sentInvites
        guarded(51, PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD, Icons.icon(
            Material.PAPER, tr("common.sent-invites"), tr("common.sent-invites-description"),
            *pending.take(10).map { "<gray>- <white>${TownyUtil.name(it.receiver.name)}" }.toTypedArray(),
            tr("common.pending", "count" to pending.size),
        )) { click -> if (click.isRightClick) run("towny:town invite sent removeall", sent) }
    }
}
