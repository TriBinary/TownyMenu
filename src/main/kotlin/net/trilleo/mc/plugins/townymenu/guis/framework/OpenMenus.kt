package net.trilleo.mc.plugins.townymenu.guis.framework

import net.trilleo.mc.plugins.townymenu.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.logging.Level

/**
 * Keeps the menus players are looking at in step with Towny.
 *
 * Menus are views of live Towny data, so when something changes them from
 * outside — another player, a command, or the new day — every open menu is
 * rendered again. Towny fires bursts of events (a mass claim, a new day), so
 * [refreshSoon] coalesces them into one refresh on the next tick.
 */
object OpenMenus {

    private var pending = false

    /** The menu [player] has open, or `null` when they have another inventory (or none) open. */
    fun of(player: Player): Menu? = player.openInventory.topInventory.getHolder(false) as? Menu

    /** Re-renders every open menu on the next tick, unless the server turned live refreshing off. */
    fun refreshSoon() {
        if (pending || !Main.instance.pluginConfig.liveMenuRefresh) return
        pending = true
        Bukkit.getScheduler().runTask(Main.instance, Runnable {
            pending = false
            refreshNow()
        })
    }

    private fun refreshNow() {
        for (player in Bukkit.getOnlinePlayers()) {
            // A command the player started is still settling; MenuActions reopens the menu when it lands.
            if (MenuActions.isBusy(player)) continue
            val menu = of(player) ?: continue
            try {
                menu.render()
            } catch (error: Exception) {
                Main.instance.logger.log(Level.WARNING, "Could not refresh ${menu::class.simpleName}", error)
            }
        }
    }
}
