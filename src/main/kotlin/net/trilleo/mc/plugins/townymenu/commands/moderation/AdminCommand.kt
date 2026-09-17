package net.trilleo.mc.plugins.townymenu.commands.moderation

import net.trilleo.mc.plugins.townymenu.guis.admin.AdminMenu
import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** Opens the admin menu. Registered as `/townymenu admin` and requires `townymenu.admin`. */
class AdminCommand : PluginCommand(
    name = "admin",
    description = "Open the server admin menu",
    permission = AdminMenu.PERMISSION
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendRichMessage(sender.tr("command.admin.players-only"))
            return true
        }
        AdminMenu(sender, null).open()
        return true
    }
}
