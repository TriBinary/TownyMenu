package net.trilleo.mc.plugins.townymenu.config

import org.bukkit.plugin.java.JavaPlugin

/**
 * A typed wrapper around the plugin's `config.yml`.
 *
 * On construction the default configuration is saved (if the file does not
 * yet exist). Values are read live from the loaded configuration, so call
 * [reload] to pick up changes made on disk without restarting the server.
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
    val messagePrefix: String
        get() = plugin.config.getString("message-prefix") ?: "[TownyMenu]"

    /** Whether pressing swap-hand (F) while sneaking opens the main menu (`sneak-swap-hand-shortcut`). */
    val sneakSwapHandShortcut: Boolean
        get() = plugin.config.getBoolean("sneak-swap-hand-shortcut", true)
}
