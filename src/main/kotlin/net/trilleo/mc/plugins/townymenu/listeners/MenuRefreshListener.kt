package net.trilleo.mc.plugins.townymenu.listeners

import com.palmergames.bukkit.towny.event.*
import com.palmergames.bukkit.towny.event.economy.BankTransactionEvent
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent
import net.trilleo.mc.plugins.townymenu.guis.framework.OpenMenus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Re-renders open menus when Towny changes something they show, so a player
 * watching a list sees residents join, land change hands, or the bank move
 * without reopening the menu. Every handler defers to [OpenMenus.refreshSoon],
 * which coalesces a burst of events into one refresh.
 */
class MenuRefreshListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onResidentJoinTown(event: TownAddResidentEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onResidentLeaveTown(event: TownRemoveResidentEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRankAdded(event: TownAddResidentRankEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRankRemoved(event: TownRemoveResidentRankEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTownJoinNation(event: NationAddTownEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTownLeaveNation(event: NationRemoveTownEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onClaim(event: TownClaimEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onUnclaim(event: TownUnclaimEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlotSettingsChanged(event: TownBlockSettingsChangedEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBankTransaction(event: BankTransactionEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onNewDay(event: NewDayEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onTownDeleted(event: DeleteTownEvent) = OpenMenus.refreshSoon()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onNationDeleted(event: DeleteNationEvent) = OpenMenus.refreshSoon()
}
