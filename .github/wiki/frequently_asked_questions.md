# Content
* [#1 - Why are NPCs showing up in the tablist?](#1---why-are-npcs-showing-up-in-the-tablist)
* [#2 - How to make TAB work with LuckPerms?](#2---how-to-make-tab-work-with-luckperms)
* [#3 - I enabled MySQL, and my prefix is now gone!](#3---i-enabled-mysql-and-my-prefix-is-now-gone)
* [#4 - Why is player sorting not working?](#4---why-is-player-sorting-not-working)
* [#5 - How to add players to %staffonline% placeholder?](#5---how-to-add-players-to-the-staffonline-placeholder)
* [#6 - How to make player heads visible in tablist?](#6---how-to-make-player-heads-visible-in-tablist)
* [#7 - Where can i find current default config files?](#7---where-can-i-find-current-default-config-files)
* [#8 - How can I split players into multiple columns?](#8---how-can-i-split-players-into-multiple-columns)
* [#9 - How to use space in prefix/suffix command?](#9---how-to-use-spaces-in-prefixsuffix-command)
* [#10 - Can I change/remove the green connection bar in tablist?](#10---can-i-changeremove-the-green-connection-bar-in-tablist)
* [#11 - Placeholder is not working](#11---placeholder-is-not-working)
* [#12 - How can I display ItemsAdder images?](#12---how-can-i-display-itemsadder-images)
* [#13 - How can I use UTF characters in configuration?](#13---how-can-i-use-utf-characters-in-configuration)
* [#14 - Is there a way to remove all players from tablist?](#14---is-there-a-way-to-remove-all-players-from-tablist)
* [#15 - How to add images to tablist?](#15---how-to-add-images-to-tablist)
* [#16 - Is MiniMessage supported?](#16---is-minimessage-supported)
* [#17 - Is it possible to show the number of players in a specific group, similar to staffonline placeholder?](#17---is-it-possible-to-show-the-number-of-players-in-a-specific-group-similar-to-staffonline-placeholder)
* [#18 - Why is the plugin flagged as a virus by Windows defender?](#18---why-is-the-plugin-flagged-as-a-virus-by-windows-defender)

## #1 - Why are NPCs showing up in the tablist?
See [Citizens FAQ](https://wiki.citizensnpcs.co/Frequently_Asked_Questions#Why_are_NPCs_showing_up_in_the_tablist.3F).

## #2 - How to make TAB work with LuckPerms?
* If you want to take prefixes/suffixes, check [this guide](https://github.com/NEZNAMY/TAB/wiki/Mini-guides-collection#taking-prefixessuffixes-from-permission-plugin) and use `%luckperms-prefix%` & `%luckperms-suffix%`.
* Sorting:
  First, configure weights correctly in LuckPerms. Then, you have 2 options:
  * Configure [sorting by groups](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#groups) (recommended).
  * Sorting by weights directly: `PLACEHOLDER_HIGH_TO_LOW:%luckperms_highest_group_weight%`.

## #3 - I enabled MySQL, and my prefix is now gone!
When enabling [MySQL](https://github.com/NEZNAMY/TAB/wiki/MySQL), it will be used as a data storage and groups.yml / users.yml files will no longer be used.  
If you want to upload your configuration to MySQL (or download it back to files), use [MySQL conversion commands](https://github.com/NEZNAMY/TAB/wiki/MySQL#data-conversion) - [`/tab mysql upload`](https://github.com/NEZNAMY/TAB/wiki/MySQL#uploading-from-files-to-mysql) and [`/tab mysql download`](https://github.com/NEZNAMY/TAB/wiki/MySQL#downloading-from-mysql-to-files), respectively.  
After you are done, run `/tab reload` for the changes to take effect.

## #4 - Why is player sorting not working?
See [Common mistakes section on sorting page](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#common-mistakes).

## #5 - How to add players to the %staffonline% placeholder?
Give them the `tab.staff` permission.

## #6 - How to make player heads visible in tablist?
Players who did not buy the game will never be able to see heads.
The client displays Player heads when connection to the server is an online connection authenticated through Mojang.
It cannot be directly controlled by plugins.
The intended way to reach this is by setting `online-mode=true` in `server.properties`.  
If you want to allow pirates on your server,
you can still display heads for players who bought the game by changing their connections to online connections.
Most commonly used plugins for this are [FastLogin](https://www.spigotmc.org/resources/14153/) and [JPremium](https://www.spigotmc.org/resources/27766/),
which change connections of premium players who enabled it to online connections.  
The same goes for disabling heads - you cannot disable them if you have online mode enabled.

## #7 - Where can i find current default config files?
You can do any of the following:
* Check the [source code](https://github.com/NEZNAMY/TAB/tree/master/shared/src/main/resources/config).
* Delete or rename a file, and the plugin will regenerate it.
* Open the plugin jar as a zip file and take files from there.

## #8 - How can I split players into multiple columns?
The client puts players into columns, it is not managed by the server.
The only way is to get more players.
They automatically split into more columns at 21, 41 and 61 players (due to limit of 20 entries per column).
If you don't mind getting all 80 slots filled with fake player slots,
check out the [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) feature.

## #9 - How to use spaces in prefix/suffix command?
Use "", for example `/tab group owner tabprefix "&2&lOwner&r "`

## #10 - Can I change/remove the green connection bar in tablist?
The connection bar is client sided and cannot be re-textured or removed by TAB.
You will need a custom minecraft client or a resource pack.

## #11 - Placeholder is not working
Most common reasons for a placeholder to not work include:
* Using % symbol by itself, which breaks placeholder starts and ends and therefore breaks all placeholders after it (use %% to display the symbol)
* Trying to use a PlaceholderAPI placeholder without downloading its expansion or not having PlaceholderAPI installed at all
* Trying to use [TAB's internal bukkit-only placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#bukkit-only) on BungeeCord
* Trying to use [PlaceholderAPI placeholders on BungeeCord](https://github.com/NEZNAMY/TAB/wiki/How-to-set-up-PlaceholderAPI-support-on-bungeecord) without installing [bridge plugin](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge)

The full list of reasons can be found on the [Placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#placeholder-is-not-working) page.

## #12 - How can I display ItemsAdder images?
ItemsAdder offers placeholders, which return the respective symbol to which your desired image is bound to. You can get them using ItemsAdder's PlaceholderAPI expansion using their `%img_<name>%` placeholder. ItemsAdder's internal placeholders (`:something:`) will not work in TAB.

If you are unable to change the references
(for example, because they are defined in permission plugin and used elsewhere as well),
you can use [PlaceholderAPI](https://github.com/NEZNAMY/TAB/wiki/Quick-PlaceholderAPI-startup-guide)'s `imgfix` expansion
that replaces format of given placeholders into the correct format.  
Example: `%luckperms_prefix%` -> `%imgfix_{luckperms_prefix}%`

## #13 - How can I use UTF characters in configuration?
**Option 1**: [Save the file in UTF-8 encoding](https://github.com/NEZNAMY/TAB/wiki/How-to-save-the-config-in-UTF8-encoding) and use your desired symbol directly.

**Option 2**:  Find hex code of your symbol (4 hexadecimal numbers) and use `\uxxxx` format,
where `xxxx` is hex code of the symbol.  
Make sure to also wrap the text into `""` for the text to properly take `\` as an escape symbol.
Using `''` will result in the text being display literally, not as a code.

**Note:** Minecraft does not support every single UTF symbol and displays unsupported symbols as a box. You can try sending your symbol into chat and see if it works or not. If not, it's not supported by MC.

## #14 - Is there a way to remove all players from tablist?
In Minecraft, for a player to be visible in-game, they also need to appear in the tablist.
Remember the issue with NPCs showing up in the tablist? It's the same underlying mechanic.

If you're okay with players not being visible in the world, you can use a plugin that hides all players completely.

Since Minecraft 1.19.3, this limitation has been lifted — it's now possible to have entities in the game without showing them in the tablist.
However, the TAB plugin does **not** support removing all players from the tablist in any version of Minecraft.

An alternative solution using TAB is putting a lot of empty lines into header,
which will push all players out of the screen.

## #15 - How to add images to tablist?
You can check out [this reddit post](https://www.reddit.com/r/admincraft/comments/llrgty/comment/gnswdcz/?utm_source=share&utm_medium=web2x&context=3).
When using the symbol in configuration using \u format, remember to use `""` in config instead of `''`.

## #16 - Is MiniMessage supported?
Yes, however, it must be included in your server software (it is only in Paper / Velocity). Read more about MiniMessage hook [here](https://github.com/NEZNAMY/TAB/wiki/How-to-use-Minecraft-components#minimessage).

## #17 - Is it possible to show the number of players in a specific group, similar to staffonline placeholder?
The plugin's internal placeholders are limited, and this kind of functionality is not included.
However,
you can achieve this using PlaceholderAPI's `PlayerList` expansion
and following [this example](https://github.com/Tanguygab/PlayerList-PlaceholderAPI-Expansion/wiki#list-of-players-in-group).

## #18 - Why is the plugin flagged as a virus by Windows defender?
First, this is a false positive.
[Here](https://www.spigotmc.org/threads/windows-defender-false-positives.639507/)'s a spigot thread confirming it.

After experimenting with VirusTotal,
it turns out the antivirus software available on the website used flags [bStats](https://github.com/Bastian/bStats) library as malicious.
Its purpose is to collect anonymous data about servers and submitting it to [bStats.org](https://bstats.org/),
where plugin authors can see how many servers use their plugins (if they don't disable it).
It also checks for CPU and OS types, which could be the reason for the false positive.
[Here](https://bstats.org/plugin/bukkit/TAB%20Reborn/5304) is one of TAB's pages as an example.

Windows defender could be flagging it for the same reason.