# Content
* [About](#about)
* [Configuration](#configuration)
    * [Defining bossbars](#defining-bossbars)
        * [style](#style)
        * [color](#color)
        * [progress](#progress)
        * [text](#text)
        * [announcement-bar](#announcement-bar)
        * [display-condition](#display-condition)
    * [Announce command](#announce-command)
    * [Additional settings](#additional-settings)
* [Limitations](#limitations)
* [Additional info](#additional-info)
    * [Additional note 1 - Hiding bar itself](#additional-note-1---hiding-bar-itself)
    * [Additional note 2 - Not hiding on server switch](#additional-note-2---not-hiding-on-server-switch)
* [API](#api)
* [Examples](#examples)
    * [Example 1 - Animated bossbar color](#example-1---animated-bossbar-color)
    * [Example 2 - Switching between bossbars with condition](#example-2---switching-between-bossbars-with-condition)
    * [Example 3 - Periodical appearing and disappearing](#example-3---periodical-appearing-and-disappearing)

# About
![](https://images-ext-2.discordapp.net/external/0H5v5gcK12jm-O_kljlx-iYdJ1Q3wBsY_Dch7Jr_aAk/https/image.prntscr.com/image/x4VewIuiRwO-XLGTvDxfWw.png)  
Bars with text on top of the screen originally designed to display the health of ender dragon & wither,
but plugins found another use for it.
In 1.9, mojang added a packet
dedicated to displaying text without requiring an entity and allowing customizable color and style as well.
TAB only supports this new functionality, and the feature does not support 1.8 and lower.

This feature can be configured in **config.yml** under **bossbar** section.

# Configuration
## Defining bossbars
```
bossbar:
  bars:
    MyBossbar:
      style: "PROGRESS"
      color: "%animation:barcolors%"
      progress: "100"
      text: "&fWebsite: &bwww.domain.com"
```
Every bossbar line has 4 properties:

### style
Type of bossbar division.  
You can choose from 5 styles: **PROGRESS**, **NOTCHED_6**, **NOTCHED_10**, **NOTCHED_12** and **NOTCHED_20**  
![](https://images-ext-1.discordapp.net/external/yMuJNksnulOd_ZtI3yGw6CaQhon-ewVHsqaMzT8RcGk/https/image.prntscr.com/image/FzWSvsalTW6YVRPPK3oa9g.png)  
These were defined by mojang, no other options are available. Other plugins may be using different names, but it's the same result in the end.


### color
Color of bossbar.  
You can choose from 7 colors: **BLUE**, **GREEN**, **PINK**, **PURPLE**, **RED**, **WHITE** and **YELLOW**  
![](https://images-ext-2.discordapp.net/external/MwNGvtNT0vspb_xiGUyx_WQNayvSmb8h0Sk4W7Vxfoc/https/image.prntscr.com/image/fM7kq5XIRCiTN5n-ffpNyA.png)  
These were defined by mojang, no other options are available.


### progress
A number from 0 to 100. Accepts decimals.  
![image](https://user-images.githubusercontent.com/6338394/196031770-2208848c-8e38-44cc-aa49-c86427160cab.png)  
If you want to show the progress of a placeholder's value relative to a maximum one, you can use the [Math Expansion](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders#math) from PlaceholderAPI and use a placeholder like this one: `%math_{placeholder_current_value}/{placeholder_max_value}*100%` or `%math_{placeholder_current_value}/<max_value>*100%` if your max value is fixed.

> [!NOTE]
> Replace the text in between the `{}` with the actual placeholder you want to use and `<max_value>` with a valid number
(i.e., 20).  
You cannot use the `%placeholder%` format inside the math placeholder.

Example for showing a player's health: `%math_{player_health}/20*100%` or `%math_{player_health}/{player_max_health}*100%`

### text
Text of the bossbar. Length is not limited, supports RGB codes on 1.16+. Does not support newlines.

### announcement-bar
When set to true, the bossbar will not appear by default, only when used in announce command.
```
bossbar:
  bars:
    MyBossbar:
      style: "PROGRESS"
      color: "BLUE"
      progress: "100"
      text: "Text"
      announcement-bar: true #false or undefined means it will be visible all the time
```
### display-condition
[Condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for player to see the bossbar. Even when using announce command on a bossbar with display condition, players must meet it to see the bossbar. When condition is not defined, bossbar has no requirement to be displayed.  
**Example**:
```
bossbar:
  bars:
    MyBossbar:
      style: "PROGRESS"
      color: "BLUE"
      progress: "100"
      text: "This bossbar is only visible in world 'world'"
      display-condition: "%world%=world"
```

## Announce command
`/tab bossbar announce <name> <time>`  
`name` is name of bossbar defined in config.yml under **bossbar** section, `time` is length of display time in seconds.

When using a bossbar announcement, you have new placeholders available:
* `%bossbar_announce_time_left_<bossbar>%` - Returns remaining time of the announcement in seconds.
* `%bossbar_announce_time_total_<bossbar>%` - Returns total time of the announcement in seconds (the inputted number).

You can further use these placeholders in progress, for example,
using Math expansion from PlaceholderAPI, such as `%math_{tab_placeholder_bossbar_announce_time_left_<bossbar>}/{tab_placeholder_bossbar_announce_time_total_<bossbar>}*100%`.

## Additional settings
| Option name            | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                | false         | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                              |
| toggle-command         | /bossbar      | A command to toggle bossbar visibility for player running the command. This command requires `tab.bossbar.toggle` permission. <br />**Note:** This command will not appear in command auto-complete, because it's not a real registered command. Registered commands must be defined in plugin jar directly and cannot be dynamic. If you want to solve this, you can try getting a dummy private plugin made which registers that command. |
| remember-toggle-choice | false         | When enabled, disabling bossbar using `toggle-command` is remembered between reloads and restarts.                                                                                                                                                                                                                                                                                                                                          |
| hidden-by-default      | false         | When enabled, bossbar will automatically be hidden until toggle command is used.                                                                                                                                                                                                                                                                                                                                                            |

# Limitations
* Does not support newlines. If you want to display more lines, you'll need to create multiple bossbars.
* The bar itself cannot be removed from the server side. However, it can be removed with a resource pack.

# Additional info
## Additional note 1 - Hiding bar itself
If you want to hide the bossbar itself and only show text, this is possible, but not from the plugin's side.
You'll need to create a custom resource pack where the bar is invisible.
Then, you need to force your players to get the resource pack.

## Additional note 2 - Not hiding on server switch
When under a proxy network and having TAB installed on backend server and switching to another server,
the bossbar will not hide.
This is because proxies makes it look like a world switch to the client.
To avoid this, the only way is to install TAB on the proxy and disable bossbar on that server.

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `BossBarManager` instance. Get it using `TabAPI.getInstance().getBossBarManager()`. If this feature is disabled, the method will return `null`.

A boss bar is made up of a few things:
* A name. This is either the name given in the config, if the bar was loaded from the config, or a random UUID string when created with API.
* A title. This is the display message of the boss bar.
* The progress from 0 to 100.
* The color. This determines what color the boss bar will appear when it is shown to clients.
* The style. This determines how many segments, if any, the boss bar will be split to.

Let's start with methods in `BossBarManager`:
* `BossBarManager#createBossBar(String, float, BarColor, BarStyle` - Creates new bossbar using direct values.
* `BossBarManager#createBossBar(String, String, String, String` - Creates new bossbar. Accepts all the previous parameters as strings, to allow for placeholders, though the given placeholders must match the requirements (progress will be a number, color and style matching enum names).
* `BossBarManager#getBossBar(String)` - Returns bossbar by name defined in config.
* `BossBarManager#toggleBossBar(TabPlayer, boolean)` - Toggles all bossbars for specified player. The boolean flags whether toggle message defined in messages.yml should be sent to the player or not.
* `BossBarManager#hasBossBarVisible(TabPlayer)` - Returns `true` if player has bossbars visible, `false` if disabled using toggling.
* `BossBarManager#setBossBarVisible(TabPlayer, boolean, boolean)` - Sets bossbar visibility status for specified player to specified value (first boolean). Second boolean determines whether toggle message configured in messages.yml should be sent or not.
* `BossBarManager#sendBossBarTemporarily(TabPlayer, String, int)` - Temporarily sends bossbar to player. String defines name of the bossbar defined in config. Int defines length in milliseconds.
* `BossBarManager#announceBossBar(String, int)` - Sends bossbar with specified name in config to all players for `int` milliseconds.
* `BossBarManager#getAnnouncedBossBars()` - Returns all bossbars, which are currently being announced.
* `BossBarManager#getRegisteredBossBars()` - Returns map of all registered bossbars using configuration or the API.

Methods in `BossBar`:
* `BossBar#getName()` - Returns name of the bossbar defined in config, or randomly generated UUID string when generated using API.
* `BossBar#setTitle(String)` - Sets title. Accepts placeholders, which will be replaced and periodically updated.
* `BossBar#setProgress(String)` - Sets progress. Accepts placeholders, which will be replaced and periodically updated.
* `BossBar#setProgress(float)` - Sets progress.
* `BossBar#setColor(String)` - Sets color. Accepts placeholders, which will be replaced and periodically updated.
* `BossBar#setColor(BarColor)` - Sets color.
* `BossBar#setStyle(String)` - Sets style. Accepts placeholders, which will be replaced and periodically updated.
* `BossBar#setStyle(BarStyle)` - Sets style.
* `BossBar#getTitle()` - Returns raw title.
* `BossBar#getProgress()` - Returns raw progress as string.
* `BossBar#getColor()` - Returns raw color as string.
* `BossBar#getStyle()` - Returns raw style as string.
* `BossBar#addPlayer(TabPlayer)` - Sends the bossbar to specified player.
* `BossBar#removePlayer(TabPlayer)` - Removes the bossbar from specified player.
* `BossBar#getPlayers()` - Returns all players that can currently see this bossbar.
* `BossBar#containsPlayer(TabPlayer)` - Returns `true` if player can see this bossbar, `false` if not.
* `BossBar#isAnnouncementBar()` - Returns `true` if this bossbar is defined as announcement bar in config, `false` if visible permanently.

# Examples
## Example 1 - Animated bossbar color
You can animate the color of a bossbar using [animations](https://github.com/NEZNAMY/TAB/wiki/Animations).  
First, create an animation in **animations.yml**, such as
```
BossColor:
  change-interval: 1000
  texts:
    - "BLUE"
    - "YELLOW"
```
Then, use the animation in bossbar configuration
```
  bars:
    ServerInfo:
      style: "PROGRESS"
      color: "%animation:BossColor%"
      progress: "100"
      text: "&fWebsite: &bwww.domain.com"
```
This bossbar will change color every second between blue and yellow colors.  
You can do the same with progress, style and text as well.

## Example 2 - Switching between bossbars with condition
If you want the plugin to switch between bossbars based on a condition, first consider using condition for text itself
(or some other property).
If this is not an option (you want text, color, style and progress to be different, which would result in 4 conditions),
create 2 bars and give them the same condition, except, negate it.
If your original condition uses `=`, make second condition use `!=`.
For number comparisons, negate `<` using `>=` and so on.  
**Example**:
```
  bars:
    full:
      style: "PROGRESS"
      color: "YELLOW"
      progress: "100"
      text: "&aYour health is full"
      display-condition: "%health%=20"
    notfull:
      style: "PROGRESS"
      color: "RED"
      progress: "0"
      text: "&cYou took damage"
      display-condition: "%health%!=20"
```

## Example 3 - Periodical appearing and disappearing
This can be achieved using [animations](https://github.com/NEZNAMY/TAB/wiki/Animations) and [conditions](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders). We will create an animation with 2 frames and check if it's on the first one, then display the bossbar. Otherwise, don't display it.  
To get started, create an animation in **animations.yml**:
```
animations:
  bossbar-toggle:
    change-interval: 10000
    texts:
      - "ON"
      - "OFF"
```
Then, create a display condition that checks if the animation is on the first frame (`ON`):
```
  bars:
    ServerInfo:
      display-condition: "%animation:bossbar-toggle%=ON" # Check for the first frame of the animation
      style: "PROGRESS"
      color: "%animation:barcolors%"
      progress: "100" # in %
      text: "&fWebsite: &bwww.domain.com"
```
Now, the bossbar will appear and disappear every 10 seconds.