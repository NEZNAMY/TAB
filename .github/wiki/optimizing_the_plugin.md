# Content
* [About](#about)
* [Ways of optimizing](#ways-of-optimizing)
    * [#1 - [All platforms] Static text instead of placeholders](#1---all-platforms-static-text-instead-of-placeholders)
    * [#2 - [All platforms] Placeholder refresh intervals](#2---all-platforms-placeholder-refresh-intervals)
    * [#3 - [All platforms] Don't use RGB](#3---all-platforms-dont-use-rgb)
    * [#4 - [All platforms] Don't use animations](#4---all-platforms-dont-use-animations)
    * [#5 - [All platforms] Disable tablist name formatting](#5---all-platforms-disable-tablist-name-formatting)
    * [#6 - [All platforms] Increase permission refresh interval](#6---all-platforms-increase-permission-refresh-interval)
    * [#7 - [Proxies] Disable TAB expansion](#7---proxies-disable-tab-expansion)
    * [#8 - [Proxies] Disable update-latency option in global playerlist](#8---proxies-disable-update-latency-option-in-global-playerlist)
* [Netty false positive in spark reports](#netty-false-positive-in-spark-reports)
    * [Why there is a false positive](#why-there-is-a-false-positive)
    * [How to read the real value](#how-to-read-the-real-value)
        * [Packet listening usage](#packet-listening-usage)
        * [Sending packets](#sending-packets)

# About
This guide will explain all primary ways you can reduce plugin's CPU usage besides the obvious things like "don't spam animations" or "disable features you don't want".  
The usage can be checked at any time using [**/tab cpu**](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-cpu) command. If the numbers appear to be higher than they should be, check this guide for various ways to optimize the plugin. Most of these apply for all platforms, but some of them are only specific for some platforms.

# Ways of optimizing
## #1 - [All platforms] Static text instead of placeholders
Static text doesn't need to be refreshed periodically, resulting in better performance.
Some placeholders can be replaced with static text, such as placeholder for max player counts,
since that value doesn't change at runtime.
Another option is to configure prefixes per-group in config
instead of using placeholders to take those values from your permission plugin.

## #2 - [All platforms] Placeholder refresh intervals
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

## #3 - [All platforms] Don't use RGB
Though RGB colors look good, they require the text to be split into several components with `color` field used instead of the whole text pasted into the `text` field. This is a complicated process that takes quite some time. Sticking to legacy colors results in better performance.

## #4 - [All platforms] Don't use animations
While animations look cool, they also have a high resource usage.
This is because they work by spamming updates every time an animation switches to a new frame.
When animations are not used, no updates need to be sent (unless another placeholder changes value),
not causing extra CPU usage.

## #5 - [All platforms] Disable tablist name formatting
As we already know from [Client-sided mechanics](https://github.com/NEZNAMY/TAB/wiki/Client%E2%80%90sided-mechanics#nametag-format-in-tablist) page,
the nametag format appears in tablist if tablist name is not defined.
You can take advantage of this to effectively disable one entire feature and its cpu usage,
while not losing on anything.
However, you can only do this if:
* You are fine with nametag and tablist formats being identical, which also means all [nametag limitations](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags#limitations) will transfer to tablist as well
* You don't have any plugin formatting the tablist names

Whether you have another plugin attempting to handle them or not may not be easy to identify with a lot of plugins and not enough time spent configuring them. In that case, your best choice is to try it and see. If tablist names break, it means you have another plugin and cannot take advantage of this optimization option.

## #6 - [All platforms] Increase permission refresh interval
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

## #7 - [Proxies] Disable TAB expansion
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
## #8 - [Proxies] Disable update-latency option in global playerlist
When [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) is enabled, you can enable / disable [update-latency](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist#:~:text=other%20unlisted%20servers.-,update%2Dlatency,-false) option (open the link for detailed description of the option). Disabling this option massively improves performance.

# Netty false positive in spark reports
When taking a spark report with all threads monitored instead of just the server thread using `--thread *` argument (or on a proxy where this is enabled by default since there is no main thread to monitor by default), you may notice high usage (both CPU and RAM) in netty thread when grouping threads by plugin. **Vast majority of that usage is false positive.** Let's go over why this is the case and how to read the real value.

## Why there is a false positive
When listening to an event, the server fires the events in a loop, where each event is the end point. As such, measuring every event handler individually is accurate. Here is how the hierarchy looks with example numbers:
```
Event processing in loop (6ms total)
↳ Plugin 1 (1ms)
↳ Plugin 2 (2ms)
↳ Plugin 3 (3ms)
End of loop
```
As such, the measured values are accurate and spark is able to separate each event handler based on plugin. Command executions and repeating tasks are even simpler.

The issue is with netty injection. The purpose is to listen to outgoing packets, react to them and possibly modify them. Unlike with events, these are not processed in parallel, but every handler forwards the packet to the next one (or doesn't if it should be cancelled). As such, the hierarchy looks like this (simplified version):
```
Beginning (12ms total)
↳ Plugin 1 (1ms self, 12ms total)
  ↳ Plugin 2 (1ms self, 11ms total)
    ↳ Plugin 3 (1ms self, 10ms total)
      ↳ Minecraft encoder (10ms self)
```
As such, spark will show 12ms total for Plugin 1, even though its own usage is only 1ms. This is due to a limitation of the design of Spark in combination with how netty handlers work. This is only the case for plugins with their own netty injection, it does not apply to plugins that use ProtocolLib, because that one offers event-like functionality explained above. In this case, the usage shown in spark report is 100% accurate.

## How to read the real value
An actual usage of the netty threads can be split into 2 parts:
* Listening to outgoing packets
* Sending packets, which causes it to go though the pipeline and then encode

### Packet listening usage
This one is simple and accurate. Take the shown number, expand the call stack and subtract `io.netty.channel.ChannelDuplexHandler.write()` usage, which is forwarding the packet to the next handler and would be there even if that plugin wasn't injected.  
Let's take a look at a practical example:
<img width="1091" height="266" alt="image" src="https://github.com/user-attachments/assets/6d9e8c25-8375-413b-ab9b-31b52ff9262c" />

Here, spark reports `31.71%` usage by TAB. However, `30.87%` of it is forwarding it to the next handler (`io.netty.channel.ChannelDuplexHandler.write()`), which forwards it to the next one, until eventually the server encodes the packet. Therefore, TAB's real usage by listening to outgoing packets in this report is `31.71%` - `30.87%` = `0.84%`. Much less than what you originally see.

### Sending packets
Sadly, this one is impossible to calculate directly. When a packet is scheduled for sending, it is only added to a queue, then consumed by a netty thread, not the caller thread. At that point we are losing information about where a packet comes from.  
There is some good news though.

First, vast majority of plugins barely send any packets. For example PlaceholderAPI is just an interface between plugins, chat plugins only modify chat message packets that would be sent anyway, ViaVersion same thing, and so on...


Second, plugins that send a lot of packets usually send the kind that the server either doesn't send at all, or not much. Expanding the data from the previously shown report gives us this:
<img width="1083" height="496" alt="image" src="https://github.com/user-attachments/assets/c6157e78-ef6a-4d9c-b4b7-7c7b32dd14f8" />

Here we can see that encoding team packets took 0.99%. Since TAB is the only team plugin you need for managing players, it is safe to assume most, if not all of it comes from TAB.  
Same here - 0.13% from header and footer.
<img width="1085" height="93" alt="image" src="https://github.com/user-attachments/assets/559dce56-83d3-49b2-9f4b-29b6124f944c" />

Sadly there are other operations needed than just encoding packets, such as allocating buffers and other, so this part is not that simple anymore.