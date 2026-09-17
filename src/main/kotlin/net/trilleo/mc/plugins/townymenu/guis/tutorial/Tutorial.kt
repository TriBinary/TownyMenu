package net.trilleo.mc.plugins.townymenu.guis.tutorial

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownyPermission
import net.trilleo.mc.plugins.townymenu.Main
import net.trilleo.mc.plugins.townymenu.guis.InvitesMenu
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.common.BankMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.nation.*
import net.trilleo.mc.plugins.townymenu.guis.plot.PlotAreaMenu
import net.trilleo.mc.plugins.townymenu.guis.plot.PlotGroupMenu
import net.trilleo.mc.plugins.townymenu.guis.plot.PlotMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.FriendsMenu
import net.trilleo.mc.plugins.townymenu.guis.resident.ResidentMenu
import net.trilleo.mc.plugins.townymenu.guis.town.*
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

/**
 * The menu a [Lesson] or [Chapter] points to. It is only built when opened, with `back` as its back button.
 * When [available] is `false` for a viewer, [requirement] is the translation key explaining why.
 */
class Link(
    val requirement: String?,
    private val condition: (Player) -> Boolean,
    private val factory: (Player, Menu) -> Menu?
) {

    fun available(player: Player): Boolean = condition(player)

    /** Opens the menu and returns `true`, or returns `false` when the viewer can't use it. */
    fun open(player: Player, back: Menu): Boolean {
        val menu = (if (condition(player)) factory(player, back) else null) ?: return false
        menu.open()
        return true
    }
}

/**
 * One tutorial topic. [title] and [body] are translation keys; [facts] returns translated lines with this server's
 * live Towny values. Lessons whose [visible] is `false` (e.g. economy topics without an economy) are hidden and
 * don't count towards progress.
 */
class Lesson(
    val id: String,
    val material: Material,
    val title: String,
    val body: String,
    val link: Link? = null,
    val visible: () -> Boolean = { true },
    val facts: (Player) -> List<String> = { emptyList() },
)

class Chapter(
    val material: Material,
    val title: String,
    val description: String,
    val link: Link?,
    lessons: List<Lesson>,
    private val shown: () -> Boolean = { true },
) {
    private val allLessons = lessons

    val lessons: List<Lesson>
        get() = allLessons.filter { it.visible() }

    val visible: Boolean
        get() = shown() && lessons.isNotEmpty()

    fun readCount(player: Player): Int = Tutorial.read(player).let { read -> lessons.count { it.id in read } }
}

/** Every tutorial chapter, and each player's read lessons (stored in their persistent data). */
object Tutorial {

    private val key by lazy { NamespacedKey(Main.instance, "tutorial-read") }

    fun read(player: Player): Set<String> =
        player.persistentDataContainer.get(key, PersistentDataType.LIST.strings())?.toSet().orEmpty()

    fun isRead(player: Player, lesson: Lesson): Boolean = lesson.id in read(player)

    fun setRead(player: Player, lessons: Collection<Lesson>, read: Boolean) {
        val ids = lessons.map { it.id }
        val updated = if (read) read(player) + ids else read(player) - ids.toSet()
        if (updated.isEmpty()) {
            player.persistentDataContainer.remove(key)
        } else {
            player.persistentDataContainer.set(key, PersistentDataType.LIST.strings(), updated.sorted())
        }
    }

    fun reset(player: Player) = player.persistentDataContainer.remove(key)

    val visibleChapters: List<Chapter>
        get() = chapters.filter { it.visible }

    /** The first chapter holding a lesson [player] hasn't read, or `null` when everything is read. */
    fun nextUnread(player: Player): Chapter? = visibleChapters.firstOrNull { it.readCount(player) < it.lessons.size }

    fun isComplete(player: Player): Boolean = nextUnread(player) == null

    private fun resident(player: Player): Resident? = TownyAPI.getInstance().getResident(player)
    private fun town(player: Player): Town? = resident(player)?.townOrNull
    private fun nation(player: Player): Nation? = resident(player)?.nationOrNull

    private val economy = { TownyUtil.economy }

    private fun always(open: (Player, Menu) -> Menu) = Link(null, { true }, open)

