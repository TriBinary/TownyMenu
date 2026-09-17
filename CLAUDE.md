# TownyMenu — Agent Instructions

## Project Overview

TownyMenu is a Minecraft Paper plugin that adds a GUI layer on top of [Towny](https://github.com/TownyAdvanced/Towny):
players manage their town, nation, and plots through inventory menus instead of typing Towny commands. It targets
Minecraft 26.2 (Paper API 26.2), hard-depends on Towny, and is written in Kotlin. [README.md](README.md) has the
player-facing overview.

## Tech Stack

| Tool           | Version                                          |
|:---------------|:-------------------------------------------------|
| Language       | Kotlin 2.3.10                                    |
| Build          | Gradle 9.7.1 (Kotlin DSL)                        |
| Platform       | Paper API 26.2 (MC 26.2)                         |
| Towny          | 0.103.2.7 (`towny_version` in gradle.properties) |
| Java toolchain | JDK 25                                           |

## After Every Change: Keep the Changelog and Docs in Sync

Before finishing any task that changes the plugin, do all of the following:

1. **Update the changelog** — add an entry for the change under `## Unreleased` in [CHANGELOG.md](CHANGELOG.md), in the
   same commit as the change. Every new feature gets an entry, and so does every improvement and fix.
    - Follow the SkyHanni-style format documented in [docs/RELEASING.md](docs/RELEASING.md): category (`### New
      Features` / `### Improvements` / `### Fixes` / `### Technical Details` / `### Removed Features`), then a
      `#### Feature Area` heading (`Town Menu`, `Nation Menu`, `Plot Menu`, `Resident Menu`, `Misc`, …), then `+`
      bullets.
    - Reuse the category and feature-area headings already under `## Unreleased` instead of repeating them.
    - Write player- and server-owner-facing entries for gameplay changes; put refactors, build, and tooling changes
      under `### Technical Details`.
    - Never edit the section of a version that has already been released.
    - Skip changelog entries only for changes with no effect on the shipped plugin or its workflow (e.g. fixing a typo
      in a doc).

2. **Update the developer docs** — if the change adds or alters a base class, registrar, utility, or data API, update
   [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) or [docs/UTILITY_GUIDE.md](docs/UTILITY_GUIDE.md) in the same
   task. A change to a documented workflow (e.g. the release process in [docs/RELEASING.md](docs/RELEASING.md)) updates
   that doc too. Keep this file accurate as well.

3. **Translate every new string** — player-facing text never lives in Kotlin. Add each key to **both**
   [en_US.yml](src/main/resources/lang/en_US.yml) and [zh_CN.yml](src/main/resources/lang/zh_CN.yml) in the same task,
   with a real Simplified Chinese translation, and remove keys the change no longer uses. `LangFilesTest` fails the
   build when the files disagree or a key is missing or unused. See the Translations section below.

4. **Check the README** — if the change affects anything [README.md](README.md) mentions (features, commands,
   requirements, build instructions), update it.

## Build & Run

```powershell
./gradlew build        # Builds build/libs/TownyMenu-<version>.jar
./gradlew copyPlugin   # Copies the jar and Towny (towny_version) into run/plugins/
./gradlew startServer  # Runs copyPlugin, then launches the paper-*.jar in run/
```

The local test server lives in `run/` (gitignored). `copyPlugin` puts the matching Towny jar in `run/plugins/`, but the
Paper 26.2 jar (`run/paper-*.jar`) must be downloaded by hand from https://papermc.io/downloads/paper, and `eula.txt`
accepted, before `startServer` works. The server console reads commands from the terminal running Gradle.

## Repository Layout

```
src/main/kotlin/net/trilleo/mc/plugins/townymenu/
├── Main.kt                  # Plugin entry point (Main.instance)
├── commands/                # Sub-commands (auto-registered)
│   ├── info/
│   └── moderation/
├── config/                  # PluginConfig (typed config.yml wrapper)
├── guis/                    # Inventory menus (created on demand, not registered)
│   ├── framework/           # Menu, PagedMenu, MenuActions, Icons
│   ├── common/              # ToggleMenu, PermissionMenu, BankMenu, RankMenu, Pickers
│   ├── admin/               # Admin menus (/tm admin): Towny config editor, worlds, server, towns, nations
│   ├── town/  nation/  plot/  resident/
│   ├── tutorial/            # Tutorial hub, chapter menu, and all lesson content (Tutorial.kt)
│   └── MainMenu.kt, MapMenu.kt, InvitesMenu.kt
├── listeners/               # Event listeners (auto-registered)
├── registration/            # Auto-registration engine (do not modify lightly)
└── utils/                   # itemStack DSL, Lang, TownyUtil, TownyConfig, DialogUtil, MessageUtil, LoreUtil
src/main/resources/
├── config.yml  plugin.yml
└── lang/                    # en_US.yml, zh_CN.yml — every player-facing string
```

## Auto-Registration System

The plugin uses `PackageScanner` to discover components at startup — you **never** edit `plugin.yml` or wire things
manually. Just extend the right base class and place the file in the correct package.

| Component | Base Class      | Package                 |
|:----------|:----------------|:------------------------|
| Command   | `PluginCommand` | `commands` (any depth)  |
| Listener  | `Listener`      | `listeners` (any depth) |
| Config    | `PluginConfig`  | `config`                |

Commands are sub-commands of `/townymenu` (alias `/tm`) unless `isMainCommand = true`; `/tm` with no arguments opens
`MainMenu`. Permissions are derived from commands automatically. Every command and listener needs either a no-arg
constructor or one accepting a `JavaPlugin`. See [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md).

## Menus

Menus are not registered: construct one with its context and call `open()` (e.g. `TownInfoMenu(player, town, this)`).
Extend `Menu` (or `PagedMenu` for lists) and implement `build()`, which runs on every render.

- **Mutate through `run(command, probe)`** — it runs the Towny command and re-opens the menu if the probe changed, or
  leaves it closed so Towny's chat reply is visible. Towny confirmations are shown as dialogs automatically.
- **Gate buttons with `guarded(slot, PermissionNodes.X, …)`** using the node Towny's command checks.
- **Collect text with `prompt(...)`** (Paper dialogs), never chat input.
- **Escape player-written text** (names, boards, titles, tags) with `TownyUtil.name()` / `TownyUtil.text()` before
  embedding it in MiniMessage.
- **Translate everything with `tr("key", "placeholder" to value)`** — `Menu.tr` uses the viewer's language; outside a
  menu use `sender.tr(...)`. Menu titles are translated in the constructor call:
  `Menu(player, player.tr("x.title"), …)`.
- **Keep the tutorial in sync** — a new or changed Towny feature updates its lesson in `guis/tutorial/Tutorial.kt`, and
  a new feature menu gets a `tutorialButton` for its chapter.
- **Keep `PagedMenu` entries lazy** — pass the icon as a lambda to `MenuEntry` so off-page icons are never built.

## Translations

- `Lang` loads `plugins/TownyMenu/lang/<id>.yml` (copied from `src/main/resources/lang/` on first start). With
  `language: auto` in `config.yml` each player gets the file matching their client locale (`zh_tw` → `zh_CN` by prefix),
  falling back to `en_US`.
- Values are MiniMessage with `{placeholder}` arguments. Arguments are inserted verbatim, so escape player-written text
  with `TownyUtil.name()` / `TownyUtil.text()` first. Colours belong in the translation, not in Kotlin.
- Keys are grouped by menu (`town.*`, `plot-group.*`); reuse `common.*`, `toggle.*`, and `icon.*` for shared text.
- Key names must appear as whole string literals (`tr(if (on) "a.on" else "a.off")`, not `"a.$state"`) so
  `LangFilesTest` can see them. The only runtime-built keys are `plot-type.*` and `command.*`.
- Quote YAML keys that YAML 1.1 reads as booleans (`"on"`, `"off"`, `"yes"`, `"no"`).
- Chinese terms follow Towny's own zh_CN wording: 城镇 (town), 国家 (nation), 镇长 (mayor), 国王 (nation leader), 居民
  (resident), 地块 (plot), 领地 (claims), 前哨 (outpost).

## Working with Towny

- **Towny is a hard dependency** — it is `compileOnly` in the build and listed under `depend` in `plugin.yml`, so it is
  always loaded before TownyMenu. Never shade Towny into the jar.
- **Read data through `TownyAPI`** — `TownyAPI.getInstance()` gives residents (`getResident(player)`), towns, nations,
  and `TownBlock`s. Handle `null` results: a player may have no resident record, town, or nation.
- **Prefer Towny's own actions over reimplementing them** — for anything that changes Towny state (joining, claiming,
  deposits, ranks, toggles), run the equivalent Towny command as the player — from a menu, `run("towny:town ...")` — so
  Towny's permission checks, costs, confirmations, and messages still apply. Always use the `towny:` namespace. Only
  call Towny's mutating API directly when no command covers the action, and then enforce the same permission nodes Towny
  would.
