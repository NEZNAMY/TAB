# Content
* [About](#about)
* [Configuration](#configuration)
  * [Layout](#layout)
    * [Fixed slots](#fixed-slots)
    * [Player groups](#player-groups)
  * [Chaining layouts](#chaining-layouts)
  * [Skins](#skins)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
* [Compatibility with other features](#compatibility-with-other-features)
  * [Playerlist objective incompatibility](#playerlist-objective-incompatibility)
  * [Global playerlist incompatibility](#global-playerlist-incompatibility)
  * [Per world playerlist incompatibility](#per-world-playerlist-incompatibility)
* [Additional info](#additional-info)
  * [Additional note 1 - [1.19.3 - 1.21.1] Entries in chat complete](#additional-note-1---1193---1211-entries-in-chat-complete)
  * [Additional note 2 - [1.8 - 1.21.3] Second layer of skin missing](#additional-note-2---18---1213-second-layer-of-skin-missing)
  * [Additional note 3 - Entry overlap](#additional-note-3---entry-overlap)
* [Examples](#examples)
  * [Example 1 - Per-server columns](#example-1---per-server-columns)

# About
This feature allows you to customize all 80 tablist slots. Displaying less than 4 columns is currently not supported ([here's why](https://gist.github.com/NEZNAMY/3dfcbf7d44283735d3c18266a2851651)). This feature can be enabled and configured in **config.yml** file under **layout** section.

This feature is only available for versions **1.8** and up due to massive tablist changes, which would make 1.7- compatibility require a complete rewrite of the functionality and could still cause all kinds of visual issues, including, but not limited to compatibility with other plugins adding/removing players from the tablist.

# Configuration
The feature can be configured in **config.yml** under **layout** section.  
This is how the default configuration looks:
```
layout:
  enabled: false
  direction: COLUMNS
  default-skin: mineskin:37e93c8e12cd426cb28fce31969e0674
  enable-remaining-players-text: true
  remaining-players-text: '... and %s more'
  empty-slot-ping-value: 1000
  layouts:
    default:
      slot-count: 80
      fixed-slots:
        - '1|&3Website&f:'
        - '2|&bmyserver.net'
        - '3|&8&m                       '
        - '4|&3Name&f:'
        - '5|&b%player%'
        - '7|&3Rank&f:'
        - '8|Rank: %group%'
        - '10|&3World&f:'
        - '11|&b%world%'
        - '13|&3Time&f:'
        - '14|&b%time%'
        - '21|&3Teamspeak&f:'
        - '22|&bts.myserver.net'
        - '23|&8&m                       '
        - '41|&3Store&f:'
        - '42|&bshop.myserver.net'
        - '43|&8&m                       '
      groups:
        staff:
          condition: permission:tab.staff
          slots:
            - 24-40
        players:
          slots:
            - 44-80
```
All the options are explained in the following table.

| Option name                   | Default value        | Description                                                                                                                                                                                                                                                                |
|-------------------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                       | true                 | Enables / Disables the feature                                                                                                                                                                                                                                             |
| direction                     | COLUMNS              | Defines direction of slots. Options are COLUMNS (top to bottom, left to right) and ROWS (left to right, top to bottom). This does not only change the slot numbers in configuration, but will also affect the way players are being filled into player groups.             |
| default-skin                  | "mineskin:383747683" | Default skin to display for fixed slots that do not define a skin, empty slots and fixed slots with invalid skin.                                                                                                                                                          |
| enable-remaining-players-text | true                 | When enabled and there are more players in a player group than available slots, the last slot of player group will show how many more players there are, instead of using the last slot for one more player.                                                               |
| remaining-players-text        | "... and %s more"    | Text to show if option above is enabled. Use `%s` variable for defining how many more players there are.                                                                                                                                                                   |
| empty-slot-ping-value         | 1000                 | Ping value to use for fixed slots and empty slots. The ping intervals for bars are client sided and are as following: <br />- Negative value: ✖ <br />- 0 - 149: 5 bars <br />- 150 - 299: 4 bars <br />- 300 - 599: 3 bars <br />- 600 - 999: 2 bars <br />- 1000+: 1 bar |
| layouts                       | *Map*                | Layouts to display based on conditions (see below for more info).                                                                                                                                                                                                          | 

## Layout
A layout consists of the following options:
| Option name | Description      |
|-------------|------------------|
| `condition` (optional) | [Condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) (either condition name or a conditional expression) that must be met for the layout to be displayed. If not defined, layout will be displayed without any required condition. If player does not meet condition, another layout may be displayed based on chaining (see below for more info). |
| `default-skin` (optional)      | Overrides global `default-skin` setting for this layout. |
| `slot-count` (optional)        | Amount of slots to show in this layout. If not specified, defaults to `80`. **This option is coming in TAB v5.5.0**. |
| `fixed-slots`                  | List of fixed slots, see below for more info. |
| `player-groups`                | Map of player groups, see below for more info. |

### Fixed slots
These are slots with fixed position that contain configurable text and skin.  
They can be configured under `fixed-slots` option of each layout. The definition syntax is `SLOT|TEXT[|SKIN]` (skin is optional). If you don't want to define custom skin for that slot, use `SLOT|TEXT`.  
`SLOT` - The position of the fixed slot. It can be from 1 to 80. By default, the direction is set to columns, so the first column is 1-20.  
`TEXT` - the text. It supports placeholders.  
`SKIN` - layout's skin, see below for configuration.  
Example:
```
    fixed-slots:
      - '1|&3RAM&f:'
      - '2|&b%memory-used%MB / %memory-max%MB'
      - '3|&8&m                       '
      - '4|&3TPS&f:'
      - '5|&b%tps%'
```
If you do not wish to use any fixed slots, make it an empty list (`[]`).  
Example:
```
  layouts:
    staff:
      fixed-slots: []
```

### Player groups
These are groups of players that meet the specified condition. They consist of 2 parts - condition and slots.

**1. Condition**  
The condition players must meet to be displayed in the group.
If a player doesn't meet condition for a group, the next group's condition is checked
(groups are checked in the order they are defined in config).
If player doesn't meet any condition, they are not displayed at all.
Display condition is not required (can be used to make a "default" group with the remaining players).

**2. Slots**  
Slot intervals dedicated to the group. Interval `1-5` would mean all slots from 1 to 5. In case you want to define multiple intervals to exclude some slots, define another interval below the first one.  
Example:
```
  layouts:
    myLayout:
      groups:
        staff:
          condition: permission:tab.staff
          slots:
            - '24-40'
        players:
          slots:
            - '44-80'
```
In this example, slots `24-40` will be reserved for players with `tab.staff` permission. Players without that permission will be in slots `44-80`.

## Chaining layouts
When defining more than 1 layout in config, the plugin will display the correct design based on conditions.
The plugin goes through all defined layouts, starting with the design on top.
If the layout's condition is met or not set, it is displayed.
If not, the next defined layout is checked and so on.
If no suitable layout is found (the last one has a condition requirement which wasn't met), no layout is displayed (player will see the normal tablist).
Example:
```
layouts:
  staff:
    condition: "permission:tab.staff"
    fixed-slots:
      ...
    groups:
      ...
  default:
    fixed-slots:
      ...
    groups:
      ...
```
In this example, players with `tab.staff` permission will see `staff` layout, others will see `default` layout.

## Skins
*If you're wondering how to enable / disable heads in the tablist, see [FAQ](https://github.com/NEZNAMY/TAB/wiki/Frequently-Asked-Questions#6---how-to-make-player-heads-visible-in-tablist).*

For players, their skin is displayed. For fixed slots and empty slots, you can specify which skin to show using the same skin syntax.  
Currently, TAB supports these skin formats:
| Format | Example | Description |
|--------|---------|-------------|
| `mineskin:<UUID>` | `mineskin:37e93c8e12cd426cb28fce31969e0674` | Takes UUID from [MineSkin](https://mineskin.org). |
| `player:<name>` | `player:Notch` | Displays skin of specified player. |
| `texture:<texture>` | `texture:469b3bac406b51ba0e76c2c218aa4d45fde9ea7c101c85fbd8106c92c4aa36dd` | Uses skin texture from `textures.minecraft.net`. Skin browsing websites show this value. |

# Compatibility with other plugins
This feature does not have any compatibility issues with other plugins.  
When it comes to compatibility with vanish plugins, see [Additional information - Vanish detection](https://github.com/NEZNAMY/TAB/wiki/Additional-information#vanish-detection).

# Compatibility with other features
## Playerlist objective incompatibility
To avoid showing fake players in tab-complete, TAB uses an empty string as the fake players' names.  
Though Minecraft's scoreboard objectives (which help achieve the Playerlist objective feature) only use players' names and not players' UUIDs to define values.  
As a result, it is impossible for TAB to assign a different value for each fake player since they all have the same name, making the [Playerlist objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective) feature incompatible.

## Global playerlist incompatibility
The way this feature works is by pushing all real players out of the tablist and showing fake players instead.  
As a result, enabling [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) feature won't make any difference, since real players aren't visible in the tablist.  
Layout feature is capable of working with all online players connected to the server where TAB is installed, meaning it can show players from all servers when installed on a proxy. Because of this, if you use layout, you can disable global playerlist to reduce resource usage.

## Per world playerlist incompatibility
Layout works by adding 80 fake players into the tablist, pushing real player entries out of view.
Because of this,
when using [per world playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Per-world-playerlist),
the feature will only hide the real players,
which are outside tablist and not visible anyway and not touch the layout entries.
However, you can replicate the same effect using layout feature itself, using conditions.
<details>
  <summary>Example</summary>

*We are going to ignore fixed slots in the examples,
as they don't affect anything, and you can just configure them however you want.*  
Let's create 2 layouts, one for world `world1` and one for world `world2`.
Each layout only displays players in that world.
```
  layouts:
    layout1:
      condition: "%world%=world1"
      fixed-slots: []
      groups:
        players:
          condition: "%world%=world1"
          slots:
            - 1-80
    layout2:
      condition: "%world%=world2"
      fixed-slots: []
      groups:
        players:
          condition: "%world%=world2"
          slots:
            - 1-80
```
You can also merge multiple worlds into a group.
```
  layouts:
    layout1:
      condition: "%world%=world1|%world%=world2"
      fixed-slots: []
      groups:
        players:
          condition: "%world%=world1|%world%=world2"
          slots:
            - 1-80
```
You can also replicate "ignore-effect-in-worlds" by creating a layout with condition for displaying, but not for groups.
```
  layouts:
    globalWorld:
      condition: "%world%=globalWorld"
      fixed-slots: []
      groups:
        players: # No condition here to show all players
          slots:
            - 1-80
```
</details>

# Additional info
## Additional note 1 - [1.19.3 - 1.21.1] Entries in chat complete
From 1.19.3 until 1.21.1 (inclusive), entries will also appear in chat complete.
The mechanic used to hide them on <1.19.3 can no longer be used since 1.19.3.
1.21.2 has added a new way of sorting players, which is being taken advantage of to restore empty chat complete.

## Additional note 2 - [1.8 - 1.21.3] Second layer of skin missing
Layout works by creating 80 fake entries, pushing real players out of the tablist.
Unfortunately, players must be spawned around the player viewing the tablist to see the second layer of their skin.
Since those fake entries you see are not the actual players and aren't spawned,
as a result, you won't see the second layers of skins.
This can only be fixed by actually letting the real entries display in the tablist,
which would require a rework of the feature, which is not planned.  
**1.21.4 has added a new tablist option called "show hats", which layout sets to true.
They will be visible for 1.21.4+ players.**

## Additional note 3 - Entry overlap
Tablist entries automatically adapt their width based on the longest entry.
The final width is global for all columns, not just using the longest width in each respective column.
When the requested width is higher than the game window can handle, the text in entries will start to overlap.
This is how it looks with 1/4 screen size, which is the default window size when launching Minecraft.  
<img width="855" height="519" alt="image" src="https://github.com/user-attachments/assets/2a256b25-c672-49ea-8c8f-d670aa9efd43" />  
This can be solved by increasing window size either manually, or by pressing the expand button on the top left, next to the "X".
If it still looks the same for you even though you have the game in full screen, your GUI scale can be messed up.

To fix it, go to `Esc -> Options -> Video Settings...` and set `GUI Scale: ` to a lower value.

If you are looking for a more systematic solution that will automatically work for all of your players and not just you, the only way is to make the entries shorter.
Here are a few tips how to do that:
* Avoid using suffix altogether
* Do not use bold (`&l`) code
* Use a shorter alternative for your prefixes, such as `A` instead of `Admin`
* Remove unnecessary brackets from prefix


# Examples
## Example 1 - Per-server columns
```
layout:
  enabled: true
  direction: COLUMNS
  default-skin: mineskin:383747683
  enable-remaining-players-text: true
  remaining-players-text: '... and %s more'
  empty-slot-ping-value: 1000
  layouts:
    default:
      fixed-slots:
      - "1|&2&lLobby"
      - "21|&2&lSurvival"
      - "41|&2&lCreative"
      - "61|&2&lSkyblock"
      groups:
        lobby:
          condition: "%server%=lobby"
          slots:
          - 2-20
        survival:
          condition: "%server%=survival"
          slots:
          - 22-40
        creative:
          condition: "%server%=creative"
          slots:
          - 42-60
        skyblock:
          condition: "%server%=skyblock"
          slots:
          - 62-80
```
You can also remove the last condition (in this case `condition: "%server=skyblock%"`) and the columns will be used for all other servers, instead of just a specified one.  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/81016b5a-9b40-445e-8bfc-58204f5457f5)
