package net.trilleo.mc.plugins.townymenu.guis.admin

import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.Lang
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Edits TownyMenu's own `config.yml`; every change is saved and reloaded like `/townymenu reload`. */
class PluginSettingsMenu(player: Player, back: Menu) : Menu(player, player.tr("admin-plugin.title"), 3, back) {

    private val config
        get() = Main.instance.pluginConfig

    override fun build() {
        val language = config.language
        button(10, Icons.icon(Material.BOOK, tr("admin-plugin.language"), tr("admin-plugin.language-description"),
            tr("common.current", "value" to if (language.equals(Lang.AUTO, ignoreCase = true)) tr("admin-plugin.language-auto") else TownyUtil.text(language)),
            tr("common.click-change"))) {
            val options = listOf(Lang.AUTO to tr("admin-plugin.language-auto")) + Lang.ids.map { it to "<white>${TownyUtil.text(it)}" }
            Pickers.option(this, tr("admin-plugin.language-title"), options, language, Material.BOOK) { id ->
                config.language = id
                apply()
            }.open()
        }
        button(12, Icons.icon(Material.NAME_TAG, tr("admin-plugin.prefix"), tr("admin-plugin.prefix-description"),
            tr("common.current", "value" to config.messagePrefix), tr("common.click-change"))) {
            prompt(tr("admin-plugin.prefix"), tr("admin-plugin.prefix-label"), initial = config.messagePrefix, maxLength = 256) { prefix ->
                config.messagePrefix = prefix
                apply()
            }
        }
        button(14, Icons.toggle(player, Material.SHIELD, tr("admin-plugin.shortcut"), config.sneakSwapHandShortcut,
            tr("admin-plugin.shortcut-description"))) {
            config.sneakSwapHandShortcut = !config.sneakSwapHandShortcut
            apply()
        }
        button(16, Icons.toggle(player, Material.KNOWLEDGE_BOOK, tr("admin-plugin.join-hint"), config.tutorialJoinHint,
            tr("admin-plugin.join-hint-description"))) {
            config.tutorialJoinHint = !config.tutorialJoinHint
            apply()
        }
        backButton(22)
    }

    private fun apply() {
        Main.instance.reload()
        open()
    }
}
