# TownyMenu - Utility Guide

This guide covers the utility helpers provided in `net.trilleo.mc.plugins.townymenu.utils`.

| Utility       | Description                                                                   |
|:--------------|:------------------------------------------------------------------------------|
| `itemStack`   | DSL builder for menu icons                                                    |
| `Lang`        | Translations: per-player language files and the `tr()` helper                 |
| `TownyUtil`   | Safe formatting of Towny data, money and dates, permission checks, input args |
| `Prices`      | What Towny charges for a menu action: claims, outposts, spawn travel, merges  |
| `TownyConfig` | Towny's `config.yml` as a browsable, editable tree of sections and settings   |
| `DialogUtil`  | Native text-input and confirmation dialogs (Paper Dialog API)                 |
| `MessageUtil` | Prefix-decorated message sender for players                                   |
| `LoreUtil`    | Word-aware text wrapping for item lore with style carry-over                  |
| `LoreBlocks`  | Lays an icon's lore out in blocks: separators, the action divider, spacing    |

---

## ItemStack Builder DSL

The `itemStack` DSL creates fully configured items in a single expression. All text is parsed through
[MiniMessage](https://docs.advntr.dev/minimessage/index.html) with italics turned off, and attribute tooltips are
hidden, so icons read cleanly in menus.

```kotlin
import net.trilleo.mc.plugins.townymenu.utils.itemStack

val icon = itemStack(Material.EMERALD) {
    name("<green>Deposit")
    loreWrapped("<gray>Move money from your balance into the town bank.")
    loreBreak()
    lore("<gray>Balance: <white>$120")
    loreActions(listOf("<yellow>Click to deposit"))
    glow(true)
}
```

### Builder Methods

| Method        | Signature                                        | Description                                                   |
|:--------------|:-------------------------------------------------|:--------------------------------------------------------------|
| `name`        | `name(String)`                                   | Set the display name (MiniMessage)                            |
| `lore`        | `lore(vararg String)` / `lore(Iterable<String>)` | Append lore lines (each parsed with MiniMessage)              |
| `loreWrapped` | `loreWrapped(String, maxWidth = 40)`             | Append text word-wrapped by `LoreUtil`                        |
| `loreBreak`   | `loreBreak()`                                    | Start a new lore block, separated by a blank line             |
| `loreActions` | `loreActions(Iterable<String>)`                  | Append the closing hints block, set off by a divider rule     |
| `glow`        | `glow(Boolean)`                                  | Force the enchantment glint on or off                         |
| `hideTooltip` | `hideTooltip(Boolean)`                           | Hide the whole tooltip (decorative filler)                    |
| `head`        | `head(OfflinePlayer)`                            | Show a player's skin on a `PLAYER_HEAD`                       |
| `meta`        | `meta(ItemMeta.() -> Unit)`                      | Escape hatch for direct `ItemMeta` manipulation, applied last |

### Lore Blocks

Lore reads as blocks rather than one dense wall of text, and `LoreBlocks` lays them out so every icon in the plugin
spaces the same way:

- A blank line always follows the display name, giving the title room to breathe.
- `loreBreak()` separates blocks with a blank line. A line with no visible text counts as a break too, so `lore("")`
  between blocks still works, and repeated breaks collapse into one.
- `loreActions(lines)` closes the tooltip with what a click does, set off by a divider rule as wide as the longest lore
  line. Passing no lines adds nothing.
- A break or divider left dangling at either end is dropped, so a block that turns out to be empty never leaves a hole.

`LoreBlocks` is never used directly: menus build icons through [`Icons`](DEVELOPER_GUIDE.md#icons), or with the DSL
methods above, and those decide what goes in each block. Read
[The shape of an icon](DEVELOPER_GUIDE.md#the-shape-of-an-icon) before adding one.

```text
Town Spawn
                                      <- blank line under the title
Teleport to your town's spawn point.
                                      <- loreBreak()
Cost: $25
------------------------------        <- loreActions(...)
Click to teleport
```

---

## Lang

`Lang` holds the translations for every player-facing string. See
[Translations in the Developer Guide](DEVELOPER_GUIDE.md#translations) for the file layout and rules.

| Method / Extension                   | Description                                                                                          |
|:-------------------------------------|:-----------------------------------------------------------------------------------------------------|
| `Lang.load(plugin, language)`        | Copies the bundled files to `plugins/TownyMenu/lang/` if missing and loads every language file.      |
| `Lang.tr(sender, key, vararg args)`  | The translation of `key` for `sender`, with `{name}` placeholders filled; the key itself if missing. |
| `Lang.find(sender, key)`             | The raw translation, or `null` when no language defines `key` (for runtime-built keys).              |
| `Lang.ids`                           | Ids of every loaded language file (`en_US`, `zh_CN`, and any the server owner added).                |
| `CommandSender.tr(key, vararg args)` | Extension shorthand for `Lang.tr(this, key, *args)`.                                                 |
| `Menu.tr(key, vararg args)`          | The same, for the menu's viewer.                                                                     |

```kotlin
player.sendPrefixed(player.tr("command.reload.done"))
val line = player.tr("icon.town.claims", "claims" to town.numTownBlocks, "max" to town.maxTownBlocksAsAString)
```

---

## TownyUtil

`TownyUtil` holds the helpers every Towny menu needs.

### Formatting

Towny names, boards, titles, and tags are written by players. **Always** pass them through `name` or `text` before
putting them into a MiniMessage string — otherwise a town named `<click:run_command:…>` could inject tags.

| Method / Property          | Description                                                           |
|:---------------------------|:----------------------------------------------------------------------|
| `text(String)`             | Strips legacy `&`/`§` colour codes and escapes MiniMessage tags       |
| `name(String)`             | Like `text`, and shows underscores in Towny names as spaces           |
| `money(Double)`            | Formats with the economy's currency, or `-` without an economy        |
| `balance(Government)`      | A town's or nation's cached bank balance, formatted                   |
| `date(Long)`               | Formats an epoch-millisecond timestamp as `yyyy-MM-dd`                |
| `duration(Player, Long)`   | Seconds as hours and minutes in the player's language                 |
| `secondsUntilNewDay()`     | Seconds until Towny's next new day, when taxes and upkeep are taken   |
| `onOff(Player, Boolean)`   | A coloured On / Off label in the player's language                    |
| `yesNo(Player, Boolean)`   | A coloured Yes / No label in the player's language                    |
| `plotType(Player, String)` | A plot type in the player's language (`plot-type.*`), or the raw name |
| `economy`                  | `true` when Towny's economy is active                                 |

### Permissions and Input

| Method                         | Description                                                              |
|:-------------------------------|:-------------------------------------------------------------------------|
| `can(Player, PermissionNodes)` | Tests a node exactly like Towny's commands do (ranks, wildcards, admins) |
| `can(Player, String)`          | Same, for a raw node such as `towny.command.town.rank.assistant`         |
| `otherOnlineResidents(Player)` | Visible online players (except the viewer) that have a resident record   |
| `argument(String)`             | The first word of dialog input, for single-value arguments like amounts  |
| `nameArgument(String)`         | Dialog input as a Towny name, with whitespace turned into underscores    |

```kotlin
prompt(tr("bank.deposit-title"), tr("common.amount")) { amount ->
    run("towny:town deposit ${TownyUtil.argument(amount)}", { town.account.holdingBalance })
}
```

---

## Prices

`Prices` answers "what will this button cost?" so an icon can say so before the player clicks. Every amount is read
from Towny's config and from the town or nation involved — never a fixed default — so it matches what the command
would actually take, and everything returns `0` without an economy.

| Method                                | Description                                                                          |
|:--------------------------------------|:--------------------------------------------------------------------------------------|
| `claim(Town, count)`                  | What claiming `count` chunks costs the town, at its current rising claim price       |
| `outpost()`                           | What claiming an outpost costs                                                       |
| `unclaimRefund()`                     | What a town gets back per unclaimed chunk; negative when unclaiming costs instead     |
| `plotType(String, count)`             | What setting `count` plots to a plot type costs (`reset` prices the default type)    |
| `merge(remaining, succumbing)`        | What merging the second town into the first costs, before any debt it takes on       |
| `townSpawn(Player, Town, outpost)`    | What travelling to a town's spawn or outpost costs this player                       |
| `nationSpawn(Player, Nation)`         | What travelling to a nation's spawn costs this player                                |
| `residentSpawn(Player)`               | What `/resident spawn` costs — Towny prices it as a trip home                        |

Spawn travel is not a single config value: Towny prices it by the traveller's relationship to the destination (own
town, nation member, ally, outsider) and caps it at the server's price for that relationship, so a visitor and a
resident see different numbers. `Prices` mirrors that, including the free-spawn admin permissions.

Turn an amount into lore with `Menu.costLine` / `Menu.priceLine` rather than formatting it by hand:

```kotlin
grid.add(
    PermissionNodes.TOWNY_COMMAND_TOWN_CLAIM_TOWN,
    Icons.icon(
        Material.GRASS_BLOCK, tr("claims.claim"), tr("claims.claim-description"),
        *listOfNotNull(costLine(Prices.claim(town))).toTypedArray()
    )
) {
    run("towny:town claim", claims, delayTicks = CLAIM_DELAY)
}
```

---

## DialogUtil

`DialogUtil` shows native Minecraft dialogs through Paper's Dialog API, so players never type into chat. Showing a
dialog closes any open inventory. Callbacks always run on the main server thread.

| Method                                                                                  | Description                                                                                     |
|:----------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------|
| `input(player, title, label, onSubmit, onCancel, body?, initial, maxLength, multiline)` | Text field with Confirm/Cancel. Blank input counts as cancel. `onSubmit` receives trimmed text. |
| `confirm(player, title: Component, body?, onYes, onNo)`                                 | Confirm/Cancel dialog that cannot be closed with Escape, so exactly one callback runs.          |

Inside a menu, prefer `Menu.prompt(...)`, which reopens the menu on cancel.

---

## TownyConfig

`TownyConfig` presents Towny's `config.yml` as a tree built from Towny's `ConfigNodes` enum, so every setting Towny
knows about is listed in Towny's own order with its comment. It powers the admin config editor.

| Method / Type               | Description                                                                                      |
|:----------------------------|:-------------------------------------------------------------------------------------------------|
| `sections(section)`         | Direct child `Section`s of a dotted section path (`""` for the top level).                       |
| `settings(section)`         | `Setting`s directly inside a section.                                                            |
| `count(section)`            | Number of settings anywhere below a section.                                                     |
| `search(query)`             | Settings whose path or comment contains every word of `query`.                                   |
| `write(setting, value)`     | Sets the value in Towny's loaded config and saves the file with Towny's comments.                |
| `Setting.kind`              | `BOOLEAN`, `INTEGER`, `DECIMAL`, or `TEXT` from Towny's default; `READ_ONLY` for lists and maps. |
| `Setting.value` / `default` | The loaded value (the default when the file lacks it) and Towny's default, as strings.           |
| `Setting.parse(input)`      | `input` normalised for the setting's kind, or `null` when it is not a valid value.               |
| `Setting.description`       | Towny's English comment, already escaped for MiniMessage.                                        |

`write` does not apply the change: run `/townyadmin reload config` as the player afterwards, so Towny re-reads its
settings and its permission check applies.

```kotlin
val setting = TownyConfig.search("town creation cost").first()
setting.parse(input)?.let { value ->
    TownyConfig.write(setting, value)
    run("towny:townyadmin reload config", { TownySettings.getConfig() })
}
```

---

## MessageUtil

`MessageUtil` sends prefix-decorated messages to players. The prefix is read from `config.yml` under the
`message-prefix` key and supports both plain text and
[MiniMessage](https://docs.advntr.dev/minimessage/index.html) formatting. The plugin initialises `MessageUtil`
automatically at startup and after every `/townymenu reload`, so no manual setup is required in your own commands or
listeners.

### Configuration

```yaml
# config.yml

# Plain text
message-prefix: "[TownyMenu]"

# MiniMessage (rich formatting)
message-prefix: "<gray>[<gold>TownyMenu<gray>]"
```

### Usage

```kotlin
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

// Plain text
player.sendPrefixed("Hello!")

// MiniMessage
player.sendPrefixed("<green>Operation successful!")
player.sendPrefixed("<red>Something went wrong.")

// Adventure Component
player.sendPrefixed(Component.text("Hello!", NamedTextColor.GREEN))
```

### Methods

| Method / Extension                                 | Description                                                                                  |
|:---------------------------------------------------|:---------------------------------------------------------------------------------------------|
| `MessageUtil.init(prefixString)`                   | Loads the prefix (plain text or MiniMessage). Called automatically at startup and on reload. |
| `MessageUtil.sendPrefixed(player, msg: String)`    | Sends a plain-text or MiniMessage string with the prefix prepended.                          |
| `MessageUtil.sendPrefixed(player, msg: Component)` | Sends an Adventure `Component` with the prefix prepended.                                    |
| `Player.sendPrefixed(msg: String)`                 | Extension shorthand for `MessageUtil.sendPrefixed(this, msg)`.                               |
| `Player.sendPrefixed(msg: Component)`              | Extension shorthand for `MessageUtil.sendPrefixed(this, msg)`.                               |

### Example (Listener)

```kotlin
import net.trilleo.mc.plugins.townymenu.utils.sendPrefixed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.sendPrefixed("<green>Welcome to the server!")
    }
}
```

---

## LoreUtil

`LoreUtil` wraps a single MiniMessage-formatted string into multiple lore-ready `Component` lines, respecting word
boundaries, a configurable character width, and automatic style carry-over. Each output line is prefixed with an
italic-reset so Minecraft's default purple italic lore styling is neutralized.

### Usage

```kotlin
import net.trilleo.mc.plugins.townymenu.utils.LoreUtil

// Basic wrapping (default 40 visible characters per line)
val lines =
    LoreUtil.wrapLore("<gray>This legendary blade was forged in the fires of Mount Doom and carries the power of a thousand suns.")

// Custom width
val narrow = LoreUtil.wrapLore("<red>Warning: <white>This item is extremely dangerous.", maxWidth = 30)
```

### Explicit Newlines

Use `\n` or `<newline>` to force line breaks. Each segment is wrapped independently:

```kotlin
val lines = LoreUtil.wrapLore("<gray>Line one\nLine two<newline>Line three")
// Produces 3 lines (assuming each fits within maxWidth)
```

### Style Carry-Over

Colors and decorations carry over to subsequent wrapped lines automatically:

```kotlin
val lines =
    LoreUtil.wrapLore("<red><bold>This long red bold text will wrap and the second line will still be red and bold.")
// Both lines are rendered in red bold
```

### Integration with ItemStack DSL

The `itemStack` DSL wraps text for you with `loreWrapped`, which can be mixed with plain `lore` lines:

```kotlin
import net.trilleo.mc.plugins.townymenu.utils.itemStack

val item = itemStack(Material.BELL) {
    name("<gold>Town Settings")
    loreWrapped("<gray>PvP, mobs, fire, explosions, and whether the town is open to new residents.")
    loreActions(listOf("<yellow>Click to open"))
}
```

### Parameters

| Parameter  | Type     | Default | Description                        |
|:-----------|:---------|:--------|:-----------------------------------|
| `text`     | `String` | —       | MiniMessage-formatted input string |
| `maxWidth` | `Int`    | `40`    | Maximum columns per line           |

`LoreUtil.columns(text)` measures a string in the same columns, which is how `LoreBlocks` sizes its divider rule.

### Behavior Details

- **Word-aware**: Lines break at word boundaries (spaces). A word that exceeds `maxWidth` alone is force-broken
  mid-word.
- **Visible text only**: MiniMessage tags (`<red>`, `<bold>`, etc.) do not count toward the width.
- **CJK-aware**: Chinese, Japanese, and Korean characters count as two columns (they render about twice as wide), and
  lines may break between any two of them, since those scripts don't use spaces. Closing punctuation such as `，` or
  `。` stays on the line of the character before it.
- **Style inheritance**: The style active at the end of one line is inherited by the next.
- **Italic reset**: Every output line has `italic=false` set on its root style to override Minecraft's default lore
  rendering.
- **Empty input**: Returns an empty list.
