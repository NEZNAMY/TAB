# Content
* [About](#about)
* [Condition types](#condition-types)
  * [Number comparisons](#number-comparisons)
  * [Text operations](#text-operations)
  * [Permission](#permission)
* [Usage](#usage)
  * [Conditional text](#conditional-text)
  * [Using in a field where condition is expected](#using-in-a-field-where-condition-is-expected)
  * [Using in a field where condition is expected (short format)](#using-in-a-field-where-condition-is-expected-short-format)
  * [Relational conditions](#relational-conditions)
* [Refresh interval](#refresh-interval)
* [Examples](#examples)
  * [Example 1 - Chaining conditional placeholders](#example-1---chaining-conditional-placeholders)
  * [Example 2 - Combining AND and OR](#example-2---combining-and-and-or)

# About
Conditions / conditional placeholders allow you
to create output which depends on the output of other placeholders or permission requirement.  
They have 2 main uses in the plugin:
* `display-condition` which must be met to be able to see something (bossbar, scoreboard, layout) or `disable-condition` for disabling a feature (scoreboard-teams, tablist-name-formatting, ...)
* Conditional placeholders which return defined outputs based on if the condition passes or fails

# Condition types
## Number comparisons
| Operation | Description              | Example                                                                        |
|-----------|--------------------------|--------------------------------------------------------------------------------|
| `>=`      | Greater than or equal to | `%ping%>=100` will pass if the player's ping is greater than or equal to `100` |
| `>`       | Greater than             | `%ping%>100` will pass if the player's ping is greater than `100`              |
| `<=`      | Less than or equal to    | `%ping%<=100` will pass if the player's ping is less than or equal to `100`    |
| `<`       | Less than                | `%ping%<100` will pass if the player's ping is less than `100`                 |

## Text operations
| Operation | Description                                                                  | Example                                                                                                               |
|-----------|------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `=`       | Equal to                                                                     | `%world%=world` will pass if player is in world `world`                                                               |
| `!=`      | Not equal to                                                                 | `%world%!=world` will pass if player is in any world except `world`                                                   |
| `<-`      | Contains (left side for full text, right side text to contain)               | `%world%<-lobby-` will pass if player is in any world that contains `lobby-` (such as `lobby-1` etc.)                 |
| `!<-`     | Not Contains (left side for full text, right side text to not contain)       | `%world%!<-lobby-` will pass if player is in any world that does not contain `lobby-` (such as `lobby-1` etc.)        |
| `\|-`     | Starts with (left side for full text, right side text to start with)         | `%world%\|-lobby-` will pass if player is in any world that starts with `lobby-` (such as `lobby-1` etc.)             |
| `!\|-`    | Not Starts with (left side for full text, right side text to not start with) | `%world%!\|-lobby-` will pass if player is in any world that does not start with `lobby-` (such as `lobby-1` etc.)    |
| `-\|`     | Ends with (left side for full text, right side text to end with)             | `%world%-\|nether` will pass if player is in any world that ends with `nether` (such as `world_nether` etc.)          |
| `!-\|`    | Not Ends with (left side for full text, right side text to not end with)     | `%world%!-\|nether` will pass if player is in any world that does not end with `nether` (such as `world_nether` etc.) |

> [!NOTE]
> For `=` and `!=` you can check for empty output of a placeholder using `%my_placeholder%=` and `%my_placeholder%!=`.

> [!NOTE]
> For string operations, the text must match placeholder's output exactly, including color codes, as well as lower/UPPER case letters.
If you are using [Placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements),
condition must contain the altered output.  
To see the exact output of a placeholder including color codes, use `/tab parse <player> <placeholder>`.

## Permission
| Operation             | Description                     | Example                                                                                      |
|-----------------------|---------------------------------|----------------------------------------------------------------------------------------------|
| `permission:<value>`  | Permission requirement          | `permission:my.permission` will pass if player has `my.permission` permission                |
| `!permission:<value>` | Negative permission requirement | `!permission:my.permission` will pass if player **does not have** `my.permission` permission |

# Usage
There are multiple different ways you can use conditions, all of which are described below.

## Conditional text
This way allows you to create conditional text that depends on other placeholders.

First, find the `conditions` section in your config and create a new condition like this one:
```
conditions:
  lobby:
    conditions:
    - '%server%=lobby'
    true: "You are in lobby"
    false: "You are not in lobby"
```
This condition checks if player is in server `lobby` and if yes, displays `"You are in lobby"`, otherwise displays `"Your are not in lobby"`.  
To check for more than just 1 condition, add more to the list. Additionally, you will now need to specify `type`, which is either `AND` or `OR`.

For example,
```
conditions:
  lobby:
    conditions:
    - '%server%=lobby'
    - '%server%=lobby2'
    type: OR
    true: "You are in lobby"
    false: "You are not in lobby"
```
will return the lobby text if player is in server `lobby` **OR** in server `lobby2`. If you want both of those conditions to be required to display text, use `type: AND`.  
It is not possible to combine both `AND` and `OR` in a single condition. To do that, create multiple conditions and use one inside another (see Example 2 below).

Now that the condition is created, we can use it using `%condition:lobby%`. This placeholder will return the texts configured in `true` and `false` values based on whether the condition was met or not.

## Using in a field where condition is expected
A lot of the features support either `display-condition` or `disable-condition`. The value configured is expected to be a condition. Let's say we want to use it as a display condition somewhere, for example when player is in server `lobby`. First, create the condition:
```
conditions:
  lobby:
    conditions:
    - '%server%=lobby'
```
As you can see, `true` and `false` were not configured. This is because we are not trying to display any text, we are passing the condition as an object to evaluate.  
Then, use `display-condition: lobby` in the feature. This will evaluate the condition named `lobby` and either display or not display the visual based on player's server. To check for 2 servers, use
```
conditions:
  lobby:
    conditions:
    - '%server%=lobby'
    - '%server%=lobby2'
    type: OR
```
Now, the condition `lobby` will pass if player is in server `lobby` **OR** in server `lobby2`. To require both of the conditions to be met instead of at least 1, use `type: AND`.  
It is not possible to combine both `AND` and `OR` in a single condition. To do that, create multiple conditions and use one inside another (see Example 2 below).

## Using in a field where condition is expected (short format)
When using condition as a `display-condition` or `disable-condition`, you don't need to specify `true` / `false` texts. In fact, you only need the list of conditions and separator (AND / OR).  
For this purpose, short format was created. Instead of having to create a condition and using it, you can specify the expression directly.

**Single condition example**:
```
display-condition: "%server%=lobby"
```

**Multiple conditions, all of them must be met**:
```
display-condition: "%server%=lobby;%world%=world"
```

**Multiple conditions, at least one of them must be met**:
```
display-condition: "%server%=lobby|%server%=lobby2"
```
It is not possible to combine both `AND` (`;`) and `OR` (`|`) in a single condition. To do that, create multiple conditions and use one inside another (see Example 2 below).

## Relational conditions
> [!NOTE]
> This section only describes differences from standard conditions. For more information about those, see sections above.

Relational conditions allow you to check placeholder outputs for the player who is looking instead of only the target player.

To make placeholder check for viewer instead of the target player, use `%viewer:<placeholder>%` instead of just `%placeholder%`.  
You can still check for target player using `%placeholder%` or `%target:<placeholder>%` (if you want to make it more clear for the reader).  
Example: `%viewer:server%=%server%` will pass if viewer is in the same server as the target player.

There are 2 ways to use it:
* display-condition of layout player groups (which is currently the only feature supporting it as it would not make sense elsewhere)
* A placeholder using `%rel_condition:<condition>%` syntax

If trying to use relational condition in `%condition:name%` syntax, it will return a warning text instead of evaluating the condition.
Same the other way around, if trying to use standard condition in `%rel_condition:name%` syntax, it will return a warning text.

# Refresh interval
Conditions are just placeholders after all, and, as such, they must be refreshed periodically.
Refresh intervals of conditions are not directly configurable.
They are based on placeholders used inside (subconditions, true/false values).  
Permission checks count as 1000ms.  
To configure refresh intervals of placeholders,
check out the [Optimization guide](https://github.com/NEZNAMY/TAB/wiki/Optimizing-the-plugin#2---all-platforms-placeholder-refresh-intervals).

# Examples
## Example 1 - Chaining conditional placeholders
Let's create colored ping by chaining conditions using multiple intervals for ping quality. This specific case would be easier done using [replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements), but this is just an example. Let's say we want <50 for good, <100 for medium and 100+ for bad. Let's create the first condition:
```
conditions:
  ping:
    conditions:
    - "%ping%<50"
    true: "&a%ping%"
    false: "%condition:ping2%"
```
When ping is <50, display it as green. Otherwise, use another condition. Put them together and we get
```
conditions:
  ping:
    conditions:
    - "%ping%<50"
    true: "&a%ping%" # Ping is < 50, display as green
    false: "%condition:ping2%" # Ping is 50+, check another condition
  ping2:
    conditions:
    - "%ping%<100"
    true: "&e%ping%" # Ping is < 100, display as yellow
    false: "&c%ping%" # Ping is 100+, display as red
```
Finally, we can use this ping placeholder using `%condition:ping%`.  
This example chained 2 conditions, but more can be used. There is no limit.

## Example 2 - Combining AND and OR
If you want to combine both "AND" and "OR" types, create 2 conditions and use first one in the second one.  
For example, if we want to check that player is in server `lobby` **and** in worlds **either** `world1` or `world2`,
it can be achieved in the following way:
```
conditions:
  world:
    conditions:
      - "%world%=world1"
      - "%world%=world2"
    type: OR
  main:
    conditions:
      - "%condition:world%=true"
      - "%server%=lobby"
    type: AND
```
Then, use condition `main` as the display condition (or as a placeholder - `%condition:main%`). Note that `true`/`false` values were not defined, as such, they default to `true` and `false`, respectively. Therefore, we use the placeholder from the condition and check if the result is `true`. Then, check if player is also in the specified server.
