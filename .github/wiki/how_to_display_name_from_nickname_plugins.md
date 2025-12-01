# Content
* [Tablist](#tablist)
* [Nametag](#nametag)
* [Proper compatibility with plugins that change profile name](#proper-compatibility-with-plugins-that-change-profile-name)
* [Tips & Tricks](#tips--tricks)
    * [Tip 1 - Nickname prefix](#tip-1---nickname-prefix)
    * [Tip 2 - Sorting nicked players with the lowest priority](#tip-2---sorting-nicked-players-with-the-lowest-priority)

# Tablist
This one is straightforward.
The tablist format of players is one text split into 3 parts in the plugin for convenience
(tabprefix, customtabname, tabsuffix).
Using `customtabname` you can configure name part into any placeholder you want,
including one from your nickname plugin.
All you need is a working PlaceholderAPI placeholder from your nickname plugin.
Example:  
**groups.yml**
```
_DEFAULT_:
  customtabname: '%essentials_nickname%'
```
or using in-game command `/tab group _DEFAULT_ customtabname %essentials_nickname%`.

This example uses `%essentials_nickname%` placeholder from Essentials. Giving it to `_DEFAULT_` group keyword makes it a default value for all groups that do not override this property.

> [!CAUTION]
> Do NOT put nickname placeholder into tabprefix!
> You will end up with double names!
> Put the nickname placeholder into customtabname instead.

# Nametag
Nametags are more complicated because they consist of player's username, team prefix and team suffix.
There is no intended way to do this.
The only way is changing it using a nickname plugin.
Some nickname plugins actually change name in game profile, which also changes it in nametag.
You will need to find one that offers this and then use it.
This is a complicated process with lots of side effects, so TAB does not offer it.

# Proper compatibility with plugins that change profile name
If using a nickname plugin that changes player's profile name to change it in the name tag,
this has lots of side effects.
Most notably, it breaks all name-bound features,
especially [nametags](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags) and [sorting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist).
For that reason, TAB must detect name change.
This is done automatically on Bukkit, BungeeCord and modded platforms.
**This detection is not available on Sponge and Velocity**.

> [!WARNING]
> If you have TAB installed on backend server and a proxy plugin changes nickname, TAB is not able to detect this change, since the modified packets never actually go through the backend server where TAB is installed.

> [!TIP]
> If automatic detection is not working for you (either you use TAB on Sponge / Velocity, or use proxy plugin for changing names while TAB is on backend), you can use [this API method](https://github.com/NEZNAMY/TAB/blob/master/api/src/main/java/me/neznamy/tab/api/TabPlayer.java#L88) to let TAB know player's name changed.

In order for TAB to properly detect name change, player UUID must remain the same,
otherwise it's a random entry with a random uuid and random name, which TAB isn't able to match with actual players.
Make sure your nickname plugin is not changing UUID of players.
If UUID changes, the plugin won't be able to properly format names even in the tablist,
since entries there are bound to UUIDs and by changing it, it will break.  
Identifying whether name change detection is working properly or not can be checked
by setting `debug: true` in config and checking console on name change.
It should say `Processing name change of player <player name> to <new nickname>`.
If it doesn't, something went wrong, most likely UUID not matching.

# Tips & Tricks
## Tip 1 - Nickname prefix
By default, Essentials prefixes nicknames of players in chat using `~` symbol for nicked players. We can very easily replicate that with [Conditional placeholders](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders). First, create a new condition:
```
conditions:
  nick:
    conditions:
    - '%essentials_nickname%=%player%'
    true: "%player%"              # If nickname = player name -> player is not nicked
    false: "~%essentials_nickname%" # If nickname != player name -> player is nicked
```
Then, use `%condition:nick%` in your `customtabname` instead of the placeholder directly.

# Tip 2 - Sorting nicked players with the lowest priority
Let's say your sorting list looks like this:
```
  sorting-types:
    - "GROUPS:owner,admin,mod,helper,builder,vip,default"
    - "PLACEHOLDER_A_TO_Z:%player%"
```
Let's also assume that you want every nicked player to be sorted as `default`. This can be achieved using [sorting by placeholder](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#placeholder) and creating a [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that will return output based on nick status. First, let's create the condition:
```
conditions:
  group:
    conditions:
    - '%haonick_isNicked%=true'
    true: "default"
    false: "%group%"
```
When the condition is met, meaning player is nicked, it will return the group we want nicked players to be sorted as, in this case `default`. Otherwise, return player's actual group.  
The condition check may differ with different nickname plugins. You will need to check documentation of your nickname plugin to see what kind of placeholders it offers and what they return (you can also use `/tab parse` to see what exactly did a placeholder return).  
In this case, we are using placeholder `%haonick_isNicked%`, which returns `true` for nicked players.  
If using Essentials, the line would look like this: `%essentials_nickname%!=%player%`, because Essentials is made to return player's actual name when not nicked, meaning when it's not equal, player is nicked.

Finally, use the conditional placeholder in sorting:
```
  sorting-types:
  - PLACEHOLDER:%condition:group%:owner,admin,mod,helper,builder,vip,default"
  - PLACEHOLDER_A_TO_Z:%player%
```
This will result in nicked players being sorted as `default`.  
If you want to achieve something more complicated, such as not only sorted as default, but another rank, you will need to dig deeper into your problem, analyzing available placeholders and functions that will help you achieve your result.  