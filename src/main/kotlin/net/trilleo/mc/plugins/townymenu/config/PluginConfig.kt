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

    private fun save(path: String, value: Any) {
        plugin.config.set(path, value)
        plugin.saveConfig()
    }
}
