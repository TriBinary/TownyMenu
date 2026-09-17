# TownyMenu - Change Log

## Unreleased

### New Features

#### Main Menu

+ Added a main menu, opened with `/townymenu` (`/tm`) or by pressing swap-hand (F) while sneaking.
    + The sneak + swap-hand shortcut can be turned off with `sneak-swap-hand-shortcut` in `config.yml`.
+ Added native confirm/cancel dialogs for Towny confirmations started from a menu.
+ Added native text-input dialogs for names, amounts, boards, and other values.

#### Tutorial

+ Added a tutorial covering every Towny feature worth knowing, in nine chapters: getting started, finding a town,
  running a town, claiming land, plots, permissions and protection, nations, money and taxes, and your profile.
    + Open it with `/tm tutorial`, the Tutorial button in the main menu, or the Tutorial button in each feature menu,
      which jumps straight to the chapter explaining that menu.
    + Left-click a lesson to open the menu it explains; lessons say what you need first, such as joining a town.
    + Lessons show this server's live values, such as the cost of founding a town, claim prices, upkeep, and the time of
      the new day. Money topics are hidden when the server has no economy.
    + Your progress is saved: read lessons are ticked, chapters show how many you have read, and Continue Reading opens
      the next unread chapter.
+ Players without a town are pointed to the tutorial when they join, until they have read every lesson. Turn this off
  with `tutorial-join-hint` in `config.yml` or in the admin TownyMenu settings.

#### Town Menu

+ Added a town menu covering residents, ranks, titles, invites, kicking, and handing over the mayorship.
+ Added the town bank: deposit, withdraw, and bank history.
+ Added claim management: claim a chunk, an area, or enclosed gaps, claim outposts, unclaim, buy bonus claims, and
  toggle auto-claim.
+ Added town settings (PvP, mobs, fire, explosions, open, public, peaceful, percentage taxes).
+ Added town details: name, board, tag, taxes, plot prices, spawn cost, spawn, home block, outpost spawn, primary jail,
  map colour, and selling the town.
+ Added town build permissions, trusted residents and towns, outlaws, spawn and outpost teleports, and leaving or
  deleting a town.
+ Added a town directory that ranks towns by residents, claims, balance, online players, or openness.
    + Visit, join, donate to, or buy a town, or invite it to your nation.

#### Nation Menu

+ Added a nation menu covering member towns, town invites, residents, nation ranks, and the nation bank.
+ Added allies and enemies management, including alliance proposals.
+ Added nation settings (peaceful, open, public, percentage taxes).
+ Added nation details: name, board, tag, taxes, capital, leader, spawn, and map colour.
+ Added founding, joining, leaving, and deleting nations.
+ Added a nation directory with sorting, visiting, joining, and diplomacy.

#### Plot Menu

+ Added a menu for the plot you stand in: buy, sell, give up, evict, plot type, name, and clearing.
+ Added plot settings (PvP, fire, explosions, mobs, taxed), build permissions, and trusted players.
+ Added plot group and district management.

#### Resident Menu

+ Added a profile menu with friends, personal plot permissions, a bio, bail, and spawn.
+ Added every personal toggle, including border titles, auto map, auto claim, plot borders, and the info tool.
+ Added profiles for other residents with friend, trust, outlaw, and invite actions.

#### Admin Menu

+ Added admin menus, opened with `/tm admin` or a button in the main menu, for players with `townymenu.admin` (OP by
  default).
+ Added a Towny config editor covering every setting in Towny's `config.yml`, grouped by section with Towny's own
  descriptions and a search.
    + Click a switch to flip it, or enter a new number or text; right-click restores the default. Changes are saved and
      applied immediately.
+ Added per-world settings: PvP, explosions, fire, mobs, war, jailing, land repair, claiming, wilderness permissions,
  the wilderness name, and resetting a world to defaults.
+ Added server actions: new day, new hour, backup, database save, outpost check, Towny reloads, bank withdrawal, debug,
  and developer mode toggles, and wilderness use and land repair for every world at once.
+ Added town administration for any town: teleport, rename, set mayor, add and kick residents, bonus and bought claims,
  bank deposits and withdrawals, leaving its nation, restoring a ruined town, deleting, and admin-only settings (forced
  PvP and mobs, unlimited claims, upkeep, war, top lists).
+ Added nation administration: rename, leader, capital, adding and kicking towns, bank, settings, and deleting.
+ Added resident administration with a searchable list: town membership, rename, title, surname, NPC flag, releasing
  from jail, and deleting.
+ Added TownyMenu settings: menu language, message prefix, and the sneak + swap-hand shortcut, applied without
  `/tm reload`.

#### Misc

+ Added a chunk map around the player, coloured by relation, with claim and unclaim buttons.
+ Added an invites menu for town invites, nation invites, and alliance requests.
+ Buttons for actions you lack Towny permission for are shown greyed out.
+ Clicking the chat message prefix now opens the main menu.
+ Added translations: every menu, dialog, and command message is available in English and Simplified Chinese.
    + Each player sees their Minecraft client language. Set `language` in `config.yml` to use one language for everyone.
    + Language files are saved to `plugins/TownyMenu/lang/`, where server owners can edit them or add new languages.
      `/tm reload` reloads them.
    + Chinese descriptions wrap cleanly in item tooltips.

### Fixes

#### Misc

+ Fixed players seeing the "Unknown sub-command" message twice.

### Technical Details

#### Misc

+ `copyPlugin` now also copies the Towny version from `gradle.properties` into the test server, replacing any other
  Towny version. `startServer` passes `--nogui`, forwards console input, and explains when `run/` has no Paper jar.
+ Set up the TownyMenu project from the Paper plugin template.
    + Added Towny as a required dependency.
    + Added build and release workflows.
    + Aligned the Adventure test dependencies with Paper 26.2 (5.2.0).
+ Replaced the template's auto-registered GUI system with a context-carrying menu framework (`Menu`, `PagedMenu`,
  `MenuActions`) that runs Towny commands and builds paged icons lazily.
+ Removed unused template systems: custom items, recipes, scheduled tasks, player and server data storage,
  `CountdownUtil`, `TeamUtil`, `TagUtil`, `PDCUtil`, `GameRuleUtil`, and the unused enums.
+ Added `TownyUtil` and `DialogUtil`, and trimmed the `itemStack` DSL and `PluginConfig` to what the plugin uses.
+ Added the `Lang` translation system (`tr()` on menus and command senders) and moved every player-facing string into
  `src/main/resources/lang/<id>.yml`.
    + `Icons.toggle`, `Icons.resident`, `Icons.town`, `Icons.nation`, and `TownyUtil.onOff` now take the viewing player.
    + Added `LangFilesTest`, which checks that every bundled language has the same keys and placeholders as English and
      that every key the code uses exists and is used.
+ `LoreUtil` now measures width in columns, counting CJK characters as two and breaking lines between them.
+ Added `TownyConfig`, which exposes Towny's `config.yml` as a tree of sections and typed settings built from Towny's
  `ConfigNodes`.
+ `PluginConfig` properties can now be assigned, which saves `config.yml`. `Main.reload()` applies the config and
  translations and is shared by `/tm reload` and the admin menu. Added `Lang.ids`.
