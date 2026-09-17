package net.trilleo.mc.plugins.townymenu.guis.plot

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlock
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Plot group and district management for the plot the viewer is standing in. */
class PlotGroupMenu(player: Player, back: Menu) : Menu(player, player.tr("plot-group.title"), 5, back) {

    private val plot: TownBlock?
        get() = TownyAPI.getInstance().getTownBlock(player)

    override fun build() {
        val plot = plot
        if (plot == null) {
            button(22, Icons.icon(Material.BARRIER, tr("plot-group.not-in-town")))
            return backButton(40)
        }
        val group = plot.plotObjectGroup
        val district = plot.district
        val groupProbe = { plot.plotObjectGroup?.let { it.name to it.price } }
        val districtProbe = { plot.district?.name }

        button(4, Icons.icon(Material.CHEST, tr("main.plot"), null,
            tr("plot.group", "group" to (group?.let { TownyUtil.name(it.name) } ?: tr("common.none"))),
            tr("plot.district", "district" to (district?.let { TownyUtil.name(it.name) } ?: tr("common.none")))))

        val groups = layout(10, 11, 12, 13, 14, 15, 16)
        groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_ADD,
            Icons.icon(Material.CHEST, tr("plot-group.add"), tr("plot-group.add-description"))) {
            prompt(tr("plot-group.add-title"), tr("plot-group.group-name")) { name ->
                run("towny:plot group add ${TownyUtil.nameArgument(name)}", groupProbe)
            }
        }
        if (group != null) {
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_REMOVE,
                Icons.icon(Material.HOPPER, tr("plot-group.remove"), tr("plot-group.remove-description", "group" to TownyUtil.name(group.name)))) {
                run("towny:plot group remove", groupProbe)
            }
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_RENAME,
                Icons.icon(Material.NAME_TAG, tr("plot-group.rename"), null)) {
                prompt(tr("plot-group.rename-title"), tr("common.new-name"), initial = group.name) { name ->
                    run("towny:plot group rename ${TownyUtil.nameArgument(name)}", groupProbe)
                }
            }
            if (TownyUtil.economy) {
                groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_FORSALE,
                    Icons.icon(Material.GREEN_BANNER, tr("plot-group.sell"), tr("plot-group.sell-description"),
                        tr("common.price", "price" to if (group.price >= 0) TownyUtil.money(group.price) else tr("plot-group.not-for-sale")))) {
                    prompt(tr("plot-group.sell-title"), tr("common.price-label"), initial = "0") { price ->
                        run("towny:plot group forsale ${TownyUtil.argument(price)}", groupProbe)
                    }
                }
                groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_NOTFORSALE,
                    Icons.icon(Material.RED_BANNER, tr("plot-group.stop-selling"), null)) {
                    run("towny:plot group notforsale", groupProbe)
                }
            }
            groups.add(PermissionNodes.TOWNY_COMMAND_PLOT_GROUP_DELETE,
                Icons.icon(Material.LAVA_BUCKET, tr("plot-group.delete"), tr("plot-group.delete-description", "group" to TownyUtil.name(group.name)))) {
                run("towny:plot group delete", groupProbe)
            }
        }

        val districts = layout(28, 29, 30, 31)
        districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_ADD,
            Icons.icon(Material.BARREL, tr("plot-group.district-add"), tr("plot-group.district-add-description"))) {
            prompt(tr("plot-group.district-add-title"), tr("plot-group.district-name")) { name ->
                run("towny:plot district add ${TownyUtil.nameArgument(name)}", districtProbe)
            }
        }
        if (district != null) {
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_REMOVE,
                Icons.icon(Material.HOPPER, tr("plot-group.district-remove"), null)) {
                run("towny:plot district remove", districtProbe)
            }
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_RENAME,
                Icons.icon(Material.NAME_TAG, tr("plot-group.district-rename"), null)) {
                prompt(tr("plot-group.district-rename-title"), tr("common.new-name"), initial = district.name) { name ->
                    run("towny:plot district rename ${TownyUtil.nameArgument(name)}", districtProbe)
                }
            }
            districts.add(PermissionNodes.TOWNY_COMMAND_PLOT_DISTRICT_DELETE,
                Icons.icon(Material.LAVA_BUCKET, tr("plot-group.district-delete"), null)) {
                run("towny:plot district delete", districtProbe)
            }
        }

        tutorialButton(44, Tutorial.PLOTS)
        backButton(40)
    }
}
