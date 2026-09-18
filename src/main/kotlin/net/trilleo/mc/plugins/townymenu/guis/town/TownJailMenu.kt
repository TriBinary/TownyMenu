package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.jail.Jail
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.*
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Prisoners held in the viewer's town jails. Staff can jail online residents and pardon prisoners. */
class TownJailMenu(player: Player, private val town: Town, back: Menu) :
    PagedMenu(player, player.tr("town-jail.title"), back) {

    private fun count() = town.jailedPlayerCount

    override fun entries(): List<MenuEntry> {
        val canUnjail = TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWN_UNJAIL)
        return town.jailedResidents.sortedBy { it.name.lowercase() }.map { prisoner ->
            val lines = buildList {
                add("")
                add(tr("town-jail.hours-left", "hours" to prisoner.jailHours))
                if (TownyUtil.economy && prisoner.jailBailCost > 0) {
                    add(tr("town-jail.bail", "bail" to TownyUtil.money(prisoner.jailBailCost)))
                }
                prisoner.jail?.let { jail ->
                    add(
                        tr(
                            "town-jail.held-in",
                            "jail" to jailName(this@TownJailMenu, town, jail),
                            "cell" to prisoner.jailCell + 1
                        )
                    )
                }
                if (canUnjail) add(tr("town-jail.click-unjail"))
            }
            MenuEntry({ Icons.resident(player, prisoner, *lines.toTypedArray()) }) {
                if (canUnjail) run("towny:town unjail ${prisoner.name}", ::count)
            }
        }
    }

    override fun controls() {
        tutorialButton(52, Tutorial.PROTECTION)
        if (town.hasJails()) {
            guarded(
                47, PermissionNodes.TOWNY_COMMAND_TOWN_JAIL,
                Icons.icon(Material.IRON_BARS, tr("town-jail.jail-player"), tr("town-jail.jail-player-description"))
            ) {
                Pickers.resident(this, tr("town-jail.jail-player-title"), ::canJail) { picked ->
                    JailSentenceMenu(player, town, picked, this).open()
                }.open()
            }
        } else {
            button(47, Icons.icon(Material.BARRIER, tr("town-jail.no-jails"), tr("town-jail.no-jails-description")))
        }
        button(
            51, Icons.icon(
                Material.IRON_CHAIN, tr("town-jail.jails"), tr("town-jail.jails-description"),
                tr("town-jail.jails-count", "count" to jails(town).size)
            )
        ) { jailList().open() }
    }

    /** Towny only jails online residents of the town, or of the nation when its capital may jail nation residents. */
    private fun canJail(target: Resident): Boolean =
        !target.isJailed && (target.townOrNull == town ||
                (TownySettings.canNationLeadersJailNationResidents() && town.isCapital &&
                        town.nationOrNull?.hasResident(target) == true))

    private fun jailList(): Menu = ListMenu(player, tr("town-jail.jails-title"), this) {
        jails(town).map { jail ->
            MenuEntry({
                val block = jail.townBlock
                Icons.icon(
                    Material.IRON_BARS, "<gold>${jailName(this, town, jail)}", null, *listOfNotNull(
                        if (jail == town.primaryJail) tr("town-jail.primary") else null,
                        tr("map.chunk", "x" to block.x, "z" to block.z),
                        tr("town-jail.cells", "count" to jail.jailCellCount),
                        tr("town-jail.prisoners", "count" to town.jailedResidents.count { it.jail == jail }),
                    ).toTypedArray()
                )
            })
        }
    }

    companion object {
        fun jails(town: Town): List<Jail> = town.jails.orEmpty().toList()

        /** The jail plot's name, or its number as used by `/town jail`. */
        fun jailName(menu: Menu, town: Town, jail: Jail): String =
            if (jail.hasName()) TownyUtil.text(jail.name)
            else menu.tr("town-jail.jail-number", "number" to jails(town).indexOf(jail) + 1)
    }
}
