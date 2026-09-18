package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
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

/** Manages any town as an admin (`/townyadmin town`), bypassing mayor permissions and costs. */
class AdminTownMenu(player: Player, private val town: Town, back: Menu) :
    Menu(player, player.tr("admin-town.title", "town" to TownyUtil.name(town.name)), 6, back) {

    private val command: String
        get() = "towny:townyadmin town ${town.name}"

    private val exists: Boolean
        get() = TownyAPI.getInstance().getTown(town.uuid) != null

    override fun build() {
        if (!exists) {
            button(22, Icons.icon(Material.BARRIER, tr("admin.gone")))
            return backButton(49)
        }

        button(
            4, Icons.town(
                player, town,
                tr("admin-town.bonus-blocks", "count" to town.bonusBlocks),
                tr("admin-town.bought-blocks", "count" to town.purchasedBlocks),
            )
        )

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_SPAWN,
            Icons.icon(Material.ENDER_PEARL, tr("admin-town.spawn"), tr("admin-town.spawn-description"))
        ) {
            runAndClose("$command spawn")
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TOGGLE,
            Icons.icon(Material.LEVER, tr("town.settings"), tr("admin-town.toggles-description"))
        ) {
            toggles().open()
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_RENAME,
            Icons.icon(
                Material.NAME_TAG,
                tr("admin.rename"),
                null,
                tr("common.current", "value" to TownyUtil.name(town.name))
            )
        ) {
            prompt(tr("admin.rename"), tr("common.new-name"), initial = town.name) { name ->
                run("$command rename ${TownyUtil.nameArgument(name)}", { town.name })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_MAYOR,
            Icons.icon(
                Material.GOLDEN_HELMET, tr("admin-town.mayor"), tr("admin-town.mayor-description"),
                tr("common.current", "value" to (town.mayor?.let { TownyUtil.name(it.name) } ?: "-")))) {
            residentList(tr("admin-town.mayor-title"), { !it.isMayor }) { resident ->
                run(
                    "towny:townyadmin set mayor ${town.name} ${resident.name}",
                    { town.mayor },
                    returnTo = this@AdminTownMenu
                )
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_ADD,
            Icons.icon(Material.LIME_DYE, tr("admin-town.add"), tr("admin-town.add-description"))
        ) {
            prompt(tr("admin-town.add"), tr("common.player-name")) { name ->
                run("$command add ${TownyUtil.argument(name)}", { town.numResidents })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_KICK,
            Icons.icon(
                Material.IRON_BOOTS, tr("admin-town.kick"), tr("admin-town.kick-description"),
                tr("icon.town.residents", "count" to town.numResidents)
            )
        ) {
            residentList(tr("admin-town.kick-title"), { !it.isMayor }) { resident ->
                run("$command kick ${resident.name}", { town.numResidents }, returnTo = this)
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_GIVEBONUS,
            Icons.icon(
                Material.GRASS_BLOCK, tr("admin-town.give-bonus"), tr("admin-town.give-bonus-description"),
                tr("common.current", "value" to town.bonusBlocks)
            )
        ) {
            prompt(tr("admin-town.give-bonus"), tr("common.amount")) { amount ->
                run("towny:townyadmin givebonus ${town.name} ${TownyUtil.argument(amount)}", { town.bonusBlocks })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_GIVEBOUGHTBLOCKS,
            Icons.icon(
                Material.MOSS_BLOCK, tr("admin-town.give-bought"), tr("admin-town.give-bought-description"),
                tr("common.current", "value" to town.purchasedBlocks)
            )
        ) {
            prompt(tr("admin-town.give-bought"), tr("common.amount")) { amount ->
                run("$command giveboughtblocks ${TownyUtil.argument(amount)}", { town.purchasedBlocks })
            }
        }

        if (TownyUtil.economy) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_DEPOSIT,
                Icons.icon(
                    Material.EMERALD,
                    tr("bank.deposit"),
                    tr("admin.deposit-description"),
                    tr("bank.balance", "balance" to TownyUtil.balance(town))
                )
            ) {
                prompt(tr("bank.deposit-title"), tr("common.amount")) { amount ->
                    run("$command deposit ${TownyUtil.argument(amount)}", { town.account.holdingBalance })
                }
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_WITHDRAW,
                Icons.icon(
                    Material.REDSTONE,
                    tr("bank.withdraw"),
                    tr("admin.withdraw-description"),
                    tr("bank.balance", "balance" to TownyUtil.balance(town))
                )
            ) {
                prompt(tr("bank.withdraw-title"), tr("common.amount")) { amount ->
                    run("$command withdraw ${TownyUtil.argument(amount)}", { town.account.holdingBalance })
                }
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_BANKHISTORY,
                Icons.icon(Material.WRITTEN_BOOK, tr("bank.history"), tr("bank.history-description"))
            ) {
                runAndClose("$command bankhistory")
            }
        }

        val nation = town.nationOrNull
        if (nation != null) {
            grid.add(Icons.nation(player, nation, actions = listOf(tr("common.click-view")))) {
                AdminNationMenu(player, nation, this).open()
            }
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_LEAVENATION,
                Icons.icon(Material.OAK_DOOR, tr("admin-town.leave-nation"), tr("admin-town.leave-nation-description"))
            ) {
                run("$command leavenation", { town.hasNation() })
            }
        }
        if (town.isRuined) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_UNRUIN,
                Icons.icon(Material.MOSSY_COBBLESTONE, tr("admin-town.unruin"), tr("admin-town.unruin-description"))
            ) {
                run("$command unruin", { town.isRuined })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_DELETE,
            Icons.icon(Material.TNT, tr("admin-town.delete"), tr("admin-town.delete-description"))
        ) {
            run("$command delete", ::exists, returnTo = back ?: this)
        }

        grid.add(
            Icons.icon(Material.ANVIL, tr("admin-town-tools.open"), tr("admin-town-tools.open-description"))
        ) {
            AdminTownToolsMenu(player, town, this).open()
        }

        backButton(49)
    }

    private fun residentList(title: String, filter: (Resident) -> Boolean, onPick: ListMenu.(Resident) -> Unit) {
        ListMenu(player, title, this) { list ->
            town.residents.filter(filter).sortedBy { it.name.lowercase() }.map { resident ->
                MenuEntry({ Icons.resident(player, resident, actions = listOf(tr("picker.select"))) }) {
                    list.onPick(resident)
                }
            }
        }.open()
    }

    private fun toggles(): Menu {
        fun toggle(material: Material, key: String, name: String, description: String, read: (Town) -> Boolean) =
            Toggle(
                material,
                name,
                description,
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_TOGGLE,
                "$command toggle $key"
            ) { read(town) }

        return ToggleMenu(
            player, tr("admin-town.toggles-title", "town" to TownyUtil.name(town.name)), this, listOf(
                toggle(Material.IRON_SWORD, "pvp", tr("toggle.pvp"), tr("toggle.town-pvp-description")) { it.isPVP },
                toggle(
                    Material.ZOMBIE_HEAD,
                    "mobs",
                    tr("toggle.mobs"),
                    tr("toggle.town-mobs-description")
                ) { it.hasMobs() },
                toggle(
                    Material.FLINT_AND_STEEL,
                    "fire",
                    tr("toggle.fire"),
                    tr("toggle.town-fire-description")
                ) { it.isFire },
                toggle(
                    Material.TNT,
                    "explosion",
                    tr("toggle.explosions"),
                    tr("toggle.town-explosions-description")
                ) { it.isExplosion },
                toggle(Material.OAK_DOOR, "open", tr("toggle.open"), tr("toggle.town-open-description")) { it.isOpen },
                toggle(
                    Material.ENDER_EYE,
                    "public",
                    tr("toggle.public"),
                    tr("toggle.town-public-description")
                ) { it.isPublic },
                toggle(
                    Material.WHITE_BANNER,
                    "neutral",
                    tr("toggle.peaceful"),
                    tr("toggle.town-peaceful-description")
                ) { it.isNeutral },
                toggle(
                    Material.GOLD_NUGGET,
                    "taxpercent",
                    tr("toggle.tax-percent"),
                    tr("toggle.tax-percent-description")
                ) { it.isTaxPercentage },
                toggle(
                    Material.DIAMOND_SWORD,
                    "forcepvp",
                    tr("admin-town.force-pvp"),
                    tr("admin-town.force-pvp-description")
                ) { it.isAdminEnabledPVP },
                toggle(
                    Material.WOODEN_SWORD,
                    "forcedisablepvp",
                    tr("admin-town.force-no-pvp"),
                    tr("admin-town.force-no-pvp-description")
                ) { it.isAdminDisabledPVP },
                toggle(
                    Material.CREEPER_HEAD,
                    "forcemobs",
                    tr("admin-town.force-mobs"),
                    tr("admin-town.force-mobs-description")
                ) { it.isAdminEnabledMobs },
                toggle(
                    Material.MAP,
                    "unlimitedclaims",
                    tr("admin-town.unlimited-claims"),
                    tr("admin-town.unlimited-claims-description")
                ) { it.hasUnlimitedClaims() },
                toggle(
                    Material.GOLD_INGOT,
                    "upkeep",
                    tr("admin-town.upkeep"),
                    tr("admin-town.upkeep-description")
                ) { it.hasUpkeep() },
                toggle(
                    Material.RED_BANNER,
                    "allowedtowar",
                    tr("admin-town.war"),
                    tr("admin-town.war-description")
                ) { it.isAllowedToWar },
                toggle(
                    Material.SPYGLASS,
                    "visibleontoplists",
                    tr("admin-town.top-lists"),
                    tr("admin-town.top-lists-description")
                ) { it.isVisibleOnTopLists },
            )
        )
    }
}
