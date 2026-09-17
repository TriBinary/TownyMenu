package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MapMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Claiming, unclaiming, outposts, and bonus claim purchases for the viewer's town. */
class TownClaimsMenu(player: Player, back: Menu) : Menu(player, "Town Claims", 5, back) {

    private val town: Town?
        get() = resident?.townOrNull

    override fun build() {
        val town = town ?: return backButton(40)

        button(4, Icons.icon(Material.GRASS_BLOCK, "<green>Claims", null, *buildList {
            add("<gray>Claimed: <white>${town.numTownBlocks}/${town.maxTownBlocksAsAString}")
            add("<gray>Available: <white>${if (town.hasUnlimitedClaims()) "Unlimited" else town.availableTownBlocks()}")
            add("<gray>Bonus claims: <white>${town.bonusBlocks}  <gray>Purchased: <white>${town.purchasedBlocks}")
            add("<gray>Outposts: <white>${town.maxOutpostSpawn}/${town.outpostLimit}")
            if (TownyUtil.economy) add("<gray>Claim price: <white>${TownyUtil.money(town.townBlockCost)}")
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33)
        val claims = { town.numTownBlocks }

        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
            Icons.icon(Material.GRASS_BLOCK, "<green>Claim This Chunk", "Claim the chunk you are standing in. It must touch your town.")) {
            run("towny:town claim", claims, delayTicks = CLAIM_DELAY)
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN_MULTIPLE,
            Icons.icon(Material.MOSS_BLOCK, "<green>Claim Area", "Claim a square of chunks around you.")) {
            prompt("Claim Area", "Radius in chunks", "<gray>A radius of 1 claims a 3×3 square.", initial = "1", maxLength = 3) { radius ->
                run("towny:town claim rect ${TownyUtil.argument(radius)}", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_FILL,
            Icons.icon(Material.BONE_MEAL, "<green>Fill Gaps", "Claim wilderness fully enclosed by your town around you.")) {
            run("towny:town claim fill", claims, delayTicks = CLAIM_DELAY)
        }
        if (TownySettings.isAllowingOutposts()) {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_OUTPOST,
                Icons.icon(Material.COMPASS, "<light_purple>Claim Outpost", "Claim this chunk as an outpost away from your town.")) {
                run("towny:town claim outpost", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
            Icons.icon(Material.COARSE_DIRT, "<red>Unclaim This Chunk", "Return the chunk you are standing in to the wilderness.")) {
            run("towny:town unclaim", claims, delayTicks = CLAIM_DELAY)
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM,
            Icons.icon(Material.DIRT, "<red>Unclaim Area", "Unclaim a square of chunks around you.")) {
            prompt("Unclaim Area", "Radius in chunks", initial = "1", maxLength = 3) { radius ->
                run("towny:town unclaim rect ${TownyUtil.argument(radius)}", claims, delayTicks = CLAIM_DELAY)
            }
        }
        grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_UNCLAIM_ALL,
            Icons.icon(Material.LAVA_BUCKET, "<dark_red>Unclaim Everything", "Unclaim all town land except the home block.")) {
            run("towny:town unclaim all", claims, delayTicks = CLAIM_DELAY)
        }
        if (TownyUtil.economy && TownySettings.getMaxPurchasedBlocks(town) > 0) {
            grid.add(PermissionNodes.TOWNY_COMMAND_TOWN_BUY_BONUS,
                Icons.icon(Material.EMERALD_BLOCK, "<gold>Buy Bonus Claims", "Buy extra claim slots.", "<gray>Price each: <white>${TownyUtil.money(town.bonusBlockCost)}")) {
                prompt("Buy Bonus Claims", "Number of claims", initial = "1", maxLength = 4) { amount ->
                    run("towny:town buy bonus ${TownyUtil.argument(amount)}", { town.purchasedBlocks })
                }
            }
        }
        val autoClaim = resident?.hasMode("townclaim") == true
        grid.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_TOGGLE_TOWNCLAIM,
            Icons.toggle(Material.LEATHER_BOOTS, "<green>Auto Claim", autoClaim, "Claim every chunk you walk into.")) {
            run("towny:resident toggle townclaim", { resident?.hasMode("townclaim") })
        }
        grid.add(Icons.icon(Material.FILLED_MAP, "<aqua>Map", "See the land around you.")) {
            MapMenu(player, this).open()
        }

        backButton(40)
    }

    private companion object {
        const val CLAIM_DELAY = 20L
    }
}
