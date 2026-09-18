package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Buys, sells, and gives up every plot in a square or circle around the viewer,
 * using Towny's `rect`/`circle` area selections. The shape and radius live in
 * this menu until the player runs an action.
 */
class PlotAreaMenu(player: Player, back: Menu) : Menu(player, player.tr("plot-area.title"), 6, back) {

    private var circle = false
    private var radius = "1"

    private val area: String
        get() = "${if (circle) "circle" else "rect"} $radius"

    override fun build() {
        button(4, Icons.icon(Material.FILLED_MAP, tr("plot-area.info"), tr("plot-area.info-description")))

        button(
            21, Icons.icon(
                if (circle) Material.SNOWBALL else Material.PAINTING, tr("plot-area.shape"), null,
                tr("common.current", "value" to tr(if (circle) "plot-area.circle" else "plot-area.square")),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            circle = !circle
            render()
        }
        button(
            23, Icons.icon(
                Material.COMPASS, tr("plot-area.radius"), tr("plot-area.radius-description"),
                tr("common.current", "value" to radius),
                actions = listOf(tr("common.click-change"))
            )
        ) {
            prompt(tr("plot-area.radius-title"), tr("claims.radius"), initial = radius, maxLength = 3) {
                radius = TownyUtil.argument(it)
                open()
            }
        }

        val owned = { resident?.townBlocks?.size }
        val actions = layout(29, 30, 31, 32, 33)
        actions.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_CLAIM,
            Icons.icon(Material.EMERALD, tr("plot-area.buy"), tr("plot-area.buy-description"))
        ) {
            run("towny:plot claim $area", owned, delayTicks = CLAIM_DELAY)
        }
        if (TownyUtil.economy) {
            actions.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_FORSALE,
                Icons.icon(Material.GREEN_BANNER, tr("plot-area.sell"), tr("plot-area.sell-description"))
            ) {
                val price = TownyAPI.getInstance().getTown(player.location)?.plotPrice?.takeIf { it >= 0 } ?: 0.0
                prompt(
                    tr("plot-area.sell-title"),
                    tr("common.price-label"),
                    tr("plot.for-sale-hint"),
                    initial = price.toBigDecimal().stripTrailingZeros().toPlainString()
                ) { input ->
                    run("towny:plot forsale ${TownyUtil.argument(input)} within $area")
                }
            }
        }
        actions.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_NOTFORSALE,
            Icons.icon(Material.RED_BANNER, tr("plot-area.not-for-sale"), tr("plot-area.not-for-sale-description"))
        ) {
            run("towny:plot notforsale $area")
        }
        actions.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM,
            Icons.icon(Material.COARSE_DIRT, tr("plot-area.give-up"), tr("plot-area.give-up-description"))
        ) {
            run("towny:plot unclaim $area", owned, delayTicks = CLAIM_DELAY)
        }
        actions.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM,
            Icons.icon(
                Material.LAVA_BUCKET, tr("plot-area.give-up-all"), tr("plot-area.give-up-all-description"),
                tr("profile.plots-owned", "count" to (resident?.townBlocks?.size ?: 0))
            )
        ) {
            run("towny:plot unclaim all", owned, delayTicks = CLAIM_DELAY)
        }

        tutorialButton(53, Tutorial.PLOTS)
        backButton(49)
    }

    private companion object {
        const val CLAIM_DELAY = 20L
    }
}
