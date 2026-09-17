package net.trilleo.mc.plugins.townymenu

import net.trilleo.mc.plugins.townymenu.config.PluginConfig
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.registration.CommandRegistrar
import net.trilleo.mc.plugins.townymenu.registration.ListenerRegistrar
import net.trilleo.mc.plugins.townymenu.registration.PermissionRegistrar
import net.trilleo.mc.plugins.townymenu.utils.MessageUtil
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    lateinit var pluginConfig: PluginConfig
        private set

    override fun onEnable() {
        instance = this
        pluginConfig = PluginConfig(this)
        MessageUtil.init(pluginConfig.messagePrefix)

        CommandRegistrar.registerAll(this)
        PermissionRegistrar.registerAll(this)
        ListenerRegistrar.registerAll(this)
    }

    override fun onDisable() {
        // Menu items are only protected while this plugin's listeners run, so no menu may outlive it.
        server.onlinePlayers
            .filter { it.openInventory.topInventory.getHolder(false) is Menu }
            .forEach { it.closeInventory() }
    }

    companion object {
        lateinit var instance: Main
            private set
    }
}
