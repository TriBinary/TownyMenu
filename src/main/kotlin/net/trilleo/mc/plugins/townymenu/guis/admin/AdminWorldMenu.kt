package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.`object`.TownyPermission.ActionType
import com.palmergames.bukkit.towny.`object`.TownyWorld
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** One world's Towny settings (`/townyworld`): toggles, wilderness permissions and name, and resetting to defaults. */
class AdminWorldMenu(player: Player, private val world: TownyWorld, back: Menu) :
    Menu(player, player.tr("admin-world.title", "world" to TownyUtil.text(world.name)), 4, back) {

    private val command = "towny:townyworld ${world.name}"

    override fun build() {
        button(4, Icons.icon(Material.GRASS_BLOCK, "<green>${TownyUtil.text(world.name)}", null,
            tr("admin-world.using-towny", "value" to TownyUtil.yesNo(player, world.isUsingTowny)),
            tr("admin-world.towns", "count" to world.townsInWorld.size),
            tr("admin-world.wild-name", "name" to TownyUtil.text(world.unclaimedZoneName)),
        ))

        guarded(10, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE,
            Icons.icon(Material.LEVER, tr("admin-world.toggles"), tr("admin-world.toggles-description"))) {
            toggles().open()
        }
        guarded(12, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_SET,
            Icons.icon(Material.NAME_TAG, tr("admin-world.rename-wild"), tr("admin-world.rename-wild-description"),
                tr("common.current", "value" to TownyUtil.text(world.unclaimedZoneName)))) {
            prompt(tr("admin-world.rename-wild"), tr("common.new-name"), initial = world.unclaimedZoneName) { name ->
                run("$command set wildname ${TownyUtil.nameArgument(name)}", { world.unclaimedZoneName })
            }
        }
        guarded(14, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_SET,
            Icons.icon(Material.BARRIER, tr("admin-world.reset"), tr("admin-world.reset-description"))) {
            run("$command set usedefault", ::snapshot)
        }

        WILD_PERMISSIONS.forEachIndexed { index, (type, material, key) ->
            val allowed = world.getUnclaimedZonePerm(type)
            guarded(19 + index * 2, PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_SET,
                Icons.toggle(player, material, tr(key), allowed, tr("admin-world.wild-perm-description"))) {
                val granted = WILD_PERMISSIONS.filter { (other) -> if (other == type) !allowed else world.getUnclaimedZonePerm(other) }
                val perms = granted.joinToString(",") { it.first.commonName }.ifEmpty { "none" }
                run("$command set wildperm $perms", { world.getUnclaimedZonePerm(type) })
            }
        }

        backButton(31)
    }

    private fun snapshot(): List<Any?> = toggleList().map { it.value() } + world.unclaimedZoneName

    private fun toggles(): Menu = ToggleMenu(player, tr("admin-world.toggles-title", "world" to TownyUtil.text(world.name)), this, toggleList())

    private fun toggleList(): List<Toggle> {
        fun toggle(material: Material, key: String, name: String, description: String, node: PermissionNodes, read: (TownyWorld) -> Boolean) =
            Toggle(material, name, description, node, "$command toggle $key") { read(world) }

        return listOf(
            toggle(Material.GRASS_BLOCK, "usingtowny", tr("admin-world.toggle-using-towny"), tr("admin-world.toggle-using-towny-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_USINGTOWNY) { it.isUsingTowny },
            toggle(Material.WOODEN_SHOVEL, "claimable", tr("admin-world.toggle-claimable"), tr("admin-world.toggle-claimable-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_CLAIMABLE) { it.isClaimable },
            toggle(Material.IRON_SWORD, "pvp", tr("toggle.pvp"), tr("admin-world.toggle-pvp-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_PVP) { it.isPVP },
            toggle(Material.DIAMOND_SWORD, "forcepvp", tr("admin-world.toggle-force-pvp"), tr("admin-world.toggle-force-pvp-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_FORCEPVP) { it.isForcePVP },
            toggle(Material.SHIELD, "friendlyfire", tr("admin-world.toggle-friendly-fire"), tr("admin-world.toggle-friendly-fire-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_FRIENDLYFIRE) { it.isFriendlyFireEnabled },
            toggle(Material.TNT, "explosion", tr("toggle.explosions"), tr("admin-world.toggle-explosions-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_EXPLOSION) { it.isExpl },
            toggle(Material.TNT_MINECART, "forceexplosion", tr("admin-world.toggle-force-explosions"), tr("admin-world.toggle-force-explosions-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_FORCEEXPLOSION) { it.isForceExpl },
            toggle(Material.FLINT_AND_STEEL, "fire", tr("toggle.fire"), tr("admin-world.toggle-fire-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_FIRE) { it.isFire },
            toggle(Material.FIRE_CHARGE, "forcefire", tr("admin-world.toggle-force-fire"), tr("admin-world.toggle-force-fire-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_FORCEFIRE) { it.isForceFire },
            toggle(Material.ZOMBIE_HEAD, "townmobs", tr("admin-world.toggle-town-mobs"), tr("admin-world.toggle-town-mobs-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_TOWNMOBS) { it.isForceTownMobs },
            toggle(Material.SKELETON_SKULL, "worldmobs", tr("admin-world.toggle-world-mobs"), tr("admin-world.toggle-world-mobs-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_WORLDMOBS) { it.hasWorldMobs() },
            toggle(Material.CREEPER_HEAD, "wildernessmobs", tr("admin-world.toggle-wilderness-mobs"), tr("admin-world.toggle-wilderness-mobs-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_WILDERNESSMOBS) { it.hasWildernessMobs() },
            toggle(Material.RED_BANNER, "warallowed", tr("admin-world.toggle-war"), tr("admin-world.toggle-war-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_WARALLOWED) { it.isWarAllowed },
            toggle(Material.IRON_BARS, "jailing", tr("admin-world.toggle-jailing"), tr("admin-world.toggle-jailing-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_JAILING) { it.isJailingEnabled },
            toggle(Material.OAK_SAPLING, "revertunclaim", tr("admin-world.toggle-revert-unclaim"), tr("admin-world.toggle-revert-unclaim-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_REVERTUNCLAIM) { it.isUsingPlotManagementRevert },
            toggle(Material.GUNPOWDER, "revertentityexpl", tr("admin-world.toggle-revert-entity-explosions"), tr("admin-world.toggle-revert-entity-explosions-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_REVERTENTITYEXPL) { it.isUsingPlotManagementWildEntityRevert },
            toggle(Material.RESPAWN_ANCHOR, "revertblockexpl", tr("admin-world.toggle-revert-block-explosions"), tr("admin-world.toggle-revert-block-explosions-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_REVERTBLOCKEXPL) { it.isUsingPlotManagementWildBlockRevert },
            toggle(Material.OAK_SIGN, "plotcleardelete", tr("admin-world.toggle-plot-clear-delete"), tr("admin-world.toggle-plot-clear-delete-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_PLOTCLEARDELETE) { it.isUsingPlotManagementMayorDelete },
            toggle(Material.RED_BED, "unclaimblockdelete", tr("admin-world.toggle-unclaim-block-delete"), tr("admin-world.toggle-unclaim-block-delete-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_UNCLAIMBLOCKDELETE) { it.isUsingPlotManagementDelete },
            toggle(Material.END_CRYSTAL, "unclaimentitydelete", tr("admin-world.toggle-unclaim-entity-delete"), tr("admin-world.toggle-unclaim-entity-delete-description"),
                PermissionNodes.TOWNY_COMMAND_TOWNYWORLD_TOGGLE_UNCLAIMENTITYDELETE) { it.isDeletingEntitiesOnUnclaim },
        )
    }

    private companion object {
        val WILD_PERMISSIONS = listOf(
            Triple(ActionType.BUILD, Material.BRICKS, "admin-world.wild-build"),
            Triple(ActionType.DESTROY, Material.IRON_PICKAXE, "admin-world.wild-destroy"),
            Triple(ActionType.SWITCH, Material.LEVER, "admin-world.wild-switch"),
            Triple(ActionType.ITEM_USE, Material.FLINT_AND_STEEL, "admin-world.wild-item-use"),
        )
    }
}