- **Towny's config is the one exception** — no command edits it, so the admin config editor writes through
  `TownyConfig` and then runs `/townyadmin reload config` as the player. Every other admin action runs a
  `/townyadmin` or `/townyworld` command.
- **GUIs are views, not a second source of truth** — never cache Towny data; re-read it from `TownyAPI` when a menu
  opens or refreshes.
- **Towny's package `com.palmergames.bukkit.towny.object` needs backticks in Kotlin imports** (`` `object` ``).
- **React to Towny events** — listen to Towny's Bukkit events (`com.palmergames.bukkit.towny.event.*`) in `listeners/`
  when an open menu needs to update.
- **Bumping Towny** — change `towny_version` in [gradle.properties](gradle.properties) and the Requirements table in
  [README.md](README.md) together.

## Versioning & Releases

- `plugin_version` in [gradle.properties](gradle.properties) is the single source of truth for the plugin version. It
  flows into the jar filename and, through `processResources`, into `plugin.yml` (`version: ${projectVersion}`) — never
  hardcode a version in `plugin.yml` or `build.gradle.kts`.
- [.github/workflows/build.yml](.github/workflows/build.yml) builds every push and pull request.
- Releases are made by tagging `vX.Y.Z`: [.github/workflows/release.yml](.github/workflows/release.yml) builds the jar,
  uses the matching `## Version X.Y.Z` section of `CHANGELOG.md` as the release notes, and attaches the jar. See
  [docs/RELEASING.md](docs/RELEASING.md). **Never tag or push tags unless explicitly asked** — pushing a tag publishes a
  release.

