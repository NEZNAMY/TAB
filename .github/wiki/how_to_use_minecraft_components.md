# Content
* [About](#about)
* [RGB Colors (1.16+)](#rgb-colors-116)
    * [Usage](#usage)
    * [Magic codes](#magic-codes)
    * [Invalid code handling](#invalid-code-handling)
    * [Compatibility with <1.16](#compatibility-with-116)
* [Fonts (1.16+)](#fonts-116)
    * [Usage](#usage-1)
    * [Compatibility with RGB and gradients](#compatibility-with-rgb-and-gradients)
* [Shadow color (1.21.4+)](#shadow-color-1214)
* [Object components (1.21.9+)](#object-components-1219)
    * [Atlas sprite](#atlas-sprite)
    * [Player sprite](#player-sprite)
    * [Compatibility with < 1.21.9](#compatibility-with--1219)
* [MiniMessage](#minimessage)
* [Config options](#config-options)
* [Tips & Tricks](#tips--tricks)
    * [Tip 1 - Manually defining legacy color](#tip-1---manually-defining-legacy-color)

# About
In Minecraft 1.7, new feature called components was introduced. This is an upgrade from using legacy colors (using § symbol) into creating a json object where text, color, and magic codes (boolean values) are separated.  
Additionally, it received new features called hover event and click event. The only place this affects is chat, which TAB does not handle, so these functions are not implemented by TAB at all.

Components have been receiving a lot of new functions since then, which are explained below.

There are also a few config options for components. You can find them in your **config.yml** under `components` section. They are explained [below](#config-options).

# RGB colors (1.16+)
As of 1.16, mojang introduced RGB color support.
This allows us to use 16,777,216 different colors instead of the original 16 ones.
This is not supported on <1.16 in any way.

## Usage
Supported RGB formats:
* `#RRGGBB`
* `&#RRGGBB`
* `{#RRGGBB}`
* `#<RRGGBB>`
* `&x&R&R&G&G&B&B`

Gradients:
* `<#RRGGBB>Text</#RRGGBB>`
* `{#RRGGBB>}Text{#RRGGBB<}`
* `<$#RRGGBB>Text<$#RRGGBB>`

TAB uses the first by default, but is able to accept the other types as well without leaving any characters (such as `{}`) behind.  
**Gradients only support 2 colors, using more will result in an invalid syntax and won't work.**
> [!CAUTION]
> Remember to replace `RRGGBB` with actual hexadecimal numbers (0-9, a-f)!

You can use a tool like [this one](https://htmlcolorcodes.com/) to easily find suitable colors.

## Magic codes
Just like with legacy colors, use color first, then magic codes (in any order). For example with **bold**: `#00FFFF&lText`.

For gradients, put the magic codes inside the text. Example: `<#00FF00>&lBold gradient text</#FF00FF>`.

## Invalid code handling
In case you accidentally use invalid hex color code, the plugin will simply ignore it and keep it in raw format.

## Compatibility with <1.16
Since <1.16 clients do not understand these color codes, something else must be displayed instead.
TAB, server software or viaversion (depending on your setup)
will find the closest legacy color to the desired RGB combination and will use that color instead.

# Fonts (1.16+)
The plugin offers usage of the font feature introduced in Minecraft 1.16.
> [!IMPORTANT]
> This does NOT refer to different character sets!
> This refers to different fonts defined in resource packs or MC itself.

## Usage
The syntax is `<font:FONT_NAME>Text</font>` where `FONT_NAME` is name of your desired font.

**Example**  
`<font:minecraft:alt>This is a text written in minecraft enchantment table language which no one understands</font>`

**Result**  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/9a19cb1b-2761-4500-b058-2e7a7cd68d14)


## Compatibility with RGB and gradients
When trying to use both font and RGB or gradients, font must be outside and RGB/gradients inside.

**Example**  
`<font:minecraft:alt><#00FF00>This is a colorful text written in minecraft enchantment table language which no one understands, but it looks pretty</#FF0000></font>`

**Result**  
![image](https://github.com/NEZNAMY/TAB/assets/6338394/81da5bc4-1920-4dad-bcf5-002b5de741f8)

# Shadow color (1.21.4+)
TAB (currently?) does not have any built-in way to convert text input into shadow color field. However, you can use [MiniMessage](#minimessage) for that. TAB will properly read this value from adventure components and convert it further into Minecraft components.

# Object components (1.21.9+)
A new component of "object" type was added into the game in 1.21.9. It currently has 2 implementations described below.

## Atlas sprite
This type allows you to display any item or block. The syntax is
```
<sprite:ATLAS:SPRITE>
```
where `ATLAS` is the atlas and `SPRITE` is the sprite. Note that atlas can contain the `:` symbol. In order to make TAB recognize it properly, wrap at least the first value into quotes.  
Example:
```
  header:
    - '&bDiamond helmet: <sprite:"minecraft:blocks":"minecraft:item/diamond_helmet">'
    - 'Diamond chestplate: <sprite:"minecraft:blocks":"minecraft:item/diamond_chestplate">'
    - 'Diamond leggings: <sprite:"minecraft:blocks":"minecraft:item/diamond_leggings">'
    - 'Diamond boots: <sprite:"minecraft:blocks":"minecraft:item/diamond_boots">'
```
<img width="386" height="101" alt="image" src="https://github.com/user-attachments/assets/e9a66305-2ae4-44f4-9d8e-a227fb7591e4" />


## Player sprite
> [!WARNING]
> In order to use these, you will need to disable MiniMessage support, because head components were not added to MiniMessage yet. Even when they are added, they will have a different syntax, so TAB will be updated to match the format to avoid complications. For now, use the existing syntax for prototyping and testing. To disable MiniMessage support, set:
> ```
> components:
>   minimessage-support: false
> ```
> in **config.yml**.

This type allows you to display minecraft heads. There are 3 ways to define a head:
| Type                     | Functionality                                                             |
|--------------------------|---------------------------------------------------------------------------|
| Player name              | The client checks recently received profiles (players on current server) and shows skin of player with that name. If no such player was found, connects to Mojang and gets skin of player with specified name. <br />**Note**: This means using a plugin for changing skins will result in the skin being changed here too (the client has a cache, so it won't be visible immediately (restarting the client resets the cache)). If using offline mode and no skin plugin, head won't display correct skin even if such player is registered at Mojang. |
| Player UUID              | The client checks recently received profiles (players on current server) and shows skin of player with that UUID. If no such player was found, connects to Mojang and gets skin of player with specified UUID. <br />**Note**: This means using a plugin for changing skins will result in the skin being changed here too (the client has a cache, so it won't be visible immediately (restarting the client resets the cache)). |
| Skin value and signature | The client displays skin from given value and signature |

Here are all the ways you can display player skins using TAB and their implementation (using `ALL CAPS` for "placeholder" text instead of `<>` to avoid confusion as `<>` is actual part of the syntax):
| Syntax | Explanation | Implementation |
|-----|-------------|------------|
| `<head:name:NAME>` | Shows head defined by player name | name |
| `<head:id:UUID>` | Shows head defined by player UUID | uuid |
| `<head:signed_texture:VALUE;SIGNATURE>` | Shows head defined by value and signature | value and signature |
| `<head:player:NAME` | Connects to Mojang and retrieves value and signature of defined player | value and signature |
| `<head:mineskin:ID>` | Connects to Mineskin and retrieves value and signature of specified upload ID | value and signature |
| `<head:texture:VALUE>` | Connects to Mojang and retrieves skin value and signature from "texture" | value and signature |

Here is an example for all types:
```
      footer:
      - 'Head by id: <head:id:%uuid%>'  # This will obviously not work in offline mode
      - 'Head by name: <head:name:%player%>'  # May or may not work in offline mode (see above for full explanation)
      - 'Head by raw texture: <head:signed_texture:ewogICJ0aW1lc3RhbXAiIDogMTc1NjMxNjc0OTk4MywKICAicHJvZmlsZUlkIiA6ICIyMzdkOGI1NTNmOTc0NzQ5YWE2MGU5ZmU5N2I0NTA2MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfTkVaTkFNWV8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY1ZTNhNWRmODkxYzEzMTdhZjUzMmMzNmY4NDgxNDBhNDBmMGZhMGNlZGRmZmEwNWU5NGU1NzU1OGQxY2Q4YyIKICAgIH0KICB9Cn0=;YFVoqA0CqWgnZhme5s/M7xtadURyLkyLWVsh0vN8BqdfwzN32nqlBJb2pRjkxIs0V21EOeKbGFjgdKQKzPRQdf6sTFQ/x0KOjUg8A/neAiwCvDSDrsTQ2Yf7yoVP6M2PpuB7te19N+I139mGY5psxU100x6GiV/uFfpUfG3XBJog43JtXzRJ9fRtoSeIEzqLrkpCmt6o5Mzo6GZFZc4CtI76OU90Mg9ZvTZTYelvtjFtllxTtkNZCcglzvh5R19+qtzLOzEr+N8m4Ed+5yZyezEb3LeeSgmbSLIjuOKuUupE+2F6yVYP3eKhgGLZ2G+cg9TZZjTCsNMzHqewM/1+qStzTQCdNmggXCGcIfC8HcYsBfdv4SicxBq8ff+BDyveFZMFyREpQNqX/fLmlz16cmxHvBQ9qqA+IzIsBJ7i/mrG78jBhkJsvtcHMHvTviXascCSQP1TCn58D6HJ/Agww6HFTJe/B6sX0Utzm0URE4jZK7wnhrx0q2H8OkCXc5ZwuXALqtvk0uWmZo2RMnIGQNi4nb5AUsGV8pNirhS16MfqZeJ4q0HGaIVscRp4jazab6kMVKusbuqQB1cZNbaao7mP1HAUVCd5geWQL4CQQIc6gv0q3KT2E45d0YeYqpy/RppKMWmg1+aQ5wVGQW4/p2mqXos71FKy6vP0ur6txd8=>'
      - 'Head by player: <head:player:%player%>'
      - 'Head by mineskin: <head:mineskin:37e93c8e12cd426cb28fce31969e0674>'
      - 'Head by texture: <head:texture:ff9bb9e56125c8227b94bbda9f6e0f862931c229255ba8f1205d13c44c1bb561>'
```

<img width="331" height="111" alt="image" src="https://github.com/user-attachments/assets/5912975a-a892-42c3-b9ab-7eab33ee42b6" />

## Compatibility with < 1.21.9
You may be wondering what happens when you try to use these and the client is below 1.21.9.  
See table below for behavior based on your setup.

| Setup           | Legacy text decider | Displayed text for <1.21.9 players                                                                                                                                  |
|-----------------|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backend <1.21.9 | TAB                 | `<Object components were added in 1.21.9>` (even for 1.21.9+ players)                                                                                               |
| Backend 1.21.9+ | ViaVersion          | ViaBackwards 5.5.0 - [Client disconnects with a packet decoding error](https://github.com/ViaVersion/ViaBackwards/issues/1107) <br /> ViaBackwards 5.5.1+ - (empty) |
| BungeeCord      | TAB                 | `<Object components were added in 1.21.9>`                                                                                                                          |
| Velocity        | Velocity            | Client disconnects with a packet decoding error                                                                                                                     |

# MiniMessage
TAB has [MiniMessage](https://docs.advntr.dev/minimessage/format.html) hook, however, it does not include this library on its own. Including it would cause conflicts, so it has to be included in the server software.
* It **is** included in **Paper 1.18.2+**, **Velocity** and **Sponge**.
* It **is not** included in **Spigot**, any server software for **1.18.1-**, **BungeeCord**, **Fabric**, **Forge** and **NeoForge**.

When MiniMessage is detected on your server, it is automatically used. All codes are translated to MiniMessage syntax and then parsed by MiniMessage.

# Config options
| Option name              | Default value | Description                                                                                                                                |
|--------------------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| minimessage-support      | true          | Enables / Disables MiniMessage support (if it's available).                                                                                |
| disable-shadow-for-heads | true          | Automatically disables shadows for head object components (1.21.9+) to match the appearance of online-mode heads, which don't use shadows. |

# Tips & Tricks
## Tip 1 - Manually defining legacy color
For features that don't depend on any other players (header/footer, bossbar, scoreboard), you can easily specify legacy color instead of automatic rounding for <1.16 players. This can be done using [conditional placeholders](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders).

Check for `%player-version-id%`, which returns network protocol id of the version, which can be easily compared to. 1.16's version is `735`. Final condition may look like this:

```
conditions:
  rgb:
    conditions:
    - '%player-version-id%>=735'
    true: "#00FF00 RGB text"
    false: "&a Legacy text"
```
Then, use this condition with `%condition:rgb%`.

This will not work in nametags and tablist formatting, because this would parse the condition for target player, not the viewer. To achieve this, you'll need a custom-made [relational placeholder](https://github.com/NEZNAMY/TAB/wiki/Placeholders#relational-placeholders) that checks for viewer's version.