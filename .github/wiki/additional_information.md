# About
This is a collection of random information about the plugin not necessarily limited to a single feature or function.

# Content
* [Vanish detection](#vanish-detection)
* [Async player loading](#async-player-loading)
* [Scoreboard objective and team naming](#scoreboard-objective-and-team-naming)
* [Per-version experience](#per-version-experience)
  * [Detection](#detection)
  * [Differences](#differences)
* [Debug config option](#debug-config-option)

# Vanish detection
Although TAB does not manage vanishing players in any way,
it checks vanish status for features to be properly compatible.
At the moment, vanishing detection is used for:
* `%vanished%` placeholder (used for internal functionality, but can be used to display vanish status if one wants to)
* `%online%`, `%serveronline%`, `%worldonline%` and `%staffonline%` will not count vanished players
* [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist) makes vanish plugins unable to remove players on other servers from tablist, vanish check will make TAB remove vanished players.
* Hide vanished players from [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout).
* Unregister teams of vanished players to players who cannot see them to prevent third party client exploits from seeing vanished players.

Permission `tab.seevanished` allows players to:
* See vanished players in [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist).
* See vanished players in [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout).
* Not have teams of vanished players unregistered, which would make them not appear sorted properly and have no nametag prefix/suffix.

For a player to be detected, player has to have `vanished` metadata flag set to `true`.
Most, (if not all) vanish plugins do this, including, but not limited to CMI,
Essentials, SuperVanish, PremiumVanish and probably more.
If TAB is installed on BungeeCord,
[Bridge](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) must be installed on backend servers
to forward vanish status to the proxy.

Additionally, if PremiumVanish is installed and vanish status of a player changes,
it is asked if the vanished player can be seen by each player.
Players who are marked as being able to see that vanished player by PV do not require `tab.seevaninished` permission.

# Async player loading
To not slow down the server, TAB loads players asynchronously on join.
As a casual user, this does not impact you, but there are some cases where you'll feel it:
* Players will not be accessible in the [API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) during PlayerJoinEvent
* [TAB's PlaceholderAPI expansion](https://github.com/NEZNAMY/TAB/wiki/Placeholders#placeholderapi) will not be accessible during the join process, such as when trying to use it in plugins editing join messages (the exception is `replace_` placeholder, which doesn't directly use any TAB code).

# Scoreboard objective and team naming
There's a well-known BungeeCord bug that kicks players if a duplicate team or objective is registered.
This message also shows the name of the objective/team.
Before reporting this, make sure it actually comes from TAB.

TAB's objective and team naming:
* Objectives ("Objective ... already exists in this scoreboard")
  * `TAB-BelowName` for [Belowname](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Belowname)
  * `TAB-Scoreboard` for [Scoreboard](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Scoreboard)
  * `TAB-PlayerList` for [Playerlist objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective)
* Teams ("Team ... already exists in this scoreboard")
  * For sidebar scoreboard: `TAB-Sidebar-x` where `x` is a number from 1 to 15+ (for each line)
  * For players: This is more complicated to use the 16-character limit to the max and heavily depends on configuration. If you are sorting by player names, part of player name will be there. It also often contains random non-alphanumeric symbols. The most reliable way is the team name (almost) always ending with `A` (or `B`, `C` etc. if you tried to sort several players into the same slot).

# Per-version experience
Minecraft has changed over the years.
It had some limits removed, some added, some bugs fixed, but also some bugs added.
TAB adapts code based on player's version for the best possible experience on each minecraft version.

## Detection
When TAB is installed on a proxy, the proxy's API is used to get player's version.  
When TAB is installed on a backend server, ViaVersion's API is used to get player's version (as it's the only plugin allowing multiple versions, ProtocolSupport is abandoned).

You can check what version the plugin thinks player has by using any of the following:
* `/tab parse <player> %player-version%` - Shows version name, if multiple versions share the same protocol, the newer one is returned
* `/tab parse <player> %player-version-id%` - Returns network id of player's version
* Set `debug: true` in config and check console messages on player join, it will send a message containing game version ViaVersion API has returned

Player game version detection may fail. Here are a few possible scenarios why:
* ViaVersion is installed on BungeeCord
  * And TAB on BungeeCord as well - ViaVersion makes BungeeCord API return version of the backend server player is connected to instead of player's actual version
  * And TAB is on backend servers - There is no ViaVersion to hook into, therefore, there is no suspicion of player not using server's version and not having a way to verify it
* ViaVersion API returns `-1` - There is currently no known cause when or why this happens. However, when this happens, TAB prints a console warn notifying this has happened. Plugin will assume player is using the same version as the server in such cases.

When a player's game version is not detected correctly, player's experience will be very limited, see below.

## Differences
To ensure the best possible experience, TAB is currently checking for player's version to alter its logic in some way.
If a player's version is not detected correctly, a side effect might occur instead, making the situation worse.
You will not experience all of these, only those that affect the version difference between the server and client.

| Change of logic                                                                                                                                                                                                                                                                                                               | Negative side effect if version is not detected correctly                                                                                                                                                 |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Disabling [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) for 1.7 players                                                                                                                                                                                                                                 | [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) entries being sent into the tablist of the player, breaking it as the layout is not designed support 1.7                              |
| use alternate way of sorting [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) entries for 1.19.3 - 1.21.1 as the optimal sorting way cannot be used                                                                                                                                                        | [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) entries being out of order for 1.19.3 - 1.21.1 <br />Layout entries appearing in chat complete for <1.19.3 even if they don't have to |
| Show 1.9+ bossbar on 1.8 servers for 1.9+ players using ViaVersion API                                                                                                                                                                                                                                                        | No 1.9+ bossbar for 1.9+ players on 1.8 server                                                                                                                                                            |
| Disabling [Tablist name formatting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting) for 1.7 and lower due to a 16-character limit total to take advantage of the mechanic where nametags are displayed in tablist, which extends the limit to up to 48 characters (16 prefix, 16 name, 16 suffix) | Trying to fit the entire formatting into 16 characters, resulting in massive cutting and nametag feature not working                                                                                      |
| Play around <1.13 limitations of [Scoreboard](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Scoreboard)                                                                                                                                                                                                                  | Scoreboard lines limited to ~26 characters, even for 1.13+ clients <br />Scoreboard lines limited to 14 characters for < 1.13 clients                                                                     |
| Cutting nametag prefix/suffix to 16 characters for <1.13, but keeping it full for 1.13+                                                                                                                                                                                                                                       | Nametag prefix/suffix being cut to 16 characters, even for 1.13+ clients                                                                                                                                  |

# Debug config option
You may have noticed `debug: false` in your config.
Enabling it will unlock extra console messages beyond the usual misconfiguration / errors.
They give deeper information about what's going on in the plugin in case a more complex issue comes up.
Currently, it offers the following:
* Makes plugin print thrown errors into console as well, instead of just errors.log file. They are printed into the console even if the error log reaches the 16MB limit.
* Sends a message saying how long each feature + total it took to process player join/quit or plugin reload. High values may suggest something is not optimized correctly. Originally added for myself when optimizing, but adding/removing it each time would be too much of a hassle, so it remains as a debug message.
* Sends a message when plugin detects player name change by a nick plugin. Since lots of things are bound to player names, having players change their name and plugin not detecting it would result in plugin not working correctly them. Therefore, every time a player changes their nickname (or resets it), a message should pop up.
* Sends a message whenever another plugin sends a scoreboard (such as a mini-game plugin) and TAB hides its own. Then, when that other plugin removes its scoreboard, TAB sends its own back (and sends a console message).
* Sends a message regarding the bedrock client status of players who join in using Floodgate API. This includes a message if floodgate is not enabled correctly (API instance is null), as well as true/false bedrock status of players.
* Shows network id of protocol version of players who join when ViaVersion is installed. It should show the version players are actually using. If not, the plugin may not work correctly for those players. This is usually caused by having ViaVersion on proxy and backend.
* Sends a message whenever a placeholder takes more than 50 milliseconds to retrieve value. If it only happens once, it may be a random hiccup and can be ignored. In case of consistent problems, this may cause plugin overload.
* On proxy installation, shows how long did Bridge take to answer player join plugin message. This should be below 100ms. If it's continuously significantly higher, it suggests something bad is going on, either bridge being overloaded or the server itself.