package net.trilleo.mc.plugins.townymenu.guis.nation

import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.common.BankMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentProfileMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Management hub for the viewer's own nation. */
class NationMenu(player: Player, back: Menu?) : Menu(player, player.tr("nation.title"), 6, back) {

    private val nation: Nation?
        get() = resident?.nationOrNull

    override fun build() {
        val nation = nation
        val viewer = resident
        if (nation == null || viewer == null) {
            button(22, Icons.icon(Material.BARRIER, tr("nation.not-in-nation")))
            return backButton(49)
        }

        button(4, Icons.nation(player, nation, *buildList {
            add(tr("common.founded", "date" to TownyUtil.date(nation.registered)))
            if (TownyUtil.economy) {
                add(
                    tr(
                        "nation.daily-tax",
                        "tax" to if (nation.isTaxPercentage) "${nation.taxes}%" else TownyUtil.money(nation.taxes)
                    )
                )
            }
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(
            Icons.icon(
                Material.BELL,
                tr("nation.towns"),
                tr("nation.towns-description"),
                tr("nation.towns-count", "count" to nation.numTowns)
            )
        ) {
            NationTownsMenu(player, nation, this).open()
        }
        grid.add(
            Icons.icon(
                Material.PLAYER_HEAD, tr("town.residents"), tr("nation.residents-description"),
                tr("icon.town.residents", "count" to nation.numResidents)
            )
        ) {
            residents(nation).open()
        }
        if (TownyUtil.economy) {
            grid.add(
                Icons.icon(
                    Material.GOLD_INGOT,
                    tr("nation.bank"),
                    tr("town.bank-description"),
                    tr("bank.balance", "balance" to TownyUtil.balance(nation))
                )
            ) {
                BankMenu(player, nation, this).open()
            }
        }
        grid.add(Icons.icon(Material.SHIELD, tr("nation.relations"), tr("nation.relations-description"))) {
            NationRelationsMenu(player, nation, this).open()
        }
        grid.add(Icons.icon(Material.LEVER, tr("nation.settings"), tr("nation.settings-description"))) {
            toggles().open()
        }
        grid.add(Icons.icon(Material.WRITABLE_BOOK, tr("nation.details"), tr("nation.details-description"))) {
            NationSettingsMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.ENDER_PEARL, tr("nation.spawn"), tr("nation.spawn-description"))) {
            runAndClose("towny:nation spawn")
        }
        if (viewer.isKing) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_NATION_DELETE,
                Icons.icon(Material.TNT, tr("nation.delete"), tr("nation.delete-description"))
            ) {
                run("towny:nation delete", { viewer.hasNation() }, returnTo = MainMenu(player))
            }
        } else if (viewer.isMayor) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_NATION_LEAVE,
                Icons.icon(Material.OAK_DOOR, tr("nation.leave"), tr("nation.leave-description"))
            ) {
                run("towny:nation leave", { viewer.hasNation() }, returnTo = MainMenu(player))
            }
        }

        tutorialButton(53, Tutorial.NATIONS)
        backButton(49)
    }

    private fun residents(nation: Nation): Menu =
        ListMenu(player, tr("town-members.title", "town" to TownyUtil.name(nation.name)), this) { menu ->
            nation.residents
                .sortedWith(compareByDescending<Resident> { it.isKing }
                    .thenByDescending { it.isOnline }
                    .thenBy { it.name.lowercase() })
                .map { member ->
                    MenuEntry({
                        Icons.resident(
                            player,
                            member,
                            "",
                            tr("common.click-view")
                        )
                    }) { ResidentProfileMenu(player, member, menu).open() }
                }
        }

    fun toggles(): Menu {
        fun toggle(
            material: Material,
            key: String,
            label: String,
            description: String,
            node: PermissionNodes,
            read: (Nation) -> Boolean
        ) =
            Toggle(material, label, description, node, "towny:nation toggle $key") { nation?.let(read) }

        return ToggleMenu(
            player, tr("nation.settings-title"), this, listOf(
                toggle(
                    Material.WHITE_BANNER,
                    "peaceful",
                    tr("toggle.peaceful"),
                    tr("toggle.nation-peaceful-description"),
                    PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_NEUTRAL
                ) { it.isNeutral },
                toggle(
                    Material.OAK_DOOR,
                    "open",
                    tr("toggle.open"),
                    tr("toggle.nation-open-description"),
                    PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_OPEN
                ) { it.isOpen },
                toggle(
                    Material.ENDER_EYE,
                    "public",
                    tr("toggle.public"),
                    tr("toggle.nation-public-description"),
                    PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_PUBLIC
                ) { it.isPublic },
                toggle(
                    Material.GOLD_NUGGET,
                    "taxpercent",
                    tr("toggle.tax-percent"),
                    tr("toggle.nation-tax-percent-description"),
                    PermissionNodes.TOWNY_COMMAND_NATION_TOGGLE_TAXPERCENT
                ) { it.isTaxPercentage },
            )
        )
    }
}
