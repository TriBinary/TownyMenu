package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
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

/** Residents and towns trusted to build anywhere in the viewer's town. */
class TownTrustMenu(player: Player, private val town: Town, back: Menu) : PagedMenu(player, player.tr("common.trusted-title"), back) {

    private fun snapshot() = town.trustedResidents.size to town.trustedTowns.size

    override fun entries(): List<MenuEntry> {
        val canTrust = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST)
        val canTrustTowns = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_TRUSTTOWN)
        val residents = town.trustedResidents.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.resident(player, trusted, "", tr(if (canTrust) "common.click-untrust" else "town-trust.resident")) }) {
                if (canTrust) run("towny:town trust remove ${trusted.name}", ::snapshot)
            }
        }
        val towns = town.trustedTowns.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.town(player, trusted, "", tr(if (canTrustTowns) "common.click-untrust" else "town-trust.town")) }) {
                if (canTrustTowns) run("towny:town trusttown remove ${trusted.name}", ::snapshot)
            }
        }
        return residents + towns
    }

    override fun controls() {
        tutorialButton(52, Tutorial.PROTECTION)
        guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST,
            Icons.icon(Material.PLAYER_HEAD, tr("common.trust-online"), tr("town-trust.trust-online-description"))) {
            Pickers.resident(this, tr("common.trust-title"), { !town.hasTrustedResident(it) }) { picked ->
                run("towny:town trust add ${picked.name}", ::snapshot)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST,
            Icons.icon(Material.NAME_TAG, tr("common.trust-name"), tr("common.trust-name-description"))) {
            prompt(tr("common.trust-title"), tr("common.player-name")) { name -> run("towny:town trust add ${TownyUtil.argument(name)}", ::snapshot) }
        }
        guarded(51, PermissionNodes.TOWNY_COMMAND_TOWN_TRUSTTOWN,
            Icons.icon(Material.BELL, tr("town-trust.trust-town"), tr("town-trust.trust-town-description"))) {
            Pickers.town(this, tr("town-trust.trust-town-title"), { it != town && !town.hasTrustedTown(it) }) { picked ->
                run("towny:town trusttown add ${picked.name}", ::snapshot)
            }.open()
        }
    }
}
