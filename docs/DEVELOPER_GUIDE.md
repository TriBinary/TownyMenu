# TownyMenu - Developer Guide

This guide explains how TownyMenu is put together and how to extend it: **commands** and **listeners** (discovered
automatically), **menus** (the inventory GUIs that front Towny), **translations**, and the **configuration** system.

## How Auto-Registration Works

TownyMenu uses a `PackageScanner` to discover classes at startup. It scans specific packages for concrete
(non-abstract) classes and registers them automatically. You never need to edit `plugin.yml` or manually wire anything
up.

| System        | Base Class / Interface    | Package                                      |
|:--------------|:--------------------------|:---------------------------------------------|
| Commands      | `PluginCommand`           | `net.trilleo.mc.plugins.townymenu.commands`  |
| Permissions   | *(derived from commands)* | *(automatic — no package needed)*            |
| Listeners     | `Listener`                | `net.trilleo.mc.plugins.townymenu.listeners` |
| Configuration | `PluginConfig`            | `net.trilleo.mc.plugins.townymenu.config`    |

Subpackages are also scanned, so you can freely organize classes into folders like `commands/info/` or
`listeners/player/`.

Menus are **not** auto-registered: each menu is a short-lived object created with the context it needs (see
[Menus](#menus)).

## Constructor Requirements

Every command and listener class must have one of the following constructors:

| Constructor                          | When to Use                                   |
|:-------------------------------------|:----------------------------------------------|
| No-arg constructor                   | When you don't need a reference to the plugin |
| Constructor accepting a `JavaPlugin` | When you need to access the plugin instance   |

The plugin instance is injected automatically when a `JavaPlugin` constructor is available. Code that is not
constructed by a registrar (menus, utilities) can use `Main.instance`.

---

## Commands

To create a command, extend `PluginCommand` and place the class anywhere inside the `commands` package or a subpackage.

By default every command is registered as a **sub-command** of `/townymenu` (alias `/tm`). For example, a command
with `name = "reload"` becomes `/townymenu reload`. Set `isMainCommand = true` to register the command as a
standalone top-level command instead.

When a player types `/townymenu` in-game, tab-completion automatically lists all available sub-commands.

### Categories

Commands are automatically categorised based on their **subpackage** (folder) inside the `commands` package. The
category is used by the built-in `/townymenu help` command to group commands for display.

| Command Location                | Category |
|:--------------------------------|:---------|
| `commands/PingCommand.kt`       | General  |
| `commands/game/StartCommand.kt` | Game     |
| `commands/admin/BanCommand.kt`  | Admin    |

### Help Command

The plugin ships with a built-in `/townymenu help` command. It lists every registered command grouped by category,
sorted alphabetically within each group, and formatted with colours for readability. Every command should provide a
meaningful `description` so the help output is informative.

The help list is translated: it shows `command.<name>.description` and `command.category.<category>` from the
language files when they exist (plain text, no MiniMessage tags), and the English `description` or category name
otherwise. Add both keys for every new command.

### PluginCommand Properties

| Property        | Type           | Default        | Description                                                              |
|:----------------|:---------------|:---------------|:-------------------------------------------------------------------------|
| `name`          | `String`       | *(required)*   | The command name (e.g. `"reload"` for `/townymenu reload`)           |
| `description`   | `String`       | `""`           | A brief description shown in `/townymenu help` — always provide one  |
| `usage`         | `String`       | `"/<command>"` | Usage hint shown when the command fails                                  |
| `aliases`       | `List<String>` | `emptyList()`  | Alternative names for the command (applicable to main commands only)     |
| `permission`    | `String?`      | `null`         | Permission node required to use the command (auto-registered at startup) |
| `isMainCommand` | `Boolean`      | `false`        | When `true`, the command is registered as a standalone top-level command |

### Automatic Permission Registration

When the plugin starts, the `PermissionRegistrar` scans every registered command for a non-null `permission` value and
automatically registers it with Bukkit's `PluginManager`. This means:

* Permissions are visible to permission-management plugins (e.g. LuckPerms) without manual configuration.
* Each permission defaults to `PermissionDefault.OP` — only operators have it unless explicitly granted.
* The command's `description` is used as the permission description.
* Duplicate permissions (already registered by another source) are detected and skipped.

You do **not** need to declare permissions in `plugin.yml`; simply set the `permission` property on your command and the
system handles the rest.

### Methods to Override

| Method        | Required | Description                                      |
|:--------------|:---------|:-------------------------------------------------|
| `execute`     | Yes      | Called when a player or console runs the command |
| `tabComplete` | No       | Called when tab-completion is requested          |

### Example (Sub-Command)

This command is registered as `/townymenu ping` (the default behavior):

```kotlin
package net.trilleo.mc.plugins.townymenu.commands

import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PingCommand : PluginCommand(
    name = "ping",
    description = "Check your latency",
    usage = "/townymenu ping",
    permission = "townymenu.ping"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }
        sender.sendMessage("Pong! Your ping is ${sender.ping}ms.")
        return true
    }
}
```

### Example with Tab Completion (Sub-Command)

This command is registered as `/townymenu team`:

```kotlin
package net.trilleo.mc.plugins.townymenu.commands.game

import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeamCommand : PluginCommand(
    name = "team",
    description = "Join a team",
    usage = "/townymenu team <hunters|runners>",
    permission = "townymenu.team"
) {
    private val teams = listOf("hunters", "runners")

    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0] !in teams) {
            sender.sendMessage("Usage: /townymenu team <hunters|runners>")
            return false
        }
        sender.sendMessage("You joined the ${args[0]} team!")
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 1) {
            return teams.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
```

### Example with Plugin Instance (Sub-Command)

This command is registered as `/townymenu reload`:

```kotlin
package net.trilleo.mc.plugins.townymenu.commands

import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class ReloadCommand(private val plugin: JavaPlugin) : PluginCommand(
    name = "reload",
    description = "Reload the plugin configuration",
    permission = "townymenu.reload"
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        val main = plugin as? net.trilleo.mc.plugins.townymenu.Main
        if (main == null) {
            sender.sendMessage("Error: Plugin instance type mismatch. Unable to reload configuration.")
            return true
        }
        main.pluginConfig.reload()
        sender.sendMessage("Configuration reloaded!")
        return true
    }
}
```

### Example (Main Command)

Set `isMainCommand = true` to register a standalone top-level command. This command is registered as `/globaltool`:

```kotlin
package net.trilleo.mc.plugins.townymenu.commands

import net.trilleo.mc.plugins.townymenu.registration.PluginCommand
import org.bukkit.command.CommandSender

class GlobalToolCommand : PluginCommand(
    name = "globaltool",
    description = "A standalone top-level command",
    usage = "/globaltool",
    isMainCommand = true
) {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        sender.sendMessage("Hello from /globaltool!")
        return true
    }
}
```

---

## Listeners

To create a listener, implement Bukkit's `Listener` interface and place the class anywhere inside the `listeners`
package or a subpackage.

### Methods

Annotate each event handler method with `@EventHandler`. The method must accept a single Bukkit event parameter.

### Example

```kotlin
package net.trilleo.mc.plugins.townymenu.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(
            net.kyori.adventure.text.Component.text("Welcome, ${event.player.name}!")
        )
    }
}
```

### Example with Subpackage and Plugin Instance

```kotlin
package net.trilleo.mc.plugins.townymenu.listeners.player

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class DeathListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        plugin.logger.info("${event.player.name} has been eliminated!")
    }
}
```

---

## Menus

Every GUI lives in `net.trilleo.mc.plugins.townymenu.guis`. The framework is in `guis/framework`, shared menus
(toggles, permissions, bank, ranks, pickers) in `guis/common`, and feature menus in `guis/town`, `guis/nation`,
`guis/plot`, and `guis/resident`. `MainMenu` is the entry point opened by `/townymenu` and sneak + swap-hand.

### Design Rules

- **Menus are views, not a source of truth.** `build()` runs on every render and reads fresh data from `TownyAPI`.
  Never cache Towny objects' state in a menu field; hold the object (a `Town`, a `Resident`) and re-read it.
- **Changes go through Towny commands.** Buttons call `run("towny:town toggle pvp", probe)` so Towny's permission
  checks, costs, confirmations, and messages all apply. Always use the `towny:` namespace so other plugins' aliases
  can't intercept the command.
- **Grey out what the player can't do.** Use `guarded(slot, PermissionNodes.X, item) { … }` (or `Layout.add(node, …)`)
  with the same node Towny's command checks.
- **Escape player-written text.** Pass names, boards, titles, and tags through `TownyUtil.name()` / `TownyUtil.text()`
  before embedding them in MiniMessage.
- **Translate every string.** Icon names, lore, titles, and prompts come from `tr("key")`, never from Kotlin literals.
  See [Translations](#translations).

### Menu

`Menu(player, title, rows, back)` is the base class. The title is already translated, so subclasses pass
`player.tr("my-menu.title")`. The menu is its own `InventoryHolder`, so `MenuListener` routes
clicks by checking the open inventory's holder — there is no registry and nothing to clean up when a menu closes.

| Member                                  | Description                                                                     |
|:----------------------------------------|:--------------------------------------------------------------------------------|
| `build()`                               | Abstract. Place items and click actions. Called on every `render()`.            |
| `open()`                                | Renders and opens the menu, or re-renders in place if it is already open.       |
| `render()`                              | Clears the inventory (grey filler) and calls `build()`.                         |
| `button(slot, item, action?)`           | Places an item; `action` receives the `ClickType`.                              |
| `guarded(slot, node, item, action)`     | Like `button`, but shows a grey "no permission" icon without `node`.            |
| `layout(vararg slots)`                  | Returns a `Layout` that fills the given slots in order, for optional buttons.   |
| `backButton(slot)`                      | Back arrow to `back`, or a close button when `back` is `null`.                  |
| `run(command, probe?, returnTo?, delay)`| Runs a Towny command as the player (see below).                                 |
| `runAndClose(command)`                  | Closes the menu, then runs the command (teleports, books, chat output).         |
| `prompt(title, label, …) { text -> }`   | Shows a text-input dialog; Cancel reopens the menu.                             |
| `tr(key, "name" to value, …)`           | Translates `key` into the viewer's language (see [Translations](#translations)). |
| `resident`                              | The viewer's Towny `Resident`, or `null`.                                       |

### Running Commands: `MenuActions`

Towny only reports results in chat, which is hidden behind an open inventory. `run` therefore takes a **probe** — a
lambda reading the state the command should change — and `MenuActions` compares it after `delayTicks` (default 4; use
~20 for claims, which Towny processes asynchronously):

- probe **changed** → `returnTo` (default: this menu) is re-opened, showing the new state;
- probe **unchanged**, or no probe → the menu closes so the player can read Towny's chat reply (an error, a cost).

If the command raises a Towny confirmation, `MenuActions` suppresses the chat prompt (`ConfirmationSendEvent`) and
shows a native confirmation dialog instead; accepting runs Towny's confirm command and then settles the probe as usual.

Use `returnTo = MainMenu(player)` for actions after which the current menu no longer makes sense (leaving or deleting
a town, joining a town).

### PagedMenu and ListMenu

`PagedMenu(player, title, back)` is a six-row menu. Override `entries()` to return `MenuEntry` objects; the top five
rows show one page, slot 45/53 page back and forth, 49 is the back button, and `controls()` may place extra buttons in
46–48 and 50–52. `MenuEntry` takes a **lambda** that builds the icon, so only entries on the visible page are built:

```kotlin
override fun entries(): List<MenuEntry> =
    town.residents.map { member ->
        MenuEntry({ Icons.resident(player, member, "", tr("common.click-view")) }) {
            ResidentProfileMenu(player, member, this).open()
        }
    }
```

`ListMenu(player, title, back) { menu -> entries }` is a `PagedMenu` built from a lambda, for one-off lists.

### Shared Menus (`guis/common`)

| Class            | Purpose                                                                                     |
|:-----------------|:--------------------------------------------------------------------------------------------|
| `ToggleMenu`     | A grid of `Toggle`s (material, translated name and description, node, command, value reader). |
| `PermissionMenu` | 4×4 build/destroy/switch/item-use grid for any `set perm` command.                          |
| `BankMenu`       | Deposit, withdraw, and bank history for a town or nation.                                   |
| `RankMenu`       | Grants or revokes town or nation ranks, checking the per-rank permission node.              |
| `Pickers`        | Selection menus for online residents, towns, nations, and fixed options.                    |

### Icons

`Icons` builds the standard icons: `icon(material, name, description, extraLines…)` from already-translated text, and
`toggle(player, …)` plus the summary icons `resident(player, …)`, `town(player, …)`, `nation(player, …)`, which add
their own labels in `player`'s language and accept extra lore lines.

### Example: A New Menu

```kotlin
package net.trilleo.mc.plugins.townymenu.guis.town

import com.palmergames.bukkit.towny.permissions.PermissionNodes
import net.trilleo.mc.plugins.townymenu.guis.framework.Icons
import net.trilleo.mc.plugins.townymenu.guis.framework.Menu
import net.trilleo.mc.plugins.townymenu.utils.tr
import org.bukkit.Material
import org.bukkit.entity.Player

class TownPvpMenu(player: Player, back: Menu) : Menu(player, player.tr("town-pvp.title"), 3, back) {

    override fun build() {
        val town = resident?.townOrNull ?: return backButton(22)
        guarded(13, PermissionNodes.TOWNY_COMMAND_TOWN_TOGGLE_PVP,
            Icons.toggle(player, Material.IRON_SWORD, tr("toggle.pvp"), town.isPVP, tr("toggle.town-pvp-description"))) {
            run("towny:town toggle pvp", { town.isPVP })
        }
        backButton(22)
    }
}
```

with the new title in both language files:

```yaml
# src/main/resources/lang/en_US.yml
town-pvp:
  title: "Town PvP"

# src/main/resources/lang/zh_CN.yml
town-pvp:
  title: "城镇 PvP"
```

Open it from another menu's button with `TownPvpMenu(player, this).open()`.

### Dialogs

`DialogUtil` wraps Paper's Dialog API. Menus normally use `prompt(...)`; call `DialogUtil.input` or
`DialogUtil.confirm` directly only outside a menu. Callbacks always run on the main thread.

### Opening Shortcuts

`MenuListener` opens `MainMenu` when a player presses swap-hand (F) while sneaking, unless
`sneak-swap-hand-shortcut` is `false` in `config.yml`. It also cancels every click and drag while a menu is on top, and
closes all menus when the plugin disables.

---

## Translations

Every player-facing string lives in `src/main/resources/lang/<id>.yml`. TownyMenu bundles `en_US` and `zh_CN`, and
**every change that adds or removes text updates both files**.

### How It Works

1. On startup (and `/townymenu reload`), `Lang.load` copies any missing bundled file into `plugins/TownyMenu/lang/`
   and loads every `.yml` there. Keys missing from a file fall back to the bundled copy of that language, then to
   English, so plugin updates never break edited files.
2. `language` in `config.yml` is `auto` or a language id. With `auto`, each player gets the file matching their client
   locale exactly (`zh_cn`), else one sharing its language prefix (`zh_tw` → `zh_CN`), else `en_US`. The console uses
   the configured language, or `en_US` under `auto`.
3. `tr(key, "name" to value)` looks the key up and replaces each `{name}` with `value.toString()`. A key no file
   defines is returned as-is, so a missing translation is visible in game.

### Writing Keys

```kotlin
// In a menu (Menu.tr uses the viewer's language)
button(11, Icons.icon(Material.EMERALD, tr("bank.deposit"), tr("bank.deposit-description")))
lore(tr("icon.town.residents", "count" to town.numResidents))

// Anywhere else
sender.sendPrefixed(sender.tr("command.reload.done"))
```

```yaml
bank:
  deposit: "<green>Deposit"
  deposit-description: "Move money from your balance into the bank."
```

- **Colours go in the translation**, so translators see the whole line.
- **Placeholders are inserted verbatim.** Escape player-written text with `TownyUtil.name()` / `TownyUtil.text()`.
- **Group keys by menu** (`town-details.*`) and reuse `common.*`, `icon.*`, and `toggle.*` for shared text.
- **Write keys as whole string literals.** `tr(if (held) "rank.assigned" else "rank.not-assigned")` is fine;
  `tr("rank.$state")` is not, because the test below cannot see it. `plot-type.*` and `command.*` are the only
  runtime-built keys (read with `Lang.find`, which returns `null` instead of the key).
- **Quote YAML keys that YAML 1.1 reads as booleans**: `"on"`, `"off"`, `"yes"`, `"no"`.

### LangFilesTest

`./gradlew build` runs `LangFilesTest`, which fails when:

- a bundled language is missing a key English has, or has one English lacks;
- a translation's `{placeholders}` differ from English;
- a key-shaped string literal in `src/main/kotlin` is not in `en_US.yml`;
- a key in `en_US.yml` is not used by any code (outside the runtime-built prefixes).

To bundle another language, add `lang/<id>.yml` to the resources and its id to `Lang.BUNDLED` and to the test's
language list.

---

## Adventure Library

Paper bundles the [Kyori Adventure](https://docs.advntr.dev/) library, so no extra dependency is required. Adventure
replaces the legacy Bukkit chat API and provides rich, structured text through immutable `Component` objects, as well as
APIs for titles, boss bars, sounds, and more.

### Component

`Component` is the core type. All text displayed to players must be a `Component`. The most common factory is
`Component.text(...)`, which accepts an optional colour and decoration inline:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

// Plain text
val plain = Component.text("Hello, world!")

// Coloured text
val coloured = Component.text("Hello, world!", NamedTextColor.GREEN)

// Bold + coloured text
val bold = Component.text("Hello, world!", NamedTextColor.GOLD, TextDecoration.BOLD)
```

#### Additional Component Factory Methods

| Factory                            | Description                                                              |
|:-----------------------------------|:-------------------------------------------------------------------------|
| `Component.empty()`                | A component with no content — useful as a neutral base to `.append()` to |
| `Component.newline()`              | A line-break component                                                   |
| `Component.space()`                | A single space                                                           |
| `Component.text(String)`           | Plain text component                                                     |
| `Component.translatable(String)`   | A Minecraft translation key (e.g. `"block.minecraft.dirt"`)              |
| `Component.keybind(String)`        | Displays the key bound to an action (e.g. `"key.jump"`)                  |
| `Component.join(separator, parts)` | Joins a list of components with a separator between each one             |

##### `Component.translatable` Example

`Component.translatable` renders using the player's own client language:

```kotlin
import net.kyori.adventure.text.Component

// Displays the item's translated name in the player's language
val dirtName = Component.translatable("block.minecraft.dirt")
sender.sendMessage(dirtName)
```

##### `Component.keybind` Example

`Component.keybind` renders as the key the player has bound to a given action:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

// Shows "Press [Space] to jump!" where [Space] adapts to the player's key binding
val hint = Component.text("Press ", NamedTextColor.GRAY)
    .append(Component.keybind("key.jump", NamedTextColor.YELLOW))
    .append(Component.text(" to jump!", NamedTextColor.GRAY))
sender.sendMessage(hint)
```

##### `Component.join` Example

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor

val items = listOf(
    Component.text("Sword", NamedTextColor.RED),
    Component.text("Shield", NamedTextColor.BLUE),
    Component.text("Bow", NamedTextColor.GREEN)
)
// "Sword, Shield, Bow"
val list = Component.join(JoinConfiguration.separator(Component.text(", ")), items)
sender.sendMessage(list)
```

#### NamedTextColor

`NamedTextColor` exposes the 16 standard Minecraft colours as constants:

| Constant       | In-game appearance |
|:---------------|:-------------------|
| `BLACK`        | Black              |
| `DARK_BLUE`    | Dark Blue          |
| `DARK_GREEN`   | Dark Green         |
| `DARK_AQUA`    | Dark Aqua          |
| `DARK_RED`     | Dark Red           |
| `DARK_PURPLE`  | Dark Purple        |
| `GOLD`         | Gold               |
| `GRAY`         | Gray               |
| `DARK_GRAY`    | Dark Gray          |
| `BLUE`         | Blue               |
| `GREEN`        | Green              |
| `AQUA`         | Aqua               |
| `RED`          | Red                |
| `LIGHT_PURPLE` | Light Purple       |
| `YELLOW`       | Yellow             |
| `WHITE`        | White              |

#### TextColor (Hex / RGB)

For colours beyond the 16 named constants, use `TextColor`:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

// From a hex string
val orange = TextColor.fromHexString("#FF8C00")!!
val msg = Component.text("This is orange!", orange)

// From RGB values (0–255 each)
val custom = TextColor.color(135, 206, 235) // sky blue
val sky = Component.text("Sky blue text", custom)
```

#### TextDecoration

`TextDecoration` applies visual styles to a component:

| Constant        | Effect              |
|:----------------|:--------------------|
| `BOLD`          | Bold text           |
| `ITALIC`        | Italic text         |
| `UNDERLINED`    | Underlined text     |
| `STRIKETHROUGH` | Strikethrough text  |
| `OBFUSCATED`    | Obfuscated (matrix) |

Decorations can be combined by chaining `.decorate(...)` calls:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

val fancy = Component.text("Important!", NamedTextColor.RED)
    .decorate(TextDecoration.BOLD)
    .decorate(TextDecoration.UNDERLINED)
```

### Style

`Style` bundles a colour, decorations, click event, and hover event into a reusable object. Apply it to a component with
`.style(Style)` or pass it directly to `Component.text`:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

val headerStyle = Style.style(
    NamedTextColor.GOLD,
    TextDecoration.BOLD
)

val header = Component.text("TownyMenu", headerStyle)
sender.sendMessage(header)
```

Build a `Style` with multiple properties using the builder:

```kotlin
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

val linkStyle = Style.style { builder ->
    builder.color(NamedTextColor.AQUA)
    builder.decoration(TextDecoration.UNDERLINED, true)
    builder.clickEvent(ClickEvent.openUrl("https://papermc.io"))
    builder.hoverEvent(HoverEvent.showText(Component.text("Visit Paper docs")))
}
```

### Chaining Components

Use `.append(Component)` to concatenate multiple styled segments into one message:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

val message = Component.text("[TownyMenu] ", NamedTextColor.GOLD, TextDecoration.BOLD)
    .append(Component.text("Welcome to the server!", NamedTextColor.YELLOW))

sender.sendMessage(message)
```

### Sending Messages

Both `CommandSender` (players and the console) and `Player` accept a `Component` directly via `sendMessage`:

```kotlin
// From a command
sender.sendMessage(Component.text("Command executed!", NamedTextColor.GREEN))

// From a listener
event.player.sendMessage(Component.text("You joined!", NamedTextColor.AQUA))
```

To broadcast a message to every online player, use the Bukkit server instance:

```kotlin
import org.bukkit.Bukkit

Bukkit.broadcast(Component.text("Server announcement!", NamedTextColor.GOLD))
```

### ClickEvent

A `ClickEvent` makes a component interactive when clicked in the chat window. Attach one with `.clickEvent(...)`:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

// Run a command when clicked
val runCmd = Component.text("[Click to teleport]", NamedTextColor.GREEN)
    .clickEvent(ClickEvent.runCommand("/tp spawn"))

// Pre-fill the chat bar with a command (player still has to press Enter)
val suggest = Component.text("[Click to reply]", NamedTextColor.YELLOW)
    .clickEvent(ClickEvent.suggestCommand("/msg Steve "))

// Open a URL in the player's browser
val link = Component.text("[Open website]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
    .clickEvent(ClickEvent.openUrl("https://papermc.io"))

// Copy text to the player's clipboard
val copy = Component.text("[Copy server IP]", NamedTextColor.GRAY)
    .clickEvent(ClickEvent.copyToClipboard("play.example.com"))

sender.sendMessage(runCmd)
```

#### ClickEvent Actions

| Factory method                       | Effect                                      |
|:-------------------------------------|:--------------------------------------------|
| `ClickEvent.runCommand(String)`      | Executes the command as the player          |
| `ClickEvent.suggestCommand(String)`  | Places the string in the player's chat bar  |
| `ClickEvent.openUrl(String)`         | Opens a URL in the player's default browser |
| `ClickEvent.copyToClipboard(String)` | Copies the string to the player's clipboard |
| `ClickEvent.changePage(Int)`         | Changes the page of an open book            |

### HoverEvent

A `HoverEvent` displays a tooltip when the player hovers over the component. Attach one with `.hoverEvent(...)`:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

// Show a text tooltip
val withHover = Component.text("Hover over me!", NamedTextColor.GREEN)
    .hoverEvent(
        HoverEvent.showText(
            Component.text("This is a tooltip!", NamedTextColor.GRAY)
        )
    )

// Show an item tooltip (displays the item's name, lore, and stats)
val diamond = ItemStack(Material.DIAMOND)
val withItemHover = Component.text("A diamond", NamedTextColor.AQUA)
    .hoverEvent(diamond.asHoverEvent())

sender.sendMessage(withHover)
```

#### HoverEvent Actions

| Factory method                   | Effect                                 |
|:---------------------------------|:---------------------------------------|
| `HoverEvent.showText(Component)` | Shows a rich-text tooltip              |
| `ItemStack.asHoverEvent()`       | Shows the item's name, lore, and stats |
| `Entity.asHoverEvent()`          | Shows the entity's name and UUID       |

### MiniMessage

[MiniMessage](https://docs.advntr.dev/minimessage/index.html) is a string-based format that lets you express rich text
with lightweight tags. The `ItemStack` DSL uses it internally, and you can use it anywhere you need to parse user-facing
strings (e.g. from `config.yml`) into `Component` objects.

```kotlin
import net.kyori.adventure.text.minimessage.MiniMessage

val mm = MiniMessage.miniMessage()

// Colour
val red = mm.deserialize("<red>This is red text")

// Bold + gradient
val fancy = mm.deserialize("<bold><gradient:gold:yellow>Fancy Title</gradient></bold>")

// Multiple colours in one line
val mixed = mm.deserialize("<green>Success: <white>operation completed")

sender.sendMessage(fancy)
```

#### MiniMessage with Placeholders

Use `TagResolver` to inject dynamic values into a MiniMessage string at runtime:

```kotlin
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

val mm = MiniMessage.miniMessage()

val message = mm.deserialize(
    "<gold>Welcome, <player>! You have <coins> coins.",
    Placeholder.unparsed("player", player.name),
    Placeholder.unparsed("coins", "500")
)
player.sendMessage(message)
```

#### Common MiniMessage Tags

| Tag                              | Effect                                          |
|:---------------------------------|:------------------------------------------------|
| `<color_name>` / `<red>`         | Named colour (same names as `NamedTextColor`)   |
| `<#RRGGBB>`                      | Hex colour                                      |
| `<bold>`, `<b>`                  | Bold                                            |
| `<italic>`, `<i>`                | Italic                                          |
| `<underlined>`, `<u>`            | Underline                                       |
| `<strikethrough>`, `<st>`        | Strikethrough                                   |
| `<obfuscated>`, `<obf>`          | Obfuscated                                      |
| `<gradient:color1:color2>`       | Smooth gradient between two or more colours     |
| `<rainbow>`                      | Full rainbow gradient across the text           |
| `<reset>`                        | Reset all active styles                         |
| `<newline>` / `<br>`             | Line break                                      |
| `<click:run_command:/cmd>`       | Clickable text that runs a command              |
| `<click:suggest_command:/cmd>`   | Clickable text that fills the chat bar          |
| `<click:open_url:https://...>`   | Clickable text that opens a URL                 |
| `<click:copy_to_clipboard:text>` | Clickable text that copies to clipboard         |
| `<hover:show_text:'tooltip'>`    | Text shown when the cursor hovers over the line |
| `<keybind:key.jump>`             | Renders the player's bound key for an action    |
| `<lang:block.minecraft.dirt>`    | Renders a Minecraft translation key             |

### Title

Display a large on-screen title and subtitle to a player with the Adventure `Title` API:

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import java.time.Duration

val title = Title.title(
    Component.text("Game Over", NamedTextColor.RED),           // main title
    Component.text("You were eliminated!", NamedTextColor.GRAY), // subtitle
    Title.Times.times(
        Duration.ofMillis(500),   // fade-in
        Duration.ofSeconds(3),    // stay
        Duration.ofMillis(500)    // fade-out
    )
)

player.showTitle(title)
```

To clear an active title before it finishes:

```kotlin
player.clearTitle()
```

To reset the title display timings back to their defaults:

```kotlin
player.resetTitle()
```

### Action Bar

The action bar is the text that appears just above the hotbar. It disappears on its own after a couple of seconds.

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

player.sendActionBar(
    Component.text("⚔ 10 kills", NamedTextColor.GOLD)
)
```

### Boss Bar

A boss bar is the coloured progress bar shown at the top of the screen. Create one, customise it, then add players:

```kotlin
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

val bar = BossBar.bossBar(
    Component.text("Boss Fight!", NamedTextColor.RED), // name
    1.0f,                                               // progress (0.0–1.0)
    BossBar.Color.RED,                                  // bar colour
    BossBar.Overlay.PROGRESS                            // bar style
)

// Show to a player
player.showBossBar(bar)

// Update the progress (e.g. based on boss HP)
bar.progress(0.5f)

// Update the title
bar.name(Component.text("50% HP remaining", NamedTextColor.YELLOW))

// Hide from a player
player.hideBossBar(bar)
```

#### BossBar.Color Options

| Constant | Bar colour |
|:---------|:-----------|
| `PINK`   | Pink       |
| `BLUE`   | Blue       |
| `RED`    | Red        |
| `GREEN`  | Green      |
| `YELLOW` | Yellow     |
| `PURPLE` | Purple     |
| `WHITE`  | White      |

#### BossBar.Overlay Options

| Constant     | Appearance                         |
|:-------------|:-----------------------------------|
| `PROGRESS`   | Solid bar (no notches)             |
| `NOTCHED_6`  | Bar split into 6 notched segments  |
| `NOTCHED_10` | Bar split into 10 notched segments |
| `NOTCHED_12` | Bar split into 12 notched segments |
| `NOTCHED_20` | Bar split into 20 notched segments |

### Sound

Play a sound to a player at their location using the Adventure `Sound` API:

```kotlin
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.key.Key

// Play a named sound at the player's position
player.playSound(
    Sound.sound(
        Key.key("minecraft:entity.player.levelup"), // sound key
        Sound.Source.PLAYER,                         // source category
        1.0f,                                        // volume
        1.0f                                         // pitch
    )
)
```

You can also use `net.kyori.adventure.sound.Sound.Source` to control which Minecraft audio channel the sound plays on:

| Source    | Channel shown in game settings |
|:----------|:-------------------------------|
| `MASTER`  | Master                         |
| `MUSIC`   | Music                          |
| `RECORD`  | Jukebox/Note Blocks            |
| `WEATHER` | Weather                        |
| `BLOCK`   | Blocks                         |
| `HOSTILE` | Hostile Creatures              |
| `NEUTRAL` | Friendly Creatures             |
| `PLAYER`  | Players                        |
| `AMBIENT` | Ambient/Environment            |
| `VOICE`   | Voice/Speech                   |

Stop all sounds currently playing for a player:

```kotlin
import net.kyori.adventure.sound.SoundStop

player.stopSound(SoundStop.all())
```

### Tab-List Header and Footer

Set the header and footer shown in the player list (Tab key):

```kotlin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

player.sendPlayerListHeaderAndFooter(
    Component.text("TownyMenu Server", NamedTextColor.GOLD, TextDecoration.BOLD),
    Component.text("${player.ping}ms", NamedTextColor.GRAY)
)
```

To clear the header and footer, pass empty components:

```kotlin
player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty())
```

---

## Utilities

The `utils` package (`net.trilleo.mc.plugins.townymenu.utils`) contains the `itemStack` DSL, `LoreUtil`, `MessageUtil`,
`TownyUtil`, and `DialogUtil`. See the [Utility Guide](UTILITY_GUIDE.md) for full documentation.

---

## Configuration

TownyMenu provides a typed configuration wrapper — `PluginConfig` — around the standard Bukkit `config.yml`. It
lives in the `net.trilleo.mc.plugins.townymenu.config` package and is created automatically when the plugin starts.

### How It Works

1. On first run, the default `config.yml` bundled inside the JAR (`src/main/resources/config.yml`) is copied to the
   plugin's data folder.
2. `PluginConfig` exposes each setting as a typed property that reads the loaded configuration.
3. `reload()` re-reads the file from disk, picking up changes made while the server is running.

`Main` exposes the instance as `pluginConfig`, reachable anywhere through `Main.instance.pluginConfig`.

### Default config.yml

```yaml
# TownyMenu Configuration

# A friendly prefix shown before plugin messages
message-prefix: "<click:run_command:/townymenu><gradient:yellow:gold>[TownyMenu]"

# Menu language: "auto" follows each player's client, or a language id such as "zh_CN" for everyone.
language: auto

# Open the main menu when a player presses the swap-hand key (F by default) while sneaking.
sneak-swap-hand-shortcut: true
```

### Properties

| Property                | Key                        | Description                                    |
|:------------------------|:---------------------------|:-----------------------------------------------|
| `messagePrefix`         | `message-prefix`           | MiniMessage prefix used by `MessageUtil`       |
| `language`              | `language`                 | `auto` or the language id given to `Lang.load` |
| `sneakSwapHandShortcut` | `sneak-swap-hand-shortcut` | Whether sneak + swap-hand opens the main menu  |

To add a setting, add the key to `config.yml` and a matching property to `PluginConfig`:

```kotlin
val menuSounds: Boolean
    get() = plugin.config.getBoolean("menu-sounds", true)
```

### Reloading

Call `reload()` to re-read `config.yml` from disk without restarting the server. The method copies any new default keys
into the file and saves it:

```kotlin
Main.instance.pluginConfig.reload()
```

The built-in `/townymenu reload` command already calls this method, then reloads `MessageUtil` and `Lang`.
