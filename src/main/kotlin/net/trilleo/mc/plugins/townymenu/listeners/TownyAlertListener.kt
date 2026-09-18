package net.trilleo.mc.plugins.townymenu.listeners

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.event.NationInviteTownEvent
import com.palmergames.bukkit.towny.event.NationRequestAllyNationEvent
import com.palmergames.bukkit.towny.event.TownInvitePlayerEvent
import com.palmergames.bukkit.towny.event.economy.TownEntersBankruptcyEvent
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Tells players about things they would otherwise only find by opening a menu:
 * an invitation waiting for them, and a town falling into debt or ruin. Each
 * message links to the menu that acts on it.
 */
class TownyAlertListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTownInvite(event: TownInvitePlayerEvent) {
        val resident = event.invite.receiver
        // Towny already hides its own invite messages from residents using this mode.
        if (resident.hasMode("ignoreinvites")) return
        alert(resident, "alert.town-invite", "town" to TownyUtil.name(event.invite.sender.name))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onNationInvite(event: NationInviteTownEvent) {
        alert(event.invite.receiver.mayor, "alert.nation-invite", "nation" to TownyUtil.name(event.invite.sender.name))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAllyRequest(event: NationRequestAllyNationEvent) {
        alert(event.invite.receiver.king, "alert.ally-request", "nation" to TownyUtil.name(event.invite.sender.name))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBankruptcy(event: TownEntersBankruptcyEvent) {
        alert(event.town.mayor, "alert.bankrupt", "town" to TownyUtil.name(event.town.name))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRuined(event: TownRuinedEvent) {
        val mayor = event.town.mayor ?: return
        val key = if (TownySettings.getTownRuinsReclaimEnabled()) "alert.ruined-reclaim" else "alert.ruined"
        alert(
            mayor, key,
            "town" to TownyUtil.name(event.town.name),
            "hours" to TownySettings.getTownRuinsMaxDurationHours(),
            "cost" to TownyUtil.money(TownySettings.getEcoPriceReclaimTown()),
        )
    }

    private fun alert(resident: Resident?, key: String, vararg args: Pair<String, Any?>) {
        if (!Main.instance.pluginConfig.townyAlerts) return
        val player = resident?.player ?: return
        player.sendPrefixed(player.tr(key, *args))
    }
}
