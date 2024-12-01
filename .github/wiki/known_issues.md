# About
This is collection if minecraft issues/bugs you may experience when using TAB, which are not TAB bugs and cannot be fixed with a plugin.

# 1.5.x - 1.12.x - Unwanted space
You may see unwanted space at the end of a line of text even if you didn't configure any.
This is a client sided bug.
To avoid it, add &r after ending bold segment (for example, prefix &4&lAdmin&r&6).

# 1.15.x - Strikethrough and underline
Using &m (strikethrough) and &n (underline) does not work in header,
last line of the scoreboard and possibly other places
([MC-180110](https://bugs.mojang.com/browse/MC-180110)).

# 1.20 - 1.20.4 - Chat over tablist
Visual bug rendering chat over the tablist ([MC-263256](https://bugs.mojang.com/browse/MC-263256)).

# 1.20.3+ - Text not bold
Since 1.20.3 text configured to be bold (using &l) may not appear bold. Steps to reproduce are currently unknown, if you have any information that may help, let us know.  
Clue #1 - It might be a BungeeCord bug breaking bold text. It may or may not have been fixed.