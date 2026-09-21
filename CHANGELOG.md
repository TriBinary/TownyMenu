# TownyMenu - Change Log

## Unreleased

## Version 1.1.0

### Improvements

#### Menus

+ Every menu has a new look inspired by Hypixel SkyBlock. Values are coloured by what they are — money in gold, counts
  in green, dates and times in yellow, nations in aqua, anything dangerous in red — and descriptions and tutorial lessons
  pick out their key terms, such as PvP, spawns, outposts, and upkeep, in the same colours everywhere.
+ Every button now tells you what clicking it does, in lines like "Click to open!", "Left-click to edit!", and
  "Right-click to clear!": yellow for a click or left-click, aqua for a right-click, and red when the click deletes or
  removes something. Buttons that did two things by button, such as editing or clearing a board, used to explain it in
  their description; the hints now sit in their own block at the bottom.
+ Switches show a bold ENABLED or DISABLED status and say "Click to enable!" or "Click to disable!" instead of "Click
  to toggle", and permission cells say "Click to allow!" or "Click to deny!".
+ Progress bars show how far along you are: claims used against the claim limit, outposts, progress towards the next
  town or nation level, and lessons read in the tutorial.
+ Menus have a black glass background so the icons stand out, the back button is now "Go Back" and names the menu it
  returns to, and page arrows name the page they lead to.
+ Leaderboards mark the top three with gold, silver, and bronze ranks, sort buttons point at the current order with an
  aqua arrow, and lists of pending invites use bullet points.
+ Server owners who edited their language files in `plugins/TownyMenu/lang/` keep their own text and colours. To get
  the new look, move your edits aside and let TownyMenu copy the new files on the next start; the header of
  `en_US.yml` lists the colour palette.

#### Misc

+ The sort button in the town and nation directories now goes both ways: left-click moves to the next sort order and
  right-click goes back to the previous one, so you no longer have to click all the way around to reach the order you
  just passed.

### Technical Details

#### Misc

+ TownyMenu now builds against Minecraft 26.3 (Paper API 26.3). Run it on a Paper 26.3 server.
+ Menus gain `hints(...)` for click hints from the new shared `click.*` translation keys, `progressBar(current, max)`,
  `pageArrow(...)`, and a public `title`. The old `common.click-*` keys, `icon.currently`, and `icon.click-toggle` are
  gone; the developer guide documents the click-hint rules and the colour palette.

## Version 1.0.2

### New Features

#### Bank Menu

+ Deposit and Withdraw now open a menu of amounts instead of going straight to a typing dialog: the preset amounts the
  server offers, an Everything button, and Custom Amount for typing your own figure.
+ Each amount shows whether it will actually work. An amount you cannot afford, or one outside the minimum and maximum
  Towny allows, says so instead of offering a click, so a button that would only earn an error is never offered.
+ The amount menu shows the bank balance, your own balance, and Towny's deposit or withdrawal limits when the server
  sets any.

### Improvements

#### Bank Menu

+ The Deposit, Withdraw, and Bank History buttons now have a blank row above and below them instead of being wedged
  between the bank icon and the bottom row, so the bank details, the actions, and the navigation read as three separate
  blocks. The menu is one row taller for it, and the amount menu is laid out the same way.

#### Admin Menus

+ TownyMenu Settings can edit the bank menu's preset amounts.

### Technical Details

#### Misc

+ New `bank-amounts` setting in `config.yml` lists the preset amounts the deposit and withdraw menus offer. Amounts that
  are zero or negative are dropped, duplicates removed, and the rest sorted; up to nine fit in the menu row. An empty
  list leaves players with only Everything and Custom Amount.

## Version 1.0.1

### Improvements

#### Town Menu

+ Claiming, unclaiming, filling gaps, and taking over a claim now show what this server charges — or refunds — per
  chunk, and Claim Outpost shows the outpost price.
+ Go to Spawn and Outposts show what the trip costs you, and each outpost in the outpost list shows it too.
+ Reclaiming ruins no longer shows a cost line on servers where reclaiming is free.
+ Renaming a town and changing its map colour show their cost.
+ Visiting another town shows what it will actually charge you, which depends on whether you are a resident, a nation
  member, an ally, or an outsider. Merging another town into yours shows what the merge costs.

#### Nation Menu

