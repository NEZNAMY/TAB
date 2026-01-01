# About
This is a quick guide for [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) plugin, which is often referred to on the wiki, and we also get many assistance requests with TAB which turn out to be asking for help with PlaceholderAPI. Whether its documentation is not clear enough, missing information or users just don't read it isn't the topic here to judge, instead, a quick guide with everything needed is provided.

# Installation
PlaceholderAPI is a standalone Bukkit plugin that can be installed just like any other plugin.
It does not support proxies.
If you have TAB on a proxy server and wish to use PlaceholderAPI placeholders,
you'll need to install [Bridge](https://www.spigotmc.org/resources/83966) plugin on backend servers.
Alongside other features, it forwards PlaceholderAPI support to the proxy.

# Expansions
To effectively scale and allow the level of modification it does,
PlaceholderAPI is using placeholder packages called expansions.
They are loaded into the plugin on startup.
Each expansion can be created and modified independently of the main plugin's update frequency.
Each expansion has a name, which is used to identify its placeholders.  
The syntax is `%<expansion>_<placeholder>%`, where `<expansion>` is name of the expansion,
for example `player` and `<placeholder>` is placeholder of the expansion, for example `health`,
which returns player's health.
The full placeholder syntax will then be `%player_health%`.

Some plugins offer their own expansions,
which are either integrated into the plugin itself or external on PlaceholderAPI cloud.
Integrated expansions are not downloaded; instead, plugins register them automatically.

You can download / update external expansions using `/papi ecloud download <expansion>`. After downloading it, run `/papi reload` to enable it.

# Checking if placeholder is working
To check if a placeholder is working, run `/papi parse <player> <placeholder>`. You can also use `me` as player, which parses placeholder for the player who ran the command. If the command outputs the identifier you entered and not the actual output, it means you either entered and invalid placeholder or you are missing an expansion (or you have an outdated version of the expansion), see above.  