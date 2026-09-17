package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.`object`.TownyPermission.ActionType
import com.palmergames.bukkit.towny.`object`.TownyPermission.PermLevel
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * A 4×4 grid editing build/destroy/switch/item-use permissions for residents,
 * nation, allies, and outsiders, via `<command> <level> <action> <on|off>`.
 *
 * @param command   the Towny set-perm command, e.g. `towny:town set perm`
 * @param personal  label the levels Friends/Town (resident-owned land) instead of Residents/Nation
 * @param permissions reads the current permissions; `null` when the land no longer exists
 */
class PermissionMenu(
    player: Player,
    title: String,
    back: Menu,
    private val command: String,
    private val node: PermissionNodes,
    private val personal: Boolean,
    private val permissions: () -> TownyPermission?,
) : Menu(player, title, 6, back) {

    override fun build() {
        val perms = permissions()
        if (perms == null) {
            button(22, Icons.icon(Material.BARRIER, "<red>This land no longer exists"))
            backButton(49)
            return
        }

        ACTIONS.forEachIndexed { column, action ->
            val allOn = LEVELS.all { perms.getPerm(it, action) }
            guarded(11 + column, node, itemStack(ACTION_ICONS.getValue(action)) {
                name("<gold>${ACTION_NAMES.getValue(action)}")
                loreWrapped("<gray>${ACTION_DESCRIPTIONS.getValue(action)}")
                lore("", "<yellow>Click to turn ${if (allOn) "<red>off" else "<green>on"} <yellow>for everyone")
            }) { run("$command ${action.arg} ${if (allOn) "off" else "on"}", ::snapshot) }
        }

        LEVELS.forEachIndexed { row, level ->
            val allOn = ACTIONS.all { perms.getPerm(level, it) }
            guarded(19 + row * 9, node, itemStack(LEVEL_ICONS.getValue(level)) {
                name("<aqua>${levelName(level)}")
                lore("<yellow>Click to turn ${if (allOn) "<red>off" else "<green>on"} <yellow>every action")
            }) { run("$command ${level.arg} ${if (allOn) "off" else "on"}", ::snapshot) }

            ACTIONS.forEachIndexed { column, action ->
                val allowed = perms.getPerm(level, action)
                val cell = itemStack(if (allowed) Material.LIME_CONCRETE else Material.RED_CONCRETE) {
                    name("${if (allowed) "<green>" else "<red>"}${levelName(level)}: ${ACTION_NAMES.getValue(action)}")
                    lore("<gray>Currently: ${if (allowed) "<green>Allowed" else "<red>Denied"}", "<yellow>Click to toggle")
                }
                guarded(20 + row * 9 + column, node, cell) {
                    run("$command ${level.arg} ${action.arg} ${if (allowed) "off" else "on"}", ::snapshot)
                }
            }
        }

        guarded(25, node, Icons.icon(Material.WATER_BUCKET, "<yellow>Reset", "Restore the server's default permissions.")) {
            run("$command reset", ::snapshot)
        }
        backButton(49)
    }

    private fun snapshot(): String? = permissions()?.toString()

    private fun levelName(level: PermLevel): String = when (level) {
        PermLevel.RESIDENT -> if (personal) "Friends" else "Residents"
        PermLevel.NATION -> if (personal) "Town" else "Nation"
        PermLevel.ALLY -> "Allies"
        PermLevel.OUTSIDER -> "Outsiders"
    }

    private companion object {
        val LEVELS = listOf(PermLevel.RESIDENT, PermLevel.NATION, PermLevel.ALLY, PermLevel.OUTSIDER)
        val ACTIONS = listOf(ActionType.BUILD, ActionType.DESTROY, ActionType.SWITCH, ActionType.ITEM_USE)

        val PermLevel.arg get() = name.lowercase()
        val ActionType.arg get() = if (this == ActionType.ITEM_USE) "itemuse" else name.lowercase()

        val ACTION_NAMES = mapOf(
            ActionType.BUILD to "Build", ActionType.DESTROY to "Destroy",
            ActionType.SWITCH to "Switch", ActionType.ITEM_USE to "Item Use",
        )
        val ACTION_DESCRIPTIONS = mapOf(
            ActionType.BUILD to "Place blocks.",
            ActionType.DESTROY to "Break blocks.",
            ActionType.SWITCH to "Use doors, buttons, levers, and containers.",
            ActionType.ITEM_USE to "Use items such as flint and steel or buckets.",
        )
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
