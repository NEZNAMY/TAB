# Content
* [About](#about)
* [Configuration](#configuration)
  * [Introduction](#introduction)
  * [Groups and users](#groups-and-users)
  * [Per-world / per-server](#per-world--per-server)
  * [Priority system](#priority-system)
  * [Placeholder support](#placeholder-support)
  * [Additional settings](#additional-settings)
* [Tips & Tricks](#tips--tricks)
* [API](#api)

# About
This feature allows you to configure player name formats in the tablist. It was added to minecraft in version 1.8. Versions 1.7 and lower only allow up to 16 characters including name, which is too limiting and not suppored by this plugin. If you wish to have prefix/suffix for 1.7 and lower, take advantage of [this client-sided mechanic](https://github.com/NEZNAMY/TAB/wiki/Client%E2%80%90sided-mechanics#nametag-format-in-tablist) and configure tagprefix/tagsuffix, which will appear in the tablist.  
![image](https://user-images.githubusercontent.com/6338394/205877929-4df82c78-9773-4614-b4a3-93e1673d6046.png)

Settings of this feature can be configured in **config.yml** under **tablist-name-formatting** section.

# Configuration
## Introduction
The display name is split into 3 parts for convenience:  
`tabprefix` - first part of the text, this is where prefix is usually configured  
`customtabname` - second part of the text, it will be used as player name. If customtabname is not defined at all (not just setting empty value!), player's original name will be used instead  
`tabsuffix` - last part of the text, this is where suffix is usually configured

These values are then merged together and displayed in the tablist.  
![image](https://user-images.githubusercontent.com/6338394/205877510-e40f93ce-80df-46f2-84f0-c113e0e813e8.png)

While you are free to use the 3 properties in any way you want since they are only split for convenience, it is still a good idea to use them for their designed purpose to not get lost a poor configuration. This means that the text which should appear before player's name should be put into `tabprefix`, any placeholder that is supposed to display player's name (such as from a nickname plugin) should go into `customtabname` and any text that should appear after player's name should be put into `tabsuffix`.

## Groups and users
Properties can be applied in 2 ways: groups and users. Users can be defined by both username and their UUID. Values applied to users take priority over groups.  
**groups.yml**
```
admin:
  tabprefix: "&4&lAdmin &r"
```
This can also be achieved with commands, in this case `/tab group admin tabprefix "&4&lAdmin &r"`.

**users.yml**
```
_NEZNAMY_:
  tabprefix: "&6&lTAB &r"

# An alternate way using UUID
237d8b55-3f97-4749-aa60-e9fe97b45062:
  tabprefix: "&6&lTAB &r"
```
This can also be achieved with commands, in this case `/tab player _NEZNAMY_ tabprefix "&6&lTAB &r"` or `/tab playeruuid _NEZNAMY_ tabprefix "&6&lTAB &r"`.

Properties can also be set as "default" for everyone who does not have them defined. For that purpose, a group keyword `_DEFAULT_` was made.  
**groups.yml**
```
admin:
  tabprefix: "&4&lAdmin &r"
_DEFAULT_:
  tabprefix: "&7" # This will be displayed on everyone except admin
```

## Per-world / per-server
Values can also be applied per-world (and per-server on bungeecord), where they can be defined per group/user. These values take priority over global settings. Example:  
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
For multiple worlds/servers to share the same settings, separate them with `;`.  
For worlds/servers starting with a specified text, use `*` after shared part. For ending with a shared part, use `*` at the beginning.  
Example:
```
per-world:
  world1;world2:
    _DEFAULT_:
      tabsuffix: "Shared tabsuffix in worlds world1 and world2"
  lobby-*:
    _DEFAULT_:
      tabsuffix: "Suffix in all worlds starting with lobby-"
```

## Priority system
Full list of priorities looks like this:
1. value set using the [API](#api)
2. per-world / per-server applied to username
3. value applied to username
4. per-world / per-server applied to uuid
5. value applied to uuid
6. per-world / per-server applied to player's group
7. per-world / per-server applied to group `_DEFAULT_`
8. value applied to player's group
9. value applied to group `_DEFAULT_`

This list is browsed through until the first match is found. If no match is found, empty value is used (except `customtabname`, which defaults to player's name).

Values are taken independently from each other. This means you can set per-world tabprefix, but only keep one global tabsuffix for example.

You can see source of a value displayed on player by using `/tab debug <player>` and checking "source" part of the value you are looking for.

## Placeholder support
All values fully support [TAB's internal placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#internal-placeholders) and [PlaceholderAPI placeholders](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders) including relational placeholders. Amount of placeholders is not limited and they can be used in combination with static text as well.

## Additional settings
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| enabled | true | Enables / Disables the feature |
| anti-override | true | When enabled, prevents other plugins from overriding TAB. This results in slightly higher CPU usage, but will make sure the function works as expected. <br />Unlike with teams, attempts to override TAB are not logged into anti-override.log file. This is due to high amount of false positives from TAB's own features, as well as bungeecord server switch, higher CPU usage and more. Whether you need this or not depends on how properly your plugins are configured. Some of them attempt to format tablist even if you'd not expect it from such plugin. Keep this enabled to stay safe. |
| disable-condition | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever. |

# Tips & Tricks
If you want TAB to only take prefixes/suffixes from permission plugin, delete all groups from **groups.yml** and only keep this:
```
_DEFAULT_:
  tabprefix: "%luckperms-prefix%"
  tabsuffix: "%luckperms-suffix%"
```

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

To get original value set by the plugin based on configuration:
* `TabListFormatManager#getOriginalPrefix(TabPlayer)`
* `TabListFormatManager#getOriginalName(TabPlayer)`
* `TabListFormatManager#getOriginalSuffix(TabPlayer)`

**Note**: These values are only temporary, meaning they won't get saved anywhere and will get reset on player quit or plugin reload. If you wish to save these values into file, use [commands](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-playergroupplayeruuid-name-property-value-options).  