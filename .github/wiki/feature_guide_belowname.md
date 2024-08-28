# Content
* [About](#about)
* [Configuration](#configuration)
* [Additional info](#additional-info)
    * [Additional note 1 - Copying nametag visibility rule](#additional-note-1---copying-nametag-visibility-rule)
    * [Additional note 2 - Hidden on sneak on 1.8](#additional-note-2---hidden-on-sneak-on-18)
* [Tips & Tricks](#tips--tricks)
    * [Tip 1 - Heart symbol](#tip-1---heart-symbol)
    * [Tip 2 - Displaying health as 0-10 or in %](#tip-2---displaying-health-as-0-10-or-in-)
* [Limitations](#limitations)

# About
The scoreboard objective with BELOW_NAME display slot that adds another line of text below nametag of players. It is only visible on players within 8 block range. This value is client sided and cannot be changed by the plugin.  
![](https://images-ext-1.discordapp.net/external/YlGPCRDJVeZZI0TPWmVBKyHszxSkjatmclyqUThvTz8/https/image.prntscr.com/image/jcETUzVQQYqectQ2aI4iqQ.png)

This line automatically appears on all player entities when enabled. **Therefore, it is not possible to only display it on some players or not display it on NPCs**.

The feature can be configured in **config.yml** under **belowname-objective** section.

The feature's properties are displayed as `[score]` + space + `text`, where `[score]` is:
* `number` for 1.20.2- (will show `0` on NPCs)
* `fancy-display-players` for 1.20.3+ (will show `fancy-display-default` on NPCs)

# Configuration
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| enabled | true | Enables / Disables the feature |
| number | %health% | [1.20.2-] An integer from -2147483648 to 2147483647, doesn't support decimal values. The number is always white. Supports placeholders with player-specific output, such as player health. Only visible on 1.20.2 and lower. <br/> **Note**: Even if you only support 1.20.3+, you still need to configure this value to properly evaluate to a number, because the value is still sent to the client (just not displayed). You can set it to `0` for simiplicity. |
| fancy-display-players | &c%health% | [1.20.3+] Any text, supports placeholders with per-player output. Only visible on 1.20.3+. |
| fancy-display-default | NPC | [1.20.3+] Default display for all player entities. Plugin uses the value above for every real player, therefore this default value will only appear on player entities which are not actual players, a.k.a. NPCs. Only visible on 1.20.3+. |
| text | Health | Text displayed after number. It is formally called scoreboard title, which is displayed on all player entities and value is shared on all players and therefore not bound to specific players, which makes it unable to display the value per-player. It should only contain static text or placeholders which return same value for all players. All inserted placeholders are parsed for the player viewing the text. This is intentional to allow advanced configuration with conditions for per-world values for example. |
| disable-condition | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever. |

# Additional Info
## Additional note 1 - Copying nametag visibility rule
The minecraft feature is programmed to be affected by nametag visibility rule. This means that when nametag is set to invisible, belowname will be invisible as well.

## Additional note 2 - Hidden on sneak on 1.8
Belowname is not visible on 1.8.x clients when player is sneaking. This is client-sided behavior and cannot be changed by the server.

# Tips & Tricks
## Tip 1 - Heart symbol
If you want to show health of players and display a heart symbol as text, you can use this one: `❤`. Make sure you [save config in UTF-8 encoding](https://github.com/NEZNAMY/TAB/wiki/How-to-save-the-config-in-UTF8-encoding) so it's loaded correctly.
```
belowname-objective:
  text: "&4❤"
```

## Tip 2 - Displaying health as 0-10 or in %
If you want health to display health as 0-10 instead of 0-20, you can achieve it with PlaceholderAPI:
* `%math_0_{player_health}/2%` for 0-10
* `%math_0_{player_health}*5%` for 0-100

# Limitations
* The format is a **[score] + space + shared text**, where [score] is either a white number (1.20.2-) or any text (1.20.3+).
* The text is same on all players, therefore cannot be personalized (such as player's faction). Only the [score] can be per-player.
* It appears on all entites of player type. **This includes player NPCs.**
* Text length is limited to 32 characters on <1.13  