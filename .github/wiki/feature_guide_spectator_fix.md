# Content
* [About](#about)
* [Enabling](#enabling)
* [Permissions](#permissions)
* [Additional info](#additional-info)
  * [Additional note 1 - Seeing yourself still as spectator](#additional-note-1---seeing-yourself-still-as-spectator)

# About
With the introduction of spectator gamemode in 1.8,
players having it automatically appear on the bottom of tablist with transparent name and missing [Playerlist Objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective).
This feature cancels gamemode change packets to spectator gamemode, cancelling this minecraft feature.

# Enabling
To enable this feature, open **config.yml** and set
```
prevent-spectator-effect:
  enabled: true
``` 

# Permissions
| Permission  | Description |
| ------------- | ------------- |
| `tab.spectatorbypass`  | Players with this permission will see spectators the usual way despite this feature being enabled. If you don't want players (or even staff members) to bypass this feature, don't give them that permission.  |

# Additional info
## Additional note 1 - Seeing yourself still as spectator
Receiving information about going spectator gamemode allows players to clip through walls.
Cancelling this on self would result in inability to go through walls.
To avoid this, the information will be canceled for every player except the one going spectator gamemode.
Therefore, if you change gamemode to spectator, you will still see yourself on the bottom,
but no one else will (unless someone has the bypass permission).