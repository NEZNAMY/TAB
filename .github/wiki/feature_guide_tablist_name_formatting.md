# Content
* [About](#about)
* [Configuration](#configuration)
  * [config.yml](#configyml)
  * [groups.yml and users.yml](#groupsyml-and-usersyml)
    * [Format structure](#format-structure)
    * [Global settings](#global-settings)
    * [Per-world / per-server](#per-world--per-server)
    * [Priorities](#priorities)
* [Placeholders](#placeholders)
  * [PlaceholderAPI placeholders](#placeholderapi-placeholders)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
* [API](#api)
* [Examples](#examples)
  * [Example 1 - Taking prefix/suffix from permission plugin](#example-1---taking-prefixsuffix-from-permission-plugin)

# About
This feature allows you to configure player name formats in the tablist.
It was added to Minecraft in version 1.8.
Versions 1.7 and lower only allow up to 16 characters including name,
which is too limiting and not supported by this plugin.
If you wish to have prefix/suffix for 1.7 and lower,
take advantage of [this client-sided mechanic](https://github.com/NEZNAMY/TAB/wiki/Client%E2%80%90sided-mechanics#nametag-format-in-tablist) and configure tagprefix/tagsuffix,
which will appear in the tablist.  
![image](https://user-images.githubusercontent.com/6338394/205877929-4df82c78-9773-4614-b4a3-93e1673d6046.png)

# Configuration
This feature is configured in 2 parts based on what you are configuring. Some settings are in the **config.yml**, but prefix/suffix is configured in **groups.yml** and **users.yml**.
## config.yml
The first part can be configured in **config.yml** under **tablist-name-formatting** section. It controls everything except prefix and suffix.

This is how the default configuration looks:
```
tablist-name-formatting:
  enabled: true
  disable-condition: '%world%=disabledworld'
```
All of the options are explained in the following table.  
| Option name                 | Default value         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|-----------------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                     | true                  | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| disable-condition           | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

## groups.yml and users.yml
### Format structure
The display name is split into 3 parts for convenience:  
`tabprefix` - first part of the text, this is where prefix is usually configured  
`customtabname` - second part of the text, it will be used as player name. If customtabname is not defined at all (not just setting empty value!), player's original name will be used instead  
`tabsuffix` - last part of the text, this is where suffix is usually configured

These values are then merged and displayed in the tablist.  
![image](https://user-images.githubusercontent.com/6338394/205877510-e40f93ce-80df-46f2-84f0-c113e0e813e8.png)

You are free to use the 3 properties in any way you want since they are only split for convenience.
However, it is still a good idea to use them for their designed purpose to not get lost in a poor configuration.
This means that the text which should appear before player's name should be put into `tabprefix`,
any placeholder that is supposed to display player's name (such as from a nickname plugin)
should go into `customtabname` and any text that should appear after player's name should be put into `tabsuffix`.

### Global settings
Properties can be applied in 2 ways: groups and users. Users can be defined by both username and their UUID. Values applied to users take priority over groups.

**groups.yml**
```
admin:
  tabprefix: "&4&lAdmin &r"
```
Or with an in-game command `/tab group admin tabprefix "&4&lAdmin &r"`.

**users.yml**
```
_NEZNAMY_:
  tabprefix: "&6&lTAB &r"

# An alternate way using UUID
237d8b55-3f97-4749-aa60-e9fe97b45062:
  tabprefix: "&6&lTAB &r"
```
Or with in-game commands `/tab player _NEZNAMY_ tabprefix "&6&lTAB &r"` and `/tab playeruuid _NEZNAMY_ tabprefix "&6&lTAB &r"` (the player must be online for uuid option to work).

Properties can also be set as "default" for everyone who does not have them defined.
For that purpose, a group keyword `_DEFAULT_` was made.  
Example:  
**groups.yml**
```
admin:
  tabprefix: "&4&lAdmin &r"
_DEFAULT_:
  tabprefix: "&7" # This will be displayed on everyone except admin
```

### Per-world / per-server
Values can also be applied per-world (and per-server on a proxy), where they can be defined per group/user. These values take priority over global settings. Example:  
**groups.yml**
```
per-world:
  world1:
    _DEFAULT_:
      tabprefix: "&a" # Everyone in world "world1" will have this prefix
per-server:
  server1:
    _DEFAULT_:
      tabprefix: "&a" # Everyone in server "server1" will have this prefix
```

For multiple worlds/servers to share the same settings, separate them with a`;`.  
For worlds/server starting with a specified text, use `*` after shared part. For ending with a shared part, use `*` at the beginning.  
Example:
```
per-world:
  world1;world2:
    _DEFAULT_:
      tabsuffix: "Shared tagsuffix in worlds world1 and world2"
  lobby-*:
    _DEFAULT_:
      tabsuffix: "Suffix in all worlds starting with lobby-"
```

### Priorities
The full list of priorities to choose correct prefix/name/suffix for a player looks like this:
1. value set using the [API](#api)
2. per-world / per-server applied to username
3. value applied to username
4. per-world / per-server applied to uuid
5. value applied to uuid
6. per-world / per-server applied to player's group
7. per-world / per-server applied to group `_DEFAULT_`
8. value applied to player's group
9. value applied to group `_DEFAULT_`

This list is browsed through until the first match is found. If no match is found, empty value is used.

Values are taken independently of each other.
This means you can set per-world tabprefix, but only keep one global tabsuffix, for example.

You can see the source of a value displayed on player by using `/tab debug <player>` and checking "source"
part of the value you are looking for.

# Placeholders
This feature does not offer any internal placeholders, only PlaceholderAPI placeholders.

## PlaceholderAPI placeholders
Here are TAB's PlaceholderAPI placeholders you can use when this feature is enabled:
| Placeholder | Description |
|-------------|-------------|
| `%tab_tabprefix%` | Player's current tabprefix with placeholders parsed. |
| `%tab_customtabname%` | Player's current customtabname with placeholders parsed. |
| `%tab_tabsuffix%` | Player's current tabprefix with placeholders parsed. |
| `%tab_tabprefix_raw%` | Player's current raw tabprefix with placeholder identifiers. |
| `%tab_customtabname_raw%` | Player's current raw customtabname with placeholder identifiers. |
| `%tab_tabsuffix_raw%` | Player's current raw tabsuffix with placeholder identifiers. |

# Compatibility with other plugins
Tablist formatting is a feature that cannot be effectively handled by multiple plugins at once.
To make sure no other plugin sets tablist display names when not disabled in the plugin's config properly, TAB will block attempts to modify tablist display names coming from all other plugins (if this feature is enabled and not disabled for a player with a condition).

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `TabListFormatManager` instance. Get it using `TabAPI.getInstance().getTabListFormatManager()`. If this feature is disabled, the method will return `null`.

To set the values for the respective formatting, use the following:
* `TabListFormatManager#setPrefix(TabPlayer, String)`
* `TabListFormatManager#setName(TabPlayer, String)`
* `TabListFormatManager#setSuffix(TabPlayer, String)`

To reset the values, set them to `null`.

To get custom values previously set using the API (they will return `null` if no custom value is set):
* `TabListFormatManager#getCustomPrefix(TabPlayer)`
* `TabListFormatManager#getCustomName(TabPlayer)`
* `TabListFormatManager#getCustomSuffix(TabPlayer)`

To get the original value set by the plugin based on configuration:
* `TabListFormatManager#getOriginalRawPrefix(TabPlayer)` - Prefix with raw placeholder identifiers
* `TabListFormatManager#getOriginalRawName(TabPlayer)` - Name with raw placeholder identifiers
* `TabListFormatManager#getOriginalRawSuffix(TabPlayer)` - Suffix with raw placeholder identifiers
* `TabListFormatManager#getOriginalReplacedPrefix(TabPlayer)` - Prefix with all placeholders parsed
* `TabListFormatManager#getOriginalReplacedName(TabPlayer)` - Name with all placeholders parsed
* `TabListFormatManager#getOriginalReplacedSuffix(TabPlayer)` - Suffix with all placeholders parsed

**Note**: These values are only temporary, meaning they won't get saved anywhere and will get reset on player quit or plugin reload. If you wish to save these values into file, use [commands](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-playergroupplayeruuid-name-property-value-options).

# Examples
## Example 1 - Taking prefix/suffix from permission plugin
If you want TAB to only take prefixes/suffixes from the permission plugin,
delete all groups from **groups.yml** and only keep this:
```
_DEFAULT_:
  tabprefix: "%luckperms-prefix%"
  tabsuffix: "%luckperms-suffix%"
```
Or a PlaceholderAPI placeholder from your permission plugin if you use a different one.

> [!NOTE]
> When doing this, make sure you don't delete something you didn't mean to delete, for example ta**g**prefix and ta**g**suffix from nametag feature.