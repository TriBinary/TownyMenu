<h1 align="center">
  TownyMenu
</h1>

<p align="center">
  A GUI addon for <a href="https://github.com/TownyAdvanced/Towny">Towny</a> — manage your town, nation, and plots
  through in-game menus instead of commands.
</p>

---

## Features

- **Towny, without the commands** — Players can do their everyday Towny tasks from clickable menus. Text input
  (names, amounts, bios) uses native Minecraft dialogs, and Towny's confirmations appear as confirm/cancel dialogs.
- **Open it anywhere** — `/townymenu` (or `/tm`), or press swap-hand (**F**) while sneaking.
- **Town menu** — Residents and ranks, invites, the town bank, claims (single chunk, area, fill, outposts, unclaim,
  bonus claims, auto-claim), PvP/mobs/fire/explosions/open/public/peaceful settings, name, board, tag, taxes, plot
  prices, spawn and home block, map colour, selling the town, build permissions, trusted residents and towns,
  outlaws, spawn and outpost teleports, and leaving or deleting the town.
- **Nation menu** — Member towns and invitations, residents and nation ranks, the nation bank, allies and enemies,
  peaceful/open/public settings, name, board, tag, taxes, capital, leader, spawn, map colour, and leaving or deleting
  the nation.
- **Plot menu** — For the plot you stand in: buy, sell, give up, evict, plot type, name, PvP/fire/explosion/mob/tax
  settings, build permissions, trusted players, plot groups and districts, and clearing the plot.
- **Profile menu** — Friends, personal plot permissions, a bio, bail, spawn, and every personal toggle (border titles,
  auto map, auto claim, plot borders, info tool, and more).
- **Map** — A 9×5 chunk map around you, coloured by your town, your plots, your nation, allies, and enemies, with plots
  for sale highlighted.
- **Directories** — Browse and rank every town and nation by residents, claims, bank balance, and more; visit, join,
  donate, buy a town that is for sale, or propose alliances.
- **Invites** — Accept or decline town invites, nation invites for your town, and alliance requests in one place.
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

The compiled JAR is placed in `build/libs/`. Run `./gradlew copyPlugin` to copy it into `run/plugins/` for the local
test server, or `./gradlew startServer` to copy it and start the server.

Prebuilt jars are attached to every [GitHub release](https://github.com/Trilleo/TownyMenu/releases); see the
[change log](CHANGELOG.md) for what changed in each one.

## Commands

All commands are sub-commands of `/townymenu` (alias `/tm`).

| Command      | Description                                         |
|:-------------|:----------------------------------------------------|
| `/tm`        | Open the main menu                                  |
| `/tm help`   | List all available commands                         |
| `/tm reload` | Reload the configuration and translations (OP only) |

## Configuration

| Key                        | Default | Description                                                                     |
|:---------------------------|:--------|:--------------------------------------------------------------------------------|
| `message-prefix`           | —       | MiniMessage prefix shown before plugin messages                                 |
| `language`                 | `auto`  | `auto` follows each player's client language; `en_US` or `zh_CN` forces one    |
| `sneak-swap-hand-shortcut` | `true`  | Open the main menu by pressing swap-hand (F) while sneaking                     |

## Translations

TownyMenu ships with English (`en_US`) and Simplified Chinese (`zh_CN`). On first start the language files are copied to
`plugins/TownyMenu/lang/`. Edit them to change any text, or add a new `<id>.yml` (for example `de_DE.yml`) to support
another language; players whose client uses that language see it automatically. Run `/tm reload` after editing. Keys
missing from a file fall back to the bundled copy, then to English.

## Developer Documentation

Full development guides are in the `docs/` directory:

- [DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) — How to add commands, listeners, and menus, and use the
  configuration.
- [UTILITY_GUIDE.md](docs/UTILITY_GUIDE.md) — Reference for the utility helpers (`itemStack` DSL, `TownyUtil`,
  `DialogUtil`, `MessageUtil`, `LoreUtil`).
- [COMMIT_STRUCTURE.md](docs/COMMIT_STRUCTURE.md) — Commit message conventions.
- [RELEASING.md](docs/RELEASING.md) — Writing the changelog and publishing a release.

For AI-assisted development, see [CLAUDE.md](CLAUDE.md).