+ Go to Spawn and visiting another nation show what the trip costs you.
+ Renaming a nation, changing its map colour, and moving the capital show their cost.

#### Plot Menu

+ Claiming the wilderness you stand in shows the claim price, and Claim as Outpost shows the outpost price.
+ Change Plot Type now shows what each type costs, multiplied across the group when the plot is in one.

#### Resident Menu

+ Go to Spawn shows what the trip costs you.

#### Misc

+ Prices are hidden on servers with no economy, and free actions no longer carry an empty cost line.
+ Icon tooltips are easier to read: a blank line now sits under every icon's name, and its description, details, and
  costs are separated into blocks instead of running together.
+ What a click does is now set off at the bottom of every tooltip by a divider line, so an icon's actions are easy to
  spot without reading the whole tooltip.
+ Lines that pack two or three values together — Open and Public, PvP and Mobs, Allies and Enemies — now separate them
  with a dot instead of a gap, so the pairs no longer blur into one another.

### Technical Details

#### Build

+ `copyPlugin` now deletes older `TownyMenu-*.jar` files from `run/plugins/` before copying the new build, so bumping
  the version no longer leaves the test server with two copies of the plugin. The same cleanup for Towny's own jar now
  runs even when Gradle considers the copy up to date.

#### Misc

+ Added a `Prices` utility that works out what a menu action costs — claims, outposts, spawn travel, plot types, town
  merges — from Towny's config and the town or nation involved, and `Menu.costLine` / `Menu.priceLine` to turn an amount
  into lore.
+ `Pickers.option` takes a `lines` lambda for per-option lore.
+ Added `LoreBlocks`, which lays an icon's lore out in blocks: a blank line under the display name, single separators
  between blocks, a divider rule sized to the longest line above the actions, and no separator left dangling at either
  end. The `itemStack` DSL exposes it as `loreBreak()` and `loreActions(...)`.
+ `Icons.icon`, `toggle`, `resident`, `town`, and `nation` take a named `actions` list for click hints and separate
  their own lore blocks, so every menu spaces its icons the same way without repeating `""` separators at call sites.
+ `LoreUtil.columns` exposes the column width of a string, the measure `wrapLore` already wrapped on.

## Version 1.0.0

### New Features

#### Misc

+ Open menus now update themselves when Towny changes what they show: residents joining or leaving, land being claimed,
  ranks, bank transactions, plot settings, deletions, and the new day. Turn this off with `live-menu-refresh`.
+ TownyMenu now announces in chat when you are invited to a town, your town is invited to a nation, your nation is asked
  for an alliance, or your town goes bankrupt or falls into ruin. Each message opens the menu that answers it. Turn this
  off with `towny-alerts`.
+ Added `/townymenu invites`, which opens the invites menu.

#### Main Menu

+ Added Leaderboards: towns and nations ranked by residents, land, and bank balance, players ranked by the plots they
  own, and the richest residents of your town and nation.
+ Added Prices & New Day: a countdown to the next Towny day and what the server charges for founding, claiming, upkeep,
  plots, and taxes.

#### Town Menu

+ Clicking your town at the top of the town menu opens its status: level and what the next level needs, claim and
  outpost limits, the nation zone, and the upkeep due on the next new day.
    + Warnings for ruins, bankruptcy, conquest, overclaiming, and upkeep the bank cannot cover are shown there and on
      the town icon.

+ Added a town jail menu: see prisoners and release them, jail online residents with a chosen sentence, bail, jail, and
  cell, and list the town's jails.
+ Added Cede This Plot and Take Over Claim to the claims menu, for giving a chunk to another town and taking land from
  an overclaimed town.
+ Added Merge into Your Town to other towns' pages, for mayors asking another town to merge into theirs.
+ Added Announce to Town to the residents menu.
+ Added a Nation Zone switch to town settings for towns that have a nation zone.
+ Residents of a ruined town can reclaim it from the town menu, and players without a town can reclaim ruins from Find a
  Town when the server allows it.

#### Nation Menu

+ Clicking your nation at the top of the nation menu opens its status: level, what the next level needs, the bonuses
  member towns receive, and the nation's daily upkeep.
+ Added sanctioned towns, under the nation's Towns menu: sanction towns outside the nation and lift sanctions.
+ Added Announce to Nation to the nation menu.

#### Plot Menu

