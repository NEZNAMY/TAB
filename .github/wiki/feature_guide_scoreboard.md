# Content
* [About](#about)
* [Creating a scoreboard](#creating-a-scoreboard)
  * [title](#title)
  * [lines](#lines)
    * [Alignment](#alignment)
    * [Empty placeholder output](#empty-placeholder-output)
  * [display-condition](#display-condition)
* [Chaining scoreboards](#chaining-scoreboards)
* [Announce command](#announce-command)
* [Additional settings](#additional-settings)
* [Limitations](#limitations)
* [Longer lines](#longer-lines)
* [Compatibility with other plugins](#compatibility-with-other-plugins)
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
Scoreboard objective with SIDEBAR display slot.

This feature can be configured in **config.yml** under **scoreboard** section.

# Creating a scoreboard
Scoreboards can be created in `scoreboards` section. You can define as many scoreboards as you want. Each scoreboard can have a condition requirement, which must be met to display the scoreboard. If it's not met, the next defined scoreboard is checked and so on. If the last defined scoreboard has a condition requirement which isn't met, player won't see anything.

Each scoreboard defines up to 3 parameters:

## title
Title of the scoreboard.
Limited to 32 characters on MineCraft versions below 1.13 (including color codes).
It is automatically centered by the client.

## lines
Up to 15 lines of text. You can define more, but the client won't display them.

Since 1.20.3, you can also configure the value on the right side, which no longer has to be a red number. To set it, use `||` sequence and put right-side text after it. Example:
```
      lines:
      - "Left side text 1||Right side text 1"
      - "Left side text 2....||Right side text 2...."
      - "Left side text 3.......||Right side text 3......."
```
Will give you the following:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/785e4e66-935c-4233-8a16-f3e34386cfd0)

The right side text will not be visible at 1.20.2 and lower, and numbers will be displayed instead.

### Alignment
The left side of the text is automatically aligned to the left,
while the right side is automatically aligned to the right.
This is done by the client and is out of plugin's control.
If you want to center a line, you'll need to do it manually.
If dynamic placeholder output length is preventing your from doing it,
you can try to bypass it by adding spaces before and after it to artificially increase and force the scoreboard width,
such as
`- "          Centered text          "` (or more spaces if needed).

### Empty placeholder output
When a line consists only of a placeholder that returned empty output, the line will be hidden completely. This is intentional to allow dynamic scoreboard size based on placeholder output. This is not the case if empty line is configured (`""`), to allow empty lines in configuration.

## display-condition
A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met to display this scoreboard. If it isn't met for the player, the next scoreboard's condition is checked.

# Chaining scoreboards
When defining more than 1 scoreboard in config, the plugin will display the correct scoreboard based on conditions.
The plugin goes through all defined scoreboards, starting with the scoreboard on top.
If the scoreboard's condition is met or not set, it is displayed.
If not, the next defined scoreboard is checked and so on.
If no suitable scoreboard is found (the last one has a condition requirement which wasn't met), none is displayed.
Example:
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

# Announce command
This takes priority over the current scoreboard.
Once the announcement is over, the previous scoreboard is restored.  
`/tab scoreboard announce <name> <time>`  
name - name of scoreboard to be displayed  
time - number of seconds to display the scoreboard for

# Additional settings
| Option name                | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|----------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                    | false         | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                             |
| toggle-command             | /sb           | Command that can be used to toggle scoreboard for the player running the command. Players need `tab.scoreboard.toggle` permission to use it. <br />**Note:** This command will not appear in command auto-complete, because it's not a real registered command. Registered commands must be defined in plugin jar directly and cannot be dynamic. If you want to solve this, you can try getting a dummy private plugin made which registers that command. |
| remember-toggle-choice     | false         | When enabled, toggling decision is saved into a file to remember it even after reloads/restarts/reconnects                                                                                                                                                                                                                                                                                                                                                 |
| hidden-by-default          | false         | If enabled, scoreboard will automatically be hidden on join until toggle command is used to show it.                                                                                                                                                                                                                                                                                                                                                       |
| use-numbers                | false         | If enabled, numbers 1-15 will be used in the scoreboard. If disabled, `static-number` is shown everywhere. Will not be visible for 1.20.3+ players, instead, you can configure any text to show using `\|\|text` in the lines (scroll up for more info).                                                                                                                                                                                                   |
| static-number              | 0             | If `use-numbers` is disabled, this is number to be in all lines.                                                                                                                                                                                                                                                                                                                                                                                           |
| delay-on-join-milliseconds | 0             | Delay in milliseconds to send scoreboard after joining.                                                                                                                                                                                                                                                                                                                                                                                                    |

# Limitations
* [1.5 - 1.12.2] The title is limited to 32 characters (including color codes).
* [1.5 - 1.12.2] Line length is limited to 28 characters (including color codes) (64 on lines with static text / [Longer lines](#longer-lines)).
* [1.5 - 1.20.2] The red numbers on the right really cannot be removed from the plugin side (a client modification / resource pack is necessary).
* The client only displays up to 15 lines. If a plugin sends more, only the top 15 scores will be displayed. Changing this would require a client modification (on versions below 1.8 the scoreboard won't appear at all if more than 15 lines are displayed).
* [Bedrock] Scoreboard lines may be cut off and show `...`, this can be bypassed by using [GeyserOptionalPack](https://github.com/GeyserMC/GeyserOptionalPack)

# Longer lines
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

# Compatibility with other plugins
TAB automatically detects scoreboard coming from other plugins, and when one is sent, TAB hides its own scoreboard.
Later, when the other plugin hides its scoreboard, TAB will send its scoreboard back to the player.

This detection must be available on your platform.
For that,
see the Scoreboard section on [compatibility page](https://github.com/NEZNAMY/TAB/wiki/Compatibility#supported-features-per-platform).
If it says ✔, the detection is available.
If it says ❗, it means this detection is not available on your platform.
As a result, when the other plugin hides its scoreboard, TAB will not be able to resend its own.

If you want this detection,
and it's not supported by your platform,
you can achieve it
using an alternate solution with conditions that will make TAB display no scoreboard if the condition is not met.
For this, you'll need to have more information about when exactly do plugins send scoreboards and how to detect it.

<details>
  <summary>Example using PremiumVanish</summary>

When you are vanished using PremiumVanish plugin, placeholder `%premiumvanish_isvanished%` will return `Yes` instead of the usual `No`. You can put this as a display condition to all defined scoreboards.  
An original
```
  scoreboards:
    scoreboard1:
      title: Default
      lines:
      - 'Line of text'
```
would turn into
```
  scoreboards:
    scoreboard1:
      display-condition: "%premiumvanish_isvanished%=No"
      title: Default
      lines:
      - 'Line of text'
```
Remember to add this condition to all of your scoreboards if you have more than 1
(for those with an existing condition merge them using `;`).  
Keep in mind [placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements) apply to placeholders here as well,
so if you configured a fancy output for that placeholder, you'll need to use it in the display condition as well.
To see what exactly has placeholder returned, use `/tab parse <player> <placeholder>`.
</details>

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
To announce a scoreboard to all players on the server for a given number of ticks,
use `ScoreboardManager#announceScoreboard(String, int)`,
which will look up a scoreboard with the given `String` name,
and if one is found, send it to all players for the given `int` ticks.

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
> If you want worldguard regions, use %worldguard_region_name% with region names.
> This works for any placeholder offered by the plugin or by PlaceholderAPI.

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
    yes: "" # No faction, return empty string to hide the line
    no: "Faction: %factionsuuid_faction_name%" # Player has a faction, show the static text before it as well
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