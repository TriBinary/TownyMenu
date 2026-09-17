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
    Menu(player, TownyUtil.name(target.name), 6, back) {

    override fun build() {
        button(4, Icons.resident(player, target, *buildList {
            add(tr("profile.registered", "date" to TownyUtil.date(target.registered)))
            target.nationOrNull?.let { add(tr("icon.town.nation", "nation" to TownyUtil.name(it.name))) }
            TownyUtil.about(target)?.let { add(tr("profile.about-line", "about" to TownyUtil.text(it))) }
        }.toTypedArray()))

        val viewer = resident ?: return backButton(49)
        val self = viewer == target
        val town = viewer.townOrNull
        val sameTown = town != null && town == target.townOrNull
        val nation = viewer.nationOrNull
        val sameNation = nation != null && nation == target.nationOrNull
        val actions = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

        if (!self) {
            val friend = viewer.hasFriend(target)
            actions.add(
                PermissionNodes.TOWNY_COMMAND_RESIDENT_FRIEND, Icons.icon(
                    if (friend) Material.WITHER_ROSE else Material.POPPY,
                    tr(if (friend) "resident-profile.remove-friend" else "resident-profile.add-friend"),
                    tr("profile.friends-description"),
                )
            ) {
                run(
                    "towny:resident friend ${if (friend) "remove" else "add"} ${target.name}",
                    { viewer.hasFriend(target) })
            }
        }

        if (sameTown) {
            actions.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_RANK,
                Icons.icon(
                    Material.NAME_TAG,
                    tr("resident-profile.town-ranks"),
                    tr("resident-profile.town-ranks-description")
                )
            ) {
                RankMenu.create(player, target, false, this).open()
            }
            actions.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_SET_TITLE,
                Icons.icon(
                    Material.OAK_HANGING_SIGN,
                    tr("resident-profile.title"),
                    tr("resident-profile.title-description")
                )
            ) { click ->
                editTitle(
                    "title",
                    click.isRightClick,
                    tr("resident-profile.title-prompt"),
                    tr("resident-profile.title-label")
                ) { target.title }
            }
            actions.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_SET_SURNAME,
                Icons.icon(
                    Material.OAK_SIGN,
                    tr("resident-profile.surname"),
                    tr("resident-profile.surname-description")
                )
            ) { click ->
                editTitle(
                    "surname",
                    click.isRightClick,
                    tr("resident-profile.surname-prompt"),
                    tr("resident-profile.surname-label")
                ) { target.surname }
            }
            if (!self && !target.isMayor) {
                actions.add(
                    PermissionNodes.TOWNY_COMMAND_TOWN_SET_MAYOR,
                    Icons.icon(
                        Material.GOLDEN_HELMET,
                        tr("resident-profile.make-mayor"),
                        tr("resident-profile.make-mayor-description")
                    )
                ) {
                    run("towny:town set mayor ${target.name}", { town.mayor })
                }
                actions.add(
                    PermissionNodes.TOWNY_COMMAND_TOWN_KICK,
                    Icons.icon(
                        Material.IRON_BOOTS,
                        tr("resident-profile.kick"),
                        tr("resident-profile.kick-description")
                    )
                ) {
                    run(
                        "towny:town kick ${target.name}",
                        { town.hasResident(target) },
                        returnTo = back ?: MainMenu(player)
                    )
                }
            }
        }

        if (town != null && !self) {
            val trusted = town.hasTrustedResident(target)
            actions.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_TRUST, Icons.icon(
                    Material.TRIPWIRE_HOOK,
                    tr(if (trusted) "resident-profile.untrust" else "resident-profile.trust"),
                    tr("resident-profile.trust-description"),
                )
            ) {
                run(
                    "towny:town trust ${if (trusted) "remove" else "add"} ${target.name}",
                    { town.hasTrustedResident(target) })
            }
        }

        if (town != null && !sameTown && !self) {
            val outlaw = town.hasOutlaw(target)
            actions.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_OUTLAW, Icons.icon(
                    Material.IRON_BARS,
                    tr(if (outlaw) "resident-profile.pardon" else "resident-profile.outlaw"),
                    tr("resident-profile.outlaw-description"),
                )
            ) { run("towny:town outlaw ${if (outlaw) "remove" else "add"} ${target.name}", { town.hasOutlaw(target) }) }

            if (!target.hasTown()) {
                actions.add(
                    PermissionNodes.TOWNY_COMMAND_TOWN_INVITE_ADD,
                    Icons.icon(Material.PAPER, tr("resident-profile.invite"), tr("resident-profile.invite-description"))
                ) {
                    run("towny:town add ${target.name}", { town.sentInvites.size })
                }
            }
        }

        if (sameNation) {
            actions.add(
                PermissionNodes.TOWNY_COMMAND_NATION_RANK,
                Icons.icon(
                    Material.NAME_TAG,
                    tr("resident-profile.nation-ranks"),
                    tr("resident-profile.nation-ranks-description")
                )
            ) {
                RankMenu.create(player, target, true, this).open()
            }
            if (!self && !target.isKing && target.townOrNull == nation.capital) {
                actions.add(
                    PermissionNodes.TOWNY_COMMAND_NATION_SET_KING,
                    Icons.icon(
                        Material.GOLDEN_HELMET,
                        tr("resident-profile.make-leader"),
                        tr("resident-profile.make-leader-description")
                    )
                ) {
                    run("towny:nation set king ${target.name}", { nation.king })
                }
            }
        }

        backButton(49)
    }

    private fun editTitle(field: String, clear: Boolean, title: String, label: String, current: () -> String) {
        if (clear) {
            run("towny:town set $field ${target.name}", current)
        } else {
            prompt(title, label, initial = current()) { text ->
                run("towny:town set $field ${target.name} $text", current)
            }
        }
    }
}
