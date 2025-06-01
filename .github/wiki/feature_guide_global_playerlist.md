# Content
* [About](#about)
* [Configuration](#configuration)
  * [Grouping players only from certain servers](#grouping-players-only-from-certain-servers)
  * [Configuring isolated servers](#configuring-isolated-servers)
  * [Seeing all players on the network from some server](#seeing-all-players-on-the-network-from-some-server)
* [Compatibility with vanish plugins](#compatibility-with-vanish-plugins)

# About
This feature allows you to see players from other servers, instead of only seeing players on the same server.  
If TAB is installed on a backend server, you'll need to set up [Multi server support](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Multi-server-support) for proper linking of servers. On proxy installation, this is not required.

# Configuration
| Option name                            | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|----------------------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                                | false         | Enables / Disables the feature                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| server-groups                          | *Map*         | See [Grouping players only from certain servers](#grouping-players-only-from-certain-servers)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| spy-servers                            | *List*        | See [Seeing all players on the network from some server](#seeing-all-players-on-the-network-from-some-server)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| display-others-as-spectators           | false         | When enabled, players on different servers will appear as having spectator gamemode in tablist.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| display-vanished-players-as-spectators | true          | When enabled, vanished players will show as in spectator gamemode for those, who have permission to see vanished players (tab.seevanished) (others will obviously not see them at all). Vanished players will still see themselves in the gamemode they are actually in, since changing that would cause problems (client would think it's in spectator gamemode while it isn't).                                                                                                                                                                                                                                                                |
| isolate-unlisted-servers               | false         | When enabled, servers not listed in any group will not share playerlist with any other server, instead of sharing it with other unlisted servers.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| update-latency                         | false         | When **enabled**, plugin will send ping update to players on other servers, since backend servers only send ping updates of players on the same server. This is displayed as the green bar in tablist, unless altered by a mod / resource pack (?). <br /> <br />When **disabled**, ping of players on other servers will be set to `0` (![image](https://user-images.githubusercontent.com/6338394/179717531-3c6409b6-6bf8-41c1-a150-ce0ed615e5a5.png)).  <br /> <br />Disabled by default, because it's not only not very useful, but also has a very high CPU usage to due spamming a large amount of packets (especially on large networks). |

## Grouping players only from certain servers
In case you want multiple servers to share playerlist,
but not with every server, create a server group and put servers there.
The default example
```
global-playerlist:
  server-groups:
    lobbies:
      - lobby1
      - lobby2
    group2:
      - server1
      - server2
```
Contains 2 groups, each of them consisting of 2 servers.
Server group name can be anything (in our case, they're called "lobbies" and "group2").
Under group name, list the actual server names defined in config.yml of bungeecord.

With this setup, "lobby1" and "lobby2" will share playerlist, as well as "server1" with "server2". All unlisted servers are automatically put into a hidden default group and share playerlist. This means that all other servers except these 4 will share playerlist.

To make all servers share the playerlist, simply clear server groups and set
```
  server-groups: {}
```

## Configuring isolated servers
If you want servers which are isolated (no one will see these players, and they will not see anyone on other servers),
create a new group and only put that 1 server there.
For example:
```
global-playerlist:
  server-groups:
    test1:
      - isolatedServer1
    test2:
      - isolatedServer2
```

## Seeing all players on the network from some server
If, despite configuring server groups,
you want players to see everyone on the network if they're connected to specific server(s),
you can list those servers at
```
global-playerlist:
  spy-servers:
    - spyserver1
    - spyserver2
```
Now, players connected to "spyserver1" or "spyserver2" will see everyone on the network in tablist, but no one else (unless on another spy server / in the same server group) will see them.

# Compatibility with vanish plugins
For compatibility with vanish plugins, vanish status must be detected correctly. See [Additional information - Vanish detection](https://github.com/NEZNAMY/TAB/wiki/Additional-information#vanish-detection) for more info.