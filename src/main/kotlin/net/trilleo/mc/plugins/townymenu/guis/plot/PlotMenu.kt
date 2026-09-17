package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.`object`.TownBlockTypeHandler
import com.palmergames.bukkit.towny.`object`.WorldCoord
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * The plot the viewer is standing in. Every render re-reads the player's
 * location, so walking to another chunk and refreshing shows that chunk.
 */
class PlotMenu(player: Player, back: Menu?) : Menu(player, "Plot", 6, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        if (plot == null) wilderness() else claimed(plot)
        button(50, Icons.icon(Material.CLOCK, "<yellow>Refresh", "Update this menu after moving to another chunk.")) { render() }
        backButton(49)
    }

    private fun wilderness() {
        val coord = WorldCoord.parseWorldCoord(player)
        button(4, Icons.icon(Material.OAK_SAPLING, "<green>Wilderness", "Nobody has claimed this land.",
            "<gray>Chunk: <white>${coord.x}, ${coord.z}"))
        val town = resident?.townOrNull
        val grid = layout(20, 21, 22, 23, 24)
        if (town != null) {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
                Icons.icon(Material.GRASS_BLOCK, "<green>Claim for ${TownyUtil.name(town.name)}", "Claim this chunk for your town.")) {
                run("towny:town claim", { town.numTownBlocks }, delayTicks = 20)
            }
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                Icons.icon(Material.COMPASS, "<light_purple>Claim as Outpost", "Claim this chunk as an outpost.")) {
                run("towny:town claim outpost", { town.numTownBlocks }, delayTicks = 20)
            }
        }
        grid.add(Icons.icon(Material.FILLED_MAP, "<aqua>Map", "See the land around you.")) { MapMenu(player, this).open() }
    }

    private fun claimed(plot: TownBlock) {
        val town = plot.townOrNull
        val owner = plot.residentOrNull
        val viewer = resident
        button(4, Icons.icon(Material.GRASS_BLOCK, "<gold>${if (plot.name.isNullOrBlank()) "Plot" else TownyUtil.text(plot.name)}", null, *buildList {
            add("<gray>Town: <white>${town?.let { TownyUtil.name(it.name) } ?: "-"}")
            add("<gray>Owner: <white>${owner?.let { TownyUtil.name(it.name) } ?: "The town"}")
            add("<gray>Type: <white>${TownyUtil.name(plot.type.name)}")
            add("<gray>Chunk: <white>${plot.x}, ${plot.z}")
            if (plot.isHomeBlock) add("<gold>Home block")
            if (plot.isOutpost) add("<light_purple>Outpost")
            if (plot.hasPlotObjectGroup()) add("<gray>Group: <white>${TownyUtil.name(plot.plotObjectGroup.name)}")
            if (plot.hasDistrict()) add("<gray>District: <white>${TownyUtil.name(plot.district.name)}")
            if (TownyUtil.economy) {
                if (plot.isForSale) add("<green>For sale: <white>${TownyUtil.money(plot.plotPrice)}")
                if (plot.isTaxed) add("<gray>Daily tax: <white>${TownyUtil.money(plot.plotTax)}")
            }
            add("<gray>PvP: ${TownyUtil.onOff(plot.permissions.pvp)}  <gray>Mobs: ${TownyUtil.onOff(plot.permissions.mobs)}")
            add("<gray>Fire: ${TownyUtil.onOff(plot.permissions.fire)}  <gray>Explosions: ${TownyUtil.onOff(plot.permissions.explosion)}")
            add("<gray>Claimed: <white>${TownyUtil.date(plot.claimedAt)}")
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
        val ownerProbe = { plot.residentOrNull }
        val saleProbe = { plot.isForSale to plot.plotPrice }

        if (plot.isForSale && owner != viewer) {
            grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_CLAIM, Icons.icon(
                Material.EMERALD, "<green>Buy Plot", "Become the owner of this plot.",
                "<gray>Price: <white>${TownyUtil.money(plot.plotPrice)}",
            )) { run("towny:plot claim", ownerProbe) }
        }
        if (owner != null && owner == viewer) {
            grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_UNCLAIM,
                Icons.icon(Material.COARSE_DIRT, "<red>Give Up Plot", "Return this plot to the town.")) {
                run("towny:plot unclaim", ownerProbe)
            }
        }
        if (plot.isForSale) {
            grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_NOTFORSALE,
                Icons.icon(Material.RED_BANNER, "<red>Take off Sale", "Stop selling this plot.")) {
                run("towny:plot notforsale", saleProbe)
            }
        } else {
            grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_FORSALE,
                Icons.icon(Material.GREEN_BANNER, "<gold>Put up for Sale", "Let a resident buy this plot.")) {
                prompt("Sell Plot", "Price", "<gray>Use 0 to give it away for free.", initial = "0") { price ->
                    run("towny:plot forsale ${TownyUtil.argument(price)}", saleProbe)
                }
            }
        }
        if (owner != null) {
            grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_EVICT,
                Icons.icon(Material.IRON_BOOTS, "<red>Evict Owner", "Remove ${TownyUtil.name(owner.name)} as this plot's owner.")) {
                run("towny:plot evict", ownerProbe)
            }
        }
        grid.add(Icons.icon(Material.OAK_SIGN, "<yellow>Plot Type", "Shops, embassies, arenas, farms, jails, and more.",
            "<gray>Current: <white>${TownyUtil.name(plot.type.name)}")) {
            val types = listOf("reset" to "<white>Default") +
                TownBlockTypeHandler.getTypeNames().filter { it != "default" }.sorted().map { it to "<white>${TownyUtil.name(it)}" }
            Pickers.option(this, "Plot Type", types, plot.type.name.takeIf { it != "default" } ?: "reset", Material.OAK_SIGN) { type ->
                run("towny:plot set $type", { plot.type.name }, returnTo = this)
            }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_SET_NAME,
            Icons.icon(Material.NAME_TAG, "<yellow>Rename Plot", "Shown to players as they walk in.")) {
            prompt("Rename Plot", "Plot name", initial = plot.name.orEmpty()) { name ->
                run("towny:plot set name ${TownyUtil.nameArgument(name)}", { plot.name })
            }
        }
        grid.add(Icons.icon(Material.LEVER, "<yellow>Plot Settings", "PvP, fire, explosions, mobs, and taxes on this plot.")) {
            toggles(plot).open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_SET_PERM,
            Icons.icon(Material.IRON_DOOR, "<aqua>Permissions", "Who can build, break, and interact on this plot.")) {
            PermissionMenu(player, "Plot Permissions", this, "towny:plot set perm",
                PermissionNodes.TOWNY_COMMAND_PLOT_SET_PERM, owner != null) { plot.takeIf { it.exists() }?.permissions }.open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_TRUST, Icons.icon(
            Material.TRIPWIRE_HOOK, "<green>Trusted Players", "Trusted players may build on this plot.",
            "<gray>Trusted: <white>${plot.trustedResidents.size}",
        )) { PlotTrustMenu(player, plot, this).open() }
        grid.add(Icons.icon(Material.CHEST, "<gold>Groups & Districts", "Bundle plots into groups sold together, or into districts.")) {
            PlotGroupMenu(player, this).open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_PLOT_CLEAR,
            Icons.icon(Material.BRUSH, "<red>Clear Plot", "Remove blocks placed on this plot that the server marks as clearable.")) {
            runAndClose("towny:plot clear")
        }
        if (town != null) {
            grid.add(Icons.icon(Material.BELL, "<gold>${TownyUtil.name(town.name)}", "View the town that owns this land.")) {
                TownInfoMenu(player, town, this).open()
            }
        }
        grid.add(Icons.icon(Material.FILLED_MAP, "<aqua>Map", "See the land around you.")) { MapMenu(player, this).open() }
    }

    private fun toggles(plot: TownBlock): Menu {
        fun toggle(material: Material, key: String, label: String, description: String, node: PermissionNodes, read: (TownBlock) -> Boolean) =
            Toggle(material, label, description, node, "towny:plot toggle $key") { plot.takeIf { it.exists() }?.let(read) }

        return ToggleMenu(player, "Plot Settings", this, listOf(
            toggle(Material.IRON_SWORD, "pvp", "<red>PvP", "Allow players to fight on this plot.", PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_PVP) { it.permissions.pvp },
            toggle(Material.FLINT_AND_STEEL, "fire", "<gold>Fire Spread", "Allow fire to spread on this plot.", PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_FIRE) { it.permissions.fire },
            toggle(Material.TNT, "explosion", "<dark_red>Explosions", "Allow explosions on this plot.", PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_EXPLOSION) { it.permissions.explosion },
            toggle(Material.ZOMBIE_HEAD, "mobs", "<dark_green>Hostile Mobs", "Allow hostile mobs to spawn on this plot.", PermissionNodes.TOWNY_COMMAND_PLOT_TOGGLE_MOBS) { it.permissions.mobs },
            toggle(Material.GOLD_NUGGET, "taxed", "<gold>Taxed", "Charge the plot owner the town's daily plot tax.", PermissionNodes.TOWNY_COMMAND_PLOT_ASMAYOR) { it.isTaxed },
        ))
    }
}
