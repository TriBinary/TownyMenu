package net.trilleo.mc.plugins.townymenu.guis.admin

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.common.Pickers
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** Manages any resident as an admin (`/townyadmin resident`, `/townyadmin set`). */
class AdminResidentMenu(player: Player, private val target: Resident, back: Menu) :
    Menu(player, player.tr("admin-resident.title", "name" to TownyUtil.name(target.name)), 6, back) {

    private val exists: Boolean
        get() = TownyAPI.getInstance().getResident(target.uuid) != null

    override fun build() {
        if (!exists) {
            button(22, Icons.icon(Material.BARRIER, tr("admin.gone")))
            return backButton(49)
        }

        button(4, Icons.resident(player, target, *buildList {
            add(tr("profile.registered", "date" to TownyUtil.date(target.registered)))
            if (target.isJailed) add(tr("admin-resident.jailed"))
            if (target.isNPC) add(tr("admin-resident.npc-line"))
        }.toTypedArray()))

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        val town = target.townOrNull

        if (town != null) {
            grid.add(Icons.town(player, town, actions = listOf(tr("click.view")))) {
                AdminTownMenu(player, town, this).open()
            }
            if (!target.isMayor) {
                grid.add(
                    PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_KICK,
                    Icons.icon(
                        Material.IRON_BOOTS, tr("admin-resident.kick"), tr("admin-resident.kick-description"),
                        actions = hints("click.kick")
                    )
                ) {
                    run("towny:townyadmin town ${town.name} kick ${target.name}", { target.hasTown() })
                }
            }
        } else {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOWN_ADD,
                Icons.icon(
                    Material.BELL,
                    tr("admin-resident.add-to-town"),
                    tr("admin-resident.add-to-town-description"),
                    actions = hints("click.choose-town")
                )
            ) {
                Pickers.town(this, tr("admin-resident.add-to-town"), { true }) { picked ->
                    run("towny:townyadmin town ${picked.name} add ${target.name}", { target.hasTown() })
                }.open()
            }
        }

        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RESIDENT_RENAME,
            Icons.icon(
                Material.NAME_TAG, tr("admin.rename"), tr("admin-resident.rename-description"),
                tr("common.current", "value" to TownyUtil.name(target.name)),
                actions = hints("click.rename")
            )
        ) {
            prompt(tr("admin.rename"), tr("common.new-name"), initial = target.name, maxLength = 16) { name ->
                run("towny:townyadmin resident ${target.name} rename ${TownyUtil.argument(name)}", { target.name })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_TITLE,
            Icons.icon(
                Material.OAK_HANGING_SIGN, tr("resident-profile.title"), tr("resident-profile.title-description"),
                tr("common.current", "value" to TownyUtil.text(target.title)),
                actions = hints("click.edit", "click.clear")
            )
        ) { click ->
            editTitle(
                "title",
                click.isRightClick,
                tr("resident-profile.title-prompt"),
                tr("resident-profile.title-label")
            ) { target.title }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_SET_SURNAME,
            Icons.icon(
                Material.OAK_SIGN, tr("resident-profile.surname"), tr("resident-profile.surname-description"),
                tr("common.current", "value" to TownyUtil.text(target.surname)),
                actions = hints("click.edit", "click.clear")
            )
        ) { click ->
            editTitle(
                "surname",
                click.isRightClick,
                tr("resident-profile.surname-prompt"),
                tr("resident-profile.surname-label")
            ) { target.surname }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_TOGGLE_NPC,
            Icons.toggle(
                player,
                Material.ARMOR_STAND,
                tr("admin-resident.npc"),
                target.isNPC,
                tr("admin-resident.npc-description")
            )
        ) {
            run("towny:townyadmin toggle npc ${target.name}", { target.isNPC })
        }
        if (target.isJailed) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RESIDENT_UNJAIL,
                Icons.icon(
                    Material.IRON_BARS, tr("admin-resident.unjail"), tr("admin-resident.unjail-description"),
                    actions = hints("click.release")
                )
            ) {
                run("towny:townyadmin resident ${target.name} unjail", { target.isJailed })
            }
        }
        grid.add(
            PermissionNodes.TOWNY_COMMAND_TOWNYADMIN_RESIDENT_DELETE,
            Icons.icon(
                Material.TNT, tr("admin-resident.delete"), tr("admin-resident.delete-description"),
                actions = hints("click.delete")
            )
        ) {
            run("towny:townyadmin resident ${target.name} delete", ::exists, returnTo = back ?: this)
        }

        backButton(49)
    }

    private fun editTitle(field: String, clear: Boolean, title: String, label: String, current: () -> String) {
        if (clear) {
            run("towny:townyadmin set $field ${target.name}", current)
        } else {
            prompt(title, label, initial = current()) { text ->
                run("towny:townyadmin set $field ${target.name} $text", current)
            }
        }
    }
}
