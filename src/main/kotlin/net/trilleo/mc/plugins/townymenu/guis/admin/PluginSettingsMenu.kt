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
class PluginSettingsMenu(player: Player, back: Menu) : Menu(player, player.tr("admin-plugin.title"), 4, back) {

    private val config
        get() = Main.instance.pluginConfig

    override fun build() {
        val row = layout(10, 11, 12, 13, 14, 15, 16)
        val language = config.language
        row.add(
            Icons.icon(
                Material.BOOK, tr("admin-plugin.language"), tr("admin-plugin.language-description"),
                tr(
                    "common.current",
                    "value" to if (language.equals(
                            Lang.AUTO,
                            ignoreCase = true
                        )
                    ) tr("admin-plugin.language-auto") else TownyUtil.text(language)
                ),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            val options =
                listOf(Lang.AUTO to tr("admin-plugin.language-auto")) + Lang.ids.map { it to "<white>${TownyUtil.text(it)}" }
            Pickers.option(this, tr("admin-plugin.language-title"), options, language, Material.BOOK) { id ->
                config.language = id
                apply()
            }.open()
        }
        row.add(
            Icons.icon(
                Material.NAME_TAG, tr("admin-plugin.prefix"), tr("admin-plugin.prefix-description"),
                tr("common.current", "value" to config.messagePrefix),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            prompt(
                tr("admin-plugin.prefix"),
                tr("admin-plugin.prefix-label"),
                initial = config.messagePrefix,
                maxLength = 256
            ) { prefix ->
                config.messagePrefix = prefix
                apply()
            }
        }
        row.add(
            Icons.toggle(
                player, Material.SHIELD, tr("admin-plugin.shortcut"), config.sneakSwapHandShortcut,
                tr("admin-plugin.shortcut-description")
            )
        ) {
            config.sneakSwapHandShortcut = !config.sneakSwapHandShortcut
            apply()
        }
        row.add(
            Icons.toggle(
                player, Material.KNOWLEDGE_BOOK, tr("admin-plugin.join-hint"), config.tutorialJoinHint,
                tr("admin-plugin.join-hint-description")
            )
        ) {
            config.tutorialJoinHint = !config.tutorialJoinHint
            apply()
        }
        row.add(
            Icons.toggle(
                player, Material.CLOCK, tr("admin-plugin.live-refresh"), config.liveMenuRefresh,
                tr("admin-plugin.live-refresh-description")
            )
        ) {
            config.liveMenuRefresh = !config.liveMenuRefresh
            apply()
        }
        row.add(
            Icons.toggle(
                player, Material.PAPER, tr("admin-plugin.alerts"), config.townyAlerts,
                tr("admin-plugin.alerts-description")
            )
        ) {
            config.townyAlerts = !config.townyAlerts
            apply()
        }
        row.add(
            Icons.icon(
                Material.GOLD_NUGGET, tr("admin-plugin.bank-amounts"), tr("admin-plugin.bank-amounts-description"),
                tr("common.current", "value" to config.bankAmounts.joinToString(", ").ifEmpty { tr("common.none") }),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            prompt(
                tr("admin-plugin.bank-amounts"),
                tr("admin-plugin.bank-amounts-label"),
                initial = config.bankAmounts.joinToString(", ")
            ) { input ->
                config.bankAmounts = input.split(',', ' ').mapNotNull { it.trim().toIntOrNull() }
                apply()
            }
        }
        backButton(31)
    }

    private fun apply() {
        Main.instance.reload()
        open()
    }
}