    private fun withTown(open: (Player, Town, Menu) -> Menu) =
        Link("tutorial.requires-town", { town(it) != null }) { player, back ->
            town(player)?.let {
                open(
                    player,
                    it,
                    back
                )
            }
        }

    private fun withNation(open: (Player, Nation, Menu) -> Menu) =
        Link("tutorial.requires-nation", { nation(it) != null }) { player, back ->
            nation(player)?.let {
                open(
                    player,
                    it,
                    back
                )
            }
        }

    private val townOrFind = always { player, back ->
        if (town(player) != null) TownMenu(player, back) else NoTownMenu(player, back)
    }

    private val nationOrFind = Link("tutorial.requires-town", { town(it) != null }) { player, back ->
        if (nation(player) != null) NationMenu(player, back) else NoNationMenu(player, back)
    }

    private fun Player.money(amount: Double, key: String): List<String> =
        if (TownyUtil.economy && amount > 0) listOf(tr(key, "cost" to TownyUtil.money(amount))) else emptyList()

    val GETTING_STARTED = Chapter(
        Material.COMPASS, "tutorial.start.title", "tutorial.start.description",
        always { player, _ -> MainMenu(player) },
        listOf(
            Lesson("start/towny", Material.WRITTEN_BOOK, "tutorial.start.towny", "tutorial.start.towny-body"),
            Lesson(
                "start/menus", Material.KNOWLEDGE_BOOK, "tutorial.start.menus", "tutorial.start.menus-body",
                always { player, _ -> MainMenu(player) }) { player ->
                listOf(
                    player.tr(
                        "tutorial.fact.shortcut",
                        "value" to TownyUtil.onOff(player, Main.instance.pluginConfig.sneakSwapHandShortcut)
                    )
                )
            },
            Lesson("start/buttons", Material.GRAY_DYE, "tutorial.start.buttons", "tutorial.start.buttons-body"),
            Lesson(
                "start/wilderness",
                Material.OAK_SAPLING,
                "tutorial.start.wilderness",
                "tutorial.start.wilderness-body"
            ) { player ->
                val world = TownyAPI.getInstance().getTownyWorld(player.world)
                if (world == null || !world.isUsingTowny) {
                    listOf(player.tr("tutorial.fact.world-no-towny"))
                } else {
                    listOf(
                        player.tr(
                            "tutorial.fact.wild-build",
                            "value" to TownyUtil.yesNo(
                                player,
                                world.getUnclaimedZonePerm(TownyPermission.ActionType.BUILD)
                            )
                        ),
                        player.tr(
                            "tutorial.fact.wild-destroy",
                            "value" to TownyUtil.yesNo(
                                player,
                                world.getUnclaimedZonePerm(TownyPermission.ActionType.DESTROY)
                            )
                        ),
                    )
                }
            },
            Lesson(
                "start/map", Material.FILLED_MAP, "tutorial.start.map", "tutorial.start.map-body",
                always { player, back -> MapMenu(player, back) }),
            Lesson(
                "start/invites", Material.PAPER, "tutorial.start.invites", "tutorial.start.invites-body",
                always { player, back -> InvitesMenu(player, back) }),
        ),
    )

    val FINDING_A_TOWN = Chapter(
        Material.OAK_DOOR, "tutorial.join.title", "tutorial.join.description", townOrFind,
        listOf(
            Lesson("join/why", Material.BELL, "tutorial.join.why", "tutorial.join.why-body"),
            Lesson(
                "join/browse", Material.OAK_DOOR, "tutorial.join.browse", "tutorial.join.browse-body",
                always { player, back -> TownListMenu(player, back) }),
            Lesson(
                "join/invites", Material.PAPER, "tutorial.join.invites", "tutorial.join.invites-body",
                always { player, back -> InvitesMenu(player, back) }),
            Lesson(
                "join/found", Material.GOLDEN_SHOVEL, "tutorial.join.found", "tutorial.join.found-body",
                Link("tutorial.requires-no-town", { town(it) == null }) { player, back ->
                    NoTownMenu(
                        player,
                        back
                    )
                }) { player ->
                buildList {
                    addAll(player.money(TownySettings.getNewTownPrice(), "tutorial.fact.town-cost"))
                    TownySettings.getMinDistanceFromTownHomeblocks().takeIf { it > 0 }?.let {
                        add(player.tr("tutorial.fact.town-distance", "distance" to it))
                    }
                    TownySettings.getMaxResidentsPerTown().takeIf { it > 0 }?.let {
                        add(player.tr("tutorial.fact.max-residents", "count" to it))
                    }
                }
            },
            Lesson(
                "join/leave", Material.IRON_DOOR, "tutorial.join.leave", "tutorial.join.leave-body",
                withTown { player, _, back -> TownMenu(player, back) }),
        ),
    )

