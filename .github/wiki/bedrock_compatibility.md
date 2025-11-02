# Content
* [Detection](#detection)
* [Differences](#differences)
* [Issues with `.` in name](#issues-with--in-name)

# Detection
For TAB to properly detect player as a bedrock player, floodgate and TAB must be installed on the same server instance.
If TAB is installed on backend and floodgate is on both proxy and backend,
make sure [they are connected](https://geysermc.org/wiki/floodgate/setup/?platform=proxy-servers).

To make sure floodgate is configured correctly, you have two options:
* Set `debug: true` in TAB config and check console output on join/reload. It should say `Floodgate returned bedrock status TRUE for player <player>`.
* Run `/tab parse <player> %bedrock%`. It should return `true`.

If any of them say `false` for an actual bedrock player, you didn't connect floodgate correctly.

# Differences
It is a job of plugin allowing bedrock clients to connect (probably Geyser) to correctly translate all packets.
If you are experiencing an issue on bedrock but not java, it is most likely not an issue in TAB code.

Currently, TAB does check for bedrock clients for:
* Disabling [Layout](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Layout) for bedrock players.

This is the only known change required on the TAB's side.

# Issues with `.` in name
If you configured Geyser to prefix bedrock player names with `.`, it will mess up config loading,
because `.` is used to split a section path.
This means that using `/tab player <playername containing .> ...` will not work.  
There are three solutions available:
* Use player UUID if commands are executed via another plugin (`/tab player <uuid> ...`).
* Use `playeruuid` type, which accepts player name and saves the online player's UUID (`/tab playeruuid <player name> ...`).
* Use a different username prefix than `.`.