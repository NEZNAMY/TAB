# Content
* [About](#about)
* [Configuration](#configuration)
    * [Supported replacement patterns](#supported-replacement-patterns)
        * [Pattern 1: Exact text](#pattern-1-exact-text)
        * [Pattern 2: Number intervals](#pattern-2-number-intervals)
        * [Pattern 3: else](#pattern-3-else)
    * [Supported content in values](#supported-content-in-values)
* [Using in other plugins](#using-in-other-plugins)
* [Examples](#examples)
    * [Example 1 - Adding brackets to placeholder output if it returns non-empty value](#example-1---adding-brackets-to-placeholder-output-if-it-returns-non-empty-value)
    * [Example 2 - Replicating PlaceholderAPI's %player_colored_ping%](#example-2---replicating-placeholderapis-player_colored_ping)

# About
Most placeholders don't have fully configurable output, making you unable to customize it as you'd like.
This is especially a problem with placeholders such as vanish or afk ones that return "yes"
or "no" instead of some fancy text.
It is a job of the plugins providing placeholders to allow control over outputs,
but for plugins that don't we have a solution - now you're able to configure output of any placeholder in many ways.

# Configuration
Open **config.yml** and find this section:
```
placeholder-output-replacements:
  <placeholder>:
    <original output>: <new output>
```

## Supported replacement patterns

### Pattern 1: Exact text
If the placeholder's output is exact to the text on the left side, it will return text on the right side.
<details>
  <summary>Example</summary>

```
placeholder-output-replacements:
  "%tps%":
    "20": "Running at perfect 20 TPS!"
  "%essentials_vanished%":
    "yes": " &7[V]"
    "no": ""
```

</details>

**Note 1:** If you want to replace empty output, use `""` or `''` on the left side.  
**Note 2:** The text must match exactly, including color codes. To check color codes a placeholder returns use [`/tab parse <player> <placeholder>`](https://github.com/NEZNAMY/TAB/wiki/Commands-&-Permissions#tab-parse-text) and check "Without colors" part.

### Pattern 2: Number intervals
Output will be replaced if it's numeric and belongs in a configured interval.
<details>
  <summary>Example</summary>

```
placeholder-output-replacements:
  "%tps%":
    "15-20": "Solid"
    "5-15": "Bad"
    "0-5": "Terrible"
```
</details>

**Note:** If a number is included in more than 1 interval (just like 5 and 15 from the example above), the one higher is used. In this case, 15 would give `Solid` and 5 would give `Bad`.

Intervals can be defined using both `-` and `~`.
If you need support for negative values, use `~` for an interval instead of `-`.
<details>
  <summary>Example</summary>

```
placeholder-output-replacements:
  "%my_negative_placeholder%":
    "-100~-1": "Negative"
    "0": "Zero"
    "1-100": "Positive"
```
</details>

### Pattern 3: else
If none of the configured outputs are used and "else" is set, that value will be used instead.
<details>
  <summary>Example</summary>

```
placeholder-output-replacements:
  "%tps%":
    "20": "Running at perfect 20 TPS!"
    "15-20": "Solid"
    "5-15": "Bad"
    "else": "Terrible" #0-5 in this case
```
</details>

## Supported content in values
You can use placeholders inside outputs, including the original placeholder you are altering.
It will return the original output of the placeholder.
<details>
  <summary>Example</summary>

```
placeholder-output-replacements:
  "%tps%":
    "20": "Running at perfect 20 TPS!"
    "15-20": "%tps% (Solid)"
    "5-15": "%tps% (Bad)"
    "else": "%tps% (Terrible)"
```

With this example, if TPS reaches, for example 17.5, the output would be `17.5 (Solid)`.
</details>

# Using in other plugins
These replacements are only supported inside TAB. They won't work in other plugins.  
If you want to use these in other plugins, you'll need to [enable tab's PlaceholderAPI expansion](https://github.com/NEZNAMY/TAB/wiki/Placeholders#placeholderapi) and use `%tab_replace_<placeholder>%`. If you, for example, configure output for %player_health%, use `%tab_replace_player_health%`.

This only works for PlaceholderAPI placeholders, not TAB's internal ones. For TAB's internal ones use `%tab_placeholder_<placeholder>`, such as for %health% use `%tab_placeholder_health%`.

# Examples
## Example 1 - Adding brackets to placeholder output if it returns non-empty value
When displaying faction or clan of players, you may want to add brackets if a player is in a faction/clan,
but not show them if they are not in one.
To achieve this,
we are going to use [Pattern 1: Exact text](#pattern-1-exact-text) and [Pattern 3: else](#pattern-3-else).
If the placeholder returned nothing, show nothing.
Otherwise, show the output with added brackets:
```
placeholder-output-replacements:
  "%my_placeholder%":
    "": ""
    "else": "[%my_placeholder%] "
```
Note that you'll need to match the placeholder output exactly. Not all clan/faction-like plugins return empty value when player doesn't have any. They may return just a color code, such as `&7`, in which case replacing empty value won't work. To see what the exact output of a placeholder is, use `/tab parse <player> <placeholder>` and check "Without colors" part. If that's the case, you'll need to change it on the left side. Example:
```
placeholder-output-replacements:
  "%my_placeholder%":
    "&7": ""
    "else": "[%my_placeholder%] "
```
**Don't forget that parse command applies replacements, so when trying to see what placeholder returns, you should disable any previously configured replacements for that placeholder to see the correct raw output**.
## Example 2 - Replicating PlaceholderAPI's %player_colored_ping%
*Please note that this is just an example to demonstrate the plugin's functionality. It is not supposed to discourage anyone from using PlaceholderAPI or any of its placeholders.*

The placeholder allows you to configure intervals in config.yml. Default values are <50 for good, <100 for medium and 100+ for high. We can achieve this using [Pattern 2: Number intervals](#pattern-2-number-intervals) and [Pattern 3: else](#pattern-3-else). When put together, it looks as following:
```
placeholder-output-replacements:
  "%ping%":
    "0-50": "&a%ping%"
    "51-100": "&e%ping%"
    "else": "&c%ping%"
```
Keep in mind that now the placeholder will no longer return just a number,
which can break features like [Playerlist Objective](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Playerlist-Objective),
which require a numeric input (for versions prior to 1.20.3).
If you want that feature and ping to be displayed there, replacements won't work for you,
and you'll need
to use [Conditional placeholders](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Conditional-placeholders) instead
to achieve colored ping.