+ Added Join-Day Limits, setting the fewest and most days a buyer must have lived in the town.
+ Added Jail Cells to jail plots, for adding and removing cells where you stand.
+ Added Player Overrides to plot permissions: add or remove players with their own permissions on a plot, and open
  Towny's editor to change them.
+ Right-clicking Evict Owner now evicts the owner and puts the plot back up for sale.
+ Added Many Plots at Once: buy, sell, stop selling, or give up every plot in a square or circle around you, or give up
  all your plots.
+ Added Make Outpost and Move Outpost Spawn for your town's claims.
+ Added a Plot HUD switch, and clicking the plot icon shows Towny's plot info in chat.
+ Right-clicking Rename Plot removes the plot's name.
+ Putting a plot up for sale now suggests the town's price for that plot type instead of 0.

#### Map

+ Right-clicking a plot for sale on the map buys it without walking there.

#### Resident Menu

+ Added My Plots, Daily Tax, Outlawed In, and Trusted In to the profile.
+ Added Display Modes to the profile, turning every mode off or restoring the server's defaults.
+ Added Chat Spy and Disable Admin Powers to personal settings for players allowed to use them.

#### Admin Menus

+ Added More Town Tools: town level, merging another town in, putting a town up for sale, removing conquered status,
  checking outposts, resident ranks, outlaws, trusted residents, and trusted towns.
+ Added More Nation Tools: nation level, merging, moving a town in, rechecking town distances, sanctioned towns, and
  resident ranks.
+ Added a plot menu for the chunk you stand in: claim it for a player, move it to another town, trust players, and
  unclaim.
+ Added a Towny Permissions editor for `townyperms.yml`: permission groups and their nodes, and the town and nation
  ranks mayors and leaders can hand out, including adding, removing, and renaming ranks.
+ Added purging inactive residents, checking a player's permission node, and depositing into every bank to the server
  menu.

#### Tutorial

+ Added lessons on announcements, merging towns, ceding land, taking over claims, many plots at once, join-day limits,
  player overrides, jailing players, nation sanctions, town and nation levels, your plots and taxes, leaderboards, and
  prices.
    + The ruins, plot selling, plot groups, and display lessons now cover reclaiming, evicting for resale, group-wide
      settings, and display modes.
    + The map, plot, buying, and outpost lessons now cover buying from the map, the Plot HUD, and making outposts.

### Improvements

#### Town Menu

+ The town directory can be sorted by distance from you, and then shows how far away each town's spawn is.

#### Misc

+ Menus now leave an empty row between their buttons and the bottom row holding the back button.
    + Lists show four rows of entries per page instead of five.

### Fixes

#### Plot Menu

+ Fixed selling, plot type, settings, permissions, and trust failing on plots in a plot group; on grouped plots they now
  change the whole group, and their buttons say so.

#### Town Menu

+ Fixed the second row of the claims menu starting one slot too far right.

#### Misc

+ Fixed the back button covering the Outsiders: Switch button in the permissions menu.
+ Fixed a menu reopening over, or closing, another menu opened while a command was still finishing.
+ Fixed clicking a button twice quickly running its command twice, such as toggling PvP on and straight back off.
+ Fixed pressing Escape in a text dialog leaving no menu open; the dialog now closes only with Confirm or Cancel.
+ Removed the empty line at the end of the Visit, Found a Town, and Found a Nation icons.
+ Fixed a leading space in a resident's full name when they have a surname but no title.
+ Town and nation boards and resident about texts no longer show Towny's placeholder (`/town set board [msg]`,
  `/res set about [msg]`) when none has been set, and the edit dialogs start empty instead of with the placeholder.

### Technical Details

#### Misc

+ `PermissionMenu` takes an optional `overrides` menu, and a `Toggle` with a `null` node is shown unguarded.
+ `TownyUtil` gained `duration` and `secondsUntilNewDay` for Towny's new-day countdown.
+ `ToggleMenu` splits more than 21 toggles into pages instead of asking for an inventory taller than six rows.
+ `Layout.add` logs and skips buttons beyond its slots instead of throwing, so an overfull grid no longer breaks a menu.

## Version 0.1.1

### Technical Details

#### Misc

+ Releases are now published by the repository a version bump is merged into, instead of by pushing a tag.
    + The release workflow creates the `vX.Y.Z` tag itself, skips forks, and can be re-run by hand.

## Version 0.1.0

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
