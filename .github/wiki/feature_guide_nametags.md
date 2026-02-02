# Content
* [About](#about)
* [Configuration](#configuration)
  * [config.yml](#configyml)
  * [groups.yml and users.yml](#groupsyml-and-usersyml)
    * [Global settings](#global-settings)
    * [Per-world / per-server](#per-world--per-server)
    * [Priorities](#priorities)
  * [Name color](#name-color)
* [Commands](#commands)
* [Placeholders](#placeholders)
  * [PlaceholderAPI placeholders](#placeholderapi-placeholders)
* [Limitations](#limitations)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
* [Additional info](#additional-info)
  * [Additional note 1 - NPC (in)compatibility](#additional-note-1---npc-incompatibility)
  * [Additional note 2 - Prefix/suffix on pets](#additional-note-2---prefixsuffix-on-pets)
  * [Additional note 3 - Changing name itself](#additional-note-3---changing-name-itself)
  * [Additional note 4 - F1 view](#additional-note-4---f1-view)
  * [Additional note 5 - Transparent players](#additional-note-5---transparent-players)
  * [Additional note 6 - Vanish hook](#additional-note-6---vanish-hook)
* [API](#api)
  * [Changing prefix and suffix](#changing-prefix-and-suffix)
  * [Collision](#collision)
  * [Manipulating visibility](#manipulating-visibility)
  * [Disabling team handling](#disabling-team-handling)
* [Examples](#examples)
  * [Example 1 - Taking prefix/suffix from permission plugin](#example-1---taking-prefixsuffix-from-permission-plugin)
  * [Example 2 - Making nametags invisible on a subserver](#example-2---making-nametags-invisible-on-a-subserver)

# About
Nametags are controlled by a feature called scoreboard teams. They offer 6 properties:
* Team name - used for sorting players in tablist, see [Sorting guide](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist) for more info
* Prefix - prefix displayed in nametag, it will be referred to as `tagprefix`
* Suffix - suffix displayed in nametag, it will be referred to as `tagsuffix`
* Nametag visibility rule
* Collision rule
* Team color (1.13+) - used to set name and glow color (check out [How to make TAB compatible with glow plugins](https://github.com/NEZNAMY/TAB/wiki/How-to-make-TAB-compatible-with-glow-plugins)) (on 1.12- it uses last color of prefix).
* Options - AllowFriendlyFire and CanSeeFriendlyInvisibles

When enabling this feature, TAB will control all of these. It is not possible to take values from 2 different teams (plugins). Most of the compatibility problems with other plugins can be solved with placeholders. If you want sorting but not nametags, just don't configure any prefix/suffix. If you want another plugin to handle teams, configure sorting in that plugin.

# Configuration
This feature is configured in 2 parts based on what you are configuring. Most settings are in the **config.yml**, but prefix/suffix is configured in **groups.yml** and **users.yml**.
## config.yml
The first part can be configured in **config.yml** under **scoreboard-teams** section. It controls everything except prefix and suffix.

This is how the default configuration looks (excluding sorting options):
```
scoreboard-teams:
  enabled: true
  enable-collision: true
  invisible-nametags: false
  can-see-friendly-invisibles: false
  disable-condition: '%world%=disabledworld'
```
All of the options are explained in the following table.  
| Option name                 | Default value         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|-----------------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                     | true                  | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| enable-collision            | true                  | Controls collision rule. Available values are: <br />- `true` - Collision will be enabled permanently. <br />- `false` - Collision will be disabled permanently. <br /> - *Conditional expression* - Collision will be enabled if player meets the [short format](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders#short-format) conditional expression (for example `%world%=world` will result in collision only being enabled for players in world `world`). <br /> - *Condition name* - Uses name of a [defined condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for a player to have collision enabled.                                                                                        |
| invisible-nametags          | false                 | Controls nametags visibility rule. Available values are: <br />- `true` - Nametags will be invisible. <br />- `false` - Nametags will be visible. <br /> - *Conditional expression* - Nametags will be invisible if player meets the [short format](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders#short-format) conditional expression. <br /> - *Condition name* - Uses name of a [defined condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders). <br /> Option to hide nametags was added in Minecraft **1.8**, therefore it will not work on 1.7. |
| can-see-friendly-invisibles | false                 | Controls value of the team flag. It allows you to see invisible players in the same team as transparent instead of completely invisible. Since TAB places each player into an individual team, this option will only take effect in combination with plugins that spawn a dummy clone of the player (sit or disguise plugins).                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| disable-condition           | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

## groups.yml and users.yml
This part allows you to configure prefix and suffix. They are called `tagprefix` and `tagsuffix`, where "tag" refers to name tag, to allow separation from tablist settings.

### Global settings
Properties can be applied in 2 ways: groups and users. Users can be defined by both username and their UUID. Values applied to users take priority over groups.

**groups.yml**
```
admin:
  tagprefix: "&4&lAdmin &r"
```
Or with an in-game command `/tab group admin tagprefix "&4&lAdmin &r"`.

**users.yml**
```
_NEZNAMY_:
  tagprefix: "&6&lTAB &r"

# An alternate way using UUID
237d8b55-3f97-4749-aa60-e9fe97b45062:
  tagprefix: "&6&lTAB &r"
```
Or with in-game commands `/tab player _NEZNAMY_ tagprefix "&6&lTAB &r"` and `/tab playeruuid _NEZNAMY_ tagprefix "&6&lTAB &r"` (the player must be online for uuid option to work).

Properties can also be set as "default" for everyone who does not have them defined.
For that purpose, a group keyword `_DEFAULT_` was made.  
Example:  
**groups.yml**
```
admin:
  tagprefix: "&4&lAdmin &r"
_DEFAULT_:
  tagprefix: "&7" # This will be displayed on everyone except admin
```

### Per-world / per-server
Values can also be applied per-world (and per-server on a proxy), where they can be defined per group/user. These values take priority over global settings. Example:  
**groups.yml**
```
per-world:
  world1:
    _DEFAULT_:
      tagprefix: "&a" # Everyone in world "world1" will have this prefix
per-server:
  server1:
    _DEFAULT_:
      tagprefix: "&a" # Everyone in server "server1" will have this prefix
```

For multiple worlds/servers to share the same settings, separate them with a`;`.  
For worlds/server starting with a specified text, use `*` after shared part. For ending with a shared part, use `*` at the beginning.  
Example:
```
per-world:
  world1;world2:
    _DEFAULT_:
      tagsuffix: "Shared tagsuffix in worlds world1 and world2"
  lobby-*:
    _DEFAULT_:
      tagsuffix: "Suffix in all worlds starting with lobby-"
```

### Priorities
The full list of priorities to choose correct prefix/suffix for a player looks like this:
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
This means you can set per-world tagprefix, but only keep one global tagsuffix, for example.

You can see the source of a value displayed on player by using `/tab debug <player>` and checking "source"
part of the value you are looking for.

## Name color
On versions 1.12.2 and below, name color follows the last color of prefix. This also includes magic codes. On 1.13+, this is no longer the case. Instead, a new field called "team color" was created. This is an enum constant, where we need to pick from 22 options - 16 colors, 5 magic codes and reset. **As you can see, it doesn't allow for RGB colors**. This also means name can no longer have color and magic codes simultaneously.

TAB does not have an option for configuring team color to avoid complexity, such as per-version configuration, or having to configure an additional field.
For that reason, TAB detects the last used color code and uses that for team color.
> [!CAUTION]
> An exception is when using MiniMessage syntax / using RGB codes on server which has MiniMessage library. In this scenario, it is not possible to detect reset (`&r`) as last used "color", because in Minecraft components it is not a color, rather, it resets formatting by setting all magic codes to false / color to null. If you use `&r` (`<reset>`) and notice it not working, replace it with `&f` (`<white>`) (or add it after the reset to be safe). There seems to be no difference between setting color to `WHITE` versus `RESET`.

# Commands
| Command                                                            | Permission                                                                                 | Description                                                                                                                                                                                                |
|--------------------------------------------------------------------|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /tab nametag <show/hide/toggle> \[player\] \[viewer\] \[options\]  | `tab.nametag.visibility` (for yourself) <br /> `tab.nametag.visibility.other` (for others) | Shows / hides / toggles nametag of a specified player. If viewer is specified, view is only affected for the viewer. You can use `-s` as option for silent toggling (no chat message for affected player). |
| /tab nametag <showview/hideview/toggleview> \[viewer\] \[options\] | `tab.nametag.view` (for yourself) <br /> `tab.nametag.view.other` (for others)             | Shows / hides / toggles nametag VIEW a specified player. You can use `-s` as option for silent toggling (no chat message for affected player)                                                              |
| /tab setcollision \<player\> \<true\|false\>                       | `tab.setcollision`                                                                         | Forces collision rule for specified player, overriding configuration.                                                                                                                                      |

# Placeholders
This feature does not offer any internal placeholders, only PlaceholderAPI placeholders.

## PlaceholderAPI placeholders
Here are TAB's PlaceholderAPI placeholders you can use when this feature is enabled:
| Placeholder | Description |
|-------------|-------------|
| `%tab_tagprefix%` | Player's current tagprefix with placeholders parsed. |
| `%tab_tagsuffix%` | Player's current tagprefix with placeholders parsed. |
| `%tab_tagprefix_raw%` | Player's current raw tagprefix with placeholder identifiers. |
| `%tab_tagsuffix_raw%` | Player's current raw tagsuffix with placeholder identifiers. |
| `%tab_nametag_visibility%` | "Enabled" if player can see nametags, "Disabled" if disabled using `/tab nametag toggleview` |

# Limitations
* [1.12.2-] Prefix/suffix length is limited to 16 characters (including color codes). Any characters beyond that will be cut to prevent players getting disconnected.
* [1.13+] Name color is controlled by a chat format enum constant. This means:
  * Name can only have one code. That is either color or magic code (such as &4 or &l), but not both.
  * Name does not support RGB codes. Any used RGB colors will be rounded to the nearest legacy code.
* The same value manages name color and glow color, which means they cannot be different.
* The name cannot be effectively changed and the plugin doesn't offer it.

If you want to avoid these at all costs, the only way is to create entities (armor stands / display) and make them ride the player (or teleport). TAB no longer offers this functionality due to a large amount of unfixable issues (full thread [here](https://gist.github.com/NEZNAMY/f4cabf2fd9251a836b5eb877720dee5c/)). You can, however, check out these 3rd party plugins (**not affiliated with me in any way, use at your own risk**):
* [UnlimitedNameTags](https://www.spigotmc.org/resources/unlimitednametags.117526/)
* [DisplayNameTags](https://github.com/Matt-MX/DisplayNameTags)
* [NotJustNamePlates](https://github.com/YouHaveTrouble/NotJustNameplates)
* [CustomNameplates](https://polymart.org/product/2543/customnameplates)

# Compatibility with other plugins
Scoreboard teams is a feature that cannot be handled by multiple plugins at once.
To make sure no other plugin sends their own teams when not disabled in the plugin's config properly, TAB will block players being added into teams coming from all other plugins (if this feature is enabled and not disabled for a player with a condition).

# Additional info
## Additional note 1 - NPC (in)compatibility
Teams are bound to player names, not UUIDs or entity ids.
Because of that, they will affect all player entities with that name.
This includes NPCs with the same names as online players, and prefixes/suffixes will be displayed on those as well.
To avoid it, make their names not match any online player and use holograms to display them instead
(Citizens plugin has an option for this).

## Additional note 2 - Prefix/suffix on pets
Since Minecraft 1.9, teams affect tamed animals as well, displaying prefix/suffix on them as well and if nametags are set to be invisible, they will be completely invisible as well. If you want to avoid it, install [this plugin](https://www.spigotmc.org/resources/109466/).

## Additional note 3 - Changing name itself
Teams do not allow to change the nametag name itself and TAB does not offer this functionality in any (other) way. For more information, see [How to display name from nickname plugins - Nametag](https://github.com/NEZNAMY/TAB/wiki/How-to-display-name-from-nickname-plugins#nametag).

## Additional note 4 - F1 view
Using teams causes player nametags to remain visible when using F1 view.
The plugin cannot avoid this in any way.
The only possible solution would be to modify the client.

## Additional note 5 - Transparent players
Minecraft's teams can show players in the same team transparent (partially invisible).
This requires both players (viewer and target) to be in the same team.
As such, this effect is impossible to achieve with TAB,
because it puts every player into a different team
to properly sort players and give them different prefix/suffix in nametag
(and more, such as collision and nametag visibility).

If you want this effect, the only way is to use another plugin that offers this and disable TAB's teams entirely by setting
```
scoreboard-teams:
  enabled: false
```
In config.yml. Keep in mind, you'll not be able to use any team features if you do so.

## Additional note 6 - Vanish hook
When a player is [vanished](https://github.com/NEZNAMY/TAB/wiki/Additional-information#vanish-detection), their team is unregistered for all other players who do **not** have `tab.seevanished` permission.

The purpose of this is to avoid players figuring out a staff member is still online but just vanished by "checking" registered teams and their members.
An example of "exploiting" this is to have a 3rd party client spawn a player entity with name of a chosen player (staff member) and seeing if their name is formatted (= team is registered = player is online) or not (offline).

For this reason, make sure you give all of your staff members who can see vanished players the `tab.seevanished` permission, otherwise the vanished player will appear on top of the tablist (players without team are above players with team) and their nametag will not be formatted.

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `NameTagManager` instance. Get it using `TabAPI.getInstance().getNameTagManager()`. If this feature is disabled, the method will return `null`.

## Changing prefix and suffix
To set the values for the respective formatting, use the following:
* `NameTagManager#setPrefix(TabPlayer, String)`
* `NameTagManager#setSuffix(TabPlayer, String)`

To reset them, set values to `null`.

To get custom values previously set using the API (they will return `null` if no custom value is set):
* `NameTagManager#getCustomPrefix(TabPlayer)`
* `NameTagManager#getCustomSuffix(TabPlayer)`

To get the original value set by the plugin based on configuration:
* `NameTagManager#getOriginalRawPrefix(TabPlayer)` - Prefix with raw placeholder identifiers
* `NameTagManager#getOriginalRawSuffix(TabPlayer)` - Suffix with raw placeholder identifiers
* `NameTagManager#getOriginalReplacedPrefix(TabPlayer)` - Prefix with all placeholders parsed
* `NameTagManager#getOriginalReplacedSuffix(TabPlayer)` - Suffix with all placeholders parsed

> [!NOTE]
> These values are only temporary,
> meaning they won't get saved anywhere and will get reset on player quit or plugin reload.
> If you wish to save these values into file,
> use [commands](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-playergroupplayeruuid-name-property-value-options).

## Collision
* `NameTagManager#setCollisionRule(TabPlayer, Boolean)` - Forces collision rule to the player. Use `null` to reset value and make it follow configuration again.
* `NameTagManager#getCollisionRule(TabPlayer)` - Returns forced collision rule using the API, `null` if no value was forced and player follows the configuration.

## Manipulating visibility
* `NameTagManager#hideNametag(TabPlayer)` - Hides player's nametag from all players
* `NameTagManager#hideNametag(TabPlayer, TabPlayer)` - Hides player's nametag from a specific player, where the first `TabPlayer` is the player whose nametag you want to hide, and the second `TabPlayer` is the player who you want to hide the first player's nametag from.
* `NameTagManager#showNametag(TabPlayer)` - Shows player's nametag back again for everyone
* `NameTagManager#showNametag(TabPlayer, TabPlayer)` - Shows player's nametag back. The first `TabPlayer` is the player whose nametag you want to show, and the second `TabPlayer` is the player who you want to show the first player's nametag to.
* `NameTagManager#hasHiddenNametag(TabPlayer)` - Returns `true` if player has hidden nametag for everyone, `false` if not
* `NameTagManager#hasHiddenNametag(TabPlayer, TabPlayer)` - Returns `true` if player has hidden nametag for specific player, `false` if not. The first `TabPlayer` is the player whose nametag you want to check is hidden, and the second `TabPlayer` is the player who you want to check if they can see the first player's nametag.

## Disabling team handling
* `NameTagManager#pauseTeamHandling(TabPlayer)` - Pauses team handling for a specific player. This will unregister the player's team and disable anti-override for teams.
* `NameTagManager#resumeTeamHandling(TabPlayer)` - Resumes team handling for a specific player. This will register the player's team and enable anti-override for teams.
* `NameTagManager#hasTeamHandlingPaused(TabPlayer)` - Returns `true` if handling is disabled using methods above, `false` if not.

# Examples
## Example 1 - Taking prefix/suffix from permission plugin
If you want TAB to only take prefixes/suffixes from the permission plugin,
delete all groups from **groups.yml** and only keep this:
```
_DEFAULT_:
  tagprefix: "%luckperms-prefix%"
  tagsuffix: "%luckperms-suffix%"
```
Or a PlaceholderAPI placeholder from your permission plugin if you use a different one.

> [!NOTE]
> When doing this, make sure you don't delete something you didn't mean to delete, for example ta**b**prefix and ta**b**suffix from tablist name formatting feature.

## Example 2 - Making nametags invisible on a subserver
The config option `invisible-nametags` takes either a [condition or a conditional expression](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders).  
We can use short format `%server%=<server name>`. If the server name is `server1`, it will look like this:
```
  invisible-nametags: "%server%=server1"
```
If we want to disable it in 2 servers, we separate them with `|` (OR). Result:
```
  invisible-nametags: "%server%=server1|%server%=server2"
```

> [!NOTE]
> This is just an example, the plugin is not limited to making nametags invisible only based on server.
> If you want to disable them in a world, use %world% with world name(s).
> This works for any placeholder offered by TAB or by PlaceholderAPI.