package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.`object`.TownyPermission.ActionType
import com.palmergames.bukkit.towny.`object`.TownyPermission.PermLevel
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * A 4×4 grid editing build/destroy/switch/item-use permissions for residents,
 * nation, allies, and outsiders, via `<command> <level> <action> <on|off>`.
 *
 * @param command   the Towny set-perm command, e.g. `towny:town set perm`
 * @param personal  label the levels Friends/Town (resident-owned land) instead of Residents/Nation
 * @param overrides opens the per-player permission overrides of this land, when it has them
 * @param permissions reads the current permissions; `null` when the land no longer exists
 */
class PermissionMenu(
    player: Player,
    title: String,
    back: Menu,
    private val command: String,
    private val node: PermissionNodes,
    private val personal: Boolean,
    private val overrides: ((Menu) -> Menu)? = null,
    private val permissions: () -> TownyPermission?,
) : Menu(player, title, 6, back) {

    override fun build() {
        val perms = permissions()
        if (perms == null) {
            button(22, Icons.icon(Material.BARRIER, tr("perm.gone")))
            backButton(49)
            return
        }

        ACTIONS.forEachIndexed { column, action ->
            val allOn = LEVELS.all { perms.getPerm(it, action) }
            guarded(2 + column, node, itemStack(ACTION_ICONS.getValue(action)) {
                name("<gold>${actionName(action)}")
                loreWrapped("<gray>${actionDescription(action)}")
                loreActions(listOf(tr(if (allOn) "perm.everyone-off" else "perm.everyone-on")))
            }) { run("$command ${action.arg} ${if (allOn) "off" else "on"}", ::snapshot) }
        }

        LEVELS.forEachIndexed { row, level ->
            val allOn = ACTIONS.all { perms.getPerm(level, it) }
            guarded(10 + row * 9, node, itemStack(LEVEL_ICONS.getValue(level)) {
                name("<aqua>${levelName(level)}")
                loreActions(listOf(tr(if (allOn) "perm.every-action-off" else "perm.every-action-on")))
            }) { run("$command ${level.arg} ${if (allOn) "off" else "on"}", ::snapshot) }

            ACTIONS.forEachIndexed { column, action ->
                val allowed = perms.getPerm(level, action)
                val cell = itemStack(if (allowed) Material.LIME_CONCRETE else Material.RED_CONCRETE) {
                    name("${if (allowed) "<green>" else "<red>"}${levelName(level)}: ${actionName(action)}")
                    lore(tr("icon.status", "value" to tr(if (allowed) "perm.allowed" else "perm.denied")))
                    loreActions(hints(if (allowed) "perm.click-deny" else "perm.click-allow"))
                }
                guarded(11 + row * 9 + column, node, cell) {
                    run("$command ${level.arg} ${action.arg} ${if (allowed) "off" else "on"}", ::snapshot)
                }
            }
        }

        guarded(25, node, Icons.icon(
            Material.WATER_BUCKET, tr("perm.reset"), tr("perm.reset-description"),
            actions = hints("click.reset")
        )) {
            run("$command reset", ::snapshot)
        }
        overrides?.let { open ->
            button(34, Icons.icon(
                Material.PLAYER_HEAD, tr("perm.overrides"), tr("perm.overrides-description"),
                actions = hints("click.open")
            )) {
                open(this).open()
            }
        }
        tutorialButton(53, Tutorial.PROTECTION)
        backButton(49)
    }

    private fun snapshot(): String? = permissions()?.toString()

    private fun levelName(level: PermLevel): String = when (level) {
        PermLevel.RESIDENT -> tr(if (personal) "perm.friends" else "perm.residents")
        PermLevel.NATION -> tr(if (personal) "perm.town" else "perm.nation")
        PermLevel.ALLY -> tr("perm.allies")
        PermLevel.OUTSIDER -> tr("perm.outsiders")
    }

    private fun actionName(action: ActionType): String = when (action) {
        ActionType.BUILD -> tr("perm.build")
        ActionType.DESTROY -> tr("perm.destroy")
        ActionType.SWITCH -> tr("perm.switch")
        ActionType.ITEM_USE -> tr("perm.item-use")
    }

    private fun actionDescription(action: ActionType): String = when (action) {
        ActionType.BUILD -> tr("perm.build-description")
        ActionType.DESTROY -> tr("perm.destroy-description")
        ActionType.SWITCH -> tr("perm.switch-description")
        ActionType.ITEM_USE -> tr("perm.item-use-description")
    }

    private companion object {
        val LEVELS = listOf(PermLevel.RESIDENT, PermLevel.NATION, PermLevel.ALLY, PermLevel.OUTSIDER)
        val ACTIONS = listOf(ActionType.BUILD, ActionType.DESTROY, ActionType.SWITCH, ActionType.ITEM_USE)

        val PermLevel.arg get() = name.lowercase()
        val ActionType.arg get() = if (this == ActionType.ITEM_USE) "itemuse" else name.lowercase()

        val ACTION_ICONS = mapOf(
            ActionType.BUILD to Material.BRICKS, ActionType.DESTROY to Material.IRON_PICKAXE,
            ActionType.SWITCH to Material.LEVER, ActionType.ITEM_USE to Material.FLINT_AND_STEEL,
        )
        val LEVEL_ICONS = mapOf(
            PermLevel.RESIDENT to Material.PLAYER_HEAD, PermLevel.NATION to Material.BELL,
            PermLevel.ALLY to Material.SHIELD, PermLevel.OUTSIDER to Material.ZOMBIE_HEAD,
        )
    }
}
