
This guide will explain how to solve the incompatibility with glow plugins.

# Content
* [Reason for compatibility issue](#reason-for-compatibility-issue)
* [Solution](#Solution)
* [Examples of compatible plugins](#examples-of-compatible-plugins)
* [Additional info](#Additional-info)

## Reason for compatibility issue
Glow color is managed by scoreboard teams, which also handle nametag formatting and player sorting in tablist. Only one plugin can handle teams at a time. Because of that, TAB will prevent glow plugins from assigning players into teams.

## Solution
The solution to get around this issue is to use the glow plugin's placeholder (if it has any).  
This placeholder has to be at the end of the player's `tagprefix` as it has to be the last color in the prefix.  
**NOTE: For Minecraft version 1.9 - 1.12 the prefix can't be longer than 14 characters due to [16 character limit](https://github.com/NEZNAMY/TAB/wiki/Limitations#name-tags) total (glow placeholder adds 2 characters - `&` and the color code)**

**Example:**
This example uses the placeholder of the [eGlow](https://www.spigotmc.org/resources/63295/) plugin.

**groups.yml**
```
_DEFAULT_:
  tagprefix: '%vault-prefix%%eglow_glowcolor%'
```

Keep in mind glow color is the same as name color. That's because there's only one field handling both values. However, you can bypass this with [Unlimited nametag mode](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Unlimited-nametag-mode). When enabling it, you'll get access to `customtagname` property, which is really just a part of the final armor stand string and can be freely modified.

To do so, you can configure for example
```
_DEFAULT_:
  customtagname: '&2%player%'
```
With this example, all players will have green visible name, despite their glow color.

## Examples of glow plugins with placeholders
- [eGlow](https://www.spigotmc.org/resources/63295/) (%eglow_glowcolor%) (Paid)
- [CMI](https://www.spigotmc.org/resources/3742/) (%cmi_user_glow_code%) (Paid)
- ... more ?