# Content
* [About](#about)
* [Nametag format in tablist](#nametag-format-in-tablist)
* [Tablist entry overlap](#tablist-entry-overlap)
* [Tablist entry widths](#tablist-entry-widths)

# About
This is a collection of mechanics related to the plugin explaining how does the minecraft client process information from the server and what you can and what you cannot impact.

# Nametag format in tablist
Tablist names can be formatted in 2 ways. The main one, designed explicitly for tablist names was added into Minecraft in version 1.8 and can be controlled by TAB's [Tablist name formatting](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Tablist-name-formatting) feature.  
If the feature is disabled or not applied properly (disabled with condition, some deeper compatibility issue ...), nametag prefix/suffix will be displayed in tablist instead. This mechanic is also used for <1.8 players due to 16-character tablist name limit there. You can also do this intentionally, for example for better performance.  
If you don't want any prefixes in tablist, keep the feature enabled but don't set any prefix / suffix. Disabling this feature is only useful to allow other plugins to use the feature or to intentionally display teams in tablist.

# Tablist entry overlap
When having enough online players for 3–4 columns (or using [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout)) and their name formats are too long, they might overlap. This is caused by attempting to display text wider than the monitor can handle.  
There isn't really any ideal solution for this. Here are a few tips that might help:
* Decrease GUI scale in client options to make text smaller
* Make tablist entries shorter, such as avoiding using bold text (&l) or using short aliases for ranks instead of their full names (such as `A` instead of `Admin`).

# Tablist entry widths
All tablist entries are rendered with the same width regardless of their content to match width of the longest text. This is done globally, not on a per-column basis. Therefore, one wide entry with make them all render wide, even if the content is empty. This can be a problem when using [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) to force all 80 slots. Just like in the point above, you need to make the longest entry shorter to have the tablist smaller.