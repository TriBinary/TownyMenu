<h1 align="center">
  TownyMenu
</h1>

<p align="center">
  A GUI addon for <a href="https://github.com/TownyAdvanced/Towny">Towny</a> — manage your town, nation, and plots
  through in-game menus instead of commands.
</p>

---

## Features

- **Towny, without the commands** — Clickable inventory menus for the Towny actions players use every day.

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

| Command      | Description                               |
|:-------------|:------------------------------------------|
| `/tm help`   | List all available commands               |
| `/tm reload` | Reload the plugin configuration (OP only) |

## Developer Documentation

Full development guides are in the `docs/` directory:

- [DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) — How to add commands, listeners, GUIs, tasks, items, recipes, and
  player/server data.
- [UTILITY_GUIDE.md](docs/UTILITY_GUIDE.md) — Reference for all utility helpers (`itemStack` DSL, `MessageUtil`,
  `PDCUtil`, etc.).
- [COMMIT_STRUCTURE.md](docs/COMMIT_STRUCTURE.md) — Commit message conventions.
- [RELEASING.md](docs/RELEASING.md) — Writing the changelog and publishing a release.

For AI-assisted development, see [CLAUDE.md](CLAUDE.md).
