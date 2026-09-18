package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.kyori.adventure.text.minimessage.MiniMessage
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.DialogUtil
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Server-wide Towny actions (`/townyadmin`): new day, backups, reloads, and global toggles. */
class AdminServerMenu(player: Player, back: Menu) : Menu(player, player.tr("admin-server.title"), 6, back) {

    override fun build() {
        guarded(
            10, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NEWDAY,
            Icons.icon(Material.CLOCK, tr("admin-server.new-day"), tr("admin-server.new-day-description"))
        ) {
            DialogUtil.confirm(
                player,
                MiniMessage.miniMessage().deserialize(tr("admin-server.new-day-confirm")),
                tr("admin-server.new-day-description"),
                onYes = { runAndClose("towny:townyadmin newday") },
                onNo = ::open,
            )
        }
        guarded(
            11, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_NEWHOUR,
            Icons.icon(Material.COMPASS, tr("admin-server.new-hour"), tr("admin-server.new-hour-description"))
        ) {
            runAndClose("towny:townyadmin newhour")
        }
        guarded(
            12, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_BACKUP,
            Icons.icon(Material.CHEST, tr("admin-server.backup"), tr("admin-server.backup-description"))
        ) {
            runAndClose("towny:townyadmin backup")
        }
        guarded(
            13, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_DATABASE,
            Icons.icon(
                Material.ENDER_CHEST,
                tr("admin-server.save-database"),
                tr("admin-server.save-database-description")
            )
        ) {
            runAndClose("towny:townyadmin database save")
        }
        guarded(
            14, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_CHECKOUTPOSTS,
            Icons.icon(
                Material.RECOVERY_COMPASS,
                tr("admin-server.check-outposts"),
                tr("admin-server.check-outposts-description")
            )
        ) {
            runAndClose("towny:townyadmin checkoutposts")
        }

        guarded(
            15, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_PURGE,
            Icons.icon(Material.BONE, tr("admin-server.purge"), tr("admin-server.purge-description"))
        ) {
            prompt(
                tr("admin-server.purge"),
                tr("admin-server.purge-label"),
                tr("admin-server.purge-hint"),
                initial = "90",
                maxLength = 5
            ) { days ->
                runAndClose("towny:townyadmin purge ${TownyUtil.argument(days)}")
            }
        }
        guarded(
            16, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_CHECKPERM,
            Icons.icon(Material.SPYGLASS, tr("admin-server.check-perm"), tr("admin-server.check-perm-description"))
        ) {
            Pickers.resident(this, tr("admin-server.check-perm"), { it.isOnline }) { target ->
                prompt(tr("admin-server.check-perm"), tr("admin-perms.node-label"), maxLength = 128) { node ->
                    runAndClose("towny:townyadmin checkperm ${target.name} ${TownyUtil.argument(node)}")
                }
            }.open()
        }
        reload(
            19,
            Material.COMPARATOR,
            "config",
            tr("admin-server.reload-config"),
            tr("admin-server.reload-config-description")
        )
        reload(20, Material.BOOK, "lang", tr("admin-server.reload-lang"), tr("admin-server.reload-lang-description"))
        reload(
            21,
            Material.IRON_DOOR,
            "perms",
            tr("admin-server.reload-perms"),
            tr("admin-server.reload-perms-description")
        )
        reload(
            22,
            Material.ENDER_CHEST,
            "database",
            tr("admin-server.reload-database"),
            tr("admin-server.reload-database-description")
        )
        reload(
            23,
            Material.NETHER_STAR,
            "all",
            tr("admin-server.reload-all"),
            tr("admin-server.reload-all-description")
        )

        if (TownyUtil.economy) {
            guarded(
                24, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_ECO_DEPOSITALL,
                Icons.icon(
                    Material.GOLD_BLOCK,
                    tr("admin-server.deposit-all"),
                    tr("admin-server.deposit-all-description")
                )
            ) {
                prompt(
                    tr("admin-server.deposit-all"),
                    tr("common.amount"),
                    tr("admin-server.deposit-all-hint")
                ) { amount ->
                    runAndClose("towny:townyadmin eco depositall ${TownyUtil.argument(amount)}")
                }
            }
        }
        toggle(
            28,
            Material.GOLD_INGOT,
            "townwithdraw",
            tr("admin-server.town-withdraw"),
            tr("admin-server.town-withdraw-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_TOWNWITHDRAW,
            TownySettings::getTownBankAllowWithdrawls
        )
        toggle(
            29,
            Material.DIAMOND,
            "nationwithdraw",
            tr("admin-server.nation-withdraw"),
            tr("admin-server.nation-withdraw-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_NATIONWITHDRAW,
            TownySettings::getNationBankAllowWithdrawls
        )
        toggle(
            30, Material.SPYGLASS, "debug", tr("admin-server.debug"), tr("admin-server.debug-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_DEBUG, TownySettings::getDebug
        )
        toggle(
            31,
            Material.REDSTONE_TORCH,
            "devmode",
            tr("admin-server.dev-mode"),
            tr("admin-server.dev-mode-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_DEVMODE,
            TownySettings::isDevMode
        )

        allWorlds(
            33,
            Material.GRASS_BLOCK,
            "wildernessuse",
            tr("admin-server.wilderness-use"),
            tr("admin-server.wilderness-use-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_WILDERNESSUSE
        )
        allWorlds(
            34,
            Material.OAK_SAPLING,
            "regenerations",
            tr("admin-server.regenerations"),
            tr("admin-server.regenerations-description"),
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_REGENERATIONS
        )

        backButton(49)
    }

    private fun reload(slot: Int, material: Material, target: String, name: String, description: String) {
        guarded(slot, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RELOAD, Icons.icon(material, name, description)) {
            runAndClose("towny:townyadmin reload $target")
        }
    }

    private fun toggle(
        slot: Int,
        material: Material,
        key: String,
        name: String,
        description: String,
        node: PermissionNodes,
        read: () -> Boolean
    ) {
        guarded(slot, node, Icons.toggle(player, material, name, read(), description)) {
            run("towny:townyadmin toggle $key", read)
        }
    }

    /** A setting Towny applies to every world at once and cannot read back, so it is set explicitly on or off. */
    private fun allWorlds(
        slot: Int,
        material: Material,
        key: String,
        name: String,
        description: String,
        node: PermissionNodes
    ) {
        guarded(
            slot,
            node,
            Icons.icon(
                material, name, description,
                actions = listOf(tr("admin-server.left-on"), tr("admin-server.right-off"))
            )
        ) { click ->
            runAndClose("towny:townyadmin toggle $key ${if (click.isRightClick) "off" else "on"}")
        }
    }
}
