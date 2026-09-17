package net.trilleo.mc.plugins.townymenu.utils

import com.palmergames.bukkit.towny.TownyEconomyHandler
import com.palmergames.bukkit.towny.TownyUniverse
import com.palmergames.bukkit.towny.`object`.Government
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formatting and permission helpers shared by every Towny menu.
 *
 * Anything that comes from Towny data (names, boards, titles) is player-written,
 * so it must pass through [text] before being embedded in a MiniMessage string.
 */
object TownyUtil {

    private val miniMessage = MiniMessage.miniMessage()
    private val legacyCodes = Regex("[&§][0-9a-fk-orx]", RegexOption.IGNORE_CASE)
    private val whitespace = Regex("\\s+")
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

    /** Strips legacy colour codes and escapes MiniMessage tags in player-written [value]. */
    fun text(value: String): String = miniMessage.escapeTags(value.replace(legacyCodes, ""))

    /** A Towny object name (underscores shown as spaces), safe for MiniMessage. */
    fun name(name: String): String = text(name.replace('_', ' '))

    /** Formats [amount] with the server economy's currency, or `-` when no economy is active. */
    fun money(amount: Double): String =
        if (TownyEconomyHandler.isActive()) text(TownyEconomyHandler.getFormattedBalance(amount)) else "-"

    /** Formats the balance of [government]'s bank account. */
    fun balance(government: Government): String = money(government.account.cachedBalance)

    /** Formats an epoch-millisecond timestamp as a date, or `-` when unset. */
    fun date(epochMillis: Long): String =
        if (epochMillis <= 0) "-" else dateFormat.format(Instant.ofEpochMilli(epochMillis))

    /** A coloured On/Off label. */
    fun onOff(value: Boolean): String = if (value) "<green>On" else "<red>Off"

    /** `true` when the economy is enabled, so bank and price options make sense. */
    val economy: Boolean
        get() = TownyEconomyHandler.isActive()

    /** Tests [node] the same way Towny's own commands do (ranks, wildcards, admin). */
    fun can(player: Player, node: PermissionNodes): Boolean = can(player, node.node)

    /** Tests a raw permission [node] (e.g. a per-rank node) the same way Towny does. */
    fun can(player: Player, node: String): Boolean =
        TownyUniverse.getInstance().permissionSource.testPermission(player, node)

    /** The first word of dialog input, for commands that take a single value. */
    fun argument(input: String): String = input.trim().substringBefore(' ')

    /** Dialog input as a Towny name: runs of whitespace become underscores. */
    fun nameArgument(input: String): String = input.trim().replace(whitespace, "_")

    /** Online players (other than [player]) that have a Towny resident record. */
    fun otherOnlineResidents(player: Player): List<Resident> =
        player.server.onlinePlayers
            .filter { it != player && player.canSee(it) }
            .mapNotNull { TownyUniverse.getInstance().getResident(it.uniqueId) }
            .sortedBy { it.name.lowercase() }
}
