# Content
* [About](#about)
* [Configuration](#configuration)
    * [Grouping playerlist from multiple worlds](#grouping-playerlist-from-multiple-worlds)

# About
This feature allows you to limit players you see in tablist to only players in your world, or in a group of worlds.  
This feature can be configured in **config.yml** under **per-world-playerlist** section.  
To use it, you need TAB installed on bukkit server. This feature is not available on BungeeCord.

# Configuration
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| enabled | false | Enables / Disables the feature. |
| allow-bypass-permission | false | If enabled, players with `tab.bypass` permission will see everyone in tablist despite this feature enabled. |
| ignore-effect-in-worlds | *List* | List of worlds where players will see everyone in tablist, not just players in the same world / world group |
| shared-playerlist-world-groups | *Map* | See [Grouping playerlist from multiple worlds](#grouping-playerlist-from-multiple-worlds) |

## Grouping playerlist from multiple worlds
In case you want multiple worlds to share playerlist despite this option enabled, create a group and put world names there. The default example
```
per-world-playerlist:
  shared-playerlist-world-groups:
    lobby:
      - lobby1
      - lobby2
    minigames:
      - paintball
      - bedwars
```
contains 2 groups, each of them consisting of 2 worlds. World group name can be anything (in our case they're called "lobby" and "minigames"). Under group name, list the actual world names.

With this setup, "lobby1" and "lobby2" will share playerlist, as well as "paintball" with "bedwars". All other worlds will have playerlist not shared with any other worlds. Creating a group for 1 world only is completely useless.  