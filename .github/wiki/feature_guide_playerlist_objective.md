# Content
* [About](#about)
* [Configuration](#configuration)
* [Limitations](#limitations)
* [Additional info](#additional-info)
    * [Additional note 1 - Spectator gamemode](#additional-note-1---spectator-gamemode)

# About
The scoreboard objective with PLAYER_LIST display slot. On 1.20.2- it supports 2 display types:
* yellow number  
  ![](https://images-ext-1.discordapp.net/external/ioDTFWFe9qUGg8ZgNFCPIoXN6B-EnbqHb0WXE9200a8/https/image.prntscr.com/image/w8sjR4y9QhuaEcnU5tGTmw.png)
* hearts  
  ![](https://images-ext-2.discordapp.net/external/RxWu_5hBSLUWqS7vCvSPY9PnNxkYfAMQQXwkbi6GEyU/https/image.prntscr.com/image/edpM4XpOT1q3SsQ5vYNjzQ.png)

Since 1.20.3, it can display any text.  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/2300b73e-d0cb-4eec-8ff1-e16be60bba49)

This feature can be configured in **config.yml** under **playerlist-objective** section.

# Configuration
| Option name       | Default value         | Description                                                                                                                                                                                                                                                                                                                                                          |
|-------------------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled           | true                  | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                       |
| value             | "%ping%"              | [1.20.2-] Defines value displayed. You can set this to any placeholder that outputs a number. Only visible on 1.20.2 and lower. <br/> **Note**: Even if you only support 1.20.3+, you still need to configure this value to properly evaluate to a number, because the value is still sent to the client (just not displayed). You can set it to `0` for simplicity. |
| fancy-value       | "&7Ping: %ping%"      | [1.20.3+] Value to display for 1.20.3+ clients. Supports any text.                                                                                                                                                                                                                                                                                                   |
| title             | "TAB"                 | Title to send. Only visible on Bedrock Edition.                                                                                                                                                                                                                                                                                                                      |
| render-type       | INTEGER               | Render type of the value. Supported values are `INTEGER` and `HEARTS`                                                                                                                                                                                                                                                                                                |
| disable-condition | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever.                                                                                                                                                                  |

# Limitations
* [1.5 - 1.20.2] Only 2 display types are supported - yellow number and hearts.
* [1.5 - 1.20.2] A yellow 0 will appear on all tablist entries that aren't actual players (such as NPCs and [layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) entries) and it cannot be removed.

# Additional info
## Additional note 1 - Spectator gamemode
The feature will not be visible on players with spectator gamemode. To resolve this, check out [Spectator fix](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Spectator-fix) feature.  