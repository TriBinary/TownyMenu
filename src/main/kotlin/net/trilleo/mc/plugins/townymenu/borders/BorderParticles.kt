package net.trilleo.mc.plugins.townymenu.borders

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.WorldCoord
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil.Relation
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.floor

/**
 * Draws a short wall of dust particles along every claim border near a player with [BorderView] on.
 *
 * Each side of a border is drawn just inside its own claim, in the colour of that town's relation to the viewer
 * (the same colours as the map), so a border between two towns shows both colours side by side. Nation borders are
 * taller and thicker than borders between towns of one nation, and the viewer's own plots get a low yellow line.
 * Only the viewer sees the particles, and only the part of a border within their chosen range.
 */
object BorderParticles {

    private const val INTERVAL_TICKS = 10L
    private const val INSET = 0.2

    private enum class Kind(val size: Float, val rows: Int) {
        NATION(1.4f, 3),
        TOWN(1.0f, 2),
        PLOT(0.7f, 1),
    }

    private enum class Side(val dx: Int, val dz: Int) { NORTH(0, -1), SOUTH(0, 1), WEST(-1, 0), EAST(1, 0) }

    private val relationColors = mapOf(
        Relation.TOWN to Color.fromRGB(0x55FF55),
        Relation.NATION to Color.fromRGB(0x55FFFF),
        Relation.ALLY to Color.fromRGB(0x5555FF),
        Relation.ENEMY to Color.fromRGB(0xFF5555),
        Relation.OTHER to Color.fromRGB(0xFFAA00),
    )
    private val plotColor = Color.fromRGB(0xFFFF55)

    fun start(plugin: JavaPlugin) {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (!BorderView.available) return@Runnable
            plugin.server.onlinePlayers.filter(BorderView::isOn).forEach(::draw)
        }, INTERVAL_TICKS, INTERVAL_TICKS)
    }

    private fun draw(player: Player) {
        val api = TownyAPI.getInstance()
        val world = player.world
        if (!api.isTownyWorld(world)) return
        val viewer = api.getResident(player)
        val plots = viewer != null && BorderView.showsPlots(player)
        val range = BorderView.range(player).toDouble()
        val size = TownySettings.getTownBlockSize()
        val location = player.location

        val blocks = HashMap<Pair<Int, Int>, TownBlock?>()
        fun block(x: Int, z: Int) = blocks.getOrPut(x to z) { api.getTownBlock(WorldCoord(world.name, x, z)) }

        for (x in floor((location.x - range) / size).toInt()..floor((location.x + range) / size).toInt()) {
            for (z in floor((location.z - range) / size).toInt()..floor((location.z + range) / size).toInt()) {
                val plot = block(x, z) ?: continue
                val town = plot.townOrNull ?: continue
                for (side in Side.entries) {
                    val neighbour = block(x + side.dx, z + side.dz)
                    val neighbourTown = neighbour?.townOrNull
                    val kind = when {
                        neighbourTown != town ->
                            if (town.hasNation() && neighbourTown?.nationOrNull != town.nationOrNull) Kind.NATION
                            else Kind.TOWN

                        plots && ownedBy(plot, viewer) && !ownedBy(neighbour, viewer) -> Kind.PLOT
                        else -> continue
                    }
                    val color = if (kind == Kind.PLOT) plotColor
                    else relationColors.getValue(TownyUtil.relation(viewer, town))
                    edge(player, x, z, size, side, kind, Particle.DustOptions(color, kind.size), range)
                }
            }
        }
    }

    private fun ownedBy(plot: TownBlock?, viewer: Resident?): Boolean =
        viewer != null && plot?.residentOrNull == viewer

    /** Draws one block-spaced line along [side] of the townblock at [x], [z], inset into that townblock. */
    private fun edge(
        player: Player, x: Int, z: Int, size: Int, side: Side, kind: Kind, dust: Particle.DustOptions, range: Double,
    ) {
        val location = player.location
        val fixed = when (side) {
            Side.NORTH -> z * size + INSET
            Side.SOUTH -> (z + 1) * size - INSET
            Side.WEST -> x * size + INSET
            Side.EAST -> (x + 1) * size - INSET
        }
        val alongX = side == Side.NORTH || side == Side.SOUTH
        val start = if (alongX) x * size else z * size
        repeat(size) { step ->
            val along = start + step + 0.5
            val px = if (alongX) along else fixed
            val pz = if (alongX) fixed else along
            val dx = px - location.x
            val dz = pz - location.z
            if (dx * dx + dz * dz > range * range) return@repeat
            repeat(kind.rows) { row ->
                player.spawnParticle(Particle.DUST, px, location.y + 0.5 + row, pz, 1, 0.0, 0.0, 0.0, 0.0, dust)
            }
        }
    }
}
