package net.trilleo.mc.plugins.townymenu.guis.tutorial

import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.itemStack
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * One tutorial chapter. Left-clicking a lesson marks it read and opens the menu it explains; right-clicking, or
 * clicking a lesson without a menu the viewer can open, toggles whether it is read.
 */
class TutorialChapterMenu(player: Player, private val chapter: Chapter, back: Menu?) :
    Menu(player, player.tr("tutorial.chapter-title", "chapter" to player.tr(chapter.title)), 6, back) {

    override fun build() {
        val lessons = chapter.lessons
        val read = chapter.readCount(player)

        button(4, itemStack(chapter.material) {
            name(tr(chapter.title))
            loreWrapped("<gray>${tr(chapter.description)}")
            loreBreak()
            lore(tr("tutorial.progress", "read" to read, "total" to lessons.size))
            lore(progressBar(read, lessons.size))
            chapter.link?.let { loreActions(listOf(linkLine(it))) }
            glow(read == lessons.size)
        }) { if (chapter.link?.open(player, this) != true) render() }

        val grid = layout(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        lessons.forEach { lesson ->
            val isRead = Tutorial.isRead(player, lesson)
            val link = lesson.link?.takeIf { it.available(player) }
            grid.add(lessonIcon(lesson, isRead)) { click ->
                if (link != null && click.isLeftClick) {
                    Tutorial.setRead(player, listOf(lesson), true)
                    if (!link.open(player, this)) render()
                } else {
                    Tutorial.setRead(player, listOf(lesson), !isRead)
                    render()
                }
            }
        }

        val chapters = Tutorial.visibleChapters
        val index = chapters.indexOf(chapter)
        chapters.getOrNull(index - 1)?.let { previous ->
            button(
                45,
                Icons.icon(
                    Material.ARROW,
                    tr("tutorial.previous-chapter"),
                    null,
                    tr("tutorial.chapter-line", "chapter" to tr(previous.title)),
                    actions = hints("click.open")
                )
            ) {
                TutorialChapterMenu(player, previous, back).open()
            }
        }
        chapters.getOrNull(index + 1)?.let { next ->
            button(
                53,
                Icons.icon(
                    Material.ARROW,
                    tr("tutorial.next-chapter"),
                    null,
                    tr("tutorial.chapter-line", "chapter" to tr(next.title)),
                    actions = hints("click.open")
                )
            ) {
                TutorialChapterMenu(player, next, back).open()
            }
        }

        val allRead = read == lessons.size
        button(
            51, Icons.icon(
                if (allRead) Material.BOOK else Material.WRITABLE_BOOK,
                tr(if (allRead) "tutorial.mark-all-unread" else "tutorial.mark-all-read"),
                actions = hints(if (allRead) "tutorial.click-unread" else "tutorial.click-read")
            )
        ) {
            Tutorial.setRead(player, lessons, !allRead)
            render()
        }
        backButton(49)
    }

    private fun lessonIcon(lesson: Lesson, read: Boolean): ItemStack = itemStack(lesson.material) {
        name(tr(if (read) "tutorial.lesson-read" else "tutorial.lesson-unread", "lesson" to tr(lesson.title)))
        loreWrapped("<gray>${tr(lesson.body)}")
        val facts = lesson.facts(player)
        if (facts.isNotEmpty()) {
            loreBreak()
            lore(facts)
        }
        loreBreak()
        lore(tr(if (read) "tutorial.status-read" else "tutorial.status-unread"))
        val link = lesson.link
        if (link != null && link.available(player)) {
            loreActions(
                listOf(tr("tutorial.left-open"), tr(if (read) "tutorial.right-unread" else "tutorial.right-read"))
            )
        } else {
            loreActions(
                listOfNotNull(
                    link?.let { linkLine(it) },
                    tr(if (read) "tutorial.click-unread" else "tutorial.click-read"),
                )
            )
        }
    }

    private fun linkLine(link: Link): String =
        if (link.available(player)) tr("tutorial.click-open-menu") else link.requirement?.let { tr(it) } ?: ""
}
