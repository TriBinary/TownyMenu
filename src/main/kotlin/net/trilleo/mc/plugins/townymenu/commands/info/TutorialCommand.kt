package net.trilleo.mc.plugins.townymenu.commands.info

import net.trilleo.mc.plugins.townymenu.guis.tutorial.TutorialMenu
import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** Opens the tutorial. Registered as `/townymenu tutorial`. */
class TutorialCommand : PluginCommand(
    name = "tutorial",
    description = "Learn how towns, plots, and nations work"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendRichMessage(sender.tr("command.tutorial.players-only"))
            return true
        }
        TutorialMenu(sender, null).open()
        return true
    }
}
