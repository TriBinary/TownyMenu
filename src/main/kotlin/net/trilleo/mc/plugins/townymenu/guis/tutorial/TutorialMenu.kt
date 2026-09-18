package net.trilleo.mc.plugins.townymenu.guis.tutorial

import net.kyori.adventure.text.minimessage.MiniMessage
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.DialogUtil
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

/** The tutorial hub: every chapter with the viewer's reading progress. */
class TutorialMenu(player: Player, back: Menu?) : Menu(player, player.tr("tutorial.title"), 6, back) {

    override fun build() {
        val chapters = Tutorial.visibleChapters
        val total = chapters.sumOf { it.lessons.size }
        val read = chapters.sumOf { it.readCount(player) }

        button(
            4, Icons.icon(
                Material.KNOWLEDGE_BOOK, tr("tutorial.hub"), tr("tutorial.hub-description"),
                tr("tutorial.progress", "read" to read, "total" to total),
                *listOfNotNull(if (read == total) tr("tutorial.all-read") else null).toTypedArray()
            )
        )

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        chapters.forEach { chapter ->
            grid.add(chapterIcon(chapter)) { TutorialChapterMenu(player, chapter, this).open() }
        }

        val next = Tutorial.nextUnread(player)
        if (next != null) {
            button(
                47, Icons.icon(
                    Material.SPECTRAL_ARROW, tr("tutorial.continue"), tr("tutorial.continue-description"),
                    tr("tutorial.chapter-line", "chapter" to tr(next.title))
                )
            ) {
                TutorialChapterMenu(player, next, this).open()
            }
        }
        backButton(49)
        if (read > 0) {
            button(51, Icons.icon(Material.WATER_BUCKET, tr("tutorial.reset"), tr("tutorial.reset-description"))) {
                DialogUtil.confirm(
                    player, MiniMessage.miniMessage().deserialize(tr("tutorial.reset-confirm")), null,
                    onYes = {
                        Tutorial.reset(player)
                        open()
                    },
                    onNo = ::open,
                )
            }
        }
    }

    private fun chapterIcon(chapter: Chapter) = itemStack(chapter.material) {
        val read = chapter.readCount(player)
        val total = chapter.lessons.size
        name(tr(chapter.title))
        loreWrapped("<gray>${tr(chapter.description)}")
        loreBreak()
        lore(tr("tutorial.progress", "read" to read, "total" to total))
        if (read == total) lore(tr("tutorial.chapter-complete"))
        loreActions(listOf(tr("tutorial.click-chapter")))
        glow(read == total)
    }
}
