package net.trilleo.mc.plugins.townymenu.guis.framework

import com.palmergames.bukkit.towny.confirmations.Confirmation
import com.palmergames.bukkit.towny.confirmations.event.ConfirmationSendEvent
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.utils.DialogUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * Runs Towny commands on behalf of a menu and decides what the player sees next.
 *
 * Towny reports success and failure only through chat, which is hidden while a
 * menu is open. So after a command settles, the menu compares a *probe* of the
 * state the command should change:
 *
 * - **changed** → the menu re-renders (or reopens), showing the new state;
 * - **unchanged, or no probe** → the menu stays closed so Towny's chat message
 *   (an error, a cost, a teleport notice) is visible.
 *
 * Either way, nothing happens if the player has since opened a different menu.
 *
 * Towny confirmations raised by a menu command are shown as a native dialog
 * instead of clickable chat text (see [interceptConfirmation]).
 */
object MenuActions {

    /** Ticks to wait before checking the probe; Towny runs some commands asynchronously. */
    const val DEFAULT_DELAY = 4L

    /** [origin] is the menu open when the command ran, or `null` when none was (e.g. after a dialog). */
    private class Pending(
        val menu: Menu,
        var origin: Menu?,
        val probe: (() -> Any?)?,
        val before: Any?,
        val delay: Long,
    ) {
        var awaitingConfirmation = false
    }

    private val pending = HashMap<UUID, Pending>()

    /**
     * Runs [command] as [menu]'s player. After [delayTicks], reopens [menu] if
     * [probe] changed, or otherwise leaves the player in-game to read Towny's reply.
     */
    fun perform(menu: Menu, command: String, probe: (() -> Any?)?, delayTicks: Long) {
        val player = menu.player
        val action = Pending(menu, openMenu(player), probe, probe?.invoke(), delayTicks)
        pending[player.uniqueId] = action
        player.performCommand(command)
        later(delayTicks) { settle(player, action) }
    }

    /** Takes over a Towny confirmation raised by a pending menu command and shows it as a dialog. */
    fun interceptConfirmation(event: ConfirmationSendEvent) {
        val player = event.sender as? Player ?: return
        val action = pending[player.uniqueId] ?: return
        action.awaitingConfirmation = true
        event.setSendMessage(false)
        val confirmation = event.confirmation
        later(1) { showConfirmation(player, action, confirmation) }
    }

    /** `true` while a command [player] started from a menu is still settling, so menu clicks are ignored. */
    fun isBusy(player: Player): Boolean = pending[player.uniqueId]?.awaitingConfirmation == false

    fun forget(player: Player) {
        pending.remove(player.uniqueId)
    }

    private fun showConfirmation(player: Player, action: Pending, confirmation: Confirmation) {
        if (!player.isOnline || pending[player.uniqueId] !== action) return
        val title = confirmation.title.locale(player).component()
        val body = if (confirmation.isSerious) player.tr("dialog.cannot-undo") else null
        val prefix = confirmation.pluginPrefix
        action.origin = null
        DialogUtil.confirm(
            player, title, body,
            onYes = {
                action.awaitingConfirmation = false
                player.performCommand("$prefix:${confirmation.confirmCommand}")
                later(action.delay) { settle(player, action) }
            },
            onNo = {
                pending.remove(player.uniqueId)
                player.performCommand("$prefix:${confirmation.cancelCommand}")
                action.menu.open()
            },
        )
    }

    private fun settle(player: Player, action: Pending) {
        if (pending[player.uniqueId] !== action || action.awaitingConfirmation) return
        pending.remove(player.uniqueId)
        if (!player.isOnline) return
        if (openMenu(player) !== action.origin) return
        val probe = action.probe
        if (probe != null && probe() != action.before) {
            action.menu.open()
        } else if (action.origin != null) {
            player.closeInventory()
        }
    }

    private fun openMenu(player: Player): Menu? = player.openInventory.topInventory.getHolder(false) as? Menu

    private fun later(ticks: Long, block: () -> Unit) {
        Bukkit.getScheduler().runTaskLater(Main.instance, Runnable(block), ticks)
    }
}
