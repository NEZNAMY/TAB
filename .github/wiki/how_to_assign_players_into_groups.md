# Content
* [About](#about)
* [Option 1: Primary group from permission plugin](#option-1-primary-group-from-permission-plugin)
* [Option 2: Permission nodes](#option-2-permission-nodes)
* [Taking groups from backend servers when TAB is on BungeeCord](#taking-groups-from-backend-servers-when-tab-is-on-bungeecord)

## About
TAB uses groups to assign properties (such as tabprefix) and sort players in tablist (if sorting by groups). You have 2 ways of configuring primary groups of players in TAB.

First option hooks into your permission plugin and takes groups from it. Therefore, your permission plugin must be supported.  
**Supported permission plugins**: **LuckPerms** everywhere, on Bukkit any plugin supporting **Vault**.  
If your permission plugin is not supported, you can use [Option 2](#option-2-permission-nodes).

## Option 1: Primary group from permission plugin
This is the default method and recommended. TAB will ask your permission plugin for player's primary group.  
To use it, keep `assign-groups-by-permissions: false` in your config.

Put players into groups in your permission plugin
(remember to configure weights - for luckperms /lp group \<group\> setweight \<number\>).

**Verify** group using `/tab debug <player>`. It should say `Primary permission group: <your group>`.

## Option 2: Permission nodes
Instead of taking whole groups, TAB will check for permission nodes and assign groups based on the highest permission.
1. Go to config.yml and set `assign-groups-by-permissions: true`.
2. Give `tab.group.<group name>` permission to the user/permission group.
3. Put all possible groups into `primary-group-finding-list`.
   This is necessary because:  
   #1 - there's no way to get the list of permissions, only checking for a permission.
   The list will be used for a list of permissions to check for.  
   #2 - if a player has permission for more than one group, the list will be used to pick the correct one
   (the one higher in the list).

**Remember that OP = all permissions!** That's the whole point of OP function. To grant all permissions. In that case, you need to negate permissions in your permission plugin for other groups which are higher than player's wanted group (depending on your permission plugin it can be achieved using `-tab.group.groupname` or setting the permission value to false).

# Taking groups from backend servers when TAB is on BungeeCord
By default, TAB takes groups from the permission plugin installed on the same server instance.
In the case of proxy installation, groups are by default taken from the proxy.
To see how is your proxy permission plugin configured, use its respective command
(for LuckPerms it's `/lpb user <player> info` instead of `/lp`).

If you don't have your permission plugin synced using MySQL for sharing groups and don't wish to, and want groups to be taken from backend servers instead, enable the config option:
```
use-bukkit-permissions-manager: true
```
When enabling it, all group **and** permission requests will be done through [Bridge plugin](https://github.com/NEZNAMY/TAB/wiki/TAB-Bridge) to take data from backend servers. Your groups will now match data from backend server a player is connected to (in case of LuckPerms `/lp user <player> info`).

When assigning by permissions instead of taking the primary group directly from permission plugin,
the permission nodes (`tab.group.<name>`) will be taken from backend servers instead.