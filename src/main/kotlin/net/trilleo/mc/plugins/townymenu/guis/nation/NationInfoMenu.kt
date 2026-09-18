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
    Menu(player, TownyUtil.name(nation.name), 5, back) {

    override fun build() {
        if (!nation.exists()) {
            button(13, Icons.icon(Material.BARRIER, tr("nation-info.gone")))
            return backButton(40)
        }
        button(4, Icons.nation(player, nation, tr("common.founded", "date" to TownyUtil.date(nation.registered))))

        val viewer = resident
        val own = viewer?.nationOrNull
        val grid = layout(19, 20, 21, 22, 23, 24, 25)

        if (own == nation) {
            grid.add(
                Icons.icon(
                    Material.WRITABLE_BOOK,
                    tr("nation-info.manage"),
                    tr("nation-info.manage-description")
                )
            ) {
                NationMenu(player, this).open()
            }
        }
        grid.add(
            Icons.icon(
                Material.ENDER_PEARL, tr("town-info.visit"), tr("nation-info.visit-description"),
                *listOfNotNull(
                    if (TownyUtil.economy && nation.spawnCost > 0) tr(
                        "common.cost",
                        "cost" to TownyUtil.money(nation.spawnCost)
                    ) else null
                ).toTypedArray()
            )
        ) {
            runAndClose("towny:nation spawn ${nation.name}")
        }
        grid.add(
            Icons.icon(
                Material.BELL,
                tr("nation.towns"),
                null,
                tr("nation.towns-count", "count" to nation.numTowns)
            )
        ) {
            NationTownsMenu(player, nation, this).open()
        }
        grid.add(
            Icons.icon(
                Material.SHIELD, tr("nation.relations"), null,
                tr("icon.nation.relations", "allies" to nation.allies.size, "enemies" to nation.enemies.size)
            )
        ) {
            NationRelationsMenu(player, nation, this).open()
        }

        val town = viewer?.townOrNull
        if (viewer != null && town != null && viewer.isMayor && !town.hasNation() && nation.isOpen) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_NATION_JOIN,
                Icons.icon(Material.OAK_DOOR, tr("nation-info.join"), tr("nation-info.join-description"))
            ) {
                run("towny:nation join ${nation.name}", { town.hasNation() })
            }
        }
        if (own != null && own != nation) {
            val snapshot = { Triple(own.hasAlly(nation), own.hasEnemy(nation), own.sentAllyInvites.size) }
            if (own.hasAlly(nation)) {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_NATION_ALLY_REMOVE,
                    Icons.icon(
                        Material.SHIELD,
                        tr("nation-info.end-alliance"),
                        tr("nation-info.end-alliance-description")
                    )
                ) {
                    run("towny:nation ally remove ${nation.name}", snapshot)
                }
            } else {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_NATION_ALLY_ADD,
                    Icons.icon(
                        Material.SHIELD,
                        tr("nation.propose-alliance"),
                        tr("nation-info.propose-alliance-description")
                    )
                ) {
                    run("towny:nation ally add ${nation.name}", snapshot)
                }
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_NATION_ENEMY, Icons.icon(
                    Material.IRON_SWORD,
                    tr(if (own.hasEnemy(nation)) "nation-info.make-peace" else "nation.declare-enemy"),
                    tr(if (own.hasEnemy(nation)) "nation-info.make-peace-description" else "nation-info.declare-enemy-description"),
                )
            ) { run("towny:nation enemy ${if (own.hasEnemy(nation)) "remove" else "add"} ${nation.name}", snapshot) }
        }

        backButton(40)
    }
}
