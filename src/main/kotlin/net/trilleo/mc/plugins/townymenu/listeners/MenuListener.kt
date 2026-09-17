package net.trilleo.mc.plugins.townymenu.listeners

import com.palmergames.bukkit.towny.confirmations.event.ConfirmationSendEvent
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuActions
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class MenuListener : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onClick(event: InventoryClickEvent) {
        val menu = event.view.topInventory.getHolder(false) as? Menu ?: return
        event.isCancelled = true
        if (event.clickedInventory === event.view.topInventory) menu.click(event.slot, event.click)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onDrag(event: InventoryDragEvent) {
        if (event.view.topInventory.getHolder(false) is Menu) event.isCancelled = true
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        MenuActions.forget(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onConfirmation(event: ConfirmationSendEvent) {
        MenuActions.interceptConfirmation(event)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSwapHands(event: PlayerSwapHandItemsEvent) {
        val player: Player = event.player
        if (!player.isSneaking || !Main.instance.pluginConfig.sneakSwapHandShortcut) return
        event.isCancelled = true
        MainMenu(player).open()
    }
}
