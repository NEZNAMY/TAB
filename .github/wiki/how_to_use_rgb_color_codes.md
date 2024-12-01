# Content
* [Introduction](#introduction)
* [Usage](#usage)
  * [Magic codes](#magic-codes)
* [Invalid code handling](#invalid-code-handling)
* [Compatibility with <1.16](#compatibility-with-116)
  * [Manually defining legacy color](#manually-defining-legacy-color)
* [Fonts](#fonts)
  * [Usage](#usage-1)
  * [Compatibility with RGB and gradients](#compatibility-with-rgb-and-gradients)

# Introduction
As of 1.16, mojang introduced RGB color support.
This allows us to use 256^3 different colors instead of the original 16 ones.
This is not supported on <1.16 in any way.

# Usage
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
**Remember to replace `RRGGBB` with actual hexadecimal numbers, for example `FFAA00`** (yes, some people straight up copy "RRGGBB" and ask why it is not working):  
This color code has 256 red, 170 green and 0 blue. It is equal to the legacy color `6` (gold/orange).

You can use a tool like [this one](https://htmlcolorcodes.com/) to easily find suitable colors.

## Magic codes
Just like with legacy colors, use color first, then magic codes (in any order). For example with **bold**: `#00FFFF&lText`.

For gradients, put the magic codes at the beginning of text. For example `<#00FF00>&lBold gradient text</#FF00FF>`.

# Invalid code handling
In case you accidentally use invalid hex color code, the plugin will simply ignore it and keep it in raw format.

# Compatibility with <1.16
Since <1.16 clients do not understand these color codes, something else must be displayed instead. The plugin will find the closest legacy color to the desired RGB combination and will use that color instead.

## Manually defining legacy color
If you want to specify a legacy color to use instead of plugin finding the closest one, you can define it by adding `|L` after RGB code, where `L` is a legacy color to use (0-9 a-f).  
Usage for classic codes:
* `#RRGGBB|L`
* `&#RRGGBB|L`
* `{#RRGGBB|L}`
* `#<RRGGBB|L>`
* `<#RRGGBB|L>`
* `&x&R&R&G&G&B&B|L`

For gradients,
defining color will result in the whole text
having the same color instead of each character converting to the closest colors,
resulting in color groups.
To define it, put `|L` after the beginning color of gradient.  
Usage:
* `<#RRGGBB|L>Text</#RRGGBB>`
* `{#RRGGBB|L>}Text{#RRGGBB<}`
* `<$#RRGGBB|L>Text<$#RRGGBB>`

# Fonts
The plugin offers usage of the font feature introduced in Minecraft 1.16.  
**Warning!** This does NOT refer to different character sets! This refers to different fonts defined in resource packs or MC itself.

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
