# Content
* [About](#about)
* [Configuration](#configuration)
  * [Global settings](#global-settings)
  * [Per-world / per-server](#per-world--per-server)
  * [Per-group / per-player](#per-group--per-player)
* [Additional info](#additional-info)
  * [Additional note 1 - Not resetting on server switch](#additional-note-1---not-resetting-on-server-switch)
* [Tips & Tricks](#tips--tricks)
  * [Tip 1 - Dynamic line count](#tip-1---dynamic-line-count)
* [API](#api)

# About
Minecraft feature **introduced in 1.8** showing text above and below playerlist. It cannot be displayed on 1.7 clients in any way.

![](https://images-ext-2.discordapp.net/external/Jm9G7_fX8Rq4KU-Syj57W2a_leel380bZ4lmd6c0vBs/https/image.prntscr.com/image/qvuAdtgZTDeZ4IeABi8I3g.png)

# Configuration
| Option name | Default value | Description |
| ------------- | ------------- | ------------- |
| enabled | true | Enables / Disables the feature |
| disable-condition | %world%=disabledworld | A [condition](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) that must be met for disabling the feature for players. Set to empty for not disabling the feature ever. |

## Global settings
The example above is using this configuration:
```
header-footer:
  header:
  - "This is the first line of header"
  - "2nd line of header"
  footer:
  - "This is the first line of footer"
  - "2nd line of footer"
```
It is the default header/footer for everyone, unless overridden in some way.

## Per-world / per-server
```
header-footer:
  per-world:
    <your world>:
      header:
        - "Header in specified world"
      footer:
        - "Footer in specified world"
  per-server:
    <your server>:
      header:
        - "Header in specified server"
      footer:
        - "Footer in specified server"
```

For multiple worlds/servers to share the same settings, separate them with `;`.  
For worlds/servers starting with a specified text, use `*` after the shared part. For ending with a shared part, use `*` at the beginning.  
Example:
```
header-footer:
  per-world:
    world1;world2:
      header:
        - "Shared header in worlds world1 and world2"
    lobby-*:
      header:
        - "Header in all worlds starting with lobby-"
```

**Note**: To make per-world work on BungeeCord, install the [TAB-Bridge](https://www.mc-market.org/resources/21641) plugin on your backend servers.

## Per-group / per-player
**groups.yml**
```
MyGroup:
  header:
    - "This is a header for MyGroup group"
  footer:
    - "This is a footer for MyGroup group"
per-world:
  MyWorld:
    TestGroup:
      header:
        - "Header for group TestGroup in world MyWorld"
```
Same for users, which can be configured in **users.yml**.

# Additional info
## Additional note 1 - Not resetting on server switch
When under a BungeeCord network and having TAB installed on backend server and switching to another server, the header/footer will not reset. This is because BungeeCord makes it look like a world switch to the client. To avoid this, you have 2 options:
* Install TAB on BungeeCord and disable header/footer on the server.
* Install a plugin that sends some, or even empty header/footer on join.

# Tips & Tricks
## Tip 1 - Dynamic line count
The entire header is just a single character sequence that allows newline symbol (`\n`) for new lines. It is only being split into multiple lines in config for convenience to make it readable when configuring and editing. Same for footer.  
If using a placeholder that returns the newline symbol, it will create a new line. You can use this to your advantage and create a [conditional placeholder](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) or an [animation](https://github.com/NEZNAMY/TAB/wiki/Animations) that includes newline in some frames.
<details>
  <summary>Example</summary>

**animations.yml**
```
MyAnimation:
  change-interval: 1000
  texts:
    - "First frame with only 1 line"
    - "Second frame\nconsisting of 2 lines"
```
**config.yml**
```
header-footer:
  header:
    - "%animation:MyAnimation%"
```
</details>

# API
*To get started with the API, see [Developer API](https://github.com/NEZNAMY/TAB/wiki/Developer-API) page.*

To access this feature, you'll need to obtain `HeaderFooterManager` instance. Get it using `TabAPI.getInstance().getHeaderFooterManager()`. If this feature is disabled, the method will return `null`.

To set the header and/or footer for a player, use the following:
* `HeaderFooterManager#setHeader(TabPlayer, String)`
* `HeaderFooterManager#setFooter(TabPlayer, String)`
* `HeaderFooterManager#setHeaderAndFooter(TabPlayer, String, String)`

To reset the header and/or footer for a player back to their original values, use `null` as argument.  