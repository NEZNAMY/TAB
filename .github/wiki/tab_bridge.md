# About
When TAB is installed on BungeeCord, the plugin has very limited access to information. It is unable to retrieve internal server data, as well as hook into backend plugins, because it's a different server.  
Bridge is a backend plugin that sends this data to the proxy, allowing TAB to work better when installed on BungeeCord.  
You can download it from [GitHub](https://github.com/NEZNAMY/TAB-Bridge/releases) or [Spigot](https://www.spigotmc.org/resources/83966).
# Installation
Installation is as simple as it can be. Put it into plugins folder of your backend servers. **Bridge is NOT a BungeeCord plugin!** Bridge itself has no config file, as everything is configured in the main plugin.

# Features
* Forwarding PlaceholderAPI support to the proxy​
* Allow detection if player is invisible to make nametag invisible (compensation for 1.8.x client sided bug)​
* Allow detection if player is disguised to disable collision to prevent endless push​
* Forwarding permission groups of players if no permission plugin is installed on BungeeCord​
* Forwarding vanish status for compatibility with global playerlist / layout features​
* Forward player's world for %world% and per-world settings
* Allow TAB's PlaceholderAPI expansion with TAB on BungeeCord​
* Encodes scoreboard packets (if TAB is installed on Velocity, which doesn't offer them)​
* Forward player's gamemode for spectator fix feature to work

# Compatibility
Just like with Minecraft, when there is a change in the communication protocol, older software will not be compatible with the new one. You should always be running latest versions of both TAB and Bridge.

# Confirming it works
You have many ways to verify TAB and Bridge are properly connected. Besides checking if the features above work, the easiest one is to run `/btab debug <player>`.  
If it's properly connected, you'll get a message saying this:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/3f8dd978-a8bd-4613-9ea1-a56340194859)  
If it's not properly connected, you'll get this message:  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/a90018bc-2c3b-4bf5-b54c-56621178ce80)  
If this is the case, it means Bridge is either not installed or versions are not compatible. Just update both to the latest version.