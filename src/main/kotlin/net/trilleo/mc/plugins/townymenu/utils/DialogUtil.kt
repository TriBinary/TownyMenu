package net.trilleo.mc.plugins.townymenu.utils

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.minimessage.MiniMessage
import net.trilleo.mc.plugins.townymenu.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Native Minecraft dialogs (Paper Dialog API) used for text input and
 * confirmations, so players never have to type a command or chat message.
 *
 * Callbacks always run on the main server thread.
 */
object DialogUtil {

    private const val INPUT_KEY = "value"
    private val miniMessage = MiniMessage.miniMessage()
    private val callbackOptions = ClickCallback.Options.builder()
        .uses(1)
        .lifetime(Duration.ofMinutes(10))
        .build()

    /**
     * Asks [player] for a line (or, with [multiline], a paragraph) of text.
     *
     * @param title     MiniMessage dialog title
     * @param label     MiniMessage label above the text field
     * @param body      optional MiniMessage explanation shown above the field
     * @param onSubmit  receives the trimmed input; blank input counts as a cancel
     * @param onCancel  runs when the player presses Cancel or submits nothing
     */
    fun input(
        player: Player,
        title: String,
        label: String,
        onSubmit: (String) -> Unit,
        onCancel: () -> Unit,
        body: String? = null,
        initial: String = "",
        maxLength: Int = 64,
        multiline: Boolean = false,
    ) {
        val field = DialogInput.text(INPUT_KEY, miniMessage.deserialize(label))
            .width(300)
            .initial(initial.take(maxLength))
            .maxLength(maxLength)
            .apply { if (multiline) multiline(TextDialogInput.MultilineOptions.create(8, 100)) }
            .build()
        show(
            player,
            DialogBase.builder(miniMessage.deserialize(title))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(listOfNotNull(body?.let { DialogBody.plainMessage(miniMessage.deserialize(it)) }))
                .inputs(listOf(field))
                .build(),
            button("<green>Confirm") { view ->
                val value = view.getText(INPUT_KEY)?.trim().orEmpty()
                if (value.isEmpty()) onCancel() else onSubmit(value)
            },
            button("<gray>Cancel") { onCancel() },
        )
    }

    /**
     * Asks [player] to confirm an action. The dialog cannot be dismissed with
     * Escape, so exactly one of [onYes] / [onNo] always runs.
     */
    fun confirm(player: Player, title: Component, body: String?, onYes: () -> Unit, onNo: () -> Unit) {
        show(
            player,
            DialogBase.builder(title)
                .canCloseWithEscape(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(listOfNotNull(body?.let { DialogBody.plainMessage(miniMessage.deserialize(it)) }))
                .build(),
            button("<green>Confirm") { onYes() },
            button("<red>Cancel") { onNo() },
        )
    }

    private fun button(label: String, action: (DialogResponseView) -> Unit): ActionButton =
        ActionButton.builder(miniMessage.deserialize(label))
            .width(120)
            .action(DialogAction.customClick({ view, _ -> onMainThread { action(view) } }, callbackOptions))
            .build()

    private fun show(player: Player, base: DialogBase, yes: ActionButton, no: ActionButton) {
        val dialog = Dialog.create { factory ->
            factory.empty()
                .base(base)
                .type(DialogType.confirmation(yes, no))
        }
        player.closeInventory()
        player.showDialog(dialog)
    }

    private fun onMainThread(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) block() else Bukkit.getScheduler().runTask(Main.instance, Runnable(block))
    }
}
