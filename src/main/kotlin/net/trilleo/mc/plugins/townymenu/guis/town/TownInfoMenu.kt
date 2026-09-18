package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.MainMenu
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.guis.nation.NationInfoMenu
import net.trilleo.mc.plugins.townymenu.utils.TownyUtil
import org.bukkit.Material
import org.bukkit.entity.Player

/** Public view of any town: visit, join, donate, buy, or invite it to the viewer's nation. */
class TownInfoMenu(player: Player, private val town: Town, back: Menu) :
    Menu(player, TownyUtil.name(town.name), 5, back) {

    override fun build() {
        if (!town.exists()) {
            button(13, Icons.icon(Material.BARRIER, tr("town-info.gone")))
            return backButton(40)
        }
        button(4, Icons.town(player, town, tr("common.founded", "date" to TownyUtil.date(town.registered))))

        val viewer = resident
        val grid = layout(19, 20, 21, 22, 23, 24, 25)

        if (viewer?.townOrNull == town) {
            grid.add(Icons.icon(Material.WRITABLE_BOOK, tr("town-info.manage"), tr("town-info.manage-description"))) {
                TownMenu(player, this).open()
            }
        }
        grid.add(
            Icons.icon(
                Material.ENDER_PEARL, tr("town-info.visit"), tr("town-info.visit-description"),
                *listOfNotNull(
                    if (TownyUtil.economy && town.spawnCost > 0) tr(
                        "common.cost",
                        "cost" to TownyUtil.money(town.spawnCost)
                    ) else null
                ).toTypedArray()
            )
        ) {
            runAndClose("towny:town spawn ${town.name}")
        }
        grid.add(
            Icons.icon(
                Material.PLAYER_HEAD,
                tr("town.residents"),
                null,
                tr("icon.town.residents", "count" to town.numResidents)
            )
        ) {
            TownMembersMenu(player, town, this).open()
        }
        town.nationOrNull?.let { nation ->
            grid.add(
                Icons.icon(
                    Material.BEACON,
                    tr("town-info.nation", "nation" to TownyUtil.name(nation.name)),
                    tr("town-info.nation-description")
                )
            ) {
                NationInfoMenu(player, nation, this).open()
            }
        }
        if (viewer != null && !viewer.hasTown() && town.isOpen) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_JOIN,
                Icons.icon(Material.OAK_DOOR, tr("town-info.join"), tr("town-info.join-description"))
            ) {
                run("towny:town join ${town.name}", { viewer.hasTown() }, returnTo = MainMenu(player))
            }
        }
        if (TownyUtil.economy && viewer?.townOrNull != town) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_DEPOSIT_OTHERTOWN,
                Icons.icon(Material.EMERALD, tr("town-info.donate"), tr("town-info.donate-description"))
            ) {
                prompt(
                    tr("town-info.donate-title", "town" to TownyUtil.name(town.name)),
                    tr("common.amount")
                ) { amount ->
                    run(
                        "towny:town deposit ${TownyUtil.argument(amount)} ${town.name}",
                        { town.account.holdingBalance })
                }
            }
        }
        if (TownyUtil.economy && town.isForSale && viewer?.townOrNull != town) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_BUYTOWN, Icons.icon(
                    Material.GOLD_BLOCK, tr("town-info.buy"), tr("town-info.buy-description"),
                    tr("common.price", "price" to TownyUtil.money(town.forSalePrice)),
                )
            ) { run("towny:town buytown ${town.name}", { town.mayor }, returnTo = MainMenu(player)) }
        }
        val viewerTown = viewer?.townOrNull
        if (viewerTown != null && viewerTown != town && viewer.isMayor) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_TOWN_MERGE,
                Icons.icon(
                    Material.STRUCTURE_VOID, tr("town-info.merge"),
                    tr(
                        "town-info.merge-description",
                        "town" to TownyUtil.name(town.name),
                        "own" to TownyUtil.name(viewerTown.name)
                    )
                )
            ) {
                run("towny:town merge ${town.name}")
            }
        }
        val nation = viewer?.nationOrNull
        if (nation != null && !town.hasNation() && viewer.isKing) {
            grid.add(
                PermissionNodes.TOWNY_COMMAND_NATION_INVITE_ADD,
                Icons.icon(
                    Material.PAPER,
                    tr("town-info.invite"),
                    tr("town-info.invite-description", "nation" to TownyUtil.name(nation.name))
                )
            ) {
                run("towny:nation add ${town.name}", { nation.sentInvites.size })
            }
        }

        backButton(40)
    }
}
