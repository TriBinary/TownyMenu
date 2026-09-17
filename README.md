<h1 align="center">
  TownyMenu
</h1>

<p align="center">
  A GUI addon for <a href="https://github.com/TownyAdvanced/Towny">Towny</a> — manage your town, nation, and plots
  through in-game menus instead of commands.
</p>

---

## Features

- **Towny, without the commands** — Players can do their everyday Towny tasks from clickable menus. Text input (names,
  amounts, bios) uses native Minecraft dialogs, and Towny's confirmations appear as confirm/cancel dialogs.
- **Open it anywhere** — `/townymenu` (or `/tm`), or press swap-hand (**F**) while sneaking.
- **Tutorial** — Nine chapters teach every Towny feature worth knowing, with this server's real prices and limits. Each
  lesson opens the menu it explains, every feature menu has a Tutorial button for its chapter, and reading progress is
  saved. New players without a town are pointed to it when they join.
- **Town menu** — Residents and ranks, invites, the town bank, claims (single chunk, area, fill, outposts, unclaim,
  bonus claims, auto-claim, ceding a chunk to another town, taking over claims), PvP/mobs/fire/explosions/open/public/
  peaceful/nation zone settings, name, board, tag, taxes, plot prices, spawn and home block, map colour, selling the
  town, build permissions, trusted residents and towns, outlaws, the jail (jailing and releasing prisoners, jails and
  cells), announcements, merging towns, reclaiming ruins, spawn and outpost teleports, and leaving or deleting the
  town.
- **Nation menu** — Member towns and invitations, residents and nation ranks, the nation bank, allies and enemies,
  sanctioned towns, announcements, peaceful/open/public settings, name, board, tag, taxes, capital, leader, spawn, map
  colour, and leaving or deleting the nation.
- **Plot menu** — For the plot you stand in: buy, sell, give up, evict (and resell), plot type, name,
  PvP/fire/explosion/mob/tax settings, build permissions and per-player overrides, trusted players, join-day limits,
  jail cells, outposts, plot groups and districts, clearing the plot, the plot HUD, and buying, selling, or giving up
  every plot in an area at once. On a plot in a group, these settings change the whole group.
- **Profile menu** — Friends, personal plot permissions, a bio, bail, spawn, every personal toggle (border titles,
  auto map, auto claim, plot borders, info tool, chat spy, admin bypass, and more), and clearing or resetting all
  modes at once.
- **Map** — A 9×5 chunk map around you, coloured by your town, your plots, your nation, allies, and enemies, with plots
  for sale highlighted; right-click one to buy it.
- **Directories** — Browse and rank every town and nation by residents, claims, bank balance, and more; visit, join,
  donate, buy a town that is for sale, or propose alliances.
- **Invites** — Accept or decline town invites, nation invites for your town, and alliance requests in one place.
- **Admin menus** — Server admins can edit every setting in Towny's `config.yml` (grouped by section, searchable,
  applied instantly), change per-world settings, run new days, backups, and reloads, manage any town, nation, or
  resident, and change TownyMenu's own settings. Open them with `/tm admin` or from the main menu.
- **English and Chinese** — Every menu, dialog, and message follows each player's Minecraft language (English or
  Simplified Chinese). Server owners can edit the translations or add new languages.
- **Respects Towny** — Every action runs the matching Towny command, so Towny's permissions, costs, cooldowns, and
  messages apply unchanged. Buttons you lack permission for are shown greyed out.

## Requirements

| Dependency | Version    |
|:-----------|:-----------|
| Paper      | 26.2+      |
| Java       | 25+        |
| Towny      | 0.103.2.7+ |

TownyMenu is an addon: Towny must be installed on the server, or TownyMenu will not load.

## Building

```powershell
./gradlew build
```

The compiled JAR is placed in `build/libs/`. Run `./gradlew copyPlugin` to copy it, along with the matching Towny jar,
into `run/plugins/` for the local test server, or `./gradlew startServer` to copy them and start the server. Before the
first start, download a Paper 26.2 jar from [papermc.io](https://papermc.io/downloads/paper) into `run/`, then accept
the EULA in `run/eula.txt` after the first launch.

Prebuilt jars are attached to every [GitHub release](https://github.com/Trilleo/TownyMenu/releases); see the
[change log](CHANGELOG.md) for what changed in each one.

## Commands

All commands are sub-commands of `/townymenu` (alias `/tm`).

| Command        | Description                                         |
|:---------------|:----------------------------------------------------|
| `/tm`          | Open the main menu                                  |
| `/tm help`     | List all available commands                         |
| `/tm tutorial` | Open the tutorial                                   |
| `/tm reload`   | Reload the configuration and translations (OP only) |
| `/tm admin`    | Open the admin menu (OP only)                       |

## Configuration

| Key                        | Default | Description                                                                 |
|:---------------------------|:--------|:----------------------------------------------------------------------------|
| `message-prefix`           | —       | MiniMessage prefix shown before plugin messages                             |
| `language`                 | `auto`  | `auto` follows each player's client language; `en_US` or `zh_CN` forces one |
| `sneak-swap-hand-shortcut` | `true`  | Open the main menu by pressing swap-hand (F) while sneaking                 |
| `tutorial-join-hint`       | `true`  | Suggest the tutorial to players without a town when they join               |

## Translations

TownyMenu ships with English (`en_US`) and Simplified Chinese (`zh_CN`). On first start the language files are copied to
`plugins/TownyMenu/lang/`. Edit them to change any text, or add a new `<id>.yml` (for example `de_DE.yml`) to support
another language; players whose client uses that language see it automatically. Run `/tm reload` after editing. Keys
missing from a file fall back to the bundled copy, then to English.

## Developer Documentation

Full development guides are in the `docs/` directory:

- [DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) — How to add commands, listeners, and menus, and use the configuration.
- [UTILITY_GUIDE.md](docs/UTILITY_GUIDE.md) — Reference for the utility helpers (`itemStack` DSL, `TownyUtil`,
  `DialogUtil`, `MessageUtil`, `LoreUtil`).
- [COMMIT_STRUCTURE.md](docs/COMMIT_STRUCTURE.md) — Commit message conventions.
- [RELEASING.md](docs/RELEASING.md) — Writing the changelog and publishing a release.

For AI-assisted development, see [CLAUDE.md](CLAUDE.md).
