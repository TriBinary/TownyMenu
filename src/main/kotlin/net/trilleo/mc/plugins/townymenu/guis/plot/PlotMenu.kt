package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.TownBlockTypeHandler
import com.palmergames.bukkit.towny.`object`.WorldCoord
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The plot the viewer is standing in. Every render re-reads the player's
 * location, so walking to another chunk and refreshing shows that chunk.
 */
class PlotMenu(player: Player, back: Menu?) : Menu(player, player.tr("plot.title"), 6, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        if (plot == null) wilderness() else claimed(plot)
        button(50, Icons.icon(Material.CLOCK, tr("map.refresh"), tr("plot.refresh-description"))) { render() }
        tutorialButton(53, Tutorial.PLOTS)
        backButton(49)
    }

    private fun wilderness() {
        val coord = WorldCoord.parseWorldCoord(player)
        button(
            4, Icons.icon(
                Material.OAK_SAPLING, "<green>${tr("main.wilderness")}", tr("plot.wilderness-description"),
                tr("map.chunk", "x" to coord.x, "z" to coord.z)
            )
        )
        val town = resident?.townOrNull
        val grid = layout(20, 21, 22, 23, 24)
        if (town != null) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
                Icons.icon(
                    Material.GRASS_BLOCK,
                    tr("plot.claim", "town" to TownyUtil.name(town.name)),
                    tr("plot.claim-description")
                )
            ) {
                run("towny:town claim", { town.numTownBlocks }, delayTicks = 20)
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                Icons.icon(Material.COMPASS, tr("plot.claim-outpost"), tr("plot.claim-outpost-description"))
            ) {
                run("towny:town claim outpost", { town.numTownBlocks }, delayTicks = 20)
            }
        }
        grid.add(Icons.icon(Material.FILLED_MAP, tr("main.map"), tr("common.map-description"))) {
            MapMenu(
                player,
                this
            ).open()
        }
    }

    private fun claimed(plot: TownBlock) {
        val town = plot.townOrNull
        val owner = plot.residentOrNull
        val viewer = resident
        button(
            4,
            Icons.icon(
                Material.GRASS_BLOCK,
                "<gold>${if (plot.name.isNullOrBlank()) tr("plot.unnamed") else TownyUtil.text(plot.name)}",
                null,
                *buildList {
                    add(tr("icon.resident.town", "town" to (town?.let { TownyUtil.name(it.name) } ?: "-")))
                    add(tr("map.owner", "owner" to (owner?.let { TownyUtil.name(it.name) } ?: tr("map.owner-town"))))
                    add(tr("map.type", "type" to TownyUtil.plotType(player, plot.type.name)))
                    add(tr("map.chunk", "x" to plot.x, "z" to plot.z))
                    if (plot.isHomeBlock) add(tr("map.home-block"))
                    if (plot.isOutpost) add(tr("map.outpost"))
                    if (plot.hasPlotObjectGroup()) add(
                        tr(
                            "plot.group",
                            "group" to TownyUtil.name(plot.plotObjectGroup.name)
                        )
                    )
                    if (plot.hasDistrict()) add(tr("plot.district", "district" to TownyUtil.name(plot.district.name)))
                    if (TownyUtil.economy) {
                        if (plot.isForSale) add(tr("map.for-sale", "price" to TownyUtil.money(plot.plotPrice)))
                        if (plot.isTaxed) add(tr("bank.daily-tax", "tax" to TownyUtil.money(plot.plotTax)))
                    }
                    add(
                        tr(
                            "town.pvp-mobs",
                            "pvp" to TownyUtil.onOff(player, plot.permissions.pvp),
                            "mobs" to TownyUtil.onOff(player, plot.permissions.mobs)
                        )
                    )
                    add(
                        tr(
                            "town.fire-explosions", "fire" to TownyUtil.onOff(player, plot.permissions.fire),
                            "explosions" to TownyUtil.onOff(player, plot.permissions.explosion)
                        )
                    )
                    add(tr("plot.claimed", "date" to TownyUtil.date(plot.claimedAt)))
                }.toTypedArray()
            )
        )

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
        val ownerProbe = { plot.residentOrNull }
        val saleProbe = { plot.isForSale to plot.plotPrice }

        if (plot.isForSale && owner != viewer) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_CLAIM, Icons.icon(
                    Material.EMERALD, tr("plot.buy"), tr("plot.buy-description"),
                    tr("common.price", "price" to TownyUtil.money(plot.plotPrice)),
                )
            ) { run("towny:plot claim", ownerProbe) }
        }
        if (owner != null && owner == viewer) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM,
                Icons.icon(Material.COARSE_DIRT, tr("plot.give-up"), tr("plot.give-up-description"))
            ) {
                run("towny:plot unclaim", ownerProbe)
            }
        }
        if (plot.isForSale) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_NOTFORSALE,
                Icons.icon(Material.RED_BANNER, tr("plot.not-for-sale"), tr("plot.not-for-sale-description"))
            ) {
                run("towny:plot notforsale", saleProbe)
            }
        } else {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_FORSALE,
                Icons.icon(Material.GREEN_BANNER, tr("plot.for-sale"), tr("plot.for-sale-description"))
            ) {
                prompt(
                    tr("plot.for-sale-title"),
                    tr("common.price-label"),
                    tr("plot.for-sale-hint"),
                    initial = "0"
                ) { price ->
                    run("towny:plot forsale ${TownyUtil.argument(price)}", saleProbe)
                }
            }
        }
        if (owner != null) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_EVICT,
                Icons.icon(
                    Material.IRON_BOOTS,
                    tr("plot.evict"),
                    tr("plot.evict-description", "owner" to TownyUtil.name(owner.name))
                )
            ) {
                run("towny:plot evict", ownerProbe)
            }
        }
        grid.add(
            Icons.icon(
                Material.OAK_SIGN, tr("plot.type"), tr("plot.type-description"),
                tr("common.current", "value" to TownyUtil.plotType(player, plot.type.name))
            )
        ) {
            val types = listOf("reset" to "<white>${TownyUtil.plotType(player, "default")}") +
                    TownBlockTypeHandler.getTypeNames().filter { it != "default" }.sorted()
                        .map { it to "<white>${TownyUtil.plotType(player, it)}" }
            Pickers.option(
                this,
                tr("plot.type-title"),
                types,
                plot.type.name.takeIf { it != "default" } ?: "reset",
                Material.OAK_SIGN) { type ->
                run("towny:plot set $type", { plot.type.name }, returnTo = this)
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_SET_NAME,
            Icons.icon(Material.NAME_TAG, tr("plot.rename"), tr("plot.rename-description"))
        ) {
            prompt(tr("plot.rename-title"), tr("plot.plot-name"), initial = plot.name.orEmpty()) { name ->
                run("towny:plot set name ${TownyUtil.nameArgument(name)}", { plot.name })
            }
        }
        grid.add(Icons.icon(Material.LEVER, tr("plot.settings"), tr("plot.settings-description"))) {
            toggles(plot).open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_SET_PERM,
            Icons.icon(Material.IRON_DOOR, tr("common.permissions"), tr("plot.permissions-description"))
        ) {
            PermissionMenu(
                player, tr("plot.permissions-title"), this, "towny:plot set perm",
                PermissionNodes.TOWNY_COMMAND_PLOT_SET_PERM, owner != null
            ) { plot.takeIf { it.exists() }?.permissions }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_TRUST, Icons.icon(
                Material.TRIPWIRE_HOOK, tr("plot.trusted"), tr("plot.trusted-description"),
                tr("plot.trusted-count", "count" to plot.trustedResidents.size),
            )
        ) { PlotTrustMenu(player, plot, this).open() }
        grid.add(Icons.icon(Material.CHEST, tr("plot.groups"), tr("plot.groups-description"))) {
            PlotGroupMenu(player, this).open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_CLEAR,
            Icons.icon(Material.BRUSH, tr("plot.clear"), tr("plot.clear-description"))
        ) {
            runAndClose("towny:plot clear")
        }
        if (town != null) {
            grid.add(Icons.icon(Material.BELL, "<gold>${TownyUtil.name(town.name)}", tr("plot.town-description"))) {
                TownInfoMenu(player, town, this).open()
            }
        }
        grid.add(Icons.icon(Material.FILLED_MAP, tr("main.map"), tr("common.map-description"))) {
            MapMenu(
                player,
                this
            ).open()
        }
    }

    private fun toggles(plot: TownBlock): Menu {
        fun toggle(
            material: Material,
            key: String,
            label: String,
            description: String,
            node: PermissionNodes,
            read: (TownBlock) -> Boolean
        ) =
            Toggle(material, label, description, node, "towny:plot toggle $key") {
                plot.takeIf { it.exists() }?.let(read)
            }

        return ToggleMenu(
            player, tr("plot.settings-title"), this, listOf(
                toggle(
                    Material.IRON_SWORD,
                    "pvp",
                    tr("toggle.pvp"),
                    tr("toggle.plot-pvp-description"),
                    PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_PVP
                ) { it.permissions.pvp },
                toggle(
                    Material.FLINT_AND_STEEL,
                    "fire",
                    tr("toggle.fire"),
                    tr("toggle.plot-fire-description"),
                    PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_FIRE
                ) { it.permissions.fire },
                toggle(
                    Material.TNT,
                    "explosion",
                    tr("toggle.explosions"),
                    tr("toggle.plot-explosions-description"),
                    PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_EXPLOSION
                ) { it.permissions.explosion },
                toggle(
                    Material.ZOMBIE_HEAD,
                    "mobs",
                    tr("toggle.mobs"),
                    tr("toggle.plot-mobs-description"),
                    PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_MOBS
                ) { it.permissions.mobs },
                toggle(
                    Material.GOLD_NUGGET,
                    "taxed",
                    tr("toggle.taxed"),
                    tr("toggle.taxed-description"),
                    PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR
                ) { it.isTaxed },
            )
        )
    }
}
