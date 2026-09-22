package net.trilleo.mc.plugins.townymenu.guis

import net.trilleo.mc.plugins.townymenu.borders.BorderView
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** The viewer's [BorderView] settings: particle borders on or off, their own plots, the range, and a colour key. */
class BorderViewMenu(player: Player, back: Menu?) : Menu(player, player.tr("border-view.title"), 3, back) {

    override fun build() {
        if (!BorderView.available) {
            button(13, Icons.icon(Material.BARRIER, tr("border-view.unavailable"), tr("border-view.unavailable-description")))
            backButton(22)
            return
        }

        button(
            10, Icons.toggle(
                player, Material.ENDER_EYE, tr("border-view.show"), BorderView.isOn(player),
                tr("border-view.show-description")
            )
        ) {
            BorderView.setOn(player, !BorderView.isOn(player))
            render()
        }
        button(
            12, Icons.toggle(
                player, Material.GLOWSTONE_DUST, tr("border-view.plots"), BorderView.showsPlots(player),
                tr("border-view.plots-description")
            )
        ) {
            BorderView.setShowsPlots(player, !BorderView.showsPlots(player))
            render()
        }
        val range = BorderView.range(player)
        button(
            14, Icons.icon(
                Material.SPYGLASS, tr("border-view.range"), tr("border-view.range-description"),
                tr("border-view.range-current", "range" to range),
                actions = hints("click.next", "click.previous")
            )
        ) { click ->
            BorderView.setRange(player, BorderView.ranges.cycle(range, click))
            render()
        }
        button(
            16, Icons.icon(
                Material.PAPER, tr("map.legend"), tr("border-view.legend-description"),
                tr("map.legend-town"), tr("map.legend-plot"), tr("map.legend-nation"), tr("map.legend-ally"),
                tr("map.legend-enemy"), tr("map.legend-other"), tr("border-view.legend-nation-border")
            )
        )
        backButton(22)
        tutorialButton(26, Tutorial.PROFILE)
    }
}
