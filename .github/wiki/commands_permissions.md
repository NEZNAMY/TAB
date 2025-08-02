# About
This page lists all the commands and permissions that are available with TAB.
Commands have required permissions associated with them.

**BungeeCord / Velocity users who installed TAB on the proxy:**
* Use **/btab** instead of /tab. Permissions are the same.
* Permission nodes are checked on the server where the plugin is installed (by default). This means that with TAB on BungeeCord, permission nodes are checked on BungeeCord, and therefore you'll need a permission plugin there as well. If you wish to take permission groups and checks from backend servers instead, set `use-bukkit-permissions-manager: true` in config.
* Giving yourself OP on the backend server does not give you permissions on the proxy. It's a completely different server. You'll need to give yourself permissions using a permission plugin installed on the proxy.
* You are not able to execute proxy commands using a backend plugin (for example some menu plugin). If you want to achieve this, you'll need to find an updated plugin that allows you to send commands to the proxy.

# Content
* [Configuration](#configuration-commands)
  * [/tab \<player/group/playeruuid\> \<name\> \<property\> \[value\] \[options\]](#tab-playergroupplayeruuid-name-property-value-options)
  * [/tab \<player/group/playeruuid\> \<name\> remove](#tab-playergroupplayeruuid-name-remove)
  * [/tab reload](#tab-reload)
  * [/tab debug \[player\]](#tab-debug-player)
  * [/tab group \<group\>](#tab-group-group)
  * [/tab groups](#tab-groups)
* [Bossbar](#bossbar)
  * [/tab bossbar send \<name\> \[player\]](#tab-bossbar-show-name-player)
  * [/tab bossbar \[on/off/toggle\] \[player\] \[options\]](#tab-bossbar-onofftoggle-player-options)
  * [/tab bossbar announce \<name\> \<length\>](#tab-bossbar-announce-name-duration)
* [Scoreboard](#scoreboard)
  * [/tab scoreboard show \<name\> \[player\]](#tab-scoreboard-show-name-player)
  * [/tab scoreboard \[on/off/toggle\] \[player\] \[options\]](#tab-scoreboard-onofftoggle-player-options)
  * [/tab scoreboard announce \<name\> \<length\>](#tab-scoreboard-announce-name-duration)
* [Nametags / Teams](#nametags--teams)
  * [/tab nametag <show/hide/toggle> \[player\] \[viewer\] \[options\]](#tab-nametag-showhidetoggle-player-viewer-options)
  * [/tab nametag <showview/hideview/toggleview> \[viewer\] \[options\]](#tab-nametag-showviewhideviewtoggleview-viewer-options)
  * [/tab setcollision <player> <true/false>](#tab-setcollision-player-truefalse)
* [MySQL](#mysql)
  * [/tab mysql upload](#tab-mysql-upload)
  * [/tab mysql download](#tab-mysql-download)
* [Other](#other)
  * [/tab cpu](#tab-cpu)
  * [/tab parse \<player\> \<placeholder\>](#tab-parse-player-text)
* [Additional permissions](#additional-permissions)

# Configuration commands
## /tab \<player/group/playeruuid\> \<name\> \<property\> \[value\] \[options\]
* **Permission:** `tab.change.<property>`
* **Description:** Changes a property of a group/player to the given value. No value argument will result in that property being deleted.

#### Properties
* For tablist: `tabprefix`, `customtabname`, and `tabsuffix`.
* For nametag: `tagprefix` and `tagsuffix`.

#### Options
* `-s <server>` - Applying value only for a specific server
* `-w <world>` - Applying value only for a specific world

#### Names for each command type
* player: Supports both player name and raw uuid. Player doesn't need to be online.
* playeruuid: Requires name of an online player. Equal to using `player` with player's uuid.
* group: Using group names from permission plugin. `_DEFAULT_` is used for default settings for all groups.

## /tab \<player/group/playeruuid\> \<name\> remove
* **Permission:** `tab.remove`
* **Description:** Removes all direct data from the given player/group.

#### Notes
* The `playeruuid` argument requires the name of an **online player** to remove data from.

## /tab reload
* **Permission:** `tab.reload`
* **Description:** Unloads the plugin, loads configuration files including changes and loads the plugin.

## /tab debug [player]
* **Permission:** `tab.debug`
* **Description:** Shows the server version, plugin version, permission group choice logic, and sorting type. If player argument is filled, shows info about that player:
  * On BungeeCord, shows whether player is connected to the backend server with [Bridge](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) plugin or not.
  * Sorting value & explanation, useful to see what went wrong if players are not sorted correctly.
  * Primary group set using [How to assign players into groups](https://github.com/NEZNAMY/TAB/wiki/How-to-assign-players-into-groups)
  * List of all configured properties, their values and source.

## /tab group \<group\>
* **Permission:** `tab.groupinfo`
* **Description:** Shows all settings applied to the specified group, both global and per-world / per-server. Handy when plugin is connected to MySQL, where you have no other way to effectively check group settings.

## /tab groups
* **Permission:** `tab.grouplist`
* **Description:** Shows a list of all groups that have anything assigned to them, either globally or per-world / per-server. Handy when plugin is connected to MySQL, where you have no other way to effectively check your groups.

# Scoreboard
## /tab scoreboard show \<name\> \[player\]
* **Permission:** `tab.scoreboard.show` for showing to yourself, `tab.scoreboard.show.other` for showing to others.
* **Description:** Shows the scoreboard with the given `name`, either to yourself if no `player` was given, or to the given `player`.

## /tab scoreboard \[on/off/toggle\] \[player\] \[options\]
* **Permission:** `tab.scoreboard.toggle` for toggling for yourself, `tab.scoreboard.toggle.other` for toggling for others.
* **Description:** Shows / hides / toggles scoreboard of specified player. If no player was given, command affects the sender.
* **Options:**
  * `-s` for silent toggling (no chat message for affected player)

## /tab scoreboard announce \<name\> \<duration\>
* **Permission:** `tab.announce.scoreboard`
* **Description:** Shows the scoreboard with the given `name` to every player on the server for the given `duration`, in seconds.

# Bossbar
## /tab bossbar show \<name\> \[player\]
* **Permission:** `tab.bossbar.show` for showing to yourself, `tab.bossbar.show.other` for showing to others.
* **Description:** Shows the bossbar with the given `name`, either to yourself if no `player` was given, or to the given `player`.

## /tab bossbar \[on/off/toggle\] \[player\] \[options\]
* **Permission:** `tab.bossbar.toggle` for toggling for yourself, `tab.bossbar.toggle.other` for toggling for others.
* **Description:** Shows / hides / toggles bossbar of specified player. If no player was given, command affects the sender.
* **Options:**
  * `-s` for silent toggling (no chat message for affected player)

## /tab bossbar announce \<name\> \<duration\>
* **Permission:** `tab.announce.bar`
* **Description:** Shows the bossbar with the given `name` to every player on the server for the given `duration`, in seconds.

# Nametags / Teams
## /tab nametag <show/hide/toggle> \[player\] \[viewer\] \[options\]
* **Permission:** `tab.nametag.visibility` (`tab.nametag.visibility.other` for toggling for other players)
* **Description:** Shows / hides / toggles nametag of a specified player. If viewer is specified, view is only affected for the viewer.
* **Options:**
  * `-s` for silent toggling (no chat message for affected player)

## /tab nametag <showview/hideview/toggleview> \[viewer\] \[options\]
* **Permission:** `tab.nametag.view` (`tab.nametag.view.other` for toggling for other players)
* **Description:** Shows / hides / toggles nametag VIEW a specified player.
* **Options:**
  * `-s` for silent toggling (no chat message for affected player)

## /tab setcollision \<player\> \<true|false\>
* **Permission:** `tab.setcollision`
* **Description:** Forces collision rule for specified player, overriding configuration.

# MySQL
## /tab mysql upload
* **Permission:** `tab.mysql.upload`
* **Description:** Uploads all data from users.yml and groups.yml to MySQL. MySQL must be enabled and connected for this command to work.

## /tab mysql download
* **Permission:** `tab.mysql.download`
* **Description:** Downloads all data to users.yml and groups.yml from MySQL. MySQL must be enabled and connected for this command to work.

# Other
## /tab cpu
* **Permission:** `tab.cpu`
* **Description:** Shows approximate CPU usage of the plugin from the last 10 seconds. There are parts that are impossible to be measured, so this shows slightly less than the real value is. The content is self-explanatory. All major ways to decrease CPU usage can be found at [Optimizing the plugin](https://github.com/NEZNAMY/TAB/wiki/Optimizing-the-plugin).

## /tab parse \<player\> \<text\>
* **Permission:** `tab.parse`
* **Description:** Replaces all placeholders (both the plugin's internal ones, and those from PlaceholderAPI if it is installed) in the given `text`. It can be used to verify if a placeholder works as expected.

# Additional permissions
* `tab.admin` - Allows the player to execute all commands.
* `tab.bypass` - If the per-world player list is enabled as well as this permission, it allows the player to see everyone on the server, regardless of what the per-world player list settings allow. For example, if your per-world player list is set up to isolate worlds A and B, players with this permission will see all players from both A and B on their player list, whereas players that don't have the permission will only see either players from world A or players from world B, depending on what world they are in.
* `tab.staff` - Allows the player to be counted in the `%staffonline%` placeholder.
* `tab.spectatorbypass` - If enabling bypass permission in [Spectator fix](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Spectator-fix), this is the permission.
* `tab.tabcomplete` - Allows the player to auto-complete the `/tab` command.
* `tab.seevanished` - Allows the player to see other vanished players on the [Global Playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) and [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout).  