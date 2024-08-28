# Content
* [Bukkit/Spigot](#bukkitspigot)
* [BungeeCord](#bungeecord)
    * [Proxy installation](#proxy-installation)
    * [Backend installation](#backend-installation)
    * [Mixed installation](#mixed-installation)
* [Updating the plugin](#updating-the-plugin)

# Bukkit/Spigot
Installation on Bukkit/Spigot/Paper is as simple as it can be. Just put the plugin into plugins folder and restart the server.  
Dynamic (re)loads at runtime such as `/plugman load tab` to load the plugin are fully supported. However, plugins hooking into TAB may break when you use plugman to reload it.

# BungeeCord
## Proxy installation
This is the recommended setup. Put the plugin into plugins folder of the proxy server and restart it.  
**Recommended**: Install [TAB-Bridge](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) on your backend servers for PlaceholderAPI support and more.  
**Note**: With BungeeCord installation use **/btab** command instead of **/tab**.

Advantages of proxy installation compared to installing on all backend servers instead:
* Can use the [Global player list](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist)
* Can use new content for new clients even if backend server version is old (1.16+ RGB codes, 1.20.3+ scoreboard features)
* Avoid [header/footer](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Header-&-Footer#additional-note-1---not-resetting-on-server-switch) and [bossbar](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Bossbar#additional-note-2---not-hiding-on-server-switch) not disappearing on server switch
* Configuration files in a single place for easier editing / no need to use MySQL for syncing data

Disadvantages compared to installing on all backend servers instead:
* No [per-world player list](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Per-world-playerlist) (can be achieved by installing the plugin on backend servers and disabling all features except the per-world player list)

## Backend installation
Just like when not using BungeeCord, install the plugin on all servers where you want the plugin. However, this setup is not recommended (see advantages of proxy installation).

## Mixed installation
Mixed installation is absolutely not recommended and should be avoided. It requires you to disable features to make only 1 instance handle a single feature at a time, which most people fail miserably to do, resulting in issues such as header/footer flashing between proxy and backend configuration. With bridge you are able to achieve almost anything with proxy installation.

# Updating the plugin
Updating the plugin is always as simple as replacing old jar with the new one.

You do not need to reset your configuration files or make any manual changes to them. If any changes to configuration are made (changed / removed / added), TAB will automatically convert your existing configuration files to the latest format (most notably 2.9.2 -> 3.0.0, but any other changes as well).

Downgrading is completely unsupported, as old plugin version cannot know what the future config format is / will be. Just like Minecraft itself. There is no reason for downgrading anyway, as every version is superior to older versions released before it.  