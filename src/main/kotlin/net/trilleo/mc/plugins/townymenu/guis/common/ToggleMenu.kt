package net.trilleo.mc.plugins.townymenu.guis.common

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * One on/off setting backed by a Towny toggle command. [name] and [description] are already translated.
 *
 * @param value reads the current state; `null` when the target no longer exists
 */
class Toggle(
    val material: Material,
    val name: String,
    val description: String,
    val node: PermissionNodes,
    val command: String,
    val value: () -> Boolean?,
)

/** A menu of [Toggle] switches, seven per row. */
class ToggleMenu(
    player: Player,
    title: String,
    back: Menu,
    private val toggles: List<Toggle>,
) : Menu(player, title, 3 + (toggles.size + 6) / 7, back) {

    override fun build() {
        toggles.forEachIndexed { index, toggle ->
            val slot = 10 + index / 7 * 9 + index % 7
            guarded(slot, toggle.node, Icons.toggle(player, toggle.material, toggle.name, toggle.value() ?: false, toggle.description)) {
                run(toggle.command, toggle.value)
            }
        }
        backButton(inventory.size - 5)
    }
}
