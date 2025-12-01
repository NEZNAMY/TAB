# Content
* [Enabling](#enabling)
* [Methods of sorting](#methods-of-sorting)
    * [GROUPS](#groups)
    * [PERMISSIONS](#permissions)
    * [PLACEHOLDER](#placeholder)
    * [PLACEHOLDER_A_TO_Z](#placeholder_a_to_z)
    * [PLACEHOLDER_Z_TO_A](#placeholder_z_to_a)
    * [PLACEHOLDER_LOW_TO_HIGH](#placeholder_low_to_high)
    * [PLACEHOLDER_HIGH_TO_LOW](#placeholder_high_to_low)
* [Multiple elements with the same priority](#multiple-elements-with-the-same-priority)
* [Combining multiple sorting types](#combining-multiple-sorting-types)
* [Additional settings](#additional-settings)
* [Additional info](#additional-info)
    * [Additional note 1 - Limitations](#additional-note-1---limitations)
    * [Additional note 2 - per-world sorting](#additional-note-2---per-world-sorting)
    * [Additional note 3 - Compatibility issues with other plugins](#additional-note-3---compatibility-issues-with-other-plugins)
* [Common mistakes](#common-mistakes)
* [API](#api)

# Enabling
To enable sorting, you must have either [Nametags](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags) or [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) (or both) enabled.

> [!TIP]
> To verify you enabled sorting, run `/tab debug`. It will say `Sorting type:` followed by anything except `DISABLED`.

# Methods of sorting
Below are all the different methods that you can use to sort players.

## GROUPS
This is the default and recommended method. Players will be sorted by their primary permission group, according to the configured group list.

First, put your players into groups in your permission plugin.
All the ways to do it can be found
at [How to assign players into groups](https://github.com/NEZNAMY/TAB/wiki/How-to-assign-players-into-groups).
> [!TIP]
> Verify player's group using `/tab debug <player>`.
It should say `Primary permission group:` followed by group you configured.
If not, you did not assign players into groups correctly.

Second, place all of your groups in to a comma separated list **in order of priority** into `sorting-types`. Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "GROUPS:owner,admin,mod,default"
```
> [!TIP]
> Verify configured sorting priority of players using `/tab debug <player>`.

It should show a message like this:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/095d8523-e8c0-4eff-8935-b0b8e6e0d5ff)

If a group is not in the list, you will get the following:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/9c530873-87c9-4db7-8916-a6121dbeed37)  
If that's the case, you forgot to add that group into the list.

## PERMISSIONS
> [!CAUTION]
> This method is not recommended,
> as many low quality servers give OP to every staff member and don't know how to negate permissions,
> making this option not function as intended.
> If you think you can pull it off, and sorting by primary group is not an option, you can give this a try.  
This method is, however, handy when trying to sort specific players without assigning them into a group.

This method will sort players based on permission nodes they have.  
Place all of your permissions in to a comma separated list **in order of priority**.  
For example, if you have the permissions `my.permission.1`, `my.permission.2`, `my.permission.3`, and want to have players sorted in that order, you would end up with a configuration like `PERMISSIONS:my.permission.1,my.permission.2,my.permission.3`.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PERMISSIONS:my.permission.1,my.permission.2,my.permission.3"
```
> [!TIP]  
> Verify sorting permissions of a player using `/tab debug <player>`

## PLACEHOLDER
This method sorts players using the output of a placeholder and comparing that to pre-defined values.  
To configure this sorting type, write the placeholder that you want to use, followed by a colon (`:`), followed by a comma separated list of outputs to check against.  
For example, if you want to sort players using the output of the `%essentials_afk%` placeholder, depending on whether it is `yes` or `no`, you would do that using a configuration such as `PLACEHOLDER:%essentials_afk%:no,yes`, where players who the placeholder outputs `no` for will be sorted above players who the placeholder outputs `yes` for in this case, putting AFK players on the bottom.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PLACEHOLDER:%essentials_afk%:no,yes"
```

## PLACEHOLDER_A_TO_Z
This method sorts players alphabetically, according to the output of a placeholder.  
To configure this sorting type, write the placeholder that you want to use. That's it.  
For example, if you want to sort players by their name alphabetically, you would do that using a configuration such as `PLACEHOLDER_A_TO_Z:%player%`.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PLACEHOLDER_A_TO_Z:%player%"
```

## PLACEHOLDER_Z_TO_A
This method sorts players reverse alphabetically, according to the output of a placeholder.  
This is identical to the above A to Z sorting, except that the alphabet is backwards, so Z comes first, and A comes last.  
To configure this sorting type, write the placeholder that you want to use. That's it.  
For example, if you want to sort players by their name reverse alphabetically, you would do that using a configuration such as `PLACEHOLDER_Z_TO_A:%player%`.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PLACEHOLDER_Z_TO_A:%player%"
```

## PLACEHOLDER_LOW_TO_HIGH
This method sorts players numerically depending on the output of a **numeric** placeholder from the lowest value to the highest value.  
**Only placeholders that output number values will work with this sorting type!**  
Supported number interval is from -1,000,000,000 to 1,000,000,000 and 5 decimal places.  
To configure this sorting type, write the placeholder that you want to use. That's it.  
For example, if you want to sort players using the output of the `%health%` placeholder from the lowest health to highest, you would do that using a configuration such as `PLACEHOLDER_LOW_TO_HIGH:%health%`.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PLACEHOLDER_LOW_TO_HIGH:%health%"
```

## PLACEHOLDER_HIGH_TO_LOW
This method sorts players numerically depending on the output of a **numeric** placeholder from the highest value to the lowest value.  
**Only placeholders that output number values will work with this sorting type!**  
Supported number interval is from -1,000,000,000 to 1,000,000,000 and 5 decimal places.  
To configure this sorting type, write the placeholder that you want to use. That's it.  
For example, if you want to sort players using LuckPerms weights, you would do that using a configuration such as `PLACEHOLDER_HIGH_TO_LOW:%luckperms_highest_group_weight%`.  
Example:
```yaml
scoreboard-teams:
  sorting-types:
    - "PLACEHOLDER_HIGH_TO_LOW:%luckperms_highest_group_weight%"
```

# Multiple elements with the same priority
If using a sorting type that uses pre-defined values ([GROUPS](#groups), [PERMISSIONS](#permissions) or [PLACEHOLDER](#placeholder)), you can sort multiple elements with the same priority by separating them with `|` symbol. Example using group sorting:
```
scoreboard-teams:
  sorting-types:
    - "GROUPS:owner,admin,vip1|vip2,default"
```
Where `vip1` and `vip2` will be sorted with the same priority (3rd).  
This works for permissions and pre-defined placeholder values as well.

# Combining multiple sorting types
Since `sorting-types` is a list, you are able to use more than one sorting type. Sorting type priorities go in the same order as defined in the config. Let's take a look at the default config:
```
  sorting-types:
    - "GROUPS:owner,admin,mod,helper,builder,vip,default"
    - "PLACEHOLDER_A_TO_Z:%player%"
```
Here, players are sorted by their group first.
Owner goes above admin etc. However,
if 2 people have the same group (such as admin), the next sorting type decides the final order.
In this case, admin with alphabetically lower username will be higher in the tablist.

A common request is putting AFK players on the bottom of tablist. This can be achieved with the following configuration:
```
  sorting-types:
    - "PLACEHOLDER:%essentials_afk%:no,yes"
    - "GROUPS:owner,admin,mod,helper,builder,vip,default"
    - "PLACEHOLDER_A_TO_Z:%player%"
```
With AFK status taking the highest priority, AFK players will always be sorted below players which are not AFK.
Both player groups (afk and non-afk) then follow the rest of the logic explained above to define the final order.
If trying to copy-paste this example, keep
in mind [Placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements) work here as well,
so if you are using a fancier output for %essentials_afk%,
you will need to use outputs respectively when defining sorting as well.

Although you can theoretically use as many sorting types as you want, there are still strict limits set by mojang. See more about these limits and how to avoid them as much as possible at [Additional note 1 - Limitations](#additional-note-1---limitations).

# Additional settings
| Option name            | Default value | Description                                                            |
|------------------------|---------------|------------------------------------------------------------------------|
| case-sensitive-sorting | true          | When enabled, players will be sorted as A-Z a-z. When disabled, Aa-Zz. |

# Additional info
## Additional note 1 - Limitations
All sorting elements must together build a team name up to 16 characters long. Because of that, cuts in placeholder outputs may be required. TAB is already using the shortest possible values for all sorting types:
* `GROUPS`, `PERMISSIONS` and `PLACEHOLDER` - 1 character
* `PLACEHOLDER_LOW_TO_HIGH` and `PLACEHOLDER_HIGH_TO_LOW` - 3 characters
* `PLACEHOLDER_A_TO_Z` and `PLACEHOLDER_Z_TO_A` - as many as used

All your sorting types must be within a 15-character limit.
Any characters above that will be cut off (TAB reserves 1 character to ensure each player is in a unique team).
To get the most out of it, use the 1-character sorting types where possible.

You can bypass this limit by using [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) feature.
Player ordering is fully plugin-sided and therefore is not artificially limited.

## Additional note 2 - per-world sorting
Defining a per-world sorting type is not supported.
However,
this can be achieved with [Conditional placeholders](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders).
Check for %world% and return one placeholder if it matches, another one if not.
This will work with anything, not just worlds (server, regions, etc.).

## Additional note 3 - Compatibility issues with other plugins
As you already know by now, sorting is managed by scoreboard teams (their names to be exact).
Player can only be a member of one team.
In other words, only one plugin can handle teams at a time.
Having multiple plugins handling teams is supposed to end in a disaster.

Fortunately, TAB contains a function that prevents other plugins from assigning players into teams.
When a plugin tries to override TAB's teams, this action is logged into `anti-override.log` file.
If your file is empty / does not exist, it means you have no conflicting plugins / settings.
If the file exists, most of the time you can guess where the teams come from by their name.  
Here are a few common teams and their sources (`xxxx` means any, usually random character sequence):
* `collideRule_xxxx` - this comes from Paper. Not going to explain why as that would be quite long, but the way you can avoid is by setting `enable-player-collisions: true` in paper config and `enable-collision: false` in TAB config (yes, collisions will be disabled).
* `CMINPxx` - CMI, set `DisableTeamManagement: true` in `plugins/CMI/config.yml` (though CMI won't allow you to change glow color if you do, so keep that in mind).
* `CIT-xxxxxxxxxxxx` - Citizens NPC with the same name as some online player. Make NPC names not match real players and use holograms to display your desired text (/npc name or something).
* `PVP-xxxxxxxxxxxx` - Team coming from [PvPManager](https://www.spigotmc.org/resources/pvpmanager-lite.845/) plugin.

This detection, however, is not 100%.
Because of that, you may still be experiencing a compatibility issue even with anti-override enabled.
To identify such an issue, check if /tab reload fixes your sorting issue.
If it does, it's a compatibility issue of some sort.
If not, it is most likely a misconfiguration issue.

# Common mistakes
Every possible mistake could be called "not reading this wiki page", but that would make this section pointless.
For that reason, let's call it list of mistakes made when following this page.

The most common mistakes include:
* Disabling both teams and layout, not realizing it disables sorting as well.
* Mistaking `primary-group-finding-list` for the sorting list, despite that list having nothing to do with sorting and by default even having a comment above it saying it has nothing to do with sorting.
* Not configuring primary groups correctly. This can have multiple reasons, such as
    * Not configuring group weights in LuckPerms.
    * Accidentally enabling `use-bukkit-permission-manager` option when on a proxy without knowing what it does.
    * Installing TAB on a proxy without having any permission plugin on the proxy.
    * Enabling `assign-groups-by-permissions` without giving those permissions (or the opposite - giving away OP to users, resulting in the highest group being taken).

  [Debug command](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-debug-player) will help you identify if this is your case.
* Using a plugin that causes TAB to fail to apply teams (such as Tablisknu). This includes installing TAB plugin on both backend and proxy, causing the installations to conflict.
* Using TAB on Velocity without installing [VelocityScoreboardAPI](https://github.com/NEZNAMY/VelocityScoreboardAPI/releases)

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `SortingManager` instance. Get it using `TabAPI.getInstance().getSortingManager()`. If sorting is disabled, the method will return `null`.

You can change player's team name using the following methods:
* `SortingManager#forceTeamName(TabPlayer, String)` - Sets player's team name to specified value (and performs team unregister & register with new name).
* `SortingManager#getForcedTeamName(TabPlayer)` - Returns player's forced team name using the method above. Will return `null` if no value is set.

Unfortunately, there is no simple way of just changing one's position while still accounting for other players.
This is because the plugin supports a lot of sorting options, not just groups,
so doing amateur stuff like "priority 1, 2" doesn't make sense.
If you are really only interested in sorting by groups and changing group which player is sorted as,
you can use `TabPlayer#setTemporaryGroup(String)`.
To reset the group back to normal, call the function with `null` argument.