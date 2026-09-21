package net.trilleo.mc.plugins.townymenu.guis.resident

import com.palmergames.bukkit.towny.`object`.Resident
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.framework.PagedMenu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Every plot [owner] owns, with where it is, its type, and what it costs them each day. */
class ResidentPlotsMenu(player: Player, private val owner: Resident, back: Menu) :
    PagedMenu(player, player.tr("resident-plots.title"), back) {

    override fun entries(): List<MenuEntry> =
        owner.townBlocks
            .sortedWith(
                compareBy(
                    { it.townOrNull?.name?.lowercase() },
                    { it.worldCoord.worldName },
                    { it.x },
                    { it.z })
            )
            .map { plot ->
                val town = plot.townOrNull
                MenuEntry({
                    Icons.icon(
                        Material.GRASS_BLOCK,
                        "<gold>${if (plot.name.isNullOrBlank()) tr("plot.unnamed") else TownyUtil.text(plot.name)}",
                        null,
                        *buildList {
                            add(tr("icon.resident.town", "town" to (town?.let { TownyUtil.name(it.name) } ?: "-")))
                            add(
                                tr(
                                    "resident-plots.location",
                                    "world" to TownyUtil.text(plot.worldCoord.worldName),
                                    "x" to plot.x,
                                    "z" to plot.z
                                )
                            )
                            add(tr("map.type", "type" to TownyUtil.plotType(player, plot.type.name)))
                            if (plot.hasPlotObjectGroup()) {
                                add(tr("plot.group", "group" to TownyUtil.name(plot.plotObjectGroup.name)))
                            }
                            if (TownyUtil.economy && town != null) {
                                if (plot.isForSale) add(tr("map.for-sale", "price" to TownyUtil.money(plot.plotPrice)))
                                if (plot.isTaxed) add(
                                    tr(
                                        "bank.daily-tax",
                                        "tax" to TownyUtil.money(plot.type.getTax(town))
                                    )
                                )
                            }
                        }.toTypedArray(),
                        actions = listOfNotNull(if (town != null) tr("click.details") else null)
                    )
                }) { town?.let { TownInfoMenu(player, it, this).open() } }
            }

    override fun controls() {
        tutorialButton(52, Tutorial.PROFILE)
    }
}
