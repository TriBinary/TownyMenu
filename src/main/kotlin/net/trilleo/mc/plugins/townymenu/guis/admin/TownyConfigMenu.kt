package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.*
import net.trilleo.mc.plugins.townymenu.utils.*
import net.trilleo.mc.plugins.townymenu.utils.TownyConfig.Kind
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Browses one section of Towny's `config.yml`: its sub-sections, then its settings.
 * Every change is saved and applied with `/townyadmin reload config`, so it needs Towny's reload node.
 */
class TownyConfigMenu(player: Player, private val section: String, back: Menu) : PagedMenu(
    player,
    if (section.isEmpty()) player.tr("admin-config.title") else player.tr(
        "admin-config.section-title",
        "section" to section.substringAfterLast('.').replace('_', ' ')
    ),
    back,
) {

    override fun entries(): List<MenuEntry> =
        TownyConfig.sections(section).map { child ->
            MenuEntry({
                itemStack(Material.BOOKSHELF) {
                    name("<gold>${child.name}")
                    if (child.description.isNotEmpty()) loreWrapped("<gray>${shorten(child.description)}")
                    loreBreak()
                    lore(tr("admin-config.settings-count", "count" to TownyConfig.count(child.path)))
                    loreActions(listOf(tr("click.view")))
                }
            }) { TownyConfigMenu(player, child.path, this).open() }
        } + TownyConfig.settings(section).map { settingEntry(this, it) }

    override fun controls() {
        button(
            47, Icons.icon(
                Material.COMPASS, tr("admin-config.search"), tr("admin-config.search-description"),
                actions = hints("click.search")
            )
        ) {
            prompt(tr("admin-config.search-title"), tr("admin-config.search-label")) { query ->
                ListMenu(player, tr("admin-config.results-title", "query" to TownyUtil.text(query)), this) { list ->
                    TownyConfig.search(query).map { settingEntry(list, it) }
                }.open()
            }
        }
        guarded(
            51, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RELOAD,
            Icons.icon(
                Material.REPEATER, tr("admin-config.reload"), tr("admin-config.reload-description"),
                actions = hints("click.reload")
            )
        ) {
            run(RELOAD, ::loadedConfig)
        }
    }

    companion object {
        private const val RELOAD = "towny:townyadmin reload config"
        private const val MAX_DESCRIPTION = 320

        /** Reloading replaces Towny's config object, so its identity shows whether the reload succeeded. */
        private fun loadedConfig(): Any = TownySettings.getConfig()

        private fun shorten(text: String): String =
            if (text.length <= MAX_DESCRIPTION) text else text.take(MAX_DESCRIPTION).substringBeforeLast(' ') + " …"

        /** A clickable icon for [setting] inside [menu]: left-click edits, right-click restores the default. */
        fun settingEntry(menu: Menu, setting: TownyConfig.Setting): MenuEntry {
            val player = menu.player
            val kind = setting.kind
            val editable =
                kind != Kind.READ_ONLY && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RELOAD)
            val icon = {
                val value = setting.value
                val on = kind == Kind.BOOLEAN && value.toBoolean()
                itemStack(
                    when (kind) {
                        Kind.BOOLEAN -> if (on) Material.LIME_DYE else Material.GRAY_DYE
                        Kind.INTEGER, Kind.DECIMAL -> Material.GOLD_NUGGET
                        Kind.TEXT -> Material.PAPER
                        Kind.READ_ONLY -> Material.BOOK
                    }
                ) {
                    name("<yellow>${setting.name}")
                    if (setting.description.isNotEmpty()) loreWrapped("<gray>${shorten(setting.description)}")
                    loreBreak()
                    lore(buildList {
                        add(menu.tr("admin-config.path", "path" to setting.path))
                        if (kind == Kind.READ_ONLY) {
                            add(menu.tr("admin-config.read-only"))
                        } else {
                            add(menu.tr("common.current", "value" to display(player, kind, value)))
                            add(menu.tr("admin-config.default", "value" to display(player, kind, setting.default)))
                        }
                    })
                    if (kind != Kind.READ_ONLY) {
                        loreActions(buildList {
                            add(
                                menu.tr(
                                    when {
                                        !editable -> "menu.no-permission"
                                        kind == Kind.BOOLEAN -> if (on) "icon.click-disable" else "icon.click-enable"
                                        else -> "click.change"
                                    }
                                )
                            )
                            if (editable && value != setting.default) add(menu.tr("admin-config.right-reset"))
                        })
                    }
                    glow(on)
                }
            }
            if (!editable) return MenuEntry(icon)
            return MenuEntry(icon) { click ->
                when {
                    click.isRightClick -> apply(menu, setting, setting.default)
                    kind == Kind.BOOLEAN -> apply(menu, setting, (!setting.value.toBoolean()).toString())
                    else -> edit(menu, setting)
                }
            }
        }

        private fun edit(menu: Menu, setting: TownyConfig.Setting) {
            val current = setting.value
            val long = setting.kind == Kind.TEXT && current.length > 48
            menu.prompt(
                setting.name,
                menu.tr(if (setting.kind == Kind.TEXT) "admin-config.value-label" else "admin-config.number-label"),
                shorten(setting.description).ifEmpty { null },
                initial = current,
                maxLength = maxOf(256, current.length * 2),
                multiline = long,
            ) { input ->
                val value = setting.parse(input.replace(LINE_BREAKS, if (long) "" else " "))
                if (value == null) {
                    menu.player.sendPrefixed(menu.tr("admin-config.invalid-value", "value" to TownyUtil.text(input)))
                } else {
                    apply(menu, setting, value)
                }
            }
        }

        private val LINE_BREAKS = Regex("\\s*\\n\\s*")

        private fun apply(menu: Menu, setting: TownyConfig.Setting, value: String) {
            if (value == setting.value) return menu.open()
            TownyConfig.write(setting, value)
            menu.run(RELOAD, ::loadedConfig)
        }

        private fun display(player: Player, kind: Kind, value: String): String = when {
            kind == Kind.BOOLEAN -> TownyUtil.onOff(player, value.toBoolean())
            value.isEmpty() -> player.tr("admin-config.empty")
            else -> TownyUtil.text(shortenValue(value))
        }

        private fun shortenValue(value: String): String = if (value.length <= 60) value else value.take(57) + "..."
    }
}
