# About
This is a quick guide for [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) plugin, which is often referred to on the wiki and we also get many assistance requests with TAB which turn out to be asking for help with PlaceholderAPI. Whether its documentation is not clear enough, missing information or users just don't read it isn't the topic here to judge, instead, a quick guide with everything needed is provided.

# Installation
PlaceholderAPI is a standalone bukkit plugin which can be installed just like any other plugin. It does not support BungeeCord. If you have TAB on BungeeCord and wish to use PlaceholderAPI placeholders, you'll need to install [Bridge](https://www.spigotmc.org/resources/83966) plugin on backend servers. Alongside other features, it forwards PlaceholderAPI support to the proxy.

# Expansions
In order to effectively scale and allow the level of modification it does, PlaceholderAPI is using placeholder packages called expansions. They are loaded into the plugin on startup. Each expansion can be created and modified independently of the main plugin's update frequency. Each expansion has a name, which is used to identify its placeholders.  
The syntax is `%<expansion name>_<placeholder>%`, where `<expansion name>` is name of the expansion, for example `player` and `<placeholder>` is placeholder of the expansion, for example `health`, which returns player's health. The full placeholder syntax will then be `%player_health%`.  
You can therefore guess name of the expansion which a placeholder you are trying to use comes from. You have 2 ways of getting expansions. Which one it is for your specific placeholder expansion should be included in documentation.

## External expansions
First way is using external jars, which are stored on [PlaceholderAPI website](https://api.extendedclip.com/all/). You can get them by either manually downloading them into `plugins/PlaceholderAPI/expansions/` folder, or by running `/papi ecloud download <expansion name>`.  
To load new expansions, run `/papi reload`.

## Expansions in plugins
PlaceholderAPI expansion is in the end just a java class (or a set of classes), which can also be included in the plugin they are hooking into. If wiki of the plugin says something like "no download command" or "expansion is included in the plugin", it's this case. You can also identify this case by trying the previous method and download command saying expansion was not found.  
If no further installation steps are provided, these expansions are automatically enabled when their respective plugin is installed.

# Managing installed expansions
To see what expansions are currently enabled, run `/papi list`. It will show list of all currently enabled expansions. If you downloaded an expansion but it's not in the list, it may have failed to enable due to an error. Check console if you got any errors on startup.  
Updating expansions can be done in the same way as installing them. For external expansions, run the download command or download it manually and reload. For expansions in plugins, they always contain "latest" (the only) version of the expansion, so if a new placeholder was added, you need to update the plugin.

# Checking if placeholder is working
To check if a placeholder is working, run `/papi parse <player> <placeholder>`. You can also use `me` as player, which parses placeholder for the player who ran the command. If the command outputs the identifier you entered and not the actual output, it means you either entered and invalid placeholder or you are missing an expansion (or you have an outdated version of the expansion), see above.  