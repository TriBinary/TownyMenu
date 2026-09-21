package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.PlotGroup
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
import net.trilleo.mc.plugins.townymenu.utils.Prices
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The plot the viewer is standing in. Every render re-reads the player's
 * location, so walking to another chunk and refreshing shows that chunk.
 *
 * Towny refuses most single-plot commands on a plot in a group, so for grouped
 * plots the sale, type, settings, permission, trust, and join-day buttons run
 * the matching `/plot group` command and change every plot in the group.
 */
class PlotMenu(player: Player, back: Menu?) : Menu(player, player.tr("plot.title"), 6, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        if (plot == null) wilderness() else claimed(plot)
        guarded(
            48, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_HUD,
            Icons.icon(Material.SPYGLASS, tr("plot.hud"), tr("plot.hud-description"), actions = hints("click.show-hud"))
        ) {
            runAndClose("towny:plot perm hud")
        }
        button(47, Icons.icon(
            Material.FILLED_MAP, tr("main.map"), tr("common.map-description"),
            actions = hints("click.open")
        )) {
            MapMenu(player, this).open()
        }
        button(50, Icons.icon(
            Material.CLOCK, tr("map.refresh"), tr("plot.refresh-description"),
            actions = hints("click.refresh")
        )) { render() }
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
                    tr("plot.claim-description"),
                    *listOfNotNull(costLine(Prices.claim(town))).toTypedArray(),
                    actions = hints("click.claim")
                )
            ) {
                run("towny:town claim", { town.numTownBlocks }, delayTicks = 20)
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                Icons.icon(
                    Material.COMPASS, tr("plot.claim-outpost"), tr("plot.claim-outpost-description"),
                    *outpostCost().toTypedArray(),
                    actions = hints("click.claim")
                )
            ) {
                run("towny:town claim outpost", { town.numTownBlocks }, delayTicks = 20)
            }
        }
        grid.add(Icons.icon(
            Material.MAP, tr("plot.area"), tr("plot.area-description"),
            actions = hints("click.open")
        )) {
            PlotAreaMenu(player, this).open()
        }
    }

    private fun claimed(plot: TownBlock) {
        val town = plot.townOrNull
        val owner = plot.residentOrNull
        val viewer = resident
        val group = plot.plotObjectGroup
        val scope = if (group != null) "towny:plot group" else "towny:plot"
        val groupLines = listOfNotNull(group?.let {
            tr("plot.group-applies", "group" to TownyUtil.name(it.name), "count" to it.townBlocks.size)
        }).toTypedArray()
        val forSale = if (group != null) group.price >= 0 else plot.isForSale
        val price = group?.price ?: plot.plotPrice
        val canInfo = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_PLOT_PERM_INFO)
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
                    if (group != null) add(tr("plot.group", "group" to TownyUtil.name(group.name)))
                    if (plot.hasDistrict()) add(tr("plot.district", "district" to TownyUtil.name(plot.district.name)))
                    if (TownyUtil.economy) {
                        if (forSale) add(tr("map.for-sale", "price" to TownyUtil.money(price)))
                        if (plot.isTaxed) add(tr("bank.daily-tax", "tax" to TownyUtil.money(plot.plotTax)))
                    }
                    addAll(joinDayLines(plot, group))
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
                }.toTypedArray(),
                actions = listOfNotNull(if (canInfo) tr("plot.click-info") else null)
            )
        ) {
            if (canInfo) runAndClose("towny:plot info")
        }

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        val ownerProbe = { plot.residentOrNull }
        val saleProbe = { plot.plotObjectGroup?.price to plot.plotPrice }

        if (forSale && owner != viewer) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_CLAIM, Icons.icon(
                    Material.EMERALD, tr("plot.buy"), tr("plot.buy-description"),
                    *listOfNotNull(priceLine(price)).toTypedArray(),
                    actions = hints("click.buy")
                )
            ) { run("towny:plot claim", ownerProbe) }
        }
        if (owner != null && owner == viewer) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM,
                Icons.icon(
                    Material.COARSE_DIRT, tr("plot.give-up"), tr("plot.give-up-description"),
                    actions = hints("click.give-up")
                )
            ) {
                run("towny:plot unclaim", ownerProbe)
            }
        }
        if (forSale) {
            grid.add(
                if (group != null) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_NOTFORSALE else PermissionNodes.TOWNY_COMMAND_PLOT_NOTFORSALE,
                Icons.icon(
                    Material.RED_BANNER,
                    tr("plot.not-for-sale"),
                    tr("plot.not-for-sale-description"),
                    *groupLines,
                    actions = hints("click.stop-selling")
                )
            ) {
                run("$scope notforsale", saleProbe)
            }
        } else {
            grid.add(
                if (group != null) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_FORSALE else PermissionNodes.TOWNY_COMMAND_PLOT_FORSALE,
                Icons.icon(
                    Material.GREEN_BANNER, tr("plot.for-sale"), tr("plot.for-sale-description"), *groupLines,
                    actions = hints("click.set-price")
                )
            ) {
                prompt(
                    tr("plot.for-sale-title"),
                    tr("common.price-label"),
                    if (group == null) tr("plot.for-sale-hint") else null,
                    initial = (town?.getPlotTypePrice(plot.type) ?: 0.0).toBigDecimal().stripTrailingZeros()
                        .toPlainString()
                ) { input ->
                    run("$scope forsale ${TownyUtil.argument(input)}", saleProbe)
                }
            }
        }
        if (owner != null) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_EVICT,
                Icons.icon(
                    Material.IRON_BOOTS,
                    tr("plot.evict"),
                    tr("plot.evict-description", "owner" to TownyUtil.name(owner.name)),
                    *groupLines,
                    actions = listOfNotNull(tr("click.evict"), if (group == null) tr("plot.evict-resell") else null)
                )
            ) { click ->
                run(
                    if (click.isRightClick && group == null) "towny:plot evict forsale" else "towny:plot evict",
                    ownerProbe
                )
            }
        }
        grid.add(
            Icons.icon(
                Material.OAK_SIGN, tr("plot.type"), tr("plot.type-description"),
                tr("common.current", "value" to TownyUtil.plotType(player, plot.type.name)),
                *groupLines,
                actions = hints("click.choose")
            )
        ) {
            val types = listOf("reset" to "<white>${TownyUtil.plotType(player, "default")}") +
                    TownBlockTypeHandler.getTypeNames()
                        .filter { it != "default" && (group == null || it != "jail") }
                        .sorted()
                        .map { it to "<white>${TownyUtil.plotType(player, it)}" }
            val plots = group?.townBlocks?.size ?: 1
            Pickers.option(
                this,
                tr("plot.type-title"),
                types,
                plot.type.name.takeIf { it != "default" } ?: "reset",
                Material.OAK_SIGN,
                { type -> listOfNotNull(costLine(Prices.plotType(type, plots))) }) { type ->
                run("$scope set $type", { plot.type.name }, returnTo = this)
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_SET_NAME,
            Icons.icon(
                Material.NAME_TAG, tr("plot.rename"), tr("plot.rename-description"),
                actions = hints("click.left-rename", "click.clear")
            )
        ) { click ->
            if (click.isRightClick) {
                run("towny:plot set name", { plot.name })
            } else {
                prompt(tr("plot.rename-title"), tr("plot.plot-name"), initial = plot.name.orEmpty()) { name ->
                    run("towny:plot set name ${TownyUtil.nameArgument(name)}", { plot.name })
                }
            }
        }
        grid.add(Icons.icon(
            Material.LEVER, tr("plot.settings"), tr("plot.settings-description"), *groupLines,
            actions = hints("click.open")
        )) {
            toggles(plot, group != null).open()
        }
        val permNode =
            if (group != null) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_SET else PermissionNodes.TOWNY_COMMAND_PLOT_SET_PERM
        grid.add(
            permNode,
            Icons.icon(
                Material.IRON_DOOR, tr("common.permissions"), tr("plot.permissions-description"), *groupLines,
                actions = hints("click.edit-permissions")
            )
        ) {
            PermissionMenu(
                player, tr("plot.permissions-title"), this, "$scope set perm", permNode, owner != null,
                overrides = { back -> PlotOverridesMenu(player, plot, back) }
            ) { plot.takeIf { it.exists() }?.let { it.plotObjectGroup?.permissions ?: it.permissions } }.open()
        }
        grid.add(
            if (group != null) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_TRUST else PermissionNodes.TOWNY_COMMAND_PLOT_TRUST,
            Icons.icon(
                Material.TRIPWIRE_HOOK, tr("plot.trusted"), tr("plot.trusted-description"),
                tr("plot.trusted-count", "count" to (group?.trustedResidents ?: plot.trustedResidents).size),
                *groupLines,
                actions = hints("click.view")
            )
        ) { PlotTrustMenu(player, plot, this).open() }
        grid.add(
            if (group != null) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_SET else PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR,
            Icons.icon(
                Material.CLOCK, tr("plot.join-days"), tr("plot.join-days-description"),
                *joinDayLines(plot, group).ifEmpty { listOf(tr("plot.join-days-none")) }.toTypedArray(),
                *groupLines,
                actions = hints("plot.left-min-days", "plot.right-max-days")
            )
        ) { click ->
            val key = if (click.isRightClick) "maxjoindays" else "minjoindays"
            val probe = {
                plot.plotObjectGroup?.let { it.minTownMembershipDays to it.maxTownMembershipDays }
                    ?: (plot.minTownMembershipDays to plot.maxTownMembershipDays)
            }
            prompt(
                tr(if (click.isRightClick) "plot.max-join-days-title" else "plot.min-join-days-title"),
                tr("plot.days"),
                tr("plot.join-days-hint"),
                maxLength = 5
            ) { input ->
                val days = TownyUtil.argument(input)
                run("$scope set $key ${if (days == "0") "clear" else days}", probe)
            }
        }
        if (plot.isJail) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_PLOT_JAILCELL,
                Icons.icon(
                    Material.IRON_CHAIN, tr("plot.jail-cells"), tr("plot.jail-cells-description"),
                    tr("plot.jail-cells-count", "count" to (plot.jail?.jailCellCount ?: 0)),
                    actions = hints("plot.left-add-cell", "plot.right-remove-cell")
                )
            ) { click ->
                run(
                    "towny:plot jailcell ${if (click.isRightClick) "remove" else "add"}",
                    { plot.jail?.jailCellCount })
            }
        }
        if (TownySettings.isAllowingOutposts() && group == null && town != null && town == viewer?.townOrNull) {
            if (plot.isOutpost) {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_TOWN_SET_OUTPOST,
                    Icons.icon(
                        Material.RESPAWN_ANCHOR, tr("plot.outpost-spawn"), tr("plot.outpost-spawn-description"),
                        actions = hints("click.set-here")
                    )
                ) {
                    run("towny:plot set outpost spawn", { town.allOutpostSpawns.toList() })
                }
            } else if (!plot.isHomeBlock) {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                    Icons.icon(
                        Material.COMPASS, tr("plot.make-outpost"), tr("plot.make-outpost-description"),
                        *outpostCost().toTypedArray(),
                        actions = hints("click.make-outpost")
                    )
                ) {
                    run("towny:plot set outpost", { plot.isOutpost to town.allOutpostSpawns.size })
                }
            }
        }
        grid.add(Icons.icon(
            Material.CHEST, tr("plot.groups"), tr("plot.groups-description"),
            actions = hints("click.open")
        )) {
            PlotGroupMenu(player, this).open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_PLOT_CLEAR,
            Icons.icon(
                Material.BRUSH, tr("plot.clear"), tr("plot.clear-description"),
                actions = hints("click.clear-plot")
            )
        ) {
            runAndClose("towny:plot clear")
        }
        grid.add(Icons.icon(
            Material.MAP, tr("plot.area"), tr("plot.area-description"),
            actions = hints("click.open")
        )) {
            PlotAreaMenu(player, this).open()
        }
        if (town != null) {
            button(46, Icons.icon(
                Material.BELL, "<gold>${TownyUtil.name(town.name)}", tr("plot.town-description"),
                actions = hints("click.view")
            )) {
                TownInfoMenu(player, town, this).open()
            }
        }
    }

    private fun outpostCost(): List<String> = listOfNotNull(costLine(Prices.outpost()))

    /** The join-day limits Towny places on buying [plot], or on every plot of its [group]. */
    private fun joinDayLines(plot: TownBlock, group: PlotGroup?): List<String> {
        val min = group?.minTownMembershipDays ?: plot.minTownMembershipDays
        val max = group?.maxTownMembershipDays ?: plot.maxTownMembershipDays
        return listOfNotNull(
            if (min > 0) tr("plot.min-join-days", "days" to min) else null,
            if (max > 0) tr("plot.max-join-days", "days" to max) else null,
        )
    }

    private fun toggles(plot: TownBlock, grouped: Boolean): Menu {
        val scope = if (grouped) "towny:plot group" else "towny:plot"

        fun toggle(
            material: Material,
            key: String,
            label: String,
            description: String,
            node: PermissionNodes,
            read: (TownBlock) -> Boolean
        ) =
            Toggle(
                material, label, description,
                if (grouped) PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_TOGGLE else node,
                "$scope toggle $key"
            ) {
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
