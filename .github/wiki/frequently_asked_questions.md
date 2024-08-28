# Content
* [#1 - Why are NPCs showing up in the tablist?](#1---why-are-npcs-showing-up-in-the-tablist)
* [#2 - How to make TAB work with LuckPerms?](#2---how-to-make-tab-work-with-luckperms)
* [#3 - How can i make belowname not affect NPCs but keep it for players?](#3---how-can-i-make-belowname-not-affect-npcs-but-keep-it-for-players)
* [#4 - I enabled MySQL and my prefix is now gone!](#4---i-enabled-mysql-and-my-prefix-is-now-gone)
* [#5 - Why is player sorting not working?](#6---why-is-player-sorting-not-working)
* [#6 - How to add players to %staffonline% placeholder?](#7---how-to-add-players-to-the-staffonline-placeholder)
* [#7 - How to make player heads visible in tablist?](#8---how-to-make-player-heads-visible-in-tablist)
* [#8 - Where can i find current default config files?](https://github.com/NEZNAMY/TAB/tree/master/shared/src/main/resources)
* [#9 - How can I split players into multiple columns?](#10---how-can-i-split-players-into-multiple-columns)
* [#10 - How to use space in prefix/suffix command?](#11---how-to-use-spaces-in-prefixsuffix-command)
* [#11 - Can I change/remove the green connection bar in tablist?](#12---can-i-changeremove-the-green-connection-bar-in-tablist)
* [#12 - Placeholder is not working](#13---placeholder-is-not-working)
* [#13 - How can I display ItemsAdder images?](#14---how-can-i-display-itemsadder-images)
* [#14 - How can I use UTF characters in configuration?](#15---how-can-i-use-utf-characters-in-configuration)
* [#15 - Is there a way to remove all players from tablist?](#16---is-there-a-way-to-remove-all-players-from-tablist)
* [#16 - How to add images to tablist?](#17---how-to-add-images-to-tablist)
* [#17 - Is MiniMessage supported?](#18---is-minimessage-supported)
* [#18 - Is it possible to show amount of players in a specific group, similar to staffonline placeholder?](#19---is-it-possible-to-show-amount-of-players-in-a-specific-group-similar-to-staffonline-placeholder)

## #1 - Why are NPCs showing up in the tablist?
See [Citizens FAQ](https://wiki.citizensnpcs.co/Frequently_Asked_Questions#Why_are_NPCs_showing_up_in_the_tablist.3F).

## #2 - How to make TAB work with LuckPerms?
* If you want to take prefixes/suffixes, check [this guide](https://github.com/NEZNAMY/TAB/wiki/Mini-guides-collection#taking-prefixessuffixes-from-permission-plugin) and use `%luckperms-prefix%` & `%luckperms-suffix%`.
* Sorting:
  First, [Configure weights correctly in LuckPerms](https://github.com/NEZNAMY/TAB/wiki/How-to-setup-weights-priorities#luckperms). Then, you have 2 options:
    * Configure [sorting by groups](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#groups) (recommended).
    * Sorting by weights directly: `PLACEHOLDER_HIGH_TO_LOW:%luckperms_highest_group_weight%`.

## #3 - How can I make belowname not affect NPCs but keep it for players?
Make NPC plugin hide their original names (using teams) and place holograms for displaying text.

## #4 - I enabled MySQL and my prefix is now gone!
When enabling [MySQL](https://github.com/NEZNAMY/TAB/wiki/MySQL), it will be used as a data storage and groups.yml / users.yml files will no longer be used.  
If you want to upload your configuration to MySQL (or download it back to files), use [MySQL conversion commands](https://github.com/NEZNAMY/TAB/wiki/MySQL#data-conversion) - [`/tab mysql upload`](https://github.com/NEZNAMY/TAB/wiki/MySQL#uploading-from-files-to-mysql) and [`/tab mysql download`](https://github.com/NEZNAMY/TAB/wiki/MySQL#downloading-from-mysql-to-files), respectively.  
After you are done, run `/tab reload` for the changes to take effect.

## #5 - Why is player sorting not working?
See [Common mistakes section on sorting page](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist#common-mistakes).

## #6 - How to add players to the %staffonline% placeholder?
Give them the `tab.staff` permission.

## #7 - How to make player heads visible in tablist?
Player heads are displayed by the client when connection to the server is an online connection. It cannot be directly controlled by plugins. The most common way to reach this is by setting `online-mode=true` in `server.properties`.  
If you want to allow pirates on your server, you can still display heads for players who bought the game by changing their connections to online connections. Most commonly used plugins for this are [FastLogin](https://www.spigotmc.org/resources/14153/) and [JPremium](https://www.spigotmc.org/resources/27766/), which change connections of premium players who enabled it to online connections.  
Same goes for disabling heads - you cannot disable them if you have online mode enabled.

## #8 - Where can i find current default config files?
You can find all default files in the [source code](https://github.com/NEZNAMY/TAB/tree/master/shared/src/main/resources). You can also delete or rename a file and the plugin will regenerate it.

## #9 - How can I split players into multiple columns?
Players are put into columns by the client, it is not managed by the server. The only way is to get more players. They automatically split into more columns at 21, 41 and 61 players (due to limit of 20 entries per column). If you don't mind getting all 80 slots filled with fake player slots, check out the [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) feature.

## #10 - How to use spaces in prefix/suffix command?
Use "", for example `/tab group owner tabprefix "&2&lOwner&r "`

## #11 - Can I change/remove the green connection bar in tablist?
Connection bar is client sided and cannot be retextured or removed by TAB. You will need a custom minecraft client or a resource pack.

## #12 - Placeholder is not working
Most common reasons for a placeholder to not work include:
* Using % symbol by itself, which breaks placeholder starts and ends and therefore breaks all placeholders after it (use %% to display the symbol)
* Trying to use a PlaceholderAPI placeholder without downloading its expansion or not having PlaceholderAPI installed at all
* Trying to use [TAB's internal bukkit-only placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#bukkit-only) on BungeeCord
* Trying to use [PlaceholderAPI placeholders on BungeeCord](https://github.com/NEZNAMY/TAB/wiki/How-to-set-up-PlaceholderAPI-support-on-bungeecord) without installing [bridge plugin](https://www.mc-market.org/resources/20631/)

Full list of reasons can be found on the [Placeholders](https://github.com/NEZNAMY/TAB/wiki/Placeholders#placeholder-is-not-working) page.

## #13 - How can I display ItemsAdder images?
ItemsAdder offers placeholders, which return the respective symbol to which your desired image is bound to. You can get them using ItemsAdder's PlaceholderAPI expansion using their `%img_<something>%` placeholder. ItemsAdder's internal placeholders (`:something:`) will not work in TAB.

## #14 - How can I use UTF characters in configuration?
**Option 1**: [Save the file in UTF-8 encoding](https://github.com/NEZNAMY/TAB/wiki/How-to-save-the-config-in-UTF8-encoding) and use your desired symbol directly.

**Option 2**:  Find hex code of your symbol (4 hexadecimal numbers) and use `\uxxxx` format, where `xxxx` is hexcode of the symbol.  
Make sure to also wrap the text into `""` for the text to properly take `\` as an escape symbol. Using `''` will result in text being display literally, not as a code.

**Note:** Minecraft does not support every single UTF symbol and displays unsupported symbols as a box. You can try sending your symbol into chat and see if it works or not. If not, it's not supported by MC.

## #15 - Is there a way to remove all players from tablist?
For a player to be visible in game, they must be in the tablist as well. Remember the NPC in tablist issue? If you don't mind players not appearing in game, get a plugin that hides all players. If you want to see them in game however, your only choice is to put a lot of empty lines into header, which will push all players out of the screen.

## #16 - How to add images to tablist?
You can check out [this reddit post](https://www.reddit.com/r/admincraft/comments/llrgty/comment/gnswdcz/?utm_source=share&utm_medium=web2x&context=3). When using the symbol in configuration using \u format, don't forget to use `""` in config instead of `''`.

## #17 - Is MiniMessage supported?
Kind of. MiniMessage support **is** included, however, this doesn't mean it is guaranteed to work. Here are a few reasons why it may not work for you:
* MiniMessage library must be included in your server software. TAB does not include this library.
    * It **is** included in Paper 1.16.5 and higher, Velocity and Sponge 8.
    * It **is not** included in Spigot, any server software for 1.16.4 and lower, BungeeCord (and any of its forks), Sponge 7 and Fabric.
* You may not use any legacy color codes (&) in the text where you want MiniMessage support. If you use them, MiniMessage parser will throw an error. Therefore, if trying to use both legacy colors and MiniMessage syntax, MiniMessage syntax will not work.
* A plugin/mod might be shading an outdated/incomplete version of Adventure without relocating it, and java class loader might make TAB use the wrong source of the duplicated library.

This list is not final. There might still be other reasons why MiniMessage won't work for you which are not known yet. If you believe MiniMessage should be supported on your server but doesn't work, open a bug report.

## #18 - Is it possible to show amount of players in a specific group, similar to staffonline placeholder?
The plugin's internal placeholders are limited and this kind of functionality is not included. However, you can achieve this using PlaceholderAPI's `PlayerList` expansion and following [this example](https://github.com/Tanguygab/PlayerList-PlaceholderAPI-Expansion/wiki#list-of-players-in-group).