    val TOWN = Chapter(
        Material.BELL, "tutorial.town.title", "tutorial.town.description",
        withTown { player, _, back -> TownMenu(player, back) },
        listOf(
            Lesson(
                "town/ranks", Material.GOLDEN_HELMET, "tutorial.town.ranks", "tutorial.town.ranks-body",
                withTown { player, town, back -> TownMembersMenu(player, town, back) }),
            Lesson(
                "town/grow", Material.PLAYER_HEAD, "tutorial.town.grow", "tutorial.town.grow-body",
                withTown { player, town, back -> TownMembersMenu(player, town, back) }) { player ->
                buildList {
                    TownySettings.getTownBlockRatio().takeIf { it > 0 }?.let {
                        add(player.tr("tutorial.fact.claims-per-resident", "count" to it))
                    }
                    town(player)?.let { add(player.tr("icon.town.residents", "count" to it.numResidents)) }
                }
            },
            Lesson(
                "town/spawn", Material.ENDER_PEARL, "tutorial.town.spawn", "tutorial.town.spawn-body",
                withTown { player, _, back -> TownSettingsMenu(player, back) }) { player ->
                town(player)?.let { player.money(it.spawnCost, "tutorial.fact.visitor-spawn-cost") }.orEmpty()
            },
            Lesson(
                "town/settings", Material.LEVER, "tutorial.town.settings", "tutorial.town.settings-body",
                withTown { player, _, back -> TownMenu(player, back).toggles() }) { player ->
                player.money(TownySettings.getTownNeutralityCost(), "tutorial.fact.peaceful-cost")
            },
            Lesson(
                "town/details", Material.WRITABLE_BOOK, "tutorial.town.details", "tutorial.town.details-body",
                withTown { player, _, back -> TownSettingsMenu(player, back) }),
            Lesson(
                "town/announce", Material.GOAT_HORN, "tutorial.town.announce", "tutorial.town.announce-body",
                withTown { player, town, back -> TownMembersMenu(player, town, back) }),
            Lesson(
                "town/merge", Material.STRUCTURE_VOID, "tutorial.town.merge", "tutorial.town.merge-body",
                always { player, back -> TownListMenu(player, back) }) { player ->
                buildList {
                    addAll(player.money(TownySettings.getBaseCostForTownMerge().toDouble(), "tutorial.fact.merge-cost"))
                    TownySettings.getMaxDistanceForTownMerge().takeIf { it > 0 }?.let {
                        add(player.tr("tutorial.fact.merge-distance", "distance" to it))
                    }
                }
            },
            Lesson(
                "town/sell", Material.EMERALD, "tutorial.town.sell", "tutorial.town.sell-body",
                withTown { player, _, back -> TownSettingsMenu(player, back) }, economy
            ),
            Lesson(
                "town/ruins", Material.CRACKED_STONE_BRICKS, "tutorial.town.ruins", "tutorial.town.ruins-body",
                townOrFind) { player ->
                buildList {
                    add(
                        player.tr(
                            "tutorial.fact.ruins",
                            "value" to TownyUtil.onOff(player, TownySettings.getTownRuinsEnabled())
                        )
                    )
                    if (TownySettings.getTownRuinsEnabled()) {
                        add(
                            player.tr(
                                "tutorial.fact.ruins-hours",
                                "hours" to TownySettings.getTownRuinsMaxDurationHours()
                            )
                        )
                        add(
                            player.tr(
                                "tutorial.fact.ruins-reclaim",
                                "value" to TownyUtil.yesNo(player, TownySettings.getTownRuinsReclaimEnabled())
                            )
                        )
                        if (TownySettings.getTownRuinsReclaimEnabled()) {
                            addAll(player.money(TownySettings.getEcoPriceReclaimTown(), "tutorial.fact.reclaim-cost"))
                            add(
                                player.tr(
                                    "tutorial.fact.reclaim-townless",
                                    "value" to TownyUtil.yesNo(player, TownySettings.canRuinsBeReclaimedByTownlessPlayers())
                                )
                            )
                        }
                    }
                }
            },
        ),
    )

