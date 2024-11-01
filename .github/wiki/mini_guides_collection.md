# Content
Combine the knowledge gathered by reading the wiki to get interesting results, most frequently asked ones listed here.
* [Displaying kill / death counter in tablist](#displaying-kill--death-counter-in-tablist)
* [Taking prefixes/suffixes from permission plugin](#taking-prefixessuffixes-from-permission-plugin)
* [Showing player version in tablist](#showing-player-version-in-tablist)
* [Replicating Purpur's TPSBar](#replicating-purpurs-tpsbar)
* [BedWars1058 compatibility](#bedwars1058-compatibility)
* [Displaying AFK status in tablist](#displaying-afk-status-in-tablist)
* [Showing combined online player count from multiple servers](#showing-combined-online-player-count-from-multiple-servers)
  * [Option 1 - Using global playerlist](#option-1---using-global-playerlist)
  * [Option 2 - Using PlaceholderAPI](#option-2---using-placeholderapi)

# Displaying kill / death counter in tablist
This example uses PlaceholderAPI placeholders.  
Kills:
```
playerlist-objective:
  enabled: true
  value: "%statistic_player_kills%"
```  
Deaths:
```
playerlist-objective:
  enabled: true
  value: "%statistic_deaths%"
```  
**Note:** These values are taken from server stats. You can modify / reset them for each player in `<main world>/stats/<player uuid>.json` file.

# Taking prefixes/suffixes from permission plugin
Delete all groups from **groups.yml** and only keep this:
```
_DEFAULT_:
  tabprefix: "%vault_prefix%"
  tagprefix: "%vault_prefix%"    
  tabsuffix: "%vault_suffix%"    
  tagsuffix: "%vault_suffix%"
```
This requires Vault and a permission plugin to work.  
If you have luckperms installed on the same server instance, you can use TAB's internal `%luckperms-prefix%` (and suffix) placeholders instead.  
**Note:** If TAB is on bungeecord and you want prefixes to be taken from backend servers, use `%vault_prefix%` (or `%luckperms_prefix%`) with [PlaceholderAPI support on bungeecord](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge).

# Showing player version in tablist
Run `/tab group _DEFAULT_ tabsuffix " &8[&3%player-version%&8]"` or any other color combination you want. You can also include other placeholders with it. You can also modify it directly in **groups.yml** file instead. With the provided example command, you'll get the following result:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/2327b482-e8da-4940-9aa8-ea2bdda64b6f)

# Replicating Purpur's TPSBar
*This guide was made purely for demonstration of TAB's functionality to show you can even do things like this with conditions. It is not meant to be used as a replacement for Purpur or any of its functions or discouraging users from using Purpur.*

By default, Purpur's TPSBar shows server's TPS, MSPT and player ping. These change color from green to red based on value. The easier way to achieve this is with [Placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements-(premium-only)), however, it alters placeholders everywhere inside TAB, which would result in features like [Yellow number](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Yellow-number) to stop working correctly when %ping% is used, since it would contain color codes now. For that reason, we will use [Conditions](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) instead.

When TPS is above 19, it displays as `<#55FF55>value</#00AA00>`. When above 15, `<#FFFF55>value</#FFAA00>`, otherwise `<#FF5555>value</#AA0000>`. This can be achieved with 2 conditions:
```
conditions:
  tps:
    conditions:
      - "%tps%>=19"
    yes: "<#55FF55>%tps%</#00AA00>"
    no: "%condition:tps2%"
  tps2:
    conditions:
      - "%tps%>=15"
    yes: "<#FFFF55>%tps%</#FFAA00>"
    no: "<#FF5555>%tps%</#AA0000>"
```
To use this, we will use `%condition:tps%`.  
Now, repeat for mspt and ping. For mspt, purpur's intervals are <40, <50, 50+. For ping, it's <100, <200, 200+.

Conditions for mspt and ping may look something like this:
```
conditions:
  mspt:
    conditions:
      - "%mspt%<40"
    yes: "<#55FF55>%mspt%</#00AA00>"
    no: "%condition:mspt2%"
  mspt2:
    conditions:
      - "%mspt%<50"
    yes: "<#FFFF55>%mspt%</#FFAA00>"
    no: "<#FF5555>%mspt%</#AA0000>"
  ping:
    conditions:
      - "%ping%<100"
    yes: "<#55FF55>%ping%</#00AA00>"
    no: "%condition:ping2%"
  ping2:
    conditions:
      - "%ping%<200"
    yes: "<#FFFF55>%ping%</#FFAA00>"
    no: "<#FF5555>%ping%</#AA0000>"
```
To use them, we will use `%condition:mspt%` and `%condition:ping%`. Purpur's TPSBar is using mspt as bossbar progress with range from 0 to 50. This can be achieved with PlaceholderAPI's math expansion: `%math_0_{tab_placeholder_mspt}*2%`. It always uses `GREEN` color and `NOTCHED_20` style. Let's put all of this together:
```
  bars:
    tpsbar:
      style: NOTCHED_20
      color: GREEN
      progress: "%math_0_{tab_placeholder_mspt}*2%"
      text: "&7TPS&e: %condition:tps% &7MSPT&e: %condition:mspt% &7Ping&e: %condition:ping%&7ms"
```

The last step is to configure when should this bossbar be displayed. One way is to use bossbar announce/send command. Another is to use display conditions. An example can look like `display-condition: "permission:tab.tpsbar`. Now, only players with the permission will see the bossbar.

# BedWars1058 compatibility
## Sorting
To sort teams through TAB, add the PLACEHOLDER_A_TO_Z sorting type with the %bw1058_player_team% placeholder. Example:
```yml
sorting-types:
  - "PLACEHOLDER_A_TO_Z:%bw1058_player_team%"
  - "GROUPS:admin,mod,default"
  - "PLACEHOLDER_A_TO_Z:%player%"
```
## Team name in tablist/nametag
To display the team name in the tablist, use this command: `/tab group _DEFAULT_ tabprefix %bw1058_player_team%`  
The same goes for the nametag, but with tagprefix instead: `/tab group _DEFAULT_ tagprefix %bw1058_player_team%`

To display only the first letter of the team, download this PlaceholderAPI expansion: https://api.extendedclip.com/expansions/bw1058plus/
and use `%bw1058plus_team_letter%` instead, if you want the color as well, put `%bw1058plus_team_color%` right before.

## Rank in lobby & Team name in game
If you want to display the rank of the player while he's in the lobby or waiting lobby, BUT show the team name while in game, then don't worry, there's a way =)  
You can create a condition which checks if the player's current team = nothing (which means the player isn't playing), and return either the rank or the team name depending on that condition. Example:
```yml
conditions:
  rankOrTeam:
    conditions:
      - "%bw1058_player_team%="
    yes: "%luckperms-prefix%"
    no: "%bw1058_player_team%"
```
And then do `/tab group _DEFAULT_ tabprefix %condition:rankOrTeam%` and `/tab group _DEFAULT_ tagprefix %condition:rankOrTeam%` so it shows in your nametag and in your tablist name.

# Displaying AFK status in tablist
First, you'll need to get an AFK plugin. TAB does not track player AFK status. Then, find a PlaceholderAPI placeholder for your AFK plugin in their documentation, such as
* `%essentials_afk%` for Essentials
* `%cmi_user_afk_symbol%` for CMI
* `%purpur_player_afk%` for Purpur (server software)

Don't forget to install PlaceholderAPI and the respective expansion of your afk plugin.

Then, use the placeholder in configuration wherever you want it. Assuming you want it for all players, use `_DEFAULT_` group keyword in **groups.yml** for default settings for all groups. Combined with other settings, it may look like this for tabsufffix:
```
_DEFAULT_:
  tabsuffix: "%luckperms-suffix%%essentials_afk%"
```
Or running an in-game command: `/tab group _DEFAULT_ tabsuffix %luckperms-suffix%%essentials_afk%`.  
This is not limited to displaying AFK status in suffix, you can also using it in tabprefix instead (or nametags).


Finally, you will most likely need to set up [placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements) for the placeholder, as lots of them, including the %essentials_afk% placeholder used as an example, return `yes` and `no` values, which you most likely want to customize.  
As mentioned, by default, Essentials' afk placeholder returns `yes` / `no` (unless modified in PlaceholderAPI's config.yml). Other placeholders may return different values. To find out what a placeholder returned, use `/tab parse <player> <placeholder>`. Don't forget to include color codes as well.  
An example customization may look like this:
```
placeholder-output-replacements:
  "%essentials_afk%":
    "yes": " &4&lAFK"
    "no": "" # display nothing if not afk
```
Keep in mind that some placeholders don't need this. For example CMI has 2 placeholders - %cmi_user_afk% that returns true/false, and %cmi_user_afk_symbol% that returns AFK text configured in CMI configuration. Therefore, if you decide to use the latter one, you don't need to set up replacements.

# Showing combined online player count from multiple servers
## Option 1 - Using global playerlist
First way is to install TAB on the proxy and enable [global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist). It allows you to create groups of servers that share playerlist. These groups can then be used to show player count on those servers using a placeholder. Using the following example:
```
global-playerlist:
  enabled: true
  server-groups:
    lobbies:
      - lobby1
      - lobby2
```
Servers `lobby1` and `lobby2` will share playerlist. You will also be able to use placeholder formula `%playerlist-group_<group>%`, in this case `%playerlist-group_lobbies%`. It will show amount of players on `lobby1` and `lobby2` combined.

Advantage of this is the natural compatibility with global playerlist's settings, which is also its disavantage, since global playerlist has to be configured that way. When using [layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) feature, global playerlist is redundant and having it enabled only for the placeholder is inefficient.

## Option 2 - Using PlaceholderAPI
Unlike the first option, this one offers much more freedom, but also requires another plugin. You'll need to [download PlaceholderAPI and its expansions called math and bungee](https://github.com/NEZNAMY/TAB/wiki/Quick-PlaceholderAPI-startup-guide) (`/papi ecloud download bungee`, `/papi ecloud download math`, `/papi reload`).  
First, you need placeholders for online count on specific servers. Bungee expansion does exactly that. The format is `%bungee_<server>%`. To merge the online counts, use math expansion. The syntax will be `%math_0_<expression>%`, where `expression` uses online counts from specific servers from bungee expansion. Finally, you'll end up with a placeholder like `%math_0_{bungee_lobby1}+{bungee_lobby2}%`. This placeholder will merge online counts from `lobby1` and `lobby2`.  
Please note that by default, bungee expansion updates values only every 30 seconds. This can be changed in PlaceholderAPI's config. If the numbers still don't show correct value, check if the placeholder works correctly using `/papi parse me <placeholder>`. If not, this is not a TAB issue.

Advantage of this is no requirement for a specific TAB feature to be enabled, which also includes no need to have TAB installed on the proxy. The downside is relying on 3rd party software. If TAB is on proxy, you'll also need [bridge plugin](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge).  