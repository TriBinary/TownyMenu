package net.trilleo.mc.plugins.townymenu.commands.info

import net.trilleo.mc.plugins.townymenu.borders.BorderView
import net.trilleo.mc.plugins.townymenu.guis.BorderViewMenu
import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Opens the border view menu, or with `on`, `off`, or `toggle` switches border particles straight away.
 * Registered as `/townymenu borders`.
 */
class BordersCommand : PluginCommand(
    name = "borders",
    description = "Show town and nation borders as particles",
    usage = "/townymenu borders [on|off|toggle]"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendRichMessage(sender.tr("command.borders.players-only"))
            return true
        }
        if (!BorderView.available) {
            sender.sendPrefixed(sender.tr("command.borders.unavailable"))
            return true
        }
        val option = args.firstOrNull()?.lowercase()
        if (option == null) {
            BorderViewMenu(sender, null).open()
            return true
        }
        val on = when (option) {
            "on" -> true
            "off" -> false
            "toggle" -> !BorderView.isOn(sender)
            else -> {
                sender.sendPrefixed(sender.tr("command.borders.usage"))
                return true
            }
        }
        BorderView.setOn(sender, on)
        sender.sendPrefixed(sender.tr(if (on) "command.borders.on" else "command.borders.off"))
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> =
        if (args.size == 1) OPTIONS.filter { it.startsWith(args[0].lowercase()) } else emptyList()

    private companion object {
        val OPTIONS = listOf("on", "off", "toggle")
    }
}
