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
    * [Example 1 - per-world scoreboards](#example-1---per-world-scoreboards)
    * [Example 2 - Periodical scoreboard switching](#example-2---periodical-scoreboard-switching)

# About
Scoreboard objective with SIDEBAR display slot.

This feature can be configured in **config.yml** under **scoreboard** section.

# Creating a scoreboard
Scoreboards can be created in `scoreboards` section. You can define as many scoreboards as you want. Each scoreboard can have a condition requirement, which must be met to display the scoreboard. If it's not met, the next defined scoreboard is checked and so on. If the last defined scoreboard has a condition requirement which isn't met, player won't see anything.

Each scoreboard defines up to 3 parameters:

## title
Title of the scoreboard. Limited to 32 characters on MineCraft versions below 1.13 (including color codes. It is automatically centered by the client.

## lines
Up to 15 lines of text. You can define more, but client won't display them.

Since 1.20.3, you can also configure the value on the right side, which no longer has to be a red number. To set it, use `||` sequence and put right-side text after it. Example:
```
      lines:
      - "Left side text 1||Right side text 1"
      - "Left side text 2....||Right side text 2...."
      - "Left side text 3.......||Right side text 3......."
```
Will give you the following:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/785e4e66-935c-4233-8a16-f3e34386cfd0)

The right side text will not be visible on 1.20.2 and lower and numbers will be displayed instead. In order to properly see the right side not only the client has to be 1.20.3+, but the server as well, since plugin cannot send a packet that does not exist (on older versions). BungeeCord always includes new content and therefore counts as latest version. This can be used to send new content to new players while having outdated server version.

### Alignment
Left side of the text is automatically aligned to the left, while the right side is automatically aligned to the right. This is done by the client and is out of plugin's control. If you want to center a line, you'll need to do it manually. If dynamic placeholder output length is preventing your from doing it, you can try to bypass it by adding spaces before and after it to artificially increase and force the scoreboard width, such as `- "          Centered text          "` (or more spaces if needed).

### Empty placeholder output
When a line consists only of a placeholder that returned empty output, the line will be hidden completely. This is intentional to allow dynamic scoreboard size based on placeholder output. This is not the case if empty line is configured (`""`), to allow empty lines in configuration.

## display-condition
A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met to display this scoreboard. If it isn't met for the player, the next scoreboard's condition is checked.

# Chaining scoreboards
When defining more than 1 scoreboard in config, the plugin will display the correct scoreboard based on conditions.  Plugin goes through all defined scoreboards, starting with the scoreboard on top. If the scoreboard's condition is met or not set, it is displayed. If not, next defined scoreboard is checked and so on. If no suitable scoreboard is found (the last one has a condition requirement which wasn't met), none is displayed. Example:
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
This takes priority over current scoreboard. Once the announce is over, previous scoreboard is restored.  
`/tab announce scoreboard <name> <time>`  
name - name of scoreboard to be displayed  
time - amount of seconds to display the scoreboard for

# Additional settings
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| enabled | false | Enables / Disables the feature |
| toggle-command | /sb | Command that can be used to toggle scoreboard for the player running the command. Players need `tab.scoreboard.toggle` permission to use it. <br />**Note:** This command will not appear in command auto-complete, because it's not a real registered command. Registered commands must be defined in plugin jar directly and cannot be dynamic. If you want to solve this, you can try getting a dummy private plugin made which registers that command. |
| remember-toggle-choice | false | When enabled, toggling decision is saved into a file to remember it even after reloads/restarts/reconnects |
| hidden-by-default | false | If enabled, scoreboard will automatically be hidden on join until toggle command is used to show it. |
| use-numbers | false | If enabled, numbers 1-15 will be used in the scoreboard. If disabled, `static-number` is shown everywhere. |
| static-number | 0 | If `use-numbers` is disabled, this is number to be in all lines. |
| delay-on-join-milliseconds | 0 | Delay in milliseconds to send scoreboard after joining. |
| respect-other-plugins | true | When enabled, TAB will hide its scoreboard if another plugin sent one and send it back once the other plugin's scoreboard is hidden. | 

# Limitations
* Title is limited to 32 characters (including color codes) on <1.13.
* Line length is limited to 28 characters (including color codes) on <1.13 (68 on lines with static text / [Longer lines](#longer-lines)).
* Only displays up to 15 lines.
* The red numbers on the right really cannot be removed from the plugin side (a client modification / resource pack is needed) on 1.20.2 and lower. They no longer appear on 1.20.3+ and instead any text can be configured (see above).

# Longer lines
In order to make sure the scoreboard never flickers, it's only using prefix/suffix components to display text. These can easily be changed without any visual issues. The player name part, however, cannot be changed. A line has to be completely removed and a new one added. There's a chance a frame is rendered between these 2 packets are sent, causing the scoreboard to have 1 line missing for 1 frame, causing visual issue known as "flickering".

However, if your scoreboard only has placeholders that don't change value often (pretty much any placeholder except animations, really), you can make TAB use player name part, allowing you to use additional up to 40 characters. To do so, add `Long|` prefix to all lines which should use this bypass.
```
      lines:
        - "Classic line with shorter limit"
        - "Long|Long line with much higher character limit"
```
This bypass is automatically enabled for all lines which only use static text (since there's nothing to refresh and cause flickering), therefore doing it for such lines (like in the example above) has no effect.

# Compatibility with other plugins
TAB automatically detects scoreboard coming from other plugins and when one is sent, TAB hides its own scoreboard. Later, when the other plugin hides its scoreboard, TAB will send its scoreboard back to the player.

This detection must be available on your platform. For that, see Scoreboard section on [compatibility page](https://github.com/NEZNAMY/TAB/wiki/Compatibility#supported-features-per-platform). If it says ✔, the detection is available. If it says ❗, it means this detection is not available on your platform. As a result, when the other plugin hides its scoreboard, TAB will not be able to resend its own.

If you want this detection and it's not supported by your platform, you can achieve it using an alternate solution with conditions that will make TAB display no scoreboard if the condition is not met. For this, you'll need to have more information about when exactly do plugins send scoreboards and how to detect it.

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
Don't forget to add this condition to all of your scoreboards if you have more than 1 (for those with an existing condition merge them using `;`).  
Keep in mind [placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements) apply to placeholders here as well, so if you configured a fancy output for that placeholder, you'll need to use it in the display condition as well. To see what exactly has placeholder returned, use `/tab parse <player> <placeholder>`.
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
To announce a scoreboard to all players on the server for a given amount of ticks, use `ScoreboardManager#announceScoreboard(String, int)`, which will lookup a scoreboard with the given `String` name, and if one is found, send it to all players for the given `int` ticks.

# Examples
## Example 1 - per-world scoreboards
We will be using a condition of "equals" (=) type and check for output of `%world%` placeholder. If condition is met, player is in that world and scoreboard is displayed. If not, checking for next scoreboard's condition. If none of the conditions are met, display default scoreboard.
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
*Note: This is just an example, the plugin is not limited to displaying scoreboard only per world. If you want per server scoreboards on BungeeCord, use %server% with server names. If you want worldguard regions, use %worldguard_region_name% with region names. This works for any placeholder offered by the plugin or by PlaceholderAPI.*

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
Then, we will use output of this animation as a display condition. If animation is on the first frame, display first scoreboard. Otherwise, display second scoreboard.  
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
We don't need to define display condition for second scoreboard, since if animation is not on the first frame, it has to be on the second one. If using more than 2, you will need to define display condition for all of them (except the last one).