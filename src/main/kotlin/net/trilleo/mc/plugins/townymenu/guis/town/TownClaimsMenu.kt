package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.tutorial.Tutorial
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Claiming, unclaiming, outposts, and bonus claim purchases for the viewer's town. */
class TownClaimsMenu(player: Player, back: Menu) : Menu(player, player.tr("claims.title"), 6, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town ?: return backButton(49)

        button(4, Icons.icon(Material.GRASS_BLOCK, tr("claims.info"), null, *buildList {
            add(tr("claims.claimed", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString))
            add(
                tr(
                    "claims.available",
                    "available" to if (town.hasUnlimitedClaims()) tr("claims.unlimited") else town.availableTownBlocks()
                )
            )
            add(tr("claims.bonus", "bonus" to town.bonusBlocks, "purchased" to town.purchasedBlocks))
            add(tr("claims.outposts", "outposts" to town.maxOutpostSpawn, "limit" to town.outpostLimit))
            if (TownyUtil.economy) add(tr("claims.price", "price" to TownyUtil.money(town.townBlockCost)))
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33)
        val claims = { town.numTownBlocks }

        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
            Icons.icon(Material.GRASS_BLOCK, tr("claims.claim"), tr("claims.claim-description"))
        ) {
            run("towny:town claim", claims, delayTicks = CLAIM_DELAY)
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN_MULTIPLE,
            Icons.icon(Material.MOSS_BLOCK, tr("claims.claim-area"), tr("claims.claim-area-description"))
        ) {
            prompt(
                tr("claims.claim-area-title"),
                tr("claims.radius"),
                tr("claims.radius-hint"),
                initial = "1",
                maxLength = 3
            ) { radius ->
                run("towny:town claim rect ${TownyUtil.argument(radius)}", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_FILL,
            Icons.icon(Material.BONE_MEAL, tr("claims.fill"), tr("claims.fill-description"))
        ) {
            run("towny:town claim fill", claims, delayTicks = CLAIM_DELAY)
        }
        if (TownySettings.isAllowingOutposts()) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                Icons.icon(Material.COMPASS, tr("claims.outpost"), tr("claims.outpost-description"))
            ) {
                run("towny:town claim outpost", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
            Icons.icon(Material.COARSE_DIRT, tr("claims.unclaim"), tr("claims.unclaim-description"))
        ) {
            run("towny:town unclaim", claims, delayTicks = CLAIM_DELAY)
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
            Icons.icon(Material.DIRT, tr("claims.unclaim-area"), tr("claims.unclaim-area-description"))
        ) {
            prompt(tr("claims.unclaim-area-title"), tr("claims.radius"), initial = "1", maxLength = 3) { radius ->
                run("towny:town unclaim rect ${TownyUtil.argument(radius)}", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM_ALL,
            Icons.icon(Material.LAVA_BUCKET, tr("claims.unclaim-all"), tr("claims.unclaim-all-description"))
        ) {
            run("towny:town unclaim all", claims, delayTicks = CLAIM_DELAY)
        }
        if (TownyUtil.economy && TownySettings.getMaxPurchasedBlocks(town) > 0) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_BUY_BONUS,
                Icons.icon(
                    Material.EMERALD_BLOCK, tr("claims.buy-bonus"), tr("claims.buy-bonus-description"),
                    tr("claims.price-each", "price" to TownyUtil.money(town.bonusBlockCost))
                )
            ) {
                prompt(
                    tr("claims.buy-bonus-title"),
                    tr("claims.bonus-amount"),
                    initial = "1",
                    maxLength = 4
                ) { amount ->
                    run("towny:town buy bonus ${TownyUtil.argument(amount)}", { town.purchasedBlocks })
                }
            }
        }
        val autoClaim = resident?.hasMode("townclaim") == true
        grid.add(
            PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNCLAIM,
            Icons.toggle(
                player,
                Material.LEATHER_BOOTS,
                tr("claims.auto-claim"),
                autoClaim,
                tr("claims.auto-claim-description")
            )
        ) {
            run("towny:resident toggle townclaim", { resident?.hasMode("townclaim") })
        }
        grid.add(Icons.icon(Material.FILLED_MAP, tr("main.map"), tr("common.map-description"))) {
            MapMenu(player, this).open()
        }

        tutorialButton(53, Tutorial.CLAIMS)
        backButton(49)
    }

    private companion object {
        const val CLAIM_DELAY = 20L
    }
}
