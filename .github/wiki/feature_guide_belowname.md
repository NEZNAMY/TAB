# Content
* [About](#about)
* [Configuration](#configuration)
* [Additional info](#additional-info)
  * [Additional note 1 - Copying nametag visibility rule](#additional-note-1---copying-nametag-visibility-rule)
  * [Additional note 2 - Hidden on sneak on 1.8](#additional-note-2---hidden-on-sneak-on-18)
  * [Additional note 3 - Visible on NPCs](#additional-note-3---visible-on-npcs)
* [Tips & Tricks](#tips--tricks)
  * [Tip 1 - Heart symbol](#tip-1---heart-symbol)
  * [Tip 2 - Displaying health as 0-10 or in %](#tip-2---displaying-health-as-0-10-or-in-)
* [Limitations](#limitations)
* [Examples](#examples)
  * [Example 1 - Per-world values](#example-1---per-world-values)
  * [Example 2 - Hiding `title` for 1.20.3+ players](#example-2---hiding-title-for-1203-players)

# About
The scoreboard objective with BELOW_NAME display slot that adds another line of text below nametag of players.
It is only visible on players within 8-block range.
This value is client sided and cannot be changed by the plugin.  
![](https://images-ext-1.discordapp.net/external/YlGPCRDJVeZZI0TPWmVBKyHszxSkjatmclyqUThvTz8/https/image.prntscr.com/image/jcETUzVQQYqectQ2aI4iqQ.png)

This line automatically appears on all player entities when enabled. **Therefore, it is not possible to only display it on some players or not display it on NPCs**.

The feature can be configured in **config.yml** under **belowname-objective** section.

The feature's properties are displayed as `[score]` + space + `title` (no, the space cannot be removed), where `[score]` is:
* `value` for 1.20.2- (will show `0` on NPCs)
* `fancy-value` for 1.20.3+ (will show `fancy-value-default` on NPCs)

# Configuration
| Option name         | Default value         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
|---------------------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled             | true                  | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| value               | %health%              | [1.20.2-] An integer from -2147483648 to 2147483647, doesn't support decimal values. The number is always white. Supports placeholders with player-specific output, such as player health. Only visible on 1.20.2 and lower. <br/> **Note**: Even if you only support 1.20.3+, you still need to configure this value to properly evaluate to a number, because the value is still sent to the client (just not displayed). You can set it to `0` for simplicity.                                                              |
| fancy-value         | &c%health%            | [1.20.3+] Any text, supports placeholders with per-player output. Only visible on 1.20.3+.                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| fancy-value-default | NPC                   | [1.20.3+] Default display for all player entities. Plugin uses the value above for every real player, therefore this default value will only appear on player entities which are not actual players, a.k.a. NPCs. Only visible on 1.20.3+.                                                                                                                                                                                                                                                                                     |
| title               | Health                | Text displayed after `value`. It is formally called scoreboard title, which is displayed on all player entities and value is shared on all players and therefore not bound to specific players, which makes it unable to display the value per-player. It should only contain static text or placeholders which return same value for all players. All inserted placeholders are parsed for the player viewing the text. This is intentional to allow advanced configuration with conditions for per-world values for example. |
| disable-condition   | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever.                                                                                                                                                                                                                                                                                                                            |

# Additional Info
## Additional note 1 - Copying nametag visibility rule
The minecraft feature is programmed to be affected by nametag visibility rule.
This means that when the nametag is set to invisible, belowname will be invisible as well.

## Additional note 2 - Hidden on sneak on 1.8
Belowname is not visible on 1.8.x clients when player is sneaking. This is client-sided behavior and cannot be changed by the server.

## Additional note 3 - Visible on NPCs
Belowname objective is automatically attached to all entities of player type with the default value of
`0` (<1.20.3) or value configured as `fancy-value-default` (1.20.3+).
This includes player NPCs.

There is currently only one way to make them not visible on NPCs.
From [Additional note 1 - Copying nametag visibility rule](#additional-note-1---copying-nametag-visibility-rule) we know
that belowname is not visible if player's nametag is invisible.
NPC plugin can take advantage of this by using teams to hide the original name and display a hologram instead.

This is how you can achieve it using the following popular NPC plugins:
* **Citizens**:
  * 1 - Select the NPC (`/npc select <ID>` or `/npc select` to select the nearest NPC)
  * 2 - Hide its original nametag (which also hides belowname) using `/npc name`
  * 3 - Display your desired text using `/npc hologram add <text>` ([more info](https://wiki.citizensnpcs.co/Commands#:~:text=the%20NPC%20hitbox-,/npc%20hologram,-add%20%5Btext%5D%20%7C%20set))

# Tips & Tricks
## Tip 1 - Heart symbol
If you want to show health of players and display a heart symbol as text, you can use this one: `❤`.
```
belowname-objective:
  title: "&4❤"
```
> [!IMPORTANT]
> Make sure your config is saved in UTF-8 encoding to properly read the heart symbol.

## Tip 2 - Displaying health as 0-10 or in %
If you want health to display health as 0-10 instead of 0-20, you can achieve it with PlaceholderAPI:
* `%math_0_{player_health}/2%` for 0-10
* `%math_0_{player_health}*5%` for 0-100

# Limitations
* [1.5 - 1.12.2] Title length is limited to 32 characters (including color codes).
* [1.5 - 1.20.2] `value` is limited to a white number.
* The format is a **[score] + space + shared title**. No, the space cannot be removed.
* The title is the same on all players, therefore, it cannot be personalized (such as player's faction). Only [score] can be per-player.
* It appears on all entities of player type. **This includes player NPCs.**

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
  belowname1:
    conditions:
    - "%world%=world1"
    true: "Text to display in world world1" # Player is in world world1, display the text
    false: "%condition:belowname2%" # Player is not in world world1, check another condition
  belowname2:
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
belowname-objective:
  fancy-value: "%condition:belowname1%"
```
If you are under 1.20.3 and want to make `value` and `title` conditional,
create two condition chains, one for each value and use them.

> [!NOTE]
> This is just an example, the plugin is not limited to displaying different values only per world.
> If you want per server values on BungeeCord, use %server% with server names.
> If you want WorldGuard regions, use %worldguard_region_name% with region names.
> This works for any placeholder offered by the plugin or by PlaceholderAPI.

## Example 2 - Hiding `title` for 1.20.3+ players
1.20.3 has replaced `value` field,
which is limited to a white number with value configurable as `fancy-value`, which has no limits.
However, `title` field is still visible on 1.20.3+.
If you support both version ranges and want to make the best out of the new functionality,
you might want to not show `title` to 1.20.3+ players anymore,
since you can fully customize the text you want to display in `fancy-value`.

First, let's remind ourselves what `title` *actually* is.
It is actually called scoreboard title.
Yes, just like the one on top of sidebar, just displayed elsewhere.
This is why it displays on all players and placeholders are parsed for the viewing player.
With this knowledge, all we need to do is check for player's version and only show something for <1.20.3.
For this, we will use `%player-version-id%` placeholder,
which returns the network protocol version of player's game version, which can then be compared to.
```
conditions:
  version:
    - "%player-version-id%>=765" # 1.20.3 is 765
  true: "" # Show empty value for 1.20.3+
  false: "Some text to display"
```
> [!WARNING]
> DO NOT JUST RANDOMLY PASTE THIS ENTIRE "CONDITIONS" SECTION INTO YOUR CONFIG!
> INSTEAD, EDIT YOUR EXISTING CONDITIONS SECTION TO PREVENT HAVING THE SECTION TWICE,
> HAVING SECOND ONE COMPLETELY OVERRIDE THE FIRST ONE!
```
belowname-objective:
  title: "%condition:version%"
```
Unfortunately, due to the limitations of this feature,
there is a forced space between `value`/`fancy-value` and `title`,
even if the `title` is empty.
You'll either need to live with it, or put some shared text there to avoid having a space at the end.  