    val CLAIMS = Chapter(
        Material.GRASS_BLOCK, "tutorial.claims.title", "tutorial.claims.description",
        withTown { player, _, back -> TownClaimsMenu(player, back) },
        listOf(
            Lesson(
                "claims/chunks", Material.GRASS_BLOCK, "tutorial.claims.chunks", "tutorial.claims.chunks-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }) { player ->
                buildList {
                    add(player.tr("tutorial.fact.chunk-size", "size" to TownySettings.getTownBlockSize()))
                    town(player)?.let {
                        add(
                            player.tr(
                                "icon.town.claims",
                                "claims" to it.numTownBlocks,
                                "max" to it.maxTownBlocksAsAString
                            )
                        )
                    }
                }
            },
            Lesson(
                "claims/claim", Material.WOODEN_SHOVEL, "tutorial.claims.claim", "tutorial.claims.claim-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }) { player ->
                buildList {
                    addAll(player.money(TownySettings.getClaimPrice(), "tutorial.fact.claim-cost"))
                    val increase = TownySettings.getClaimPriceIncreaseValue()
                    if (TownyUtil.economy && increase > 1) {
                        add(player.tr("tutorial.fact.claim-increase", "percent" to Math.round((increase - 1) * 100)))
                    }
                }
            },
            Lesson(
                "claims/auto", Material.LEATHER_BOOTS, "tutorial.claims.auto", "tutorial.claims.auto-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }),
            Lesson(
                "claims/unclaim", Material.COARSE_DIRT, "tutorial.claims.unclaim", "tutorial.claims.unclaim-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }) { player ->
                player.money(TownySettings.getClaimRefundPrice(), "tutorial.fact.claim-refund")
            },
            Lesson(
                "claims/outposts", Material.COMPASS, "tutorial.claims.outposts", "tutorial.claims.outposts-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }) { player ->
                if (!TownySettings.isAllowingOutposts()) {
                    listOf(player.tr("tutorial.fact.outposts-disabled"))
                } else {
                    buildList {
                        addAll(player.money(TownySettings.getOutpostCost(), "tutorial.fact.outpost-cost"))
                        town(player)?.let {
                            add(
                                player.tr(
                                    "claims.outposts",
                                    "outposts" to it.maxOutpostSpawn,
                                    "limit" to TownySettings.getMaxOutposts(it)
                                )
                            )
                        }
                    }
                }
            },
            Lesson(
                "claims/bonus", Material.GOLD_BLOCK, "tutorial.claims.bonus", "tutorial.claims.bonus-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }, economy
            ) { player ->
                player.money(TownySettings.getPurchasedBonusBlocksCost(), "tutorial.fact.bonus-cost")
            },
            Lesson(
                "claims/cede", Material.OAK_BOAT, "tutorial.claims.cede", "tutorial.claims.cede-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) }),
            Lesson(
                "claims/takeover", Material.IRON_SWORD, "tutorial.claims.takeover", "tutorial.claims.takeover-body",
                withTown { player, _, back -> TownClaimsMenu(player, back) },
                { TownySettings.isOverClaimingAllowingStolenLand() }
            ),
        ),
    )

    val PLOTS = Chapter(
        Material.OAK_SIGN, "tutorial.plots.title", "tutorial.plots.description",
        always { player, back -> PlotMenu(player, back) },
        listOf(
            Lesson(
                "plots/what", Material.GRASS_BLOCK, "tutorial.plots.what", "tutorial.plots.what-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/buy", Material.EMERALD, "tutorial.plots.buy", "tutorial.plots.buy-body",
                always { player, back -> MapMenu(player, back) }) { player ->
                listOfNotNull(resident(player)?.let { player.tr("profile.plots-owned", "count" to it.townBlocks.size) })
            },
            Lesson(
                "plots/sell", Material.GREEN_BANNER, "tutorial.plots.sell", "tutorial.plots.sell-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/area", Material.MAP, "tutorial.plots.area", "tutorial.plots.area-body",
                always { player, back -> PlotAreaMenu(player, back) }),
            Lesson(
                "plots/types", Material.OAK_SIGN, "tutorial.plots.types", "tutorial.plots.types-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/settings", Material.LEVER, "tutorial.plots.settings", "tutorial.plots.settings-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/trust", Material.TRIPWIRE_HOOK, "tutorial.plots.trust", "tutorial.plots.trust-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/join-days", Material.CLOCK, "tutorial.plots.join-days", "tutorial.plots.join-days-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "plots/groups", Material.CHEST, "tutorial.plots.groups", "tutorial.plots.groups-body",
                always { player, back -> PlotGroupMenu(player, back) }),
        ),
    )

    val PROTECTION = Chapter(
        Material.SHIELD, "tutorial.protection.title", "tutorial.protection.description",
        always { player, back -> ResidentMenu(player, back).permissions() },
        listOf(
            Lesson(
                "protection/actions",
                Material.IRON_PICKAXE,
                "tutorial.protection.actions",
                "tutorial.protection.actions-body"
            ),
            Lesson(
                "protection/groups",
                Material.PLAYER_HEAD,
                "tutorial.protection.groups",
                "tutorial.protection.groups-body"
            ),
            Lesson(
                "protection/town", Material.IRON_DOOR, "tutorial.protection.town", "tutorial.protection.town-body",
                withTown { player, _, back -> TownMenu(player, back).permissions() }),
            Lesson(
                "protection/personal",
                Material.OAK_DOOR,
                "tutorial.protection.personal",
                "tutorial.protection.personal-body",
                always { player, back -> ResidentMenu(player, back).permissions() }),
            Lesson(
                "protection/friends", Material.POPPY, "tutorial.protection.friends", "tutorial.protection.friends-body",
                always { player, back -> FriendsMenu(player, back) }),
            Lesson(
                "protection/trust",
                Material.TRIPWIRE_HOOK,
                "tutorial.protection.trust",
                "tutorial.protection.trust-body",
                withTown { player, town, back -> TownTrustMenu(player, town, back) }),
            Lesson(
                "protection/overrides",
                Material.WRITABLE_BOOK,
                "tutorial.protection.overrides",
                "tutorial.protection.overrides-body",
                always { player, back -> PlotMenu(player, back) }),
            Lesson(
                "protection/flags",
                Material.FLINT_AND_STEEL,
                "tutorial.protection.flags",
                "tutorial.protection.flags-body",
                always { player, back ->
                    if (town(player) != null) TownMenu(player, back).toggles() else ResidentMenu(
                        player,
                        back
                    ).toggles()
                }),
            Lesson(
                "protection/outlaws",
                Material.IRON_BARS,
                "tutorial.protection.outlaws",
                "tutorial.protection.outlaws-body",
                withTown { player, town, back -> OutlawsMenu(player, town, back) }),
            Lesson(
                "protection/jailing",
                Material.IRON_BARS,
                "tutorial.protection.jailing",
                "tutorial.protection.jailing-body",
                withTown { player, town, back -> TownJailMenu(player, town, back) }) { player ->
                buildList {
                    if (TownySettings.isAllowingBail()) {
                        addAll(player.money(TownySettings.getBailAmount(), "tutorial.fact.bail"))
                        addAll(player.money(TownySettings.getBailMaxAmount(), "tutorial.fact.max-bail"))
                    }
                    town(player)?.let { add(player.tr("town-jail.jails-count", "count" to it.jails.orEmpty().size)) }
                }
            },
            Lesson(
                "protection/jail", Material.IRON_CHAIN, "tutorial.protection.jail", "tutorial.protection.jail-body",
                always { player, back -> ResidentMenu(player, back) }) { player ->
                buildList {
                    TownySettings.getJailedMaxHours().takeIf { it > 0 }
                        ?.let { add(player.tr("tutorial.fact.jail-hours", "hours" to it)) }
                    resident(player)?.takeIf { it.isJailed }
                        ?.let { add(player.tr("tutorial.fact.jailed", "hours" to it.jailHours)) }
                }
            },
        ),
    )

    val NATIONS = Chapter(
        Material.BEACON, "tutorial.nations.title", "tutorial.nations.description", nationOrFind,
        listOf(
            Lesson(
                "nations/what", Material.BEACON, "tutorial.nations.what", "tutorial.nations.what-body",
                always { player, back -> NationListMenu(player, back) }),
            Lesson(
                "nations/join", Material.OAK_DOOR, "tutorial.nations.join", "tutorial.nations.join-body",
                always { player, back -> NationListMenu(player, back) }) { player ->
                TownySettings.getNumResidentsJoinNation().takeIf { it > 1 }
                    ?.let { listOf(player.tr("tutorial.fact.join-nation-residents", "count" to it)) }.orEmpty()
            },
            Lesson(
                "nations/found", Material.GOLDEN_HELMET, "tutorial.nations.found", "tutorial.nations.found-body",
                Link("tutorial.requires-town-no-nation", { town(it) != null && nation(it) == null }) { player, back ->
                    NoNationMenu(player, back)
                }) { player ->
                buildList {
                    addAll(player.money(TownySettings.getNewNationPrice(), "tutorial.fact.nation-cost"))
                    TownySettings.getNumResidentsCreateNation().takeIf { it > 1 }?.let {
                        add(player.tr("tutorial.fact.create-nation-residents", "count" to it))
                    }
                }
            },
            Lesson(
                "nations/towns", Material.BELL, "tutorial.nations.towns", "tutorial.nations.towns-body",
                withNation { player, nation, back -> NationTownsMenu(player, nation, back) }),
            Lesson(
                "nations/relations", Material.SHIELD, "tutorial.nations.relations", "tutorial.nations.relations-body",
                withNation { player, nation, back -> NationRelationsMenu(player, nation, back) }),
            Lesson(
                "nations/sanctions", Material.RED_BANNER, "tutorial.nations.sanctions", "tutorial.nations.sanctions-body",
                withNation { player, nation, back -> NationSanctionsMenu(player, nation, back) }),
            Lesson(
                "nations/settings", Material.LEVER, "tutorial.nations.settings", "tutorial.nations.settings-body",
                withNation { player, _, back -> NationMenu(player, back).toggles() }) { player ->
                player.money(TownySettings.getNationNeutralityCost(), "tutorial.fact.peaceful-cost")
            },
            Lesson(
                "nations/spawn", Material.ENDER_PEARL, "tutorial.nations.spawn", "tutorial.nations.spawn-body",
                withNation { player, _, back -> NationSettingsMenu(player, back) }) { player ->
                listOf(
                    player.tr(
                        "tutorial.fact.nation-spawn",
                        "value" to TownyUtil.onOff(player, TownySettings.isConfigAllowingNationSpawn())
                    )
                )
            },
        ),
    )

    val ECONOMY = Chapter(
        Material.GOLD_INGOT, "tutorial.economy.title", "tutorial.economy.description",
        withTown { player, town, back -> BankMenu(player, town, back) },
        listOf(
            Lesson("economy/day", Material.CLOCK, "tutorial.economy.day", "tutorial.economy.day-body") { player ->
                val offset = TownySettings.getNewDayTime()
                listOf(
                    player.tr(
                        "tutorial.fact.new-day",
                        "time" to "%02d:%02d".format(offset / 3600 % 24, offset / 60 % 60)
                    ),
                    player.tr(
                        "tutorial.fact.daily-taxes",
                        "value" to TownyUtil.onOff(player, TownySettings.isTaxingDaily())
                    ),
                )
            },
            Lesson(
                "economy/bank", Material.GOLD_INGOT, "tutorial.economy.bank", "tutorial.economy.bank-body",
                withTown { player, town, back -> BankMenu(player, town, back) }) { player ->
                listOfNotNull(town(player)?.let { player.tr("bank.balance", "balance" to TownyUtil.balance(it)) })
            },
            Lesson(
                "economy/resident-tax",
                Material.PAPER,
                "tutorial.economy.resident-tax",
                "tutorial.economy.resident-tax-body",
                withTown { player, _, back -> TownSettingsMenu(player, back) }) { player ->
                val resident = resident(player)
                val town = resident?.townOrNull
                if (resident == null || town == null) emptyList() else listOf(
                    player.tr(
                        "bank.daily-tax",
                        "tax" to if (town.isTaxPercentage) "${town.taxes}%" else TownyUtil.money(town.taxes)
                    ),
                    player.tr("profile.tax-owed", "tax" to TownyUtil.money(resident.getTaxOwing(true))),
                )
            },
            Lesson(
                "economy/plot-tax", Material.GOLD_NUGGET, "tutorial.economy.plot-tax", "tutorial.economy.plot-tax-body",
                withTown { player, _, back -> TownSettingsMenu(player, back) }) { player ->
                town(player)?.let { listOf(player.tr("town.plot-tax", "tax" to TownyUtil.money(it.plotTax))) }.orEmpty()
            },
            Lesson(
                "economy/upkeep",
                Material.HOPPER,
                "tutorial.economy.upkeep",
                "tutorial.economy.upkeep-body"
            ) { player ->
                val town = town(player)
                val nation = nation(player)
                buildList {
                    if (town != null && town.hasUpkeep()) {
                        addAll(player.money(TownySettings.getTownUpkeepCost(town), "tutorial.fact.town-upkeep"))
                    } else if (town == null) {
                        addAll(player.money(TownySettings.getTownUpkeep(), "tutorial.fact.town-upkeep"))
                    }
                    if (nation != null) addAll(
                        player.money(
                            TownySettings.getNationUpkeepCost(nation),
                            "tutorial.fact.nation-upkeep"
                        )
                    )
                }
            },
            Lesson(
                "economy/nation-tax",
                Material.BEACON,
                "tutorial.economy.nation-tax",
                "tutorial.economy.nation-tax-body",
                withNation { player, nation, back -> BankMenu(player, nation, back) }) { player ->
                nation(player)?.let {
                    listOf(
                        player.tr(
                            "nation.daily-tax",
                            "tax" to if (it.isTaxPercentage) "${it.taxes}%" else TownyUtil.money(it.taxes)
                        )
                    )
                }.orEmpty()
            },
            Lesson(
                "economy/bankruptcy",
                Material.BARRIER,
                "tutorial.economy.bankruptcy",
                "tutorial.economy.bankruptcy-body"
            ) { player ->
                buildList {
                    add(
                        player.tr(
                            "tutorial.fact.bankruptcy",
                            "value" to TownyUtil.onOff(player, TownySettings.isTownBankruptcyEnabled())
                        )
                    )
                    town(player)?.takeIf { it.isBankrupt }
                        ?.let { add(player.tr("tutorial.fact.debt", "debt" to TownyUtil.money(it.debtBalance))) }
                }
            },
        ),
        economy,
    )

    val PROFILE = Chapter(
        Material.PLAYER_HEAD, "tutorial.profile.title", "tutorial.profile.description",
        always { player, back -> ResidentMenu(player, back) },
        listOf(
            Lesson(
                "profile/menu", Material.PLAYER_HEAD, "tutorial.profile.menu", "tutorial.profile.menu-body",
                always { player, back -> ResidentMenu(player, back) }),
            Lesson(
                "profile/others", Material.SPYGLASS, "tutorial.profile.others", "tutorial.profile.others-body",
                always { player, back ->
                    town(player)?.let { TownMembersMenu(player, it, back) } ?: TownListMenu(
                        player,
                        back
                    )
                }),
            Lesson(
                "profile/display", Material.GLOWSTONE_DUST, "tutorial.profile.display", "tutorial.profile.display-body",
                always { player, back -> ResidentMenu(player, back).toggles() }),
            Lesson(
                "profile/spawn", Material.RED_BED, "tutorial.profile.spawn", "tutorial.profile.spawn-body",
                always { player, back -> ResidentMenu(player, back) }),
        ),
    )

    val chapters = listOf(GETTING_STARTED, FINDING_A_TOWN, TOWN, CLAIMS, PLOTS, PROTECTION, NATIONS, ECONOMY, PROFILE)
}
