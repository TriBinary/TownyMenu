package net.trilleo.mc.plugins.townymenu.commands.moderation

import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * Reloads the plugin configuration and language files from disk.
 *
 * Registered as `/townymenu reload` and requires the
 * `townymenu.reload` permission.
 */
class ReloadCommand(private val plugin: JavaPlugin) : PluginCommand(
    name = "reload",
    description = "Reload the plugin configuration and translations",
    permission = "townymenu.reload"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        val main = plugin as? Main
        if (main == null) {
            if (sender is Player) {
                sender.sendPrefixed(sender.tr("command.reload.error"))
            } else {
                sender.sendRichMessage(sender.tr("command.reload.error"))
            }
            return true
        }
        main.reload()
        if (sender is Player) {
            sender.sendPrefixed(sender.tr("command.reload.done"))
        } else {
            sender.sendRichMessage(sender.tr("command.reload.done"))
        }
        return true
    }
}