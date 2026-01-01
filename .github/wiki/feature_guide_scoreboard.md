# Content
* [About](#about)
* [Configuration](#configuration)
  * [Scoreboard](#scoreboard)
    * [Line syntax](#line-syntax)
    * [Hiding a line](#hiding-a-line)
    * [Longer lines](#longer-lines)
  * [Chaining scoreboards](#chaining-scoreboards)
* [Commands](#commands)
* [Placeholders](#placeholders)
  * [PlaceholderAPI placeholders](#placeholderapi-placeholders)
* [Limitations](#limitations)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
* [Additional info](#additional-info)
  * [Additional note 1 - Text alignment](#additional-note-1---text-alignment)
  * [Additional note 2 - Geyser / Bedrock issues](#additional-note-2---geyser--bedrock-issues)
* [API](#api)
  * [Creating custom scoreboards](#creating-custom-scoreboards)
  * [Showing custom scoreboards](#showing-custom-scoreboards)
  * [Toggling scoreboard visibility](#toggling-scoreboard-visibility)
  * [Announcing a scoreboard](#announcing-a-scoreboard)
* [Examples](#examples)
  * [Example 1 - Per-world scoreboards](#example-1---per-world-scoreboards)
  * [Example 2 - Periodical scoreboard switching](#example-2---periodical-scoreboard-switching)
  * [Example 3 - Per-version scoreboards](#example-3---per-version-scoreboards)
  * [Example 4 - Conditional lines](#example-4---conditional-lines)

# About
This features gives you control over Minecraft's scoreboard objective feature with SIDEBAR slot.

# Configuration
The feature can be configured in **config.yml** under **scoreboard** section.  
This is how the default configuration looks:
```
scoreboard:
  enabled: false
  toggle-command: /sb
  remember-toggle-choice: false
  hidden-by-default: false
  delay-on-join-milliseconds: 0
  scoreboards:
    scoreboard-1.20.3+:
      title: "<#E0B11E>MyServer</#FF0000>"
      display-condition: "%player-version-id%>=765;%bedrock%=false" # Only display it to players using 1.20.3+ AND NOT bedrock edition
      lines:
        - "&7%date%"
        - "%animation:MyAnimation1%"
        - "&6Online:"
        - "* &eOnline&7:||%online%"
        - "* &eCurrent World&7:||%worldonline%"
        - "* &eStaff&7:||%staffonline%"
        - ""
        - "&6Personal Info:"
        - "* &bRank&7:||%group%"
        - "* &bPing&7:||%ping%&8ms"
        - "* &bWorld&7:||%world%"
        - "%animation:MyAnimation1%"
    scoreboard:
      title: "<#E0B11E>MyServer</#FF0000>"
      lines:
        - "&7%date%"
        - "%animation:MyAnimation1%"
        - "&6Online:"
        - "* &eOnline&7: &f%online%"
        - "* &eCurrent World&7: &f%worldonline%"
        - "* &eStaff&7: &f%staffonline%"
        - ""
        - "&6Personal Info:"
        - "* &bRank&7: &f%group%"
        - "* &bPing&7: &f%ping%&8ms"
        - "* &bWorld&7: &f%world%"
        - "%animation:MyAnimation1%"
```
All of the options are explained in the following table.  
| Option name                | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|----------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                    | false         | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                             |
| toggle-command             | /sb           | Command that can be used to toggle scoreboard for the player running the command. Players need `tab.scoreboard.toggle` permission to use it. <br />**Note:** Commands are not supposed to be registered / unregistered dynamically at runtime. As such, you may run into small issues when changing the toggle command and reloading. After you modify the command to your liking, reconnect to properly see it in tabcomplete. If you don't use TAB on a proxy, consider restarting the server as well for the old command to actually be unregistered. |
| remember-toggle-choice     | false         | When enabled, toggling decision is saved into a file to remember it even after reloads/restarts/reconnects                                                                                                                                                                                                                                                                                                                                                 |
| hidden-by-default          | false         | If enabled, scoreboard will automatically be hidden on join until toggle command is used to show it.                                                                                                                                                                                                                                                                                                                                                       |
| use-numbers                | false         | If enabled, numbers 1-15 will be used in the scoreboard. If disabled, `static-number` is shown everywhere. Will not be visible for 1.20.3+ players, instead, you can configure any text to show using `\|\|text` in the lines (scroll up for more info).                                                                                                                                                                                                   |
| static-number              | 0             | If `use-numbers` is disabled, this is number to be in all lines.                                                                                                                                                                                                                                                                                                                                                                                           |
| delay-on-join-milliseconds | 0             | Delay in milliseconds to send scoreboard after joining.                                                                                                                                                                                                                                                                                                                                                                                                    |
| scoreboards                | *Map*         | Scoreboards to display based on conditions (see below for more info).
|

## Scoreboard
A scoreboard consists of the following options:

| Option name                    | Description                                                                                                                                                                                                                                                                                                                                                                                                      |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `display-condition` (optional) | [Condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) (either condition name or a conditional expression) <br /> that must be met for the design to be displayed. <br />If not defined, design will be displayed without any required condition. <br />If player does not meet condition, another design may be displayed <br /> based on chaining (see below for more info). |
| `title `                       | Title of the scoreboard. Limited to 32 characters (including color codes) on Minecraft versions below 1.13. It is automatically centered by the client.                                                                                                                                                                                                                                                          |
| `lines`                        | List of lines to display. The client will only display up to 15 lines. See below for more information about syntax.                                                                                                                                                                                                                                                                                              |

**Example**:
```
  scoreboards:
    MyScoreboard:
      display-condition: "%world%=world" # Only display it in world "world"
      title: "Scoreboard for world %world%"
      lines:
        - "Line1||Right side 1"
        - "Line2||Right side 2"
        - "Line3||Right side 3"
```

### Line syntax
The lines are specified as a list of strings.

Since 1.20.3, you can also configure the value on the right side, which no longer has to be a red number. To set it, use `||` sequence and put right-side text after it. Example:
```
      lines:
      - "Left 1---"
      - "||---Right 1"
      - "Left 2------"
      - "||------Right 2"
      - "Left 3---------"
      - "||---------Right 3"
      - "Left 4------------"
      - "||------------Right 4"
      - "Left 5---------------"
      - "||---------------Right 5"
      - "Left 6------------------"
      - "||------------------Right 6"
```
Will give you the following:  
<img width="305" height="239" alt="image" src="https://github.com/user-attachments/assets/a712b5fb-21ea-4ee4-83ec-d962bd431a07" />

Additionally, the following configuration:
```
      lines:
      - "Left 1---||---Right 1"
      - "Left 2------||------Right 2"
      - "Left 3---------||---------Right 3"
      - "Left 4------------||------------Right 4"
      - "Left 5---------------||---------------Right 5"
      - "Left 6------------------||------------------Right 6"
```
Will give you  
<img width="583" height="131" alt="image" src="https://github.com/user-attachments/assets/15e809c6-4ecd-4234-a30e-744c2d413335" />


The right side text will not be visible at 1.20.2 and lower, and numbers will be displayed instead.

### Hiding a line
When a line consists only of a placeholder that returned empty output, the line will be hidden completely. This is intentional to allow dynamic scoreboard size based on placeholder output. This is not the case if empty line is configured (`""`), to allow empty lines in configuration.

### Longer lines
*Since 1.13 the line length is unlimited, so you don't need to worry about this if you use 1.13+.*

To make sure the scoreboard never flickers, it's only using prefix/suffix components to display text.
These can easily be changed without any visual issues.
The player name part, however, cannot be changed.
A line has to be completely removed and a new one added.
There's a chance a frame is rendered between these 2 packets are sent,
causing the scoreboard to have 1 line missing for 1 frame, causing the visual issue known as "flickering".

However,
if your scoreboard only has placeholders that don't change value often
(pretty much any placeholder except animations, really),
you can make TAB use player name part,
allowing you to use an additional up to 40 characters.
To do so, add `Long|` prefix to all lines that should use this bypass.
```
      lines:
        - "Classic line with shorter limit"
        - "Long|Long line with much higher character limit"
```
This bypass is automatically enabled for all lines that only use static text
(since there's nothing to refresh and cause flickering),
therefore doing it for such lines (like in the example above) has no effect.

## Chaining scoreboards
When defining more than 1 scoreboard in config, the plugin will display the correct scoreboard based on conditions.
The plugin goes through all defined scoreboards, starting with the scoreboard on top.
If the scoreboard's condition is met or not set, it is displayed.
If not, the next defined scoreboard is checked and so on.
If no suitable scoreboard is found (the last one has a condition requirement which wasn't met), none is displayed.  
**Example**:
```
  scoreboards:
    scoreboard1:
      display-condition: "permission:tab.scoreboard.admin"
      title: "Title 1"
      lines:
        - "Scoreboard for admins"
    scoreboard2:
      display-condition: "%world%=myWorld"
      title: "Title 2"
      lines:
        - "Scoreboard in world myWorld"
    scoreboard3:
      title: "Title 3"
      lines:
        - "Line 1"
```
Scoreboard `scoreboard1` is checked first. If condition is not met, `scoreboard2` is checked. If that condition is not met, `scoreboard3` is displayed.  
If a player meets both conditions (has defined permission and is in defined world), `scoreboard1` will be displayed, because it is defined first and therefore has the highest priority. You can switch scoreboards based on your priority needs.

# Commands
| Command                                              | Permission                                                                               | Description                                                                                                                                                                                      |
|------------------------------------------------------|------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/tab scoreboard show <name> [player]`               | `tab.scoreboard.show` (for yourself) <br /> `tab.scoreboard.show.other` (for others)     | Shows the scoreboard with the given `name`, either to yourself if no `player` was given, or to the given `player`.                                                                               |
| `/tab scoreboard [on/off/toggle] [player] [options]` | `tab.scoreboard.toggle` (for yourself) <br /> `tab.scoreboard.toggle.other` (for others) | Shows / hides / toggles scoreboard of specified player. If no player was given, command affects the sender. You can use `-s` as option for silent toggling (no chat message for affected player) |
| `/tab scoreboard announce <name> <duration>`         | `tab.announce.scoreboard`                                                                | Shows the scoreboard with the given `name` to every player on the server for the given `duration`, in seconds.                                                                                   |

# Placeholders
This feature does not offer any internal placeholders, only PlaceholderAPI placeholders.

## PlaceholderAPI placeholders
Here are TAB's PlaceholderAPI placeholders you can use when this feature is enabled:
| Placeholder | Description |
|-------------|-------------|
| `%tab_scoreboard_name%` | Returns name of player's currently displayed scoreboard or empty string if none is displayed due to no display condition being met. |
| `%tab_scoreboard_visible%` | "Enabled" if visible, "Disabled" if not (toggled with a command) |

# Limitations
* [1.12.2-] The title is limited to 32 characters (including color codes).
* [1.12.2-] Line length is limited to 28 characters (including color codes) (64 on lines with static text / [Longer lines](#longer-lines)).
* [1.20.2-] The red numbers on the right really cannot be removed from the plugin side (a client modification / resource pack is necessary).
* [Bedrock] Scoreboard lines may be cut off and show `...`, this can be bypassed by using [GeyserOptionalPack](https://github.com/GeyserMC/GeyserOptionalPack)
* The client only displays up to 15 lines. If a plugin sends more, only the top 15 scores will be displayed. Changing this would require a client modification (on versions below 1.8 the scoreboard won't appear at all if more than 15 lines are sent).

# Compatibility with other plugins
TAB automatically detects scoreboard coming from other plugins, and when one is sent, TAB hides its own scoreboard.
Later, when the other plugin hides its scoreboard, TAB will send its scoreboard back to the player.

# Additional info
## Additional note 1 - Text alignment
**Title**:  
The title is always centered by the client. You cannot disable this. You can only pad it with spaces to move it towards your desired direction.

**Lines**:  
The left side of the text is automatically aligned to the left,
while the right side is automatically aligned to the right.
This is done by the client and is out of plugin's control.
If you want to center a line, you'll need to do it manually.
If dynamic placeholder output length is preventing your from doing it,
you can try to bypass it by adding spaces before and after it to artificially increase and force the scoreboard width,
such as
`- "          Centered text          "`.

## Additional note 2 - Geyser / Bedrock issues
Currently, there is a great suspicion that there is a geyser bug causing scoreboard to not appear for Bedrock players ([Geyser #5304](https://github.com/GeyserMC/Geyser/issues/5304) and more). This affects other scoreboard plugins as well, not just TAB. If you are experiencing this issue, make sure it is not caused by TAB configuration (for example using %bedrock%=false as display condition). If that wasn't the case, consider making a high quality bug report on Geyser with steps to reproduce.

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `ScoreboardManager` instance. Get it using `TabAPI.getInstance().getScoreboardManager()`. If this feature is disabled, the method will return `null`.

## Creating custom scoreboards
You can create and register new scoreboards using `ScoreboardManager#createScoreboard(String, String, List<String>)`, where the first `String` is the name, the second `String` is the title that will be displayed, and the `List<String>` is the list of lines that the scoreboard will display.

## Showing custom scoreboards
To show your new scoreboard to a player, you can use `ScoreboardManager#showScoreboard(TabPlayer, Scoreboard)`, where the `TabPlayer` is the player you want to show the scoreboard to, and the `Scoreboard` is the scoreboard you want to show. Doing this will also disable automatic scoreboard updating for the player, so that your scoreboard does not get overridden until you unregister it.  
To reset the scoreboard that a player is using to the default scoreboard that they should be seeing as determined by the config, use `ScoreboardManager#resetScoreboard(TabPlayer)`.  
To determine whether a player has a custom scoreboard set (one that was shown through the API), use `ScoreboardManager#hasCustomScoreboard(TabPlayer)`.

## Toggling scoreboard visibility
To change whether a player can see scoreboards, use `ScoreboardManager#setScoreboardVisible(TabPlayer, boolean, boolean)`, and to determine whether a player can see scoreboards, use `ScoreboardManager#hasScoreboardVisible(TabPlayer)`.  
To toggle whether a player can see scoreboards, use `ScoreboardManager#toggleScoreboard(TabPlayer, boolean)`. This will either make scoreboards visible if they are not already, or make them hidden if they are currently visible.

## Announcing a scoreboard
To announce a scoreboard to all players on the server for a given number of milliseconds,
use `ScoreboardManager#announceScoreboard(String, int)`,
which will look up a scoreboard with the given `String` name,
and if one is found, send it to all players for the given `int` milliseconds.

# Examples
## Example 1 - Per-world scoreboards
We will be using a condition of "equals" (=) type and check for output of `%world%` placeholder.
If condition is met, it means player is in that world and the scoreboard is displayed.
If not, check for the next scoreboard's condition.
If none of the conditions are met, display the default scoreboard.
```
  scoreboards:
    # start
    scoreboard1:
      display-condition: "%world%=world1"
      title: 'Scoreboard 1'
      lines:
      - 'You are in world world1'
    # first condition didn't pass, try next
    scoreboard2:
      display-condition: "%world%=world2"
      title: 'Scoreboard 2'
      lines:
      - 'You are in world world2'
    # first two conditions didn't pass, try next
    scoreboard3:
      display-condition: "%world%=world3"
      title: 'Scoreboard 3'
      lines:
      - 'You are in world world3'
    # none of the conditions above passed, display default scoreboard
    default:
      title: 'Default scoreboard'
      lines:
      - 'You are not in any of the worlds listed above'
```
> [!NOTE]
> This is just an example, the plugin is not limited to displaying scoreboard only per world.
> If you want per server scoreboards on proxy, use %server% with server names.
> This works for any placeholder offered by TAB or by PlaceholderAPI.

## Example 2 - Periodical scoreboard switching
We can use [animations](https://github.com/NEZNAMY/TAB/wiki/Animations) to make the plugin switch between scoreboards using display condition. Let's say we want to switch between 2 scoreboards. First, we create an animation:  
**animations.yml**
```
switchingScoreboard:
  change-interval: 10000
  texts:
  - "1"
  - "2"
```
This animation will switch frames every 10 seconds.  
Then, we will use the output of this animation as a display condition.
If animation is on the first frame, display the first scoreboard.
Otherwise, display the second scoreboard.
Result:
**config.yml**
```
  scoreboards:
    scoreboard1:
      display-condition: "%animation:switchingScoreboard%=1" # If animation is on the first frame
      title: "Scoreboard 1"
      lines:
      - 'Text'
    # Animation is on the second frame
    scoreboard2:
      title: "Scoreboard 2"
      lines:
      - 'Text'
```
We don't need to define display condition for the second scoreboard,
since if animation is not on the first frame, it has to be on the second one.
If using more than 2, you will need to define display condition for all of them (except the last one).

## Example 3 - Per-version scoreboards
1.20.3 has added new features to scoreboards, which are not visible to older clients. If you want to use new features for new players while still displaying things properly for older players, you'll need to create 2 different scoreboards.  
You'll find 2 placeholders useful for this:
* `%player-version-id%` - Returns network ID of player's protocol version. Unlike with version names, you can perform numerical comparisons with these. You can find all version IDs [here](https://github.com/NEZNAMY/TAB/blob/master/shared/src/main/java/me/neznamy/tab/shared/ProtocolVersion.java). For example, 1.20.3 is 765.
* `%bedrock%` - Returns `true` if player is using the bedrock edition, which does not support new functions either.

We only want the first scoreboard for players with version 765 or greater and not bedrock players. This can be achieved with the following condition: `display-condition: "%player-version-id%>=765;%bedrock%=false"`.

Let's go ahead and set it up (with proper spacing):
```
  scoreboards:
    scoreboard-1.20.3+:
      title: "Scoreboard for 1.20.3+ and not bedrock"
      display-condition: "%player-version-id%>=765;%bedrock%=false" # Only display it to players using 1.20.3+ AND NOT bedrock edition
      lines:
        - "* &eOnline&7:||%online%"
        - "* &eCurrent World&7:||%worldonline%"
        - "* &eStaff&7:||%staffonline%"
        - "* &bRank&7:||%group%"
        - "* &bPing&7:||%ping%&8ms"
        - "* &bWorld&7:||%world%"
    scoreboard:
      title: "Scoreboard for <1.20.3 and bedrock"
      lines:
        - "* &eOnline&7: &f%online%"
        - "* &eCurrent World&7: &f%worldonline%"
        - "* &eStaff&7: &f%staffonline%"
        - "* &bRank&7: &f%group%"
        - "* &bPing&7: &f%ping%&8ms"
        - "* &bWorld&7: &f%world%"
```

## Example 4 - Conditional lines
Sometimes you may want a dynamic line that only shows sometimes. TAB is already made to hide lines if they only consist of a placeholder that returned empty value. However, you may want to add more text around it, such as
```
- "Faction: %factionsuuid_faction_name%"
```
Now, even if the placeholder returns empty string, text `Faction: ` will remain, and as such, the line will stay. In order to avoid this, create a [conditional placeholder](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders). If the placeholder returned empty value, show empty value. Otherwise, show result of the placeholder with the static text:
```
conditions:
  faction:
    conditions:
    - '%factionsuuid_faction_name%='
    true: "" # No faction, return empty string to hide the line
    false: "Faction: %factionsuuid_faction_name%" # Player has a faction, show the static text before it as well
```
> [!WARNING]
> DO NOT JUST RANDOMLY PASTE THIS ENTIRE "CONDITIONS" SECTION INTO YOUR CONFIG!
> INSTEAD, EDIT YOUR EXISTING CONDITIONS SECTION TO PREVENT HAVING THE SECTION TWICE,
> HAVING SECOND ONE COMPLETELY OVERRIDE THE FIRST ONE!

Now, configure your scoreboard line as:
```
- "%condition:faction%"
```
and it will only display if the placeholder returned non-empty value.