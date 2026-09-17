package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Public view of any nation: visit, join, towns, relations, and diplomacy with the viewer's nation. */
class NationInfoMenu(player: Player, private val nation: Nation, back: Menu) :
    Menu(player, TownyUtil.name(nation.name), 4, back) {

    override fun build() {
        if (!nation.exists()) {
            button(13, Icons.icon(Material.BARRIER, "<red>This nation no longer exists"))
            return backButton(31)
        }
        button(4, Icons.nation(nation, "<gray>Founded: <white>${TownyUtil.date(nation.registered)}"))

        val viewer = resident
        val own = viewer?.nationOrNull
        val grid = layout(19, 20, 21, 22, 23, 24, 25)

        if (own == nation) {
            grid.add(Icons.icon(Material.WRITABLE_BOOK, "<aqua>Manage Your Nation", "Open your nation's management menu.")) {
                NationMenu(player, this).open()
            }
        }
        grid.add(Icons.icon(Material.ENDER_PEARL, "<light_purple>Visit", "Teleport to this nation's spawn.",
            if (TownyUtil.economy && nation.spawnCost > 0) "<gray>Cost: <white>${TownyUtil.money(nation.spawnCost)}" else "")) {
            runAndClose("towny:nation spawn ${nation.name}")
        }
        grid.add(Icons.icon(Material.BELL, "<gold>Towns", null, "<gray>Towns: <white>${nation.numTowns}")) {
            NationTownsMenu(player, nation, this).open()
        }
        grid.add(Icons.icon(Material.SHIELD, "<aqua>Allies & Enemies", null,
            "<gray>Allies: <white>${nation.allies.size}  <gray>Enemies: <white>${nation.enemies.size}")) {
            NationRelationsMenu(player, nation, this).open()
        }

        val town = viewer?.townOrNull
        if (viewer != null && town != null && viewer.isMayor && !town.hasNation() && nation.isOpen) {
            grid.add(PermissionNodes.TOWNY_COMMAND_NATION_JOIN,
                Icons.icon(Material.OAK_DOOR, "<green>Join Nation", "This nation is open to every town.")) {
                run("towny:nation join ${nation.name}", { town.hasNation() })
            }
        }
        if (own != null && own != nation) {
            val snapshot = { Triple(own.hasAlly(nation), own.hasEnemy(nation), own.sentAllyInvites.size) }
            if (own.hasAlly(nation)) {
                grid.add(PermissionNodes.TOWNY_COMMAND_NATION_ALLY_REMOVE,
                    Icons.icon(Material.SHIELD, "<red>End Alliance", "Remove this nation from your allies.")) {
                    run("towny:nation ally remove ${nation.name}", snapshot)
                }
            } else {
                grid.add(PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ADD,
                    Icons.icon(Material.SHIELD, "<green>Propose Alliance", "Send an alliance request to this nation.")) {
                    run("towny:nation ally add ${nation.name}", snapshot)
                }
            }
            grid.add(PermissionNodes.TOWNY_COMMAND_NATION_ENEMY, Icons.icon(
                Material.IRON_SWORD,
                if (own.hasEnemy(nation)) "<green>Make Peace" else "<red>Declare Enemy",
                if (own.hasEnemy(nation)) "Remove this nation from your enemies." else "Mark this nation as an enemy.",
            )) { run("towny:nation enemy ${if (own.hasEnemy(nation)) "remove" else "add"} ${nation.name}", snapshot) }
        }

        backButton(31)
    }
}
