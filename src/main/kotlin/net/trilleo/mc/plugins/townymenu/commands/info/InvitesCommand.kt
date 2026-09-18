package net.trilleo.mc.plugins.townymenu.commands.info

import net.trilleo.mc.plugins.townymenu.guis.InvitesMenu
import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** Opens the invites menu, which TownyMenu's invite alerts link to. Registered as `/townymenu invites`. */
class InvitesCommand : PluginCommand(
    name = "invites",
    description = "Answer town, nation, and alliance invitations"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendRichMessage(sender.tr("command.invites.players-only"))
            return true
        }
        InvitesMenu(sender, null).open()
        return true
    }
}
