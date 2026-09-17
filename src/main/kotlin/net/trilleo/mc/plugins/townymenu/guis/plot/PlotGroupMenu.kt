package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Plot group and district management for the plot the viewer is standing in. */
class PlotGroupMenu(player: Player, back: Menu) : Menu(player, "Groups & Districts", 5, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        if (plot == null) {
            button(22, Icons.icon(Material.BARRIER, "<red>You are not standing in a town"))
            return backButton(40)
        }
        val group = plot.plotObjectGroup
        val district = plot.district
        val groupProbe = { plot.plotObjectGroup?.let { it.name to it.price } }
        val districtProbe = { plot.district?.name }

        button(4, Icons.icon(Material.CHEST, "<gold>This Plot", null,
            "<gray>Group: <white>${group?.let { TownyUtil.name(it.name) } ?: "None"}",
            "<gray>District: <white>${district?.let { TownyUtil.name(it.name) } ?: "None"}"))

        val groups = layout(10, 11, 12, 13, 14, 15, 16)
        groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_ADD,
            Icons.icon(Material.CHEST, "<green>Add to Group", "Add this plot to a group, creating the group if needed.")) {
            prompt("Add to Plot Group", "Group name") { name ->
                run("towny:plot group add ${TownyUtil.nameArgument(name)}", groupProbe)
            }
        }
        if (group != null) {
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_REMOVE,
                Icons.icon(Material.HOPPER, "<red>Remove from Group", "Take this plot out of ${TownyUtil.name(group.name)}.")) {
                run("towny:plot group remove", groupProbe)
            }
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_RENAME,
                Icons.icon(Material.NAME_TAG, "<yellow>Rename Group", null)) {
                prompt("Rename Plot Group", "New name", initial = group.name) { name ->
                    run("towny:plot group rename ${TownyUtil.nameArgument(name)}", groupProbe)
                }
            }
            if (TownyUtil.economy) {
                groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_FORSALE,
                    Icons.icon(Material.GREEN_BANNER, "<gold>Sell Group", "Sell every plot in the group together.",
                        "<gray>Price: <white>${if (group.price >= 0) TownyUtil.money(group.price) else "Not for sale"}")) {
                    prompt("Sell Plot Group", "Price", initial = "0") { price ->
                        run("towny:plot group forsale ${TownyUtil.argument(price)}", groupProbe)
                    }
                }
                groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_NOTFORSALE,
                    Icons.icon(Material.RED_BANNER, "<red>Stop Selling Group", null)) {
                    run("towny:plot group notforsale", groupProbe)
                }
            }
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_DELETE,
                Icons.icon(Material.LAVA_BUCKET, "<dark_red>Delete Group", "Ungroup every plot in ${TownyUtil.name(group.name)}.")) {
                run("towny:plot group delete", groupProbe)
            }
        }

        val districts = layout(28, 29, 30, 31)
        districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_ADD,
            Icons.icon(Material.BARREL, "<green>Add to District", "Add this plot to a district, creating the district if needed.")) {
            prompt("Add to District", "District name") { name ->
                run("towny:plot district add ${TownyUtil.nameArgument(name)}", districtProbe)
            }
        }
        if (district != null) {
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_REMOVE,
                Icons.icon(Material.HOPPER, "<red>Remove from District", null)) {
                run("towny:plot district remove", districtProbe)
            }
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_RENAME,
                Icons.icon(Material.NAME_TAG, "<yellow>Rename District", null)) {
                prompt("Rename District", "New name", initial = district.name) { name ->
                    run("towny:plot district rename ${TownyUtil.nameArgument(name)}", districtProbe)
                }
            }
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_DELETE,
                Icons.icon(Material.LAVA_BUCKET, "<dark_red>Delete District", null)) {
                run("towny:plot district delete", districtProbe)
            }
        }

        backButton(40)
    }
}
