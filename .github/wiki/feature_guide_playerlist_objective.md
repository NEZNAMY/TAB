# Content
* [About](#about)
* [Configuration](#configuration)
* [Limitations](#limitations)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
* [Additional info](#additional-info)
  * [Additional note 1 - Spectator gamemode](#additional-note-1---spectator-gamemode)
* [Examples](#examples)
  * [Example 1 - Per-world values](#example-1---per-world-values)

# About
This features gives you control over Minecraft's scoreboard objective feature with PLAYER_LIST slot.
On 1.20.2- it supports 2 display types:
* yellow number  
  ![](https://images-ext-1.discordapp.net/external/ioDTFWFe9qUGg8ZgNFCPIoXN6B-EnbqHb0WXE9200a8/https/image.prntscr.com/image/w8sjR4y9QhuaEcnU5tGTmw.png)
* hearts  
  ![](https://images-ext-2.discordapp.net/external/RxWu_5hBSLUWqS7vCvSPY9PnNxkYfAMQQXwkbi6GEyU/https/image.prntscr.com/image/edpM4XpOT1q3SsQ5vYNjzQ.png)

Since 1.20.3, it can display any text.  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/2300b73e-d0cb-4eec-8ff1-e16be60bba49)

# Configuration
The feature can be configured in **config.yml** under **playerlist-objective** section.  
This is how the default configuration looks:
```
playerlist-objective:
  enabled: true
  value: "%ping%"
  fancy-value: "&7Ping: %ping%"
  title: "TAB" # Only visible on Bedrock Edition
  render-type: INTEGER
  disable-condition: '%world%=disabledworld'
```
All of the options are explained in the following table.
| Option name       | Default value         | Description                                                                                                                                                                                                                                                                                                                                                          |
|-------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled           | true                  | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                       |
| value             | "%ping%"              | [1.20.2-] An integer from -2147483648 to 2147483647, doesn't support decimal values. The number is always yellow. Supports placeholders with player-specific output, such as player health. Only visible on 1.20.2 and lower. <br/> **Note**: Even if you only support 1.20.3+, you still need to configure this value to properly evaluate to a number, because the value is still sent to the client (just not displayed). You can set it to `0` for simplicity. |
| fancy-value       | "&7Ping: %ping%"      | [1.20.3+] Any text, supports placeholders with per-player output. Only visible on 1.20.3+, where it completely replaces `value`.                                                                                                                                                                                                                                                                                                   |
| title             | "TAB"                 | Title to send. Only visible on Bedrock Edition.                                                                                                                                                                                                                                                                                                                      |
| render-type       | INTEGER               | Render type of the value. Supported values are `INTEGER` and `HEARTS`.                                                                                                                                                                                                                                                                                                |
| disable-condition | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) (either name of a condition or a conditional expression) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever. <br/> **Note**: Disabling the feature for a player means sending objective unregister packet to them, which results in player not seeing the feature on anyone anymore. It doesn't work the other way around - you cannot disable this feature on target players, only for viewers.                                                                                                                                                                  |

# Limitations
* [1.20.2-] Only 2 display types are supported - yellow number and hearts.
* [1.20.2-] A yellow 0 will appear on all tablist entries that aren't actual players (such as NPCs and [layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) entries) and it cannot be removed.

# Compatibility with other plugins
TAB does not contain any sort of compatibility functionality for this feature.
It will not try to prevent other plugins from applying the feature and neither will it detect it to re-add back once the other plugin removes it.
Therefore, if another plugin also sends playerlist objective, TAB's may not show anymore (depending on who sends it first).
Make sure you do not have any other plugin sending it to ensure the feature works properly.

You can check the list of objectives registered by either commands or plugins using Bukkit API using `/scoreboard objectives list`.  
If it contains one that could be the cause, you can unregister it by running `/scoreboard objectives remove <name>`. After doing so, reload TAB, so it resends its objectives again.  
Note that if this was automatically generated by a plugin, it will probably be added back again.

# Additional info
## Additional note 1 - Spectator gamemode
The feature will not be visible on players with spectator gamemode. To resolve this, check out [Spectator fix](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Spectator-fix) feature.

# Examples
## Example 1 - Per-world values
This feature doesn't directly support per-world values or similar.
However, this can be achieved with conditions.
Let's make `fancy-value` conditional based on player's world.
Let's make an example with 3 worlds.
Check if player is in world `world1`, then show one text.
If not, check if player is in world `world2`, then show another text.
If not, show the final text.
This can be achieved by chaining 2 conditions:
```
conditions:
  playerlist1:
    conditions:
    - "%world%=world1"
    true: "Text to display in world world1" # Player is in world world1, display the text
    false: "%condition:playerlist2%" # Player is not in world world1, check another condition
  playerlist2:
    conditions:
    - "%world%=world2"
    true: "Text to display in world world2" # Player is in world world2, display the text
    false: "Text to display in other worlds" # Player is not in any of the 2 worlds
```
> [!WARNING]
> DO NOT JUST RANDOMLY PASTE THIS ENTIRE "CONDITIONS" SECTION INTO YOUR CONFIG!
> INSTEAD, EDIT YOUR EXISTING CONDITIONS SECTION TO PREVENT HAVING THE SECTION TWICE,
> HAVING SECOND ONE COMPLETELY OVERRIDE THE FIRST ONE!

Finally, use this condition as value in `fancy-value`:
```
playerlist-objective:
  fancy-value: "%condition:playerlist1%"
```

> [!NOTE]
> This is just an example, the plugin is not limited to displaying different values only per world.
> If you want per server values on proxy, use %server% with server names.
> This works for any placeholder offered by TAB or by PlaceholderAPI.
