# Content
* [Requirements](#requirements)
  * [Java](#java)
* [Supported server software and versions](#supported-server-software-and-versions)
* [Supported features per platform](#supported-features-per-platform)
* [Plugin / mod hooks](#plugin--mod-hooks)
* [Compatibility issues](#compatibility-issues)

# Requirements
TAB does not depend on any other plugins. The experience can however be enhanced by installing other plugins, see below.

## Java
TAB's shared module is compiled with Java 8. For the platforms themselves, TAB does have a higher requirement, however, it does not require a Java version higher than the server software itself does. Specifically:
* Fabric and NeoForge require Java 25
* Velocity requires Java 17
* Bukkit and BungeeCord are ok with Java 8

# Supported server software and versions
The table below shows which server software and Minecraft versions are supported by TAB.  
The plugin is being developed for the latest Minecraft version, so support for older versions may be dropped at some point in the future.  
If you want to use TAB on an unsupported version, you can try using an older version of the plugin or request a backport.  
Explanation of values in the table:
* ✔ = Supported by the latest release.
* ❌ = Not supported at all.
* `/` = Software does not exist for this Minecraft version, so support is not possible.
* `<version>` = Latest version of TAB that supports this version.
* `<version> backport` = Latest version of TAB that supports this version. A backport of this TAB version was made to support an older MC version and uploaded to [Modrinth](https://modrinth.com/plugin/tab-was-taken). Use its download filter to find the correct jar for your server software and version.

| Minecraft version \ Server software | Bukkit | Fabric         | Forge          | NeoForge       | Sponge |
|-------------------------------------|--------|----------------|----------------|----------------|--------|
| 26.1 - 26.1.2                       | ✔      | ✔              | ❌              | ✔              | ❌      |
| 1.21.11                             | ✔      | 6.0.1          | 6.0.1          | 6.0.1          | ❌      |
| 1.21.10                             | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.3.2  |
| 1.21.9                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.3.2  |
| 1.21.8                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.5  |
| 1.21.7                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.5  |
| 1.21.6                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.5  |
| 1.21.5                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.21.4                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.21.3                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.21.2                              | ✔      | 5.5.0 backport | /              | 5.5.0 backport | 5.2.1  |
| 1.21.1                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.21                                | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.20.6                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.20.5                              | ✔      | 5.5.0 backport | /              | 5.5.0 backport | /      |
| 1.20.4                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.20.3                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | /      |
| 1.20.2                              | ✔      | 5.5.0 backport | 5.5.0 backport | 5.5.0 backport | 5.2.1  |
| 1.20.1                              | ✔      | 5.5.0 backport | 5.5.0 backport | /              | 5.2.1  |
| 1.20                                | ✔      | 5.5.0 backport | 5.5.0 backport | /              | 5.2.1  |
| 1.19.4                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.19.3                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.19.2                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.19.1                              | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.19                                | 5.2.0  | 5.0.7          | ❌              | /              | /      |
| 1.18.2                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.18.1                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.18                                | 5.5.0  | 5.0.7          | ❌              | /              | /      |
| 1.17.1                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.17                                | ✔      | 5.0.7          | /              | /              | 5.2.1  |
| 1.16.5                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.16.4                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.16.3                              | 5.5.0  | 5.0.7          | ❌              | /              | /      |
| 1.16.2                              | 5.5.0  | 5.0.7          | ❌              | /              | /      |
| 1.16.1                              | 5.5.0  | 5.0.7          | ❌              | /              | /      |
| 1.16                                | 5.5.0  | 5.0.7          | /              | /              | /      |
| 1.15.2                              | ✔      | 5.0.7          | ❌              | /              | 5.2.1  |
| 1.15.1                              | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.15                                | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.14.4                              | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.14.3                              | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.14.2                              | ✔      | 5.0.7          | ❌              | /              | /      |
| 1.14.1                              | ✔      | 5.0.7          | /              | /              | /      |
| 1.14                                | ✔      | 5.0.7          | /              | /              | /      |
| 1.13.2                              | ✔      | /              | ❌              | /              | /      |
| 1.13.1                              | ✔      | /              | /              | /              | /      |
| 1.13                                | 5.5.0  | /              | /              | /              | /      |
| 1.12.2                              | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.12.1                              | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.12                                | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.11.1                              | ✔      | /              | /              | /              | 5.2.1  |
| 1.11                                | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.10.2                              | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.10.1                              | ✔      | /              | /              | /              | /      |
| 1.10                                | ✔      | /              | ❌              | /              | /      |
| 1.9.4                               | ✔      | /              | ❌              | /              | 5.2.1  |
| 1.9.3                               | ✔      | /              | /              | /              | /      |
| 1.9.2                               | 5.5.0  | /              | /              | /              | /      |
| 1.9.1                               | 5.5.0  | /              | /              | /              | /      |
| 1.9                                 | 5.5.0  | /              | ❌              | /              | 5.2.1  |
| 1.8.8                               | ✔      | /              | ❌              | /              | ❌      |
| 1.8.5 - 1.8.7                       | ✔      | /              | /              | /              | /      |
| 1.8.1 - 1.8.4                       | 5.5.0  | /              | /              | /              | /      |
| 1.8                                 | 5.5.0  | /              | ❌              | /              | ❌      |
| 1.7.10                              | ✔      | /              | ❌              | /              | /      |
| 1.7.3 - 1.7.9                       | 5.5.0  | /              | /              | /              | /      |
| 1.7.2                               | 5.5.0  | /              | ❌              | /              | /      |
| 1.5 - 1.6.4                         | 5.2.5  | /              | ❌              | /              | /      |


For proxies, the plugin is made to work with the latest build.
Since breaking changes don't happen too often, it means a wide range of versions is usually supported.
When a breaking change occurs, the plugin is updated to support the new version,
automatically making old versions incompatible.
Since proxies support all client versions, there is never a reason to stay outdated,
so you can always safely update to a new build of your proxy software if the plugin requires it.

# Supported features per platform
| Feature          <sup>Platform</sup>                                                                                                                               | Bukkit / Hybrid | Fabric / Forge / NeoForge | BungeeCord | Velocity                                                                   |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|---------------------------|------------|----------------------------------------------------------------------------|
| [Belowname](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Belowname)                                                                                          | ✔               | ✔                         | ✔          | ✔ (via [VSAPI](https://github.com/NEZNAMY/VelocityScoreboardAPI/releases)) |
| [BossBar](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Bossbar)                                                                                              | ✔               | ✔                         | ✔          | ✔                                                                          |
| [Global Playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist)                                                                          | ✔               | ✔                         | ✔          | ✔                                                                          |
| [Header/Footer](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Header-&-Footer)                                                                                | ✔               | ✔                         | ✔          | ✔                                                                          |
| [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout)                                                                                                | ✔               | ✔                         | ✔          | ✔                                                                          |
| [Nametags](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags) & [Sorting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist) | ✔               | ✔                         | ✔          | ✔ (via [VSAPI](https://github.com/NEZNAMY/VelocityScoreboardAPI/releases)) |
| [Per world playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Per-world-playerlist)                                                                    | ✔               | ❌                         | ❌          | ❌                                                                          |
| [Playerlist objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective)                                                                    | ✔               | ✔                         | ✔          | ✔ (via [VSAPI](https://github.com/NEZNAMY/VelocityScoreboardAPI/releases)) |
| [Scoreboard](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Scoreboard)                                                                                        | ✔               | ✔                         | ✔          | ✔ (via [VSAPI](https://github.com/NEZNAMY/VelocityScoreboardAPI/releases)) |
| [Spectator fix](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Spectator-fix)                                                                                  | ✔               | ✔                         | ✔          | ✔                                                                          |
| [Tablist names](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting)                                                                        | ✔               | ✔                         | ✔          | ✔                                                                          |

✔ = Fully functional  
❌ = Completely missing

# Plugin / mod hooks
To enhance user experience, TAB hooks into other plugins / mods for better experience.
This set of plugins / mods is different for each platform based on their availability.
Some are available on all platforms, some only in a few.

| Platform      | Plugins / mods                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| All platforms | [**Floodgate**](https://github.com/GeyserMC/Floodgate) - For properly detecting bedrock players to adapt features for the best possible user experience. <br /> [**LuckPerms**](https://github.com/LuckPerms/LuckPerms) - Detecting permission groups of players for per-group settings. <br />  [**ViaVersion**](https://github.com/ViaVersion/ViaVersion) - For properly detecting player's version to adapt features for the best possible user experience.                                                                                                                                                           |
| Bukkit        | [**LibsDisguises**](https://github.com/libraryaddict/LibsDisguises) - Detecting disguised players to disable collision to avoid endless push by colliding with own copy created by LibsDisguises. <br />[**PlaceholderAPI**](https://github.com/PlaceholderAPI/PlaceholderAPI) - Allows users to use its placeholders inside TAB. <br />[**PremiumVanish**](https://www.spigotmc.org/resources/14404) - Supporting PremiumVanish's vanishing levels instead of using a basic compatibility system. <br />[**Vault**](https://github.com/milkbowl/Vault) - Detecting permission groups of players for per-group settings. |
| BungeeCord    | [**PremiumVanish**](https://www.spigotmc.org/resources/14404) - Supporting PremiumVanish's vanishing levels instead of using a basic compatibility system. <br />[**RedisBungee**](https://github.com/ProxioDev/RedisBungee) - Communicating with other proxies to properly display visuals on players on another proxy.                                                                                                                                                                                                                                                                                                 |
| Fabric        | [**fabric-permissions-api**](https://github.com/lucko/fabric-permissions-api) - Supporting permission nodes instead of OP levels. <br />[**placeholder-api**](https://modrinth.com/mod/placeholder-api) - Displaying placeholders from that mod and offering TAB's placeholders into it.                                                                                                                                                                                                                                                                                                                                 |
| Forge         | [**LuckPerms**](https://github.com/LuckPerms/LuckPerms) - Permission checks with a string instead of using vanilla OP levels. <br /> [**ForgePlaceholderAPI**](https://github.com/EnvyWare/ForgePlaceholderAPI) - Allows users to use its placeholders inside TAB. <br /> [**Vanishmod**](https://modrinth.com/mod/vanishmod) - Getting player's vanish status.                                                                                                                                                                                                                                                          |
| NeoForge      | [**LuckPerms**](https://github.com/LuckPerms/LuckPerms) - Permission checks with a string instead of using vanilla OP levels.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Velocity      | [**PremiumVanish**](https://www.spigotmc.org/resources/14404) - Supporting PremiumVanish's vanishing levels instead of using a basic compatibility system. <br />[**RedisBungee**](https://github.com/ProxioDev/RedisBungee) - Communicating with other proxies to properly display visuals on players on another proxy. <br />[**VelocityScoreboardAPI**](https://github.com/NEZNAMY/VelocityScoreboardAPI) - Sending scoreboard packets (scoreboard-teams, belowname-objective, playerlist-objective, scoreboard)                                                                                                      |

# Compatibility issues
* **Glow plugins** will fail to apply glow color correctly. Check [How to make the plugin compatible with glow plugins](https://github.com/NEZNAMY/TAB/wiki/How-to-make-TAB-compatible-with-glow-plugins) for more information.
* Some Skript addons may cause features to not work properly, such as sorting and scoreboard.
* **Waterfall**'s `disable_tab_list_rewrite: true` **may** cause tablist to use offline UUIDs while TAB expects online UUIDs, causing various problems (most notably tablist formatting not working). Checking for this option is not an option either, because tablist rewrite might still be enabled despite being disabled (don't ask how, I have no idea). Set the option to `false` if you are experiencing issues.
* **ViaVersion on proxy and TAB on backend** acts like a client-sided protocol hack, making it impossible for TAB to know player's real version and causing issues related to it, see [Per-version experience](https://github.com/NEZNAMY/TAB/wiki/Additional-information#per-version-experience) for more info. Avoid this combination. Either install ViaVersion on all backend servers instead or install TAB on the proxy as well.
* **Nexo**'s `hide_scoreboard_numbers` config option (may apply to ItemsAdder and Oraxen as well) makes [Belowname](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Belowname)'s and [Playerlist objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective)'s `value` and `fancy-value` not visible. When using these features, you'll need to keep the option disabled.
* **LimboAuth** may prevent TAB from applying tablist formatting in a way that is not detectable using Velocity API, resulting in the feature not working properly on join.
* **Custom clients / resource packs** - Unofficially modified Minecraft clients often tend to break things. Just Lunar client has tons of bugs that can be reproduced with TAB. Resource packs may also contain modifications you are not aware of, making things not look the way you want them to. If you are experiencing any visual issue and are using a custom client or resource pack, try it with a clean vanilla client. If it works there, it's an issue with the client / resource pack and TAB cannot do anything about it.  
  For example, here are a few bugs in LunarClient / FeatherClient that you may run into when using TAB:
  * They add their icon to players in tablist, but don't widen the entries. This results in player names overlapping with latency bar. You can avoid this by configuring some spaces in tabsuffix.
  * They don't support color field in chat components, which means they don't support RGB codes and will display bossbar without colors as well.
  * Rendering belowname 3 times.
  * They don't respect nametag visibility rule, showing their own nametag using F5 even if set to invisible by the plugin.
  * When the scoreboard is set to use all 0s, lines are rendered in opposite order on 1.20.3+.
  * They don't display 1.20.3+ NumberFormat feature in scoreboard objectives.
* **XCord** - Has additional amazing optimizations that prevent TAB from being able to read outgoing packets and modify them if needed, resulting in some features not working properly, such as [tablist name formatting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting).
* **Random Spigot/BungeeCord forks** - All safe patches for improving security & performance are present in well-known public open-source projects, such as Paper, Purpur or Waterfall. Using a random overpriced closed-source (and probably obfuscated) fork from BuiltByBit may not be safe, since they likely include unsafe patches that may break compatibility with plugins in an attempt to fix things that are not broken. Before spending your entire budget on such a fork, reconsider it first. Paper (and its forks) is a performance-oriented fork used by 2/3 of all MC servers worldwide, while the rest is still stuck on Spigot. It is highly unlikely your needs are so specific you need every single "improvement" anyone can come up with. If you need a feature, Purpur is a feature-oriented fork. Together with plugins you should achieve what you are looking for.