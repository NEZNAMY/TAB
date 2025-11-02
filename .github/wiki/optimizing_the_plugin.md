This guide will explain all primary ways you can reduce plugin's CPU usage besides the obvious things like "don't spam animations" or "disable features you don't want".  
The usage can be checked at any time using [**/tab cpu**](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-cpu) command. If the numbers appear to be higher than they should be, check this guide for various ways to optimize the plugin. Most of these apply for all platforms, but some of them are only specific for some platforms.

# Content
* [#1 - [All platforms] Static text instead of placeholders](#1---all-platforms-static-text-instead-of-placeholders)
* [#2 - [All platforms] Placeholder refresh intervals](#2---all-platforms-placeholder-refresh-intervals)
* [#3 - [All platforms] Don't use RGB](#3---all-platforms-dont-use-rgb)
* [#4 - [All platforms] Don't use animations](#4---all-platforms-dont-use-animations)
* [#5 - [All platforms] Disable tablist name formatting](#5---all-platforms-disable-tablist-name-formatting)
* [#6 - [All platforms] Increase permission refresh interval](#6---all-platforms-increase-permission-refresh-interval)
* [#7 - [Proxies] Disable TAB expansion](#7---proxies-disable-tab-expansion)
* [#8 - [Proxies] Disable update-latency option in global playerlist](#8---proxies-disable-update-latency-option-in-global-playerlist)

# #1 - [All platforms] Static text instead of placeholders
Static text doesn't need to be refreshed periodically, resulting in better performance.
Some placeholders can be replaced with static text, such as placeholder for max player counts,
since that value doesn't change at runtime.
Another option is to configure prefixes per-group in config
instead of using placeholders to take those values from your permission plugin.

# #2 - [All platforms] Placeholder refresh intervals
Some placeholders take a long time to process and result in high cpu usage. However, this can be optimized by configuring refresh interval of placeholders.

Internal placeholders use a value hardcoded into the plugin by default. PlaceholderAPI placeholders use `default-refresh-interval`.

To begin, find this part in your config
```yml
placeholder-refresh-intervals:
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

# #5 - [All platforms] Disable tablist name formatting
As we already know from [Client-sided mechanics](https://github.com/NEZNAMY/TAB/wiki/Client%E2%80%90sided-mechanics#nametag-format-in-tablist) page,
the nametag format appears in tablist if tablist name is not defined.
You can take advantage of this to effectively disable one entire feature and its cpu usage,
while not losing on anything.
However, you can only do this if:
* You are fine with nametag and tablist formats being identical, which also means all [nametag limitations](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags#limitations) will transfer to tablist as well
* You don't have any plugin formatting the tablist names

Whether you have another plugin attempting to handle them or not may not be easy to identify with a lot of plugins and not enough time spent configuring them. In that case, your best choice is to try it and see. If tablist names break, it means you have another plugin and cannot take advantage of this optimization option.

# #6 - [All platforms] Increase permission refresh interval
Plugin needs to constantly check permissions for:
* Permission checks in conditions
* Permission checks if sorting by permissions
* Group retrieving from permission plugin (for sorting / per-group properties)

default value
```
permission-refresh-interval: 1000
```
Refreshes them every second.
If you don't need any permission/group changes to take effect within a second,
you can increase this value for better performance.

# #7 - [Proxies] Disable TAB expansion
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
# #8 - [Proxies] Disable update-latency option in global playerlist
When [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) is enabled, you can enable / disable [update-latency](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist#:~:text=other%20unlisted%20servers.-,update%2Dlatency,-false) option (open the link for detailed description of the option). Disabling this option massively improves performance.  
