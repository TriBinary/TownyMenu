package net.trilleo.mc.plugins.townymenu.utils

import com.palmergames.bukkit.config.ConfigNodes
import com.palmergames.bukkit.towny.TownySettings
import org.bukkit.configuration.ConfigurationSection

/**
 * Towny's `config.yml` as a browsable tree, built from Towny's own [ConfigNodes].
 *
 * Towny has no command that edits its config, so values are written through
 * [TownySettings] and saved with Towny's comments intact. Callers then run
 * `/townyadmin reload config` as the player so the change takes effect.
 */
object TownyConfig {

    enum class Kind { BOOLEAN, INTEGER, DECIMAL, TEXT, READ_ONLY }

    /** One editable leaf of the config. [description] is Towny's English comment, already escaped for MiniMessage. */
    class Setting(val path: String, val default: String, val description: String) {
        val name: String = path.substringAfterLast('.').replace('_', ' ')

        val kind: Kind
            get() = when {
                TownySettings.getConfig().let { it.isList(path) || it.isConfigurationSection(path) } -> Kind.READ_ONLY
                default == "true" || default == "false" -> Kind.BOOLEAN
                INTEGER.matches(default) -> Kind.INTEGER
                DECIMAL.matches(default) -> Kind.DECIMAL
                else -> Kind.TEXT
            }

        /** The value currently loaded by Towny, or the default when the file lacks it. */
        val value: String
            get() = TownySettings.getConfig().get(path)?.takeUnless { it is ConfigurationSection || it is List<*> }
                ?.toString() ?: default

        /** [input] as this setting stores it, or `null` when it is not a valid value for [kind]. */
        fun parse(input: String): String? = when (kind) {
            Kind.BOOLEAN -> input.lowercase().toBooleanStrictOrNull()?.toString()
            Kind.INTEGER -> input.toIntOrNull()?.toString()
            Kind.DECIMAL -> input.toDoubleOrNull()?.toString()
            Kind.TEXT -> input
            Kind.READ_ONLY -> null
        }
    }

    /** A config section: its dotted [path] (empty for the root), readable name, and Towny's comment. */
    class Section(val path: String, val description: String) {
        val name: String = path.substringAfterLast('.').replace('_', ' ')
    }

    private val INTEGER = Regex("-?\\d+")
    private val DECIMAL = Regex("-?\\d+\\.\\d+")
    private val BANNER = Regex("[#+|=\\-\\s]*")

    /** Nodes Towny manages itself or that only carry a comment. */
    private val HIDDEN = setOf("version", "permissions")

    private val nodes: List<ConfigNodes> by lazy {
        ConfigNodes.entries.filter { it.root.substringBefore('.') !in HIDDEN }
    }

    private val settings: List<Setting> by lazy {
        nodes.filter { node -> nodes.none { it.root.startsWith(node.root + ".") } }
            .map { Setting(it.root, it.default, describe(it)) }
    }

    private val descriptions: Map<String, String> by lazy { nodes.associate { it.root to describe(it) } }

    /** The direct child sections of [section] (`""` for the top level), in Towny's order. */
    fun sections(section: String): List<Section> =
        settings.mapNotNull { setting -> childSection(section, setting.path) }
            .distinct()
            .map { Section(it, descriptions[it].orEmpty()) }

    /** The settings directly inside [section], in Towny's order. */
    fun settings(section: String): List<Setting> =
        settings.filter { it.path.substringBeforeLast('.', "") == section }

    /** Number of settings anywhere below [section]. */
    fun count(section: String): Int = settings.count { it.path.startsWith("$section.") }

    /** Settings whose path or description contains [query], ignoring case. */
    fun search(query: String): List<Setting> {
        val words = query.lowercase().split(' ', '_').filter { it.isNotBlank() }
        return settings.filter { setting ->
            val haystack = "${setting.path.replace('_', ' ')} ${setting.description}".lowercase()
            words.all(haystack::contains)
        }
    }

    /** Sets [setting] to [value] in memory and saves Towny's `config.yml`. */
    fun write(setting: Setting, value: String) {
        TownySettings.setProperty(setting.path, value)
        TownySettings.saveConfig()
    }

    private fun childSection(parent: String, path: String): String? {
        val prefix = if (parent.isEmpty()) "" else "$parent."
        if (!path.startsWith(prefix)) return null
        val rest = path.removePrefix(prefix)
        return if ('.' in rest) prefix + rest.substringBefore('.') else null
    }

    private fun describe(node: ConfigNodes): String =
        node.comments
            .map { it.trim().trim('#').trim().trim('|').trim() }
            .filterNot { BANNER.matches(it) }
            .joinToString(" ")
            .let(TownyUtil::text)
}