## Commit Convention

Format: `<Tag>: <imperative message>` — no trailing period. One granular commit per logical change.

| Tag           | Use for                                   |
|:--------------|:------------------------------------------|
| `Feature`     | New functionality                         |
| `Fix`         | Bug / crash / logic error repairs         |
| `Improvement` | Refines existing code, UX, or performance |
| `Internal`    | Docs, comments, repo maintenance          |
| `Backend`     | Build system, dependency, config changes  |
| `Update`      | Version bumps                             |

Example: `Feature: Add town overview menu`

Tags map to changelog categories: `Feature` → `### New Features`, `Improvement` → `### Improvements`, `Fix` →
`### Fixes`, `Backend` / `Internal` → `### Technical Details`, `Update` → usually no entry. A `Feature`, `Improvement`,
or `Fix` commit carries its own changelog entry. See [docs/COMMIT_STRUCTURE.md](docs/COMMIT_STRUCTURE.md).

## Code Conventions

- **Kotlin idioms** — use `object` for singletons, `data class` for value types, extension functions for utility.
- **No comments by default** — only add one when the WHY is non-obvious (hidden constraint, workaround, subtle
  invariant). Never describe WHAT the code does.
- **No unused code** — delete dead code entirely rather than commenting it out or renaming with `_`.
- **No hard-coded player-facing text** — every string goes through `tr()` and the language files.
- **MiniMessage everywhere** — all player-facing text uses Kyori Adventure MiniMessage tags (`<red>`, `<bold>`,
  `<gradient:…>`). Never use `ChatColor`.
- **Build menu icons with the DSL** — use `itemStack { }` for GUI items and `LoreUtil` for wrapped lore.
- **No manual registration** — never edit `plugin.yml` commands/listeners. The auto-registration system handles
  everything.
