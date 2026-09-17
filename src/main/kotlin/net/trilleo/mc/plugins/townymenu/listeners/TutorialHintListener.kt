package net.trilleo.mc.plugins.townymenu.listeners

import com.palmergames.bukkit.towny.TownyAPI
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/** Points players without a town to the tutorial when they join, until they have read every lesson. */
class TutorialHintListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        // Towny registers new residents during the join tick, and the hint reads better after the join messages.
        player.server.scheduler.runTaskLater(Main.instance, Runnable {
            if (!player.isOnline || !Main.instance.pluginConfig.tutorialJoinHint) return@Runnable
            if (TownyAPI.getInstance().getResident(player)
                    ?.hasTown() == true || Tutorial.isComplete(player)
            ) return@Runnable
            player.sendPrefixed(player.tr("tutorial.join-hint"))
        }, HINT_DELAY_TICKS)
    }

    companion object {
        private const val HINT_DELAY_TICKS = 60L
    }
}
