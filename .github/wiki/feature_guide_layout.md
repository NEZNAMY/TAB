# Content
* [About](#about)
* [Creating a layout](#creating-a-layout)
    * [Display condition](#display-condition)
    * [Fixed slots](#fixed-slots)
    * [Player groups](#player-groups)
* [Managing multiple layouts with conditions](#managing-multiple-layouts-with-conditions)
* [Skins](#skins)
    * [Format](#format)
* [Additional settings](#additional-settings)
* [Additional info](#additional-info)
    * [Additional note 1 - Playerlist objective incompatibility](#additional-note-1---playerlist-objective-incompatibility)
    * [Additional note 2 - Global playerlist incompatibility](#additional-note-2---global-playerlist-incompatibility)
    * [Additional note 3 - Entries in chat complete](#additional-note-3---entries-in-chat-complete)
    * [Additional note 4 - Per world playerlist incompatibility](#additional-note-4---per-world-playerlist-incompatibility)
* [Examples](#examples)
    * [Example 1 - Per-server columns](#example-1---per-server-columns)

# About
This feature allows you to customize all 80 tablist slots. Displaying less than 4 columns is currently not supported. This feature can be enabled and configured in **config.yml** file under **layout** section.

This feature is only available for versions **1.8** and up due to massive tablist changes, which would make 1.7- compatibility require a complete rewrite of the functionality and could still cause all kinds of visual issues, including, but not limited to compatibility with other plugins adding/removing players from the tablist.

# Creating a layout
Layouts can be creating in config under `layout.layouts` section. Each layout contains 3 settings: Fixed slots, player groups and display condition (optional).

## Display condition
Condition that must be met for player to see the layout. If player does not meet the condition, next defined layout's condition is checked. If player does not meet any defined display condition, they will see default tablist. Display condition is optional and doesn't need to be set.  
For list of possible condition types see [Conditions page](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders).  
Example:
```
layouts:
  staff:
    condition: "permission:tab.staff"
```
This layout will only be visible to players with `tab.staff` permission.

## Fixed slots
These are slots with fixed position that contain configurable text and skin.  
They can be configured under `fixed-slots` option of each layout. The definition syntax is `SLOT|TEXT|SKIN`. If you don't want to define custom skin for that slot, use `SLOT|TEXT`.  
`SLOT` - the position of the fixed slot. It can be from 1 to 80. By default, the direction is set to columns, so the first column is 1-20.  
`TEXT` - the text. It supports placeholders.  
`SKIN` - layout's skin, see below for configuration.  
Example:
```
layouts:
  staff:
    fixed-slots:
      - '1|&3RAM&f:'
      - '2|&b%memory-used%MB / %memory-max%MB'
      - '3|&8&m                       '
      - '4|&3TPS&f:'
      - '5|&b%tps%'
```

## Player groups
These are groups of players which meet the specified condition. They consist of 2 parts - condition and slots.

**1. Condition**  
Condition players must meet to be displayed in the group. If a player doesn't meet condition for group, the next group's condition is checked (groups are checked in the order they are defined in config). If player doesn't meet any codnition, they are not displayed at all. Display condition is not required (can be used for default group).

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

# Managing multiple layouts with conditions
You can define multiple layouts and display them based on a condition. To do so, define `condition` for layouts. Conditions are checked in the order layouts are defined in config. If layout doesn't specify condition, it is displayed with no requirement. If player does not meet display condition for any layout, they will see the classic tablist.  
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

# Skins
In slots where players are, their skin is displayed. For fixed slots, if their skin is defined, it is used, otherwise `default-skin` is used. It is also used if invalid skin is specified anywhere.

## Format
Currently, TAB supports 3 skin formats:
* `mineskin:<ID>` - takes UUID from [MineSkin](https://mineskin.org). You can find some pre-uploaded skins at the bottom of this page.
* `player:<name>` - displays skin of player `name`
* `texture:<texture>` - uses literal skin texture. This lets you use heads from [Minecraft-Heads](https://minecraft-heads.com/custom-heads) by taking the texture in the `Minecraft-URL` field.    
  ![](https://cdn.discordapp.com/attachments/817789229479624728/916373892237512715/unknown.png)

Some pre-uploaded Mineskin skins:
```yml
BTLP's balance.png: 510604280
BTLP's clock.png: 2053951237
BTLP's server.png: 604037953
BTLP's ping.png: 796704708
BTLP's players.png: 1160612233
BTLP's rank.png: 527763880
Aqua: 1749359849
Black: 1551047136
Blue: 1870064311
Dark Aqua: 1893783461
Dark Blue: 899643609
Dark Gray: 383747683
Dark Green: 588254656
Dark Purple: 2061328517
Dark Red: 924139093
Gold: 1074335506
Gray: 745109047
Green: 224445819
Pink: 1818936290
Red: 1160568696
White: 1105851698
Yellow: 1307755006
```

# Additional settings
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| direction | COLUMNS | Defines direction of slots. Options are COLUMNS (top to bottom, left to right) and ROWS (left to right, top to bottom). This does not only change the slot numbers in configuration, but will also affect the way players are being filled into player groups. |
| enable-remaining-players-text | true | When enabled, the last slot of player group will show how many more players there are, instead of using the last slot for one more player.
| remaining-players-text | "... and %s more" | Text to show if option above is enabled.
| default-skin | "mineskin:1753261242" | Default skin to display for fixed slots that do not define a skin, empty slots and fixed slots with invalid skin.
| empty-slot-ping-value | 1000 | Ping value to use for fixed slots and empty slots. The ping intervals for bars are client sided and are as following: <br />- Negative value: ✖ <br />- 0 - 149: 5 bars <br />- 150 - 299: 4 bars <br />- 300 - 599: 3 bars <br />- 600 - 999: 2 bars <br />- 1000+: 1 bar

![image](https://user-images.githubusercontent.com/6338394/179363352-40f815d4-fc37-4ca1-8056-298488e84a60.png)
![image](https://user-images.githubusercontent.com/6338394/179355373-3e50b8c5-95d7-4470-b93f-deb162ccc145.png)

# Additional info
## Additional note 1 - Playerlist objective incompatibility
To avoid showing fake players in tab-complete, TAB uses an empty string as the fake players' names.  
Though Minecraft's scoreboard objectives (which help achieve the Playerlist objective feature) only use players' names and not players' UUIDs to define values.  
As a result, it is impossible for TAB to assign a different value for each fake player since they all have the same name, making the [Playerlist objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective) feature incompatible.

## Additional note 2 - Global playerlist incompatibility
The way this feature works is by pushing all real players out of the tablist and showing fake players instead.  
As a result, enabling [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) feature won't make any difference, since real players aren't visible in the tablist.  
Layout feature is capable of working with all online players connected to the server where TAB is installed, meaning it can show players from all servers when installed on BungeeCord. Because of this, if you use layout, you can just disable global playerlist to reduce resource usage.

## Additional note 3 - Entries in chat complete
Since 1.19.3, entries will also appear in chat complete. The mechanic I was using to hide them on older versions can no longer be used since 1.19.3. There is currently no way to avoid this. Not even everyone's beloved Hypixel found a solution to this.

## Additional note 4 - Per world playerlist incompatibility
Layout works by adding 80 fake players into the tablist, pushing real player entries out of view. Because of this, when using [per world playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Per-world-playerlist), the feature will only hide the real players, which are outside of tablist and not visible anyway and not touch the layout entries. However, you can replicate the same effect using layout feature itself, using conditions.
<details>
  <summary>Example</summary>

*We are going to ignore fixed slots in the examples, as they don't affect anything and you can just configure them however you want.*  
Let's create 2 layouts, one for world `world1` and one for world `world2`. Each layout only displays players in that world.
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

# Examples
## Example 1 - Per-server columns
```
layout:
  enabled: true
  direction: COLUMNS
  default-skin: mineskin:1753261242
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
