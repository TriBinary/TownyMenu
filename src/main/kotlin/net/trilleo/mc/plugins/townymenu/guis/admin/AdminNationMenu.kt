package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Manages any nation as an admin (`/townyadmin nation`). */
class AdminNationMenu(player: Player, private val nation: Nation, back: Menu) :
    Menu(player, player.tr("admin-nation.title", "nation" to TownyUtil.name(nation.name)), 5, back) {

    private val command: String
        get() = "towny:townyadmin nation ${nation.name}"

    private val exists: Boolean
        get() = TownyAPI.getInstance().getNation(nation.uuid) != null

    override fun build() {
        if (!exists) {
            button(22, Icons.icon(Material.BARRIER, tr("admin.gone")))
            return backButton(40)
        }

        button(4, Icons.nation(player, nation))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_TOGGLE,
            Icons.icon(Material.LEVER, tr("admin-nation.toggles"), tr("admin-nation.toggles-description"))
        ) {
            toggles().open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_RENAME,
            Icons.icon(
                Material.NAME_TAG,
                tr("admin.rename"),
                null,
                tr("common.current", "value" to TownyUtil.name(nation.name))
            )
        ) {
            prompt(tr("admin.rename"), tr("common.new-name"), initial = nation.name) { name ->
                run("$command rename ${TownyUtil.nameArgument(name)}", { nation.name })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_SET,
            Icons.icon(
                Material.PLAYER_HEAD, tr("nation-details.leader"), tr("admin-nation.leader-description"),
                tr("common.current", "value" to (nation.king?.let { TownyUtil.name(it.name) } ?: "-")))) {
            ListMenu(player, tr("nation-details.leader-title"), this) {
                nation.capital?.residents.orEmpty().filter { !it.isKing }.sortedBy { it.name.lowercase() }
                    .map { candidate ->
                        MenuEntry({ Icons.resident(player, candidate, "", tr("nation-details.leader-click")) }) {
                            run("$command set king ${candidate.name}", { nation.king }, returnTo = this)
                        }
                    }
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_SET,
            Icons.icon(
                Material.GOLDEN_HELMET, tr("nation-details.capital"), null,
                tr("common.current", "value" to (nation.capital?.let { TownyUtil.name(it.name) } ?: "-")))) {
            townList(tr("nation-details.capital-title"), tr("nation-details.capital-click")) { town ->
                run("$command set capital ${town.name}", { nation.capital }, returnTo = this)
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_ADD,
            Icons.icon(Material.LIME_DYE, tr("admin-nation.add"), tr("admin-nation.add-description"))
        ) {
            Pickers.town(this, tr("admin-nation.add"), { !it.hasNation() }) { town ->
                run("$command add ${town.name}", { nation.numTowns })
            }.open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_KICK,
            Icons.icon(
                Material.IRON_BOOTS, tr("admin-nation.kick"), tr("admin-nation.kick-description"),
                tr("admin-nation.towns", "count" to nation.numTowns)
            )
        ) {
            townList(tr("admin-nation.kick"), tr("picker.select")) { town ->
                run("$command kick ${town.name}", { nation.numTowns })
            }
        }

        if (TownyUtil.economy) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_DEPOSIT,
                Icons.icon(
                    Material.EMERALD,
                    tr("bank.deposit"),
                    tr("admin.deposit-description"),
                    tr("bank.balance", "balance" to TownyUtil.balance(nation))
                )
            ) {
                prompt(tr("bank.deposit-title"), tr("common.amount")) { amount ->
                    run("$command deposit ${TownyUtil.argument(amount)}", { nation.account.holdingBalance })
                }
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_WITHDRAW,
                Icons.icon(
                    Material.REDSTONE,
                    tr("bank.withdraw"),
                    tr("admin.withdraw-description"),
                    tr("bank.balance", "balance" to TownyUtil.balance(nation))
                )
            ) {
                prompt(tr("bank.withdraw-title"), tr("common.amount")) { amount ->
                    run("$command withdraw ${TownyUtil.argument(amount)}", { nation.account.holdingBalance })
                }
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_BANKHISTORY,
                Icons.icon(Material.WRITTEN_BOOK, tr("bank.history"), tr("bank.history-description"))
            ) {
                runAndClose("$command bankhistory")
            }
        }

        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_DELETE,
            Icons.icon(Material.TNT, tr("admin-nation.delete"), tr("admin-nation.delete-description"))
        ) {
            run("$command delete", ::exists, returnTo = back ?: this)
        }

        backButton(40)
    }

    /** Lists the nation's towns other than the capital. */
    private fun townList(title: String, hint: String, onPick: (Town) -> Unit) {
        ListMenu(player, title, this) {
            nation.towns.filter { !nation.isCapital(it) }.sortedBy { it.name.lowercase() }.map { town ->
                MenuEntry({ Icons.town(player, town, "", hint) }) { onPick(town) }
            }
        }.open()
    }

    private fun toggles(): Menu {
        fun toggle(material: Material, key: String, name: String, description: String, read: (Nation) -> Boolean) =
            Toggle(
                material,
                name,
                description,
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NATION_TOGGLE,
                "$command toggle $key"
            ) { read(nation) }

        return ToggleMenu(
            player, tr("admin-nation.toggles-title", "nation" to TownyUtil.name(nation.name)), this, listOf(
                toggle(
                    Material.WHITE_BANNER,
                    "peaceful",
                    tr("toggle.peaceful"),
                    tr("toggle.nation-peaceful-description")
                ) { it.isNeutral },
                toggle(
                    Material.OAK_DOOR,
                    "open",
                    tr("toggle.open"),
                    tr("toggle.nation-open-description")
                ) { it.isOpen },
                toggle(
                    Material.ENDER_EYE,
                    "public",
                    tr("toggle.public"),
                    tr("toggle.nation-public-description")
                ) { it.isPublic },
                toggle(
                    Material.GOLD_NUGGET,
                    "taxpercent",
                    tr("toggle.tax-percent"),
                    tr("toggle.nation-tax-percent-description")
                ) { it.isTaxPercentage },
            )
        )
    }
}
