package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.common.BankMenu
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.nation.NationMenu
import net.trilleo.mc.plugins.townymenu.guis.nation.NoNationMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Management hub for the viewer's own town. */
class TownMenu(player: Player, back: Menu?) : Menu(player, player.tr("town.title"), 6, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town
        if (town == null) {
            button(22, Icons.icon(Material.BARRIER, tr("town.not-in-town")))
            return backButton(49)
        }
        val viewer = resident ?: return backButton(49)

        button(4, Icons.town(player, town, *buildList {
            add(tr("common.founded", "date" to TownyUtil.date(town.registered)))
            if (TownyUtil.economy) {
                add(tr("bank.daily-tax", "tax" to if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)))
                add(tr("town.plot-tax", "tax" to TownyUtil.money(town.plotTax)))
            }
            add(tr("town.outposts", "count" to town.maxOutpostSpawn))
            add(tr("town.pvp-mobs", "pvp" to TownyUtil.onOff(player, town.isPVP), "mobs" to TownyUtil.onOff(player, town.hasMobs())))
            add(tr("town.fire-explosions", "fire" to TownyUtil.onOff(player, town.isFire), "explosions" to TownyUtil.onOff(player, town.isExplosion)))
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)

        grid.add(Icons.icon(Material.PLAYER_HEAD, tr("town.residents"), tr("town.residents-description"), tr("icon.town.residents", "count" to town.numResidents))) {
            TownMembersMenu(player, town, this).open()
        }
        if (TownyUtil.economy) {
            grid.add(Icons.icon(Material.GOLD_INGOT, tr("town.bank"), tr("town.bank-description"), tr("bank.balance", "balance" to TownyUtil.balance(town)))) {
                BankMenu(player, town, this).open()
            }
        }
        grid.add(Icons.icon(Material.GRASS_BLOCK, tr("claims.info"), tr("town.claims-description"),
            tr("icon.town.claims", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString))) {
            TownClaimsMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.FILLED_MAP, tr("main.map"), tr("common.map-description"))) {
            MapMenu(player, this).open()
        }
        grid.add(Icons.icon(Material.LEVER, tr("town.settings"), tr("town.settings-description"))) {
            toggles().open()
        }
        grid.add(Icons.icon(Material.WRITABLE_BOOK, tr("town.details"), tr("town.details-description"))) {
            TownSettingsMenu(player, this).open()
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_PERM,
            Icons.icon(Material.IRON_DOOR, tr("common.permissions"), tr("town.permissions-description"))) {
            PermissionMenu(player, tr("town.permissions-title"), this, "towny:town set perm",
                PermissionNodes.TOWNY_COMMAND_TOWN_SET_PERM, false) { TownyAPI.getInstance().getResident(player)?.townOrNull?.permissions }.open()
        }
        grid.add(Icons.icon(Material.TRIPWIRE_HOOK, tr("common.trusted"), tr("town.trusted-description"))) {
            TownTrustMenu(player, town, this).open()
        }
        grid.add(Icons.icon(Material.IRON_BARS, tr("town.outlaws"), tr("town.outlaws-description"), tr("town.outlaws-count", "count" to town.outlaws.size))) {
            OutlawsMenu(player, town, this).open()
        }
        grid.add(Icons.icon(Material.ENDER_PEARL, tr("town.spawn"), tr("town.spawn-description"))) {
            runAndClose("towny:town spawn")
        }
        if (town.hasOutpostSpawn()) {
            grid.add(Icons.icon(Material.COMPASS, tr("town.outpost-teleport"), tr("town.outpost-teleport-description"))) {
                OutpostsMenu(player, town, this).open()
            }
        }
        val nation = town.nationOrNull
        grid.add(Icons.icon(Material.BEACON, tr("main.nation"),
            if (nation != null) tr("town.nation-open", "nation" to TownyUtil.name(nation.name)) else tr("town.nation-none"))) {
            if (town.hasNation()) NationMenu(player, this).open() else NoNationMenu(player, this).open()
        }

        if (viewer.isMayor) {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_DELETE,
                Icons.icon(Material.TNT, tr("town.delete"), tr("town.delete-description"))) {
                run("towny:town delete", { viewer.hasTown() }, returnTo = MainMenu(player))
            }
        } else {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_LEAVE,
                Icons.icon(Material.OAK_DOOR, tr("town.leave"), tr("town.leave-description"))) {
                run("towny:town leave", { viewer.hasTown() }, returnTo = MainMenu(player))
            }
        }

        backButton(49)
    }

    private fun toggles(): Menu {
        fun toggle(material: Material, key: String, label: String, description: String, node: PermissionNodes, read: (Town) -> Boolean) =
            Toggle(material, label, description, node, "towny:town toggle $key") { town?.let(read) }

        return ToggleMenu(player, tr("town.settings-title"), this, listOf(
            toggle(Material.IRON_SWORD, "pvp", tr("toggle.pvp"), tr("toggle.town-pvp-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_PVP) { it.isPVP },
            toggle(Material.ZOMBIE_HEAD, "mobs", tr("toggle.mobs"), tr("toggle.town-mobs-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_MOBS) { it.hasMobs() },
            toggle(Material.FLINT_AND_STEEL, "fire", tr("toggle.fire"), tr("toggle.town-fire-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_FIRE) { it.isFire },
            toggle(Material.TNT, "explosion", tr("toggle.explosions"), tr("toggle.town-explosions-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_EXPLOSION) { it.isExplosion },
            toggle(Material.OAK_DOOR, "open", tr("toggle.open"), tr("toggle.town-open-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_OPEN) { it.isOpen },
            toggle(Material.ENDER_EYE, "public", tr("toggle.public"), tr("toggle.town-public-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_PUBLIC) { it.isPublic },
            toggle(Material.WHITE_BANNER, "neutral", tr("toggle.peaceful"), tr("toggle.town-peaceful-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_NEUTRAL) { it.isNeutral },
            toggle(Material.GOLD_NUGGET, "taxpercent", tr("toggle.tax-percent"), tr("toggle.tax-percent-description"), PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_TAXPERCENT) { it.isTaxPercentage },
        ))
    }
}
