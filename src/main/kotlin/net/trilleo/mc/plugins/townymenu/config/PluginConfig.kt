package net.trilleo.mc.plugins.townymenu.config

import org.bukkit.plugin.java.JavaPlugin

/**
 * A typed wrapper around the plugin's `config.yml`.
 *
 * On construction the default configuration is saved (if the file does not
 * yet exist). Values are read live from the loaded configuration, so call
 * [reload] to pick up changes made on disk without restarting the server.
 * Assigning a property saves `config.yml` immediately; call
 * [net.trilleo.mc.plugins.townymenu.Main.reload] afterwards to apply it.
 *
 * @param plugin the owning plugin instance
 */
class PluginConfig(private val plugin: JavaPlugin) {

    init {
        plugin.saveDefaultConfig()
    }

    /** Re-reads `config.yml` from disk, adding any keys missing from the file. */
    fun reload() {
        plugin.reloadConfig()
        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    /** The MiniMessage prefix shown before plugin messages (`message-prefix`). */
    var messagePrefix: String
        get() = plugin.config.getString("message-prefix") ?: "[TownyMenu]"
        set(value) = save("message-prefix", value)

    /** `auto` to follow each player's client language, or a language id such as `zh_CN` (`language`). */
    var language: String
        get() = plugin.config.getString("language") ?: "auto"
        set(value) = save("language", value)

    /** Whether pressing swap-hand (F) while sneaking opens the main menu (`sneak-swap-hand-shortcut`). */
    var sneakSwapHandShortcut: Boolean
        get() = plugin.config.getBoolean("sneak-swap-hand-shortcut", true)
        set(value) = save("sneak-swap-hand-shortcut", value)

    /** Whether players without a town are pointed to the tutorial when they join (`tutorial-join-hint`). */
    var tutorialJoinHint: Boolean
        get() = plugin.config.getBoolean("tutorial-join-hint", true)
        set(value) = save("tutorial-join-hint", value)

    /** Whether open menus are re-rendered when Towny changes what they show (`live-menu-refresh`). */
    var liveMenuRefresh: Boolean
        get() = plugin.config.getBoolean("live-menu-refresh", true)
        set(value) = save("live-menu-refresh", value)

    /** Whether TownyMenu announces invitations, bankruptcy, and ruin in chat (`towny-alerts`). */
    var townyAlerts: Boolean
        get() = plugin.config.getBoolean("towny-alerts", true)
        set(value) = save("towny-alerts", value)

    /** Whether players may turn on particle borders for towns and nations (`border-view`). */
    var borderView: Boolean
        get() = plugin.config.getBoolean("border-view", true)
        set(value) = save("border-view", value)

    /** The largest border view range, in blocks, a player may pick (`border-view-max-range`), kept within 1–64. */
    var borderViewMaxRange: Int
        get() = plugin.config.getInt("border-view-max-range", 16).coerceIn(1, 64)
        set(value) = save("border-view-max-range", value.coerceIn(1, 64))

    /**
     * The preset amounts the deposit and withdraw menus offer (`bank-amounts`).
     *
     * Towny's bank commands take whole numbers, and only nine buttons fit in the menu row,
     * so the list is cleaned up on the way in and out: non-positive amounts are dropped,
     * duplicates removed, and the rest sorted and capped at nine. An empty list is a choice —
     * the menus then offer only "All" and a custom amount — so only a missing key falls back.
     */
    var bankAmounts: List<Int>
        get() = if (plugin.config.isSet("bank-amounts")) clean(plugin.config.getIntegerList("bank-amounts"))
        else DEFAULT_BANK_AMOUNTS
        set(value) = save("bank-amounts", clean(value))

    private fun save(path: String, value: Any) {
        plugin.config.set(path, value)
        plugin.saveConfig()
    }

    private fun clean(amounts: List<Int>): List<Int> = amounts.filter { it > 0 }.distinct().sorted().take(9)

    companion object {
        private val DEFAULT_BANK_AMOUNTS = listOf(10, 100, 1000, 10000)
    }
}
