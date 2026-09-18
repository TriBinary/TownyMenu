package net.trilleo.mc.plugins.townymenu.guis.resident

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.PermissionMenu
import net.trilleo.mc.plugins.townymenu.guis.common.Toggle
import net.trilleo.mc.plugins.townymenu.guis.common.ToggleMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.ListMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.framework.MenuEntry
import net.trilleo.mc.plugins.townymenu.guis.town.TownInfoMenu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.Prices
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** The viewer's own resident profile: friends, personal toggles, plot permissions, and more. */
class ResidentMenu(player: Player, back: Menu?) : Menu(player, player.tr("profile.title"), 5, back) {

    override fun build() {
        val resident = resident ?: return backButton(40)

        button(4, Icons.resident(player, resident, *buildList {
            add(tr("profile.registered", "date" to TownyUtil.date(resident.registered)))
            add(tr("profile.friends-count", "count" to resident.friends.size))
            add(tr("profile.plots-owned", "count" to resident.townBlocks.size))
            if (TownyUtil.economy) {
                resident.accountOrNull?.let { add(tr("bank.balance", "balance" to TownyUtil.money(it.holdingBalance))) }
                if (resident.hasTown()) add(
                    tr(
                        "profile.tax-owed",
                        "tax" to TownyUtil.money(resident.getTaxOwing(true))
                    )
                )
            }
            TownyUtil.about(resident)?.let { add(tr("profile.about-line", "about" to TownyUtil.text(it))) }
            if (resident.isJailed) {
                add(
                    tr(
                        "profile.jailed",
                        "town" to (resident.jailTown?.let { TownyUtil.name(it.name) } ?: tr("profile.jailed-unknown")),
                        "hours" to resident.jailHours))
            }
        }.toTypedArray()))

        val row = layout(19, 20, 21, 22, 23, 24, 25)
        row.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_FRIEND,
            Icons.icon(Material.POPPY, tr("profile.friends"), tr("profile.friends-description"))
        ) {
            FriendsMenu(player, this).open()
        }
        row.add(Icons.icon(Material.LEVER, tr("profile.settings"), tr("profile.settings-description"))) {
            toggles().open()
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_PERM,
            Icons.icon(Material.IRON_DOOR, tr("profile.permissions"), tr("profile.permissions-description"))
        ) {
            permissions().open()
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_ABOUT,
            Icons.icon(Material.WRITABLE_BOOK, tr("profile.about"), tr("profile.about-description"))
        ) { click ->
            if (click.isRightClick) {
                run("towny:resident set about none", { resident.about })
            } else {
                prompt(
                    tr("profile.about-title"),
                    tr("profile.bio"),
                    initial = TownyUtil.about(resident).orEmpty(),
                    maxLength = 159
                ) { text ->
                    run("towny:resident set about $text", { resident.about })
                }
            }
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_SPAWN,
            Icons.icon(
                Material.RED_BED, tr("profile.spawn"), tr("profile.spawn-description"),
                *listOfNotNull(costLine(Prices.residentSpawn(player))).toTypedArray()
            )
        ) {
            runAndClose("towny:resident spawn")
        }
        row.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_MODE,
            Icons.icon(
                Material.MILK_BUCKET, tr("profile.modes"), tr("profile.modes-description"),
                tr(
                    "profile.modes-active",
                    "modes" to resident.modes.ifEmpty { listOf(tr("common.none")) }.joinToString(", ")
                )
            )
        ) { click ->
            run("towny:resident set mode ${if (click.isRightClick) "reset" else "clear"}", { resident.modes.toSet() })
        }
        if (resident.isJailed && TownyUtil.economy) {
            row.add(
                Icons.icon(
                    Material.IRON_BARS, tr("profile.bail"),
                    tr("profile.bail-description", "cost" to TownyUtil.money(resident.jailBailCost))
                )
            ) {
                run("towny:resident jail paybail", { resident.isJailed })
            }
        }

        standing(resident)
        tutorialButton(44, Tutorial.PROFILE)
        backButton(40)
    }

    /** The bottom-row lists of where the viewer stands: their plots, daily tax, and the towns that outlaw or trust them. */
    private fun standing(resident: Resident) {
        guarded(
            37, PermissionNodes.TOWNY_COMMAND_RESIDENT_PLOTLIST,
            Icons.icon(
                Material.GRASS_BLOCK, tr("profile.plots"), tr("profile.plots-description"),
                tr("profile.plots-owned", "count" to resident.townBlocks.size)
            )
        ) {
            ResidentPlotsMenu(player, resident, this).open()
        }
        if (TownyUtil.economy && TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_RESIDENT_TAX)) {
            button(38, taxIcon(resident))
        }
        guarded(
            42, PermissionNodes.TOWNY_COMMAND_RESIDENT_OUTLAWLIST,
            Icons.icon(
                Material.IRON_BARS, tr("profile.outlawed-in"), tr("profile.outlawed-in-description"),
                tr("profile.towns-count", "count" to resident.townsOutlawedIn.size)
            )
        ) {
            towns(tr("profile.outlawed-in-title")) { it.townsOutlawedIn }.open()
        }
        guarded(
            43, PermissionNodes.TOWNY_COMMAND_RESIDENT_TRUSTLIST,
            Icons.icon(
                Material.TRIPWIRE_HOOK, tr("profile.trusted-in"), tr("profile.trusted-in-description"),
                tr("profile.towns-count", "count" to resident.townsTrustedIn.size)
            )
        ) {
            towns(tr("profile.trusted-in-title")) { it.townsTrustedIn }.open()
        }
    }

    /** The same daily tax breakdown as Towny's `/resident tax`. */
    private fun taxIcon(resident: Resident): ItemStack {
        val exempt = resident.hasPermissionNode("towny.tax_exempt")
        val town = resident.townOrNull
        val townTax = when {
            town == null || exempt -> 0.0
            town.isTaxPercentage -> minOf(
                (resident.accountOrNull?.cachedBalance ?: 0.0) * town.taxes / 100,
                town.maxPercentTaxAmount
            )

            else -> town.taxes
        }
        val plotTax = resident.townBlocks.sumOf { plot ->
            val plotTown = plot.townOrNull
            if (plotTown == null || (exempt && plotTown.hasResident(resident))) 0.0 else plot.type.getTax(plotTown)
        }
        return Icons.icon(Material.GOLD_NUGGET, tr("profile.tax"), tr("profile.tax-description"), *buildList {
            if (exempt) add(tr("profile.tax-exempt"))
            if (town != null && !exempt) add(tr("profile.tax-town", "tax" to TownyUtil.money(townTax)))
            if (resident.townBlocks.isNotEmpty()) add(tr("profile.tax-plots", "tax" to TownyUtil.money(plotTax)))
            add(tr("profile.tax-total", "tax" to TownyUtil.money(townTax + plotTax)))
            add(tr("town-status.new-day", "time" to TownyUtil.duration(player, TownyUtil.secondsUntilNewDay())))
        }.toTypedArray())
    }

    private fun towns(title: String, source: (Resident) -> List<Town>): Menu = ListMenu(player, title, this) { menu ->
        TownyAPI.getInstance().getResident(player)?.let(source).orEmpty()
            .sortedBy { it.name.lowercase() }
            .map { town ->
                MenuEntry({ Icons.town(player, town, actions = listOf(tr("common.click-details"))) }) {
                    TownInfoMenu(player, town, menu).open()
                }
            }
    }

    fun permissions(): Menu =
        PermissionMenu(
            player, tr("profile.permissions-title"), this, "towny:resident set perm",
            PermissionNodes.TOWNY_COMMAND_RESIDENT_SET_PERM, true
        ) { TownyAPI.getInstance().getResident(player)?.permissions }

    fun toggles(): Menu {
        fun perm(
            material: Material,
            key: String,
            label: String,
            description: String,
            node: PermissionNodes?,
            read: (Resident) -> Boolean
        ) =
            Toggle(material, label, description, node, "towny:resident toggle $key") {
                TownyAPI.getInstance().getResident(player)?.let(read)
            }

        fun mode(material: Material, key: String, label: String, description: String, node: PermissionNodes?) =
            perm(material, key, label, description, node) { it.hasMode(key) }

        val viewer = TownyAPI.getInstance().getResident(player)
        val staff = listOfNotNull(
            if (TownyUtil.can(player, PermissionNodes.TOWNY_CHAT_SPY)) mode(
                Material.SPYGLASS,
                "spy",
                tr("toggle.spy"),
                tr("toggle.spy-description"),
                PermissionNodes.TOWNY_CHAT_SPY
            ) else null,
            // Admin bypass makes Towny deny the admin's own admin checks, so it can't be guarded by one.
            if (player.isOp || player.hasPermission(PermissionNodes.TOWNY_ADMIN.node) || viewer?.hasMode("adminbypass") == true) mode(
                Material.COMMAND_BLOCK,
                "adminbypass",
                tr("toggle.admin-bypass"),
                tr("toggle.admin-bypass-description"),
                null
            ) else null,
        )

        return ToggleMenu(
            player, tr("profile.settings-title"), this, listOf(
                perm(
                    Material.IRON_SWORD,
                    "pvp",
                    tr("toggle.pvp"),
                    tr("toggle.resident-pvp-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PVP
                ) { it.permissions.pvp },
                perm(
                    Material.FLINT_AND_STEEL,
                    "fire",
                    tr("toggle.fire"),
                    tr("toggle.resident-fire-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_FIRE
                ) { it.permissions.fire },
                perm(
                    Material.TNT,
                    "explosion",
                    tr("toggle.explosions"),
                    tr("toggle.resident-explosions-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_EXPLOSION
                ) { it.permissions.explosion },
                perm(
                    Material.ZOMBIE_HEAD,
                    "mobs",
                    tr("toggle.mobs"),
                    tr("toggle.resident-mobs-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_MOBS
                ) { it.permissions.mobs },
                perm(
                    Material.OAK_SIGN,
                    "bordertitles",
                    tr("toggle.border-titles"),
                    tr("toggle.border-titles-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_BORDERTITLES
                ) { it.isSeeingBorderTitles },
                mode(
                    Material.FILLED_MAP,
                    "map",
                    tr("toggle.auto-map"),
                    tr("toggle.auto-map-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_MAP
                ),
                mode(
                    Material.GRASS_BLOCK,
                    "townclaim",
                    tr("claims.auto-claim"),
                    tr("toggle.auto-claim-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNCLAIM
                ),
                mode(
                    Material.DEAD_BUSH,
                    "townunclaim",
                    tr("toggle.auto-unclaim"),
                    tr("toggle.auto-unclaim-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNUNCLAIM
                ),
                mode(
                    Material.GLOWSTONE_DUST,
                    "plotborder",
                    tr("toggle.plot-borders"),
                    tr("toggle.plot-borders-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PLOTBORDER
                ),
                mode(
                    Material.GLOWSTONE,
                    "constantplotborder",
                    tr("toggle.constant-plot-borders"),
                    tr("toggle.constant-plot-borders-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_CONSTANTPLOTBORDER
                ),
                mode(
                    Material.REDSTONE,
                    "townborder",
                    tr("toggle.town-borders"),
                    tr("toggle.town-borders-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNBORDER
                ),
                mode(
                    Material.STICK,
                    "infotool",
                    tr("toggle.info-tool"),
                    tr("toggle.info-tool-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_INFOTOOL
                ),
                mode(
                    Material.MAP,
                    "ignoreplots",
                    tr("toggle.ignore-plots"),
                    tr("toggle.ignore-plots-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_IGNOREPLOTS
                ),
                mode(
                    Material.PAPER,
                    "ignoreinvites",
                    tr("toggle.ignore-invites"),
                    tr("toggle.ignore-invites-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_IGNOREINVITES
                ),
                mode(
                    Material.WHITE_BED,
                    "bedspawn",
                    tr("toggle.bed-spawn"),
                    tr("toggle.bed-spawn-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_BEDSPAWN
                ),
                mode(
                    Material.CHEST,
                    "plotgroup",
                    tr("toggle.plot-group"),
                    tr("toggle.plot-group-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_PLOTGROUP
                ),
                mode(
                    Material.BARREL,
                    "district",
                    tr("toggle.district"),
                    tr("toggle.district-description"),
                    PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_DISTRICT
                ),
            ) + staff
        )
    }
}
