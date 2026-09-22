package net.trilleo.mc.plugins.townymenu.borders

import net.trilleo.mc.plugins.townymenu.Main
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

/**
 * Each player's border view settings, kept in their persistent data so they survive restarts.
 * [BorderParticles] reads them every time it draws.
 */
object BorderView {

    /** The distances, in blocks, a player can pick from; [ranges] drops those above the server's limit. */
    private val RANGES = listOf(4, 8, 12, 16, 24, 32)
    private const val DEFAULT_RANGE = 8

    private val enabledKey by lazy { NamespacedKey(Main.instance, "border-view") }
    private val plotsKey by lazy { NamespacedKey(Main.instance, "border-view-plots") }
    private val rangeKey by lazy { NamespacedKey(Main.instance, "border-view-range") }

    /** Whether the server lets players use border view at all (`border-view` in `config.yml`). */
    val available: Boolean
        get() = Main.instance.pluginConfig.borderView

    private val maxRange: Int
        get() = Main.instance.pluginConfig.borderViewMaxRange

    fun isOn(player: Player): Boolean =
        player.persistentDataContainer.getOrDefault(enabledKey, PersistentDataType.BOOLEAN, false)

    fun setOn(player: Player, on: Boolean) =
        player.persistentDataContainer.set(enabledKey, PersistentDataType.BOOLEAN, on)

    /** Whether the edges of the player's own plots are drawn too. */
    fun showsPlots(player: Player): Boolean =
        player.persistentDataContainer.getOrDefault(plotsKey, PersistentDataType.BOOLEAN, true)

    fun setShowsPlots(player: Player, show: Boolean) =
        player.persistentDataContainer.set(plotsKey, PersistentDataType.BOOLEAN, show)

    /** How close, in blocks, the player must be to a border to see it, capped at the server's limit. */
    fun range(player: Player): Int =
        (player.persistentDataContainer.get(rangeKey, PersistentDataType.INTEGER) ?: DEFAULT_RANGE)
            .coerceAtMost(maxRange)

    fun setRange(player: Player, range: Int) =
        player.persistentDataContainer.set(rangeKey, PersistentDataType.INTEGER, range)

    val ranges: List<Int>
        get() = RANGES.filter { it <= maxRange }.ifEmpty { listOf(maxRange) }
}
