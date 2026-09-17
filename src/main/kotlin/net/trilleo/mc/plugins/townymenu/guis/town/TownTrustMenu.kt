package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Residents and towns trusted to build anywhere in the viewer's town. */
class TownTrustMenu(player: Player, private val town: Town, back: Menu) : PagedMenu(player, "Trusted", back) {

    private fun snapshot() = town.trustedResidents.size to town.trustedTowns.size

    override fun entries(): List<MenuEntry> {
        val canTrust = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST)
        val canTrustTowns = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_TRUSTTOWN)
        val residents = town.trustedResidents.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.resident(trusted, "", if (canTrust) "<red>Click to untrust" else "<gray>Trusted resident") }) {
                if (canTrust) run("towny:town trust remove ${trusted.name}", ::snapshot)
            }
        }
        val towns = town.trustedTowns.sortedBy { it.name.lowercase() }.map { trusted ->
            MenuEntry({ Icons.town(trusted, "", if (canTrustTowns) "<red>Click to untrust" else "<gray>Trusted town") }) {
                if (canTrustTowns) run("towny:town trusttown remove ${trusted.name}", ::snapshot)
            }
        }
        return residents + towns
    }

    override fun controls() {
        guarded(47, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST,
            Icons.icon(Material.PLAYER_HEAD, "<green>Trust Online Player", "Trusted residents may build anywhere in town.")) {
            Pickers.resident(this, "Trust Player", { !town.hasTrustedResident(it) }) { picked ->
                run("towny:town trust add ${picked.name}", ::snapshot)
            }.open()
        }
        guarded(48, PermissionNodes.TOWNY_COMMAND_TOWN_TRUST,
            Icons.icon(Material.NAME_TAG, "<green>Trust by Name", "Trust any player Towny knows.")) {
            prompt("Trust Player", "Player name") { name -> run("towny:town trust add ${TownyUtil.argument(name)}", ::snapshot) }
        }
        guarded(51, PermissionNodes.TOWNY_COMMAND_TOWN_TRUSTTOWN,
            Icons.icon(Material.BELL, "<green>Trust a Town", "Every resident of a trusted town may build in your town.")) {
            Pickers.town(this, "Trust Town", { it != town && !town.hasTrustedTown(it) }) { picked ->
                run("towny:town trusttown add ${picked.name}", ::snapshot)
            }.open()
        }
    }
}
