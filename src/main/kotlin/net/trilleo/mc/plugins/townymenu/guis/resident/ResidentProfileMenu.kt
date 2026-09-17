package net.trilleo.mc.plugins.townymenu.guis.resident

import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.common.RankMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Another resident's profile. Shows the actions the viewer may take on them:
 * friendship, and — for town or nation staff — ranks, titles, trust, kicking,
 * outlawing, and inviting.
 */
class ResidentProfileMenu(player: Player, private val target: Resident, back: Menu) :
    Menu(player, TownyUtil.name(target.name), 5, back) {

    override fun build() {
        button(4, Icons.resident(target, *buildList {
            add("<gray>Registered: <white>${TownyUtil.date(target.registered)}")
            target.nationOrNull?.let { add("<gray>Nation: <white>${TownyUtil.name(it.name)}") }
            if (target.about.isNotBlank()) add("<gray>About: <white><i>${TownyUtil.text(target.about)}")
        }.toTypedArray()))

        val viewer = resident ?: return backButton(40)
        val self = viewer == target
        val town = viewer.townOrNull
        val sameTown = town != null && town == target.townOrNull
        val nation = viewer.nationOrNull
        val sameNation = nation != null && nation == target.nationOrNull
        val actions = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        if (!self) {
            val friend = viewer.hasFriend(target)
            actions.add(PermissionNodes.TOWNY_COMMAND_RESIDENT_FRIEND, Icons.icon(
                if (friend) Material.WITHER_ROSE else Material.POPPY,
                if (friend) "<red>Remove Friend" else "<light_purple>Add Friend",
                "Friends count as residents in your personal plot permissions.",
            )) { run("towny:resident friend ${if (friend) "remove" else "add"} ${target.name}", { viewer.hasFriend(target) }) }
        }

        if (sameTown) {
            actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_RANK,
                Icons.icon(Material.NAME_TAG, "<gold>Town Ranks", "Assign ranks such as assistant or sheriff.")) {
                RankMenu.create(player, target, false, this).open()
            }
            actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_TITLE,
                Icons.icon(Material.OAK_HANGING_SIGN, "<yellow>Set Title", "Left-click to set the title shown before their name. Right-click to clear it.")) { click ->
                editTitle("title", click.isRightClick) { target.title }
            }
            actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_SURNAME,
                Icons.icon(Material.OAK_SIGN, "<yellow>Set Surname", "Left-click to set the surname shown after their name. Right-click to clear it.")) { click ->
                editTitle("surname", click.isRightClick) { target.surname }
            }
            if (!self && !target.isMayor) {
                actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_SET_MAYOR,
                    Icons.icon(Material.GOLDEN_HELMET, "<gold>Make Mayor", "Hand over leadership of your town.")) {
                    run("towny:town set mayor ${target.name}", { town.mayor })
                }
                actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_KICK,
                    Icons.icon(Material.IRON_BOOTS, "<red>Kick from Town", "Remove this resident from your town.")) {
                    run("towny:town kick ${target.name}", { town.hasResident(target) }, returnTo = back ?: MainMenu(player))
                }
            }
        }

        if (town != null && !self) {
            val trusted = town.hasTrustedResident(target)
            actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_TRUST, Icons.icon(
                Material.TRIPWIRE_HOOK,
                if (trusted) "<red>Untrust" else "<green>Trust in Town",
                "Trusted residents may build everywhere in your town.",
            )) { run("towny:town trust ${if (trusted) "remove" else "add"} ${target.name}", { town.hasTrustedResident(target) }) }
        }

        if (town != null && !sameTown && !self) {
            val outlaw = town.hasOutlaw(target)
            actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW, Icons.icon(
                Material.IRON_BARS,
                if (outlaw) "<green>Pardon Outlaw" else "<red>Declare Outlaw",
                "Outlaws can be kept out of or teleported away from your town.",
            )) { run("towny:town outlaw ${if (outlaw) "remove" else "add"} ${target.name}", { town.hasOutlaw(target) }) }

            if (!target.hasTown()) {
                actions.add(PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
                    Icons.icon(Material.PAPER, "<green>Invite to Town", "Send an invitation to join your town.")) {
                    run("towny:town add ${target.name}", { town.sentInvites.size })
                }
            }
        }

        if (sameNation) {
            actions.add(PermissionNodes.TOWNY_COMMAND_NATION_RANK,
                Icons.icon(Material.NAME_TAG, "<aqua>Nation Ranks", "Assign nation ranks such as assistant.")) {
                RankMenu.create(player, target, true, this).open()
            }
            if (!self && !target.isKing && target.townOrNull == nation.capital) {
                actions.add(PermissionNodes.TOWNY_COMMAND_NATION_SET_KING,
                    Icons.icon(Material.GOLDEN_HELMET, "<aqua>Make Nation Leader", "Hand over leadership of your nation.")) {
                    run("towny:nation set king ${target.name}", { nation.king })
                }
            }
        }

        backButton(40)
    }

    private fun editTitle(field: String, clear: Boolean, current: () -> String) {
        if (clear) {
            run("towny:town set $field ${target.name}", current)
        } else {
            prompt("Set ${field.replaceFirstChar { it.uppercase() }}", field.replaceFirstChar { it.uppercase() }, initial = current()) { text ->
                run("towny:town set $field ${target.name} $text", current)
            }
        }
    }
}
