# TownyMenu - Change Log

## Unreleased

### New Features

#### Main Menu

+ Added a main menu, opened with `/townymenu` (`/tm`) or by pressing swap-hand (F) while sneaking.
    + The sneak + swap-hand shortcut can be turned off with `sneak-swap-hand-shortcut` in `config.yml`.
+ Added native confirm/cancel dialogs for Towny confirmations started from a menu.
+ Added native text-input dialogs for names, amounts, boards, and other values.

#### Town Menu

+ Added a town menu covering residents, ranks, titles, invites, kicking, and handing over the mayorship.
+ Added the town bank: deposit, withdraw, and bank history.
+ Added claim management: claim a chunk, an area, or enclosed gaps, claim outposts, unclaim, buy bonus claims, and
  toggle auto-claim.
+ Added town settings (PvP, mobs, fire, explosions, open, public, peaceful, percentage taxes).
+ Added town details: name, board, tag, taxes, plot prices, spawn cost, spawn, home block, outpost spawn, primary
  jail, map colour, and selling the town.
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

#### Misc

+ Added a chunk map around the player, coloured by relation, with claim and unclaim buttons.
+ Added an invites menu for town invites, nation invites, and alliance requests.
+ Buttons for actions you lack Towny permission for are shown greyed out.
+ Clicking the chat message prefix now opens the main menu.

### Technical Details

#### Misc

+ Set up the TownyMenu project from the Paper plugin template.
    + Added Towny as a required dependency.
    + Added build and release workflows.
    + Aligned the Adventure test dependencies with Paper 26.2 (5.2.0).
+ Replaced the template's auto-registered GUI system with a context-carrying menu framework (`Menu`, `PagedMenu`,
  `MenuActions`) that runs Towny commands and builds paged icons lazily.
+ Removed unused template systems: custom items, recipes, scheduled tasks, player and server data storage,
  `CountdownUtil`, `TeamUtil`, `TagUtil`, `PDCUtil`, `GameRuleUtil`, and the unused enums.
+ Added `TownyUtil` and `DialogUtil`, and trimmed the `itemStack` DSL and `PluginConfig` to what the plugin uses.
