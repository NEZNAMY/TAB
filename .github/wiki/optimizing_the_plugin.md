This guide will explain all primary ways you can reduce plugin's CPU usage besides the obvious things like "don't spam animations" or "disable features you don't want".  
The usage can be checked at any time using [**/tab cpu**](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-cpu) command. If the numbers appear to be higher than they should be, check this guide for various ways to optimize the plugin. Most of these apply for all platforms, but some of them are only specific for some platforms.

# Content
* [#1 - [All platforms] Static text instead of placeholders](#1---all-platforms-static-text-instead-of-placeholders)
* [#2 - [All platforms] Placeholder refresh intervals](#2---all-platforms-placeholder-refresh-intervals)
* [#3 - [All platforms] Don't use RGB](#3---all-platforms-dont-use-rgb)
* [#4 - [All platforms] Don't use animations](#4---all-platforms-dont-use-animations)
* [#5 - [All platforms] Disable anti-override](#5---all-platforms-disable-anti-override)
* [#6 - [All platforms] Disable tablist name formatting](#6---all-platforms-disable-tablist-name-formatting)
* [#7 - [All platforms] Increase permission refresh interval](#7---all-platforms-increase-permission-refresh-interval)
* [#8 - [BungeeCord / Velocity] Disable TAB expansion](#8---bungeecord--velocity-disable-tab-expansion)
* [#9 - [BungeeCord / Velocity] Disable update-latency option in global playerlist](#9---bungeecord--velocity-disable-update-latency-option-in-global-playerlist)
* [#10 - [BungeeCord] Disable ByteBuf deserialization](#10---bungeecord-disable-bytebuf-deserialization)

# #1 - [All platforms] Static text instead of placeholders
Static text doesn't need to be refreshed periodically, resulting in better performance.
Some placeholders can be replaced with static text, such as placeholder for max player counts,
since that value doesn't change at runtime.
Another option is to configure prefixes per-group in config
instead of using placeholders to take those values from your permission plugin.

# #2 - [All platforms] Placeholder refresh intervals
Some placeholders take a long time to process and result in high cpu usage. However, this can be optimized by configuring refresh interval of placeholders.

This only works for PlaceholderAPI placeholders. Internal ones have an interval hardcoded already with values I found the most appropriate.

To begin, find this part in your config
```yml
placeholderapi-refresh-intervals:
  default-refresh-interval: 500
  "%server_uptime%": 1000
  "%server_tps_1_colored%": 1000
  "%player_health%": 200
  "%player_ping%": 1000
  "%vault_prefix%": 1000
  "%rel_factionsuuid_relation_color%": 500
```
Refresh interval is in milliseconds. It must be divisible by 50.

`default-refresh-interval` is refresh interval for all unlisted PlaceholderAPI placeholders.

Keep in mind that CPU measurement is reset every 10 seconds.
This means that if you configure a higher refresh interval than 10000,
there will be intervals when placeholder didn't refresh at all,
making it look like the CPU is perfect, while on the next refresh it may be high.
For this reason, if you want to cleanly track usage, keep the interval at 10000 at most.
Using a higher interval will help if needed, but may lead to misleading info in cpu output.

# #3 - [All platforms] Don't use RGB
Though RGB colors look good, they require the text to be split into several components with `color` field used instead of the whole text pasted into the `text` field. This is a complicated process that takes quite some time. Sticking to legacy colors results in better performance.

# #4 - [All platforms] Don't use animations
While animations look cool, they also have a high resource usage.
This is because they work by spamming updates every time an animation switches to a new frame.
When animations are not used, no updates need to be sent (unless another placeholder changes value),
not causing extra CPU usage.

# #5 - [All platforms] Disable anti-override
By default, TAB prevents other plugins from overriding it.
This is because many server owners are not competent enough to configure their plugins correctly,
such as disabling CMI collision handling which breaks nametags or even disabling team handling in paper.
This obviously causes higher CPU usage.
TAB logs all override attempts of teams into [anti-override.log](https://github.com/NEZNAMY/TAB/wiki/Action-logging#anti-overridelog) file.
If you didn't get the file, it looks like you configured your plugins correctly.
In that case, you can disable anti-override for teams in TAB.
```
scoreboard-teams:
  anti-override: false
```
Tablist formatting is trickier, as override attempts are not logged because it would have a lot of false positives.
You will need to try and see.

```
tablist-name-formatting:
  anti-override: false
```

# #6 - [All platforms] Disable tablist name formatting
As we already know from [Client-sided mechanics](https://github.com/NEZNAMY/TAB/wiki/Client%E2%80%90sided-mechanics#nametag-format-in-tablist) page,
the nametag format appears in tablist if tablist name is not defined.
You can take advantage of this to effectively disable one entire feature and its cpu usage,
while not losing on anything.
However, you can only do this if:
* You are fine with nametag and tablist formats being identical, which also means all [nametag limitations](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags#limitations) will transfer to tablist as well
* You don't have any plugin formatting the tablist names

Whether you have another plugin attempting to handle them or not may not be easy to identify with a lot of plugins and not enough time spent configuring them. In that case, your best choice is to try it and see. If tablist names break, it means you have another plugin and cannot take advantage of this optimization option.

# #7 - [All platforms] Increase permission refresh interval
Plugin needs to constantly check permissions for:
* Permission checks in conditions
* Permission checks if sorting by permissions
* Group retrieving from permission plugin (for sorting / per-group properties)
* Prefix/suffix placeholders taking data from your permission plugin

default value
```
permission-refresh-interval: 1000
```
Refreshes them every second.
If you don't need any permission/group changes to take effect within a second,
you can increase this value for better performance.

# #8 - [BungeeCord / Velocity] Disable TAB expansion
To maximize the performance of TAB's response to PlaceholderAPI request,
values are tracked in advance and then quickly returned.
This process takes resources and is especially heavy on proxy installation,
where values must be constantly sent to bridge using plugin messages.  
Disabling TAB's PlaceholderAPI expansion if you don't use it improves performance.  
**config.yml**:
```
placeholders:
  register-tab-expansion: false
```
# #9 - [BungeeCord / Velocity] Disable update-latency option in global playerlist
When [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) is enabled, you can enable / disable [update-latency](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist#:~:text=other%20unlisted%20servers.-,update%2Dlatency,-false) option (open the link for detailed description of the option). Disabling this option massively improves performance.

# #10 - [BungeeCord] Disable ByteBuf deserialization
In order for anti-override to work properly, some packets must be manually deserialized due to BungeeCord not doing it. If you don't need it, you can disable those functions, which will also disable the deserialization completely. To do so, you must disable 2 functions (only having 1 disabled is not enough):  
#1 - Anti-override for teams
```
scoreboard-teams:
  anti-override: false
```
Whether you need anti-override or not depends on the configuration of your plugins.
The easiest way is to have it enabled and see if you get `anti-override.log` file in TAB folder.
If you don't, you don't need it.
If you do, you'll need to do some configuring.
[Here](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#additional-note-3---compatibility-issues-with-other-plugins)
are a few examples of software and their team names, which you may find helpful.

#2 - Scoreboard detection of other plugins
TAB checks for packets sent by other plugins, and if another plugin sends a scoreboard,
TAB hides its own and resends it after the other plugin removes it.
This check cannot be disabled if the scoreboard is enabled.