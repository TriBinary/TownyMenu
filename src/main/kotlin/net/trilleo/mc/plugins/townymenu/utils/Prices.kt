package net.trilleo.mc.plugins.townymenu.utils

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownBlockTypeHandler
import com.palmergames.bukkit.towny.`object`.spawnlevel.NationSpawnLevel
import com.palmergames.bukkit.towny.`object`.spawnlevel.TownSpawnLevel
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import org.bukkit.entity.Player

/**
 * What this server charges for the actions the menus run, so a button can show its price before
 * the player commits to it.
 *
 * Amounts come from Towny's config and from the town or nation involved, never from a fixed
 * default, so they match what the command would actually take. Everything returns `0` without an
 * economy; menus turn an amount into lore with [net.trilleo.mc.plugins.townymenu.guis.framework.Menu.costLine].
 */
object Prices {

    /** What claiming [count] chunks costs [town]. */
    fun claim(town: Town, count: Int = 1): Double =
        if (count <= 1) town.townBlockCost else runCatching { town.getTownBlockCostN(count) }.getOrDefault(0.0)

    /** What claiming an outpost costs, whatever the town already owns. */
    fun outpost(): Double = TownySettings.getOutpostCost()

    /** What a town gets back per unclaimed chunk. Negative when the server charges for unclaiming instead. */
    fun unclaimRefund(): Double = TownySettings.getClaimRefundPrice()

    /** What setting a plot to [type] costs, for [count] plots at once. Towny's `reset` is the default type. */
    fun plotType(type: String, count: Int = 1): Double {
        val name = if (type.equals("reset", ignoreCase = true)) "default" else type
        return (TownBlockTypeHandler.getType(name)?.cost ?: 0.0) * count
    }

    /** What merging [succumbing] into [remaining] costs the remaining town, before any debt it takes on. */
    fun merge(remaining: Town, succumbing: Town): Double {
        val plots = runCatching { remaining.getTownBlockCostN(succumbing.numTownBlocks) }.getOrDefault(0.0)
        return TownySettings.getBaseCostForTownMerge() + plots * TownySettings.getPercentageCostPerPlot() * 0.01
    }

    /**
     * What travelling to [town]'s spawn — or to one of its outposts — costs [player].
     *
     * Towny prices a spawn by the traveller's relationship to the town and caps it at the server's
     * own price for that relationship, so a visitor and a resident see different numbers.
     */
    fun townSpawn(player: Player, town: Town, outpost: Boolean = false): Double {
        if (!TownyUtil.economy || spawnsFree(player)) return 0.0
        val level = townSpawnLevel(TownyAPI.getInstance().getResident(player), town, outpost)
        if (level == TownSpawnLevel.UNAFFILIATED && !TownySettings.isPublicSpawnCostAffectedByTownSpawncost()) {
            return TownySettings.getSpawnTravelCost()
        }
        return minOf(level.getCost(town), level.getCost())
    }

    /** What travelling to [nation]'s spawn costs [player]. See [townSpawn]. */
    fun nationSpawn(player: Player, nation: Nation): Double {
        if (!TownyUtil.economy || spawnsFree(player)) return 0.0
        val level = nationSpawnLevel(TownyAPI.getInstance().getResident(player), nation)
        if (level == NationSpawnLevel.UNAFFILIATED && !TownySettings.isPublicSpawnCostAffectedByTownSpawncost()) {
            return TownySettings.getSpawnTravelCost()
        }
        return minOf(level.getCost(nation), level.getCost())
    }

    /** What `/resident spawn` costs [player], which Towny prices as a trip home to their own town. */
    fun residentSpawn(player: Player): Double =
        TownyAPI.getInstance().getResident(player)?.townOrNull?.let { townSpawn(player, it) } ?: 0.0

    private fun spawnsFree(player: Player): Boolean =
        TownyUtil.can(player, PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_SPAWN_FREECHARGE) ||
                TownyUtil.can(player, PermissionNodes.TOWNY_SPAWN_ADMIN_NOCHARGE)

    private fun townSpawnLevel(resident: Resident?, town: Town, outpost: Boolean): TownSpawnLevel {
        if (resident == null) return TownSpawnLevel.UNAFFILIATED
        if (TownySettings.trustedResidentsGetToSpawnToTown() &&
            (town.hasTrustedResident(resident) || resident.townOrNull?.let { town.hasTrustedTown(it) } == true)
        ) return TownSpawnLevel.TOWN_RESIDENT
        val own = resident.townOrNull ?: return TownSpawnLevel.UNAFFILIATED
        if (own == town) return if (outpost) TownSpawnLevel.TOWN_RESIDENT_OUTPOST else TownSpawnLevel.TOWN_RESIDENT
        val ownNation = resident.nationOrNull ?: return TownSpawnLevel.UNAFFILIATED
        val townNation = town.nationOrNull ?: return TownSpawnLevel.UNAFFILIATED
        return when {
            ownNation == townNation -> TownSpawnLevel.PART_OF_NATION
            // Towny only lets an enemy in when the town is peaceful, and then charges them as a resident.
            townNation.hasEnemy(ownNation) -> TownSpawnLevel.TOWN_RESIDENT
            townNation.hasAlly(ownNation) -> TownSpawnLevel.NATION_ALLY
            else -> TownSpawnLevel.UNAFFILIATED
        }
    }

    private fun nationSpawnLevel(resident: Resident?, nation: Nation): NationSpawnLevel {
        val ownNation = resident?.nationOrNull ?: return NationSpawnLevel.UNAFFILIATED
        return when {
            ownNation == nation -> NationSpawnLevel.PART_OF_NATION
            nation.hasAlly(ownNation) -> NationSpawnLevel.NATION_ALLY
            else -> NationSpawnLevel.UNAFFILIATED
        }
    }
}
