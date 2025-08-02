# Enabling
Find this section in your config:
```
proxy-support:
  enabled: true
  # Supported types: PLUGIN, REDIS, RABBITMQ
  type: PLUGIN
  plugin:
    # Compatible plugins: RedisBungee
    # If enabled and compatible plugin is found, hook is enabled to work with proxied players
    name: RedisBungee
  redis:
    url: 'redis://:password@localhost:6379/0'
  rabbitmq:
    exchange: 'plugin'
    url: 'amqp://guest:guest@localhost:5672/%2F'
```
Pick your desired way and configure it. I don't know anything about redis or rabbitmq, so good luck.

> [!IMPORTANT]
> **BACKEND INSTALLATION ONLY**  
> You should also add this into your **config.yml** and configure it:
> ```
> server-name: "server"
> ```
> Ideally, name it the same as servers in your BungeeCord / Velocity config. This value will be used for getting player's server name. Aside from %server% placeholder, it will be used for internal logic when checking for player's server. One of them is global playerlist not adding/removing players who are on the same server in the tablist, as backend servers already do this (and duplicating actions would cause problems).  
> **As a result, not configuring this value will result in everyone's server being "N/A", therefore global playerlist will think everyone is on the same server and not add anyone into the tablist, effectively making the feature not work.**

Supported plugins:
* [RedisBungee](https://github.com/ProxioDev/RedisBungee)

# Supported features
* [Belowname](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Belowname)
* [Global playerlist](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Global-playerlist)
* [Nametags](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Nametags) & [Sorting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Sorting-players-in-tablist)
* [Tablist name formatting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting)
* [Playerlist Objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective)

The hook does not and will never support [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout), which would require drastic changes to the feature itself and wouldn't be worth it, since the amount of visible players is usually even lower due to fixed slots and RedisBungee is used on networks with many players, so there will be enough players to work with anyway.