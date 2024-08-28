# Content
* [About](#about)
* [Internal placeholders](#internal-placeholders)
    * [Universal](#universal)
    * [Backend only](#backend-only)
    * [BungeeCord only](#bungeecord-only)
* [PlaceholderAPI](#placeholderapi)
* [Relational placeholders](#relational-placeholders)
    * [About](#about-1)
    * [Usage](#usage)
* [Refreshing](#refreshing)
    * [Refresh intervals](#refresh-intervals)
    * [Sync refreshing](#sync-refreshing)
* [Placeholder is not working](#placeholder-is-not-working)
    * [Internal placeholder is not working](#internal-placeholder-is-not-working)
    * [PlaceholderAPI placeholder is not working](#placeholderapi-placeholder-is-not-working)
* [API](#api)
    * [Server placeholders](#server-placeholders)
    * [Player placeholders](#player-placeholders)
    * [Relational placeholders](#relational-placeholders-1)

# About
TAB offers various placeholders to display the most common information. It also supports [PlaceholderAPI placeholders](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders).

# Internal placeholders

Basic placeholders provided by the plugin. Most of these placeholders were added because they were requested by people who don't want to install another plugin just to show username. Some of them have configurable output in config.yml in `Placeholders` section. Unused placeholders do not affect performance in any way.

Placeholders must be refreshed. All internal placeholders have manually defined refresh interval, which I found the most optimal and cannot be changed. Time is in milliseconds. `-1` is used for placeholders which are in fact constants (such as %player%), or fire an event each time they change, which can be listened to (such as %world%) and value updated. Refresh interval for conditions is set to be equal to fastest refreshing placeholder used inside each condition.

## Universal
These placeholders are available on all platforms.

| Identifier  | Refresh interval | Description |
| ------------- | ------------- | ------------- |
| %online%         | 1000 | Total online player count on server where plugin is installed (excluding [vanished players](https://github.com/NEZNAMY/TAB/wiki/Additional-information#vanish-detection)). If the plugin is installed on a proxy, it will show total player count on the whole network. |
| %world%          | -1 | Name of world where player is |
| %worldonline%    | 1000 | Amount of players in the same world (excluding [vanished players](https://github.com/NEZNAMY/TAB/wiki/Additional-information#vanish-detection)) as the player |
| %ping%           | 500 | Player's ping to the server where TAB is installed. <br />**Note**: This value is measured by the server and TAB is only retrieving that value. If it's wrong, it's not a TAB issue. |
| %player%         | -1 | Player's name |
| %time%           | 500 | Current real time, output is configurable in **config.yml** in `time-format` option |
| %date%           | 60000 | Current date, output is configurable in **config.yml** in `date-format` option |
| %staffonline%    | 2000 | Amount of online players with `tab.staff` permission |
| %nonstaffonline% | 2000 | Amount of online players without `tab.staff` permission |
| %memory-used%    | 200 | Used RAM of server in MB |
| %memory-max%     | -1 | Total RAM of server in MB |
| %memory-used-gb% | 200 | Used RAM of server in GB |
| %memory-max-gb%  | -1 | Total RAM of server in GB |
| %player-version% | -1 | User-friendly version of player, such as `1.14.4`. For versions that share the same protocol version number, the higher one is returned (for example 1.20 & 1.20.1 -> 1.20.1 is returned) |
| %player-version-id% | -1 | Network ID of player's version. You can see the full version map on [fan wiki](https://wiki.vg/Protocol_version_numbers) for all versions or [TAB's source code](https://github.com/NEZNAMY/TAB/blob/master/shared/src/main/java/me/neznamy/tab/shared/ProtocolVersion.java) for releases only. Useful for conditions to create per-version results. |
| %luckperms-prefix%  | 1000 | Prefix from LuckPerms from the server where TAB is installed |
| %luckperms-suffix%  | 1000 | Suffix from LuckPerms from the server where TAB is installed |
| %displayname%       | 500 | Display name variable set by permission plugin, typically prefix + name + suffix |
| %group% | 1000 | Returns player's primary group name. Created for internal functionality, but can be used as a display placeholder as well. |
| %vanished% | 1000 | Returns `true`/`false` based on player's vanish status. Created for internal functionality, but can be used as a display placeholder as well. |
| %bedrock% | -1 | Returns `true`/`false` based on whether player is using Bedrock edition or not (Java) |
| %% | -1 | Returns the `%` symbol. Useful to display `%` symbol without breaking all placeholders after it |

## Backend only
These placeholders only work when TAB is installed on a backend server (Bukkit / Sponge / Fabric), they don't work when TAB is on BungeeCord. If you have TAB installed on BungeeCord and wish to use these, use their [PlaceholderAPI](https://github.com/NEZNAMY/TAB/wiki/Quick-PlaceholderAPI-startup-guide) alternative with [Bridge plugin](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) installed for PlaceholderAPI support on BungeeCord.
| Identifier  | Refresh interval | Description | PlaceholderAPI alternative |
| ------------- | ------------- | ------------- | ------------- |
| %health%            | 100 | Player's health, rounded up to match Minecraft's heart display. | %player_health_rounded% |
| %displayname%       | 500 | Player's display name value set by permission plugin. | %player_displayname% |
| %tps%               | 1000 | TPS of server from the last minute (measured by server, not available on Fabric as it doesn't measure the value) | %server_tps_1% |
| %mspt%              | 1000 | Server's current milliseconds per tick (Paper / Sponge 8+ / Fabric only) | - |

## BungeeCord only
| Identifier  | Refresh interval | Description |
| ------------- | ------------- | ------------- |
| %server%                    | -1 | Name of server where player is, defined in proxy's config. |
| %serveronline%              | 1000 | Amount of online players on server where the player is (excluding vanished players) |
| %online_\<servername\>%       | 1000 | Amount of online players on specified server (excluding vanished players) |
| %playerlist-group_\<group\>% | 1000 | Amount of online players in specified [global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) group (therefore the feature must be enabled) |

# PlaceholderAPI
PlaceholderAPI is supported and relational placeholders are supported too. When parsing relational placeholders, TAB puts viewer as the first player argument and target player as second argument.

You can find most of placeholders that PlaceholderAPI offers on [PlaceholderAPI wiki](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders). Some plugins are not listed there, for those you'll need to check their own wiki for placeholders.

TAB also offers its own placeholders into PlaceholderAPI. In order to enable TAB's expansion, set
```
placeholders:
  register-tab-expansion: true
```
in **config.yml**.  
Full list of placeholders:  
| Identifier  | Description |
| ------------- | ------------- |
| %tab_tabprefix% | Current tabprefix with replaced placeholders |
| %tab_tabsuffix% | Current tabsuffix with replaced placeholders |
| %tab_tagprefix% | Current tagprefix with replaced placeholders |
| %tab_tagsuffix% | Current tagsuffix with replaced placeholders |
| %tab_customtabname% | Current customtabname with replaced placeholders |
| %tab_customtagname% | Current customtagname with replaced placeholders |
| %tab_belowname% | Current belowname with replaced placeholders |
| %tab_abovename% | Current abovename with replaced placeholders |
| %tab_tabprefix_raw% | Raw tabprefix containing unparsed placeholders |
| %tab_tabsuffix_raw% | Raw tabsuffix containing unparsed placeholders |
| %tab_tagprefix_raw% | Raw tagprefix containing unparsed placeholders |
| %tab_tagsuffix_raw% | Raw tagsuffix containing unparsed placeholders |
| %tab_customtabname_raw% | Raw customtabname containing unparsed placeholders |
| %tab_customtagname_raw% | Raw customtagname containing unparsed placeholders |
| %tab_belowname_raw% | Raw belowname containing unparsed placeholders |
| %tab_abovename_raw% | Raw abovename containing unparsed placeholders |
| %tab_scoreboard_name% | Returns name of player's currently displayed scoreboard or empty string if none is displayed due to no display condition being met |
| %tab_scoreboard_visible% | "Enabled" if visible, "Disabled" if not |
| %tab_bossbar_visible% | "Enabled" if visible, "Disabled" if not |
| %tab_nametag_preview% | "Enabled" if previewing armor stands using /tab nametag preview, "Disabled" if not |
| %tab_nametag_visibility% | "Enabled" if player can see nametags, "Disabled" if disabled using /tab nametag toggle |
| %tab_replace_\<placeholder\>% | Applies [Placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements) to a PlaceholderAPI placeholder (for example %tab_replace_essentials_vanished%) |
| %tab_placeholder_\<placeholder\>% | returns value of tab's internal placeholder (such as %tab_placeholder_animation:name% for %animation:name%) |

# Relational placeholders
## About
Unlike classic placeholders which take either 0 (placeholders with same output for all players) or 1 (per-player placeholders) players, relational placeholders take in 2 players. The first usage was to display relation between two players, specifically enemies / allies (red / green) for factions, allowing players to see faction names of other players colored based on relation. While the usage is not limited to this, the name of the 2-player placeholder type has been established as "relational" and will be called that everywhere.

## Usage
TAB does not offer any internal relational placeholders. However, it has full support for PlaceholderAPI placeholders. You can also register your own relational placeholders using the [API](#relational-placeholders-1). In PlaceholderAPI, all relational placeholder identifiers must start with `%rel_`. This rule is adopted and used in TAB as well (including those registered via API).

When passing players into the function, first player is the player viewing the text, second player is the one text is being displayed on.  
Code wise, the PlaceholderAPI request function becomes
```
public String onPlaceholderRequest(Player viewer, Player target, String identifier)
```
where `viewer` is the player looking and `target` is the player the value is being displayed on.

When trying to use placeholders from other plugins, you will need to check their documentation and search for relational placeholders. If your plugin does not appear to offer any, you ran out of luck and TAB cannot do anything about it.  
In order to properly distinguish viewer/target (if needed), your relational placeholder expansion must follow the same order as TAB does. Since PlaceholderAPI never created any convention regarding this and instead calls the players `one` and `two`, I had to decide and this is the decision.

# Refreshing
## Refresh intervals
TAB is refreshing placeholders in intervals. For internal placeholders it's using values I found the most optimal and set directly in the plugin. Refresh intervals for PlaceholderAPI placeholders can be [configured](https://github.com/NEZNAMY/TAB/wiki/Optimizing-the-plugin#2---placeholder-refresh-intervals).

## Sync refreshing
By default, all placeholders are refreshed asynchronously so they don't slow down the server, since reading should be a thread-safe operation. Some placeholders, however, require to be refreshed in the main thread, causing problems or throwing errors when refreshed asynchronously. For these, replace them with `%sync:<original placeholder>%` and they will be refreshed in the main thread, for example `%server_total_entities%` -> `%sync:server_total_entities%`.  
Keep in mind that now you should configure reasonable refresh interval for these placeholders to not cause TPS drops (with async placeholders it doesn't really matter). Configured refresh intervals and CPU usage of placeholders can be checked using `/tab cpu`.

# Placeholder is not working
When using a placeholder, you should know if it's a [TAB's internal placeholder](#internal-placeholders) or a [PlaceholderAPI placeholder](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders). If you don't, that's the problem.

**Note:** Don't forget that using `%` symbol will mess up placeholder starts and ends, breaking all placeholders after it. To make the symbol display correctly without messing up placeholders, use `%%` to display the symbol.  
**Example:** `Sale 100% OFF!` -> `Sale 100%% OFF!`.

## Internal placeholder is not working
All TAB's internal placeholders are listed above. Some of them are bukkit only (meaning they won't work on bungeecord) and some are bungee only. **Trying to use a bukkit-only placeholder on bungeecord will not work.**  
If you want to use those, [set up PlaceholderAPI support on bungeecord](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) and find their PlaceholderAPI equivalent. Large portion of those are included on [PlaceholderAPI's wiki](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders).  
For example you can use %vault_prefix% instead of %vault-prefix%, %player_health% instead of %health% and so on.

## PlaceholderAPI placeholder is not working
If you have TAB installed on bungeecord, make sure you [set up PlaceholderAPI support on bungeecord](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) as there is no other way to retrieve data from technically a different server.

If you have TAB on bukkit or the previous step did not work, make sure it works when using `/papi parse me <placeholder>`. If it does not work, the issue is not on TAB's end.

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

For working with placeholders, you will need the `PlaceholderManager`. Get it using `TabAPI.getInstance().getPlaceholderManager()`.

Placeholders have two attributes:
* An identifier, which is used to uniquely identify the placeholder. This is also what's usually used for replacement. For example, in `%uptime%`, the identifier is `%uptime%`.
* A refresh rate in milliseconds, which is how often the placeholder's refresh function will be called, to get a new value to display. Use `-1` to make the placeholder never refresh periodically. This can be used for either constants or placeholders that update on events and you wish to update their value manually using functions in respective placeholder interface.

## Server placeholders
Server placeholders are global to the entire plugin. Their values are not dependent on player.  
Examples of server placeholders would be uptime and TPS placeholders.

To register a new server placeholder, use `PlaceholderManager#registerServerPlaceholder(String, int, Supplier<Object>)`. The first two parameters are explained above, and the last parameter is a function that will be called to refresh the value of the placeholder.
<details>
  <summary>Example</summary>

```
TabAPI.getInstance().getPlaceholderManager().registerServerPlaceholder("%system-time%", 50, () -> System.currentTimeMillis());
```

This placeholder will show current system time and update every 50 milliseconds.
</details>

## Player placeholders
Player placeholders are calculated on a per-player basis. These depend on the player that we want to get the value for.  
Examples of player placeholders would be nickname and prefix.

To register a new player placeholder, use `PlaceholderManager#registerPlayerPlaceholder(String, int, Function<TabPlayer, Object>)`. The first two parameters are explained above, and the last parameter is a function that will be called to refresh the value of the placeholder.
<details>
  <summary>Example</summary>

```
TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%player-uuid%", -1, player -> player.getUniqueId());
```

This placeholder will show player's UUID and never refreshes, since UUID doesn't change at runtime.
</details>

## Relational placeholders
Relational placeholders are calculated using a pair of players, instead of a single one. These depend on the player pair that we want to get the value for. Their identifier must start with `rel_`.

To register a new relational placeholder, use `PlaceholderManager#registerRelationalPlaceholder(String, int, BiFunction<TabPlayer, TabPlayer, Object>)`. The first two parameters are explained above, and the last parameter is a function that will be called to refresh the value of the placeholder.
<details>
  <summary>Example</summary>

```
TabAPI.getInstance().getPlaceholderManager().registerRelationalPlaceholder("%rel_staff_version%", 1000, (viewer, target) -> {
    if (viewer.hasPermission("tab.staff")) {
        return target.getVersion().getFriendlyName();
    } else {
        return "";
    }
});
```

This placeholder will show version of players, but only to those, who have `tab.staff` permission. First player in the method is viewer, second player is target. Same order is applied when hooking into PlaceholderAPI as well.
</details>