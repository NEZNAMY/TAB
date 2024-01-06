package me.neznamy.tab.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A class containing various constants used everywhere in the plugin
 * to allow easier overview and modification to prevent inconsistencies.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TabConstants {

    public static final String PLUGIN_NAME = "@name@";
    public static final String PLUGIN_ID = "@id@";
    public static final String PLUGIN_VERSION = "@version@";
    public static final String PLUGIN_DESCRIPTION = "@description@";
    public static final String PLUGIN_WEBSITE = "@website@";
    public static final String PLUGIN_AUTHOR = "@author@";

    public static final String NO_GROUP = "NONE";
    public static final String DEFAULT_GROUP = "_DEFAULT_";
    public static final String PLUGIN_MESSAGE_CHANNEL_NAME = "tab:bridge-5";
    public static final String REDIS_CHANNEL_NAME = PLUGIN_NAME;
    public static final String PIPELINE_HANDLER_NAME = PLUGIN_NAME;

    public static final String COMMAND_BACKEND = "tab";
    public static final String COMMAND_PROXY = "btab";

    public static final int BSTATS_PLUGIN_ID_BUKKIT = 5304;
    public static final int BSTATS_PLUGIN_ID_BUNGEE = 10535;
    public static final int BSTATS_PLUGIN_ID_SPONGE = 17732;
    public static final int BSTATS_PLUGIN_ID_VELOCITY = 10533;

    public static final int MAX_LOG_SIZE = 1000000;

    /**
     * Feature sub-category explaining why / when a certain feature
     * needed CPU time to process tasks.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CpuUsageCategory {

        // Events
        public static final String PLAYER_JOIN = "Player Join";
        public static final String PLAYER_QUIT = "Player Quit";
        public static final String WORLD_SWITCH = "World Switch";
        public static final String SERVER_SWITCH = "Server Switch";
        public static final String COMMAND_PREPROCESS = "Command Preprocess";
        public static final String PLAYER_SNEAK = "Player Sneak";
        public static final String PLAYER_RESPAWN = "Player Respawn";
        public static final String PLUGIN_MESSAGE = "Plugin Message processing";
        public static final String REDIS_BUNGEE_MESSAGE = "Redis Message processing";

        // Packets
        public static final String ANTI_OVERRIDE = "Anti override";
        public static final String PING_CHANGE = "Processing ping change";
        public static final String NICK_PLUGIN_COMPATIBILITY = "Compatibility with nick plugins";
        public static final String BYTE_BUF = "ByteBuf";
        public static final String PACKET_PLAYER_MOVE = "Processing player move";
        public static final String PACKET_ENTITY_MOVE_PASSENGER = "Processing entity move (with player passenger)";
        public static final String PACKET_ENTITY_SPAWN = "Processing entity spawn";
        public static final String PACKET_ENTITY_DESTROY = "Processing entity destroy";
        public static final String RAW_PACKET_OUT = "Packet reading (out)";
        public static final String PACKET_LOGIN = "Login packet";
        public static final String SCOREBOARD_PACKET_CHECK = "Checking for other plugins";

        // Placeholders
        public static final String PLACEHOLDER_REFRESH_INIT = "Phase #1 - Preparing for request";
        public static final String PLACEHOLDER_REQUEST = "Phase #2 - Requesting new values";
        public static final String PLACEHOLDER_SAVE = "Phase #3 - Saving results";

        // Other
        public static final String GAMEMODE_CHANGE = "Processing gamemode change";
        public static final String TABLIST_CLEAR = "TabList entry re-add";
        public static final String VANISH_CHANGE = "Vanish status change";
        public static final String REFRESHING_NAME_TAG_VISIBILITY = "Refreshing NameTag visibility";
        public static final String PROCESSING_PLAYER_MOVEMENT = "Processing player movement";
        public static final String TELEPORTING_WITHER = "Teleporting wither";
    }

    /**
     * Permission nodes used by the plugin
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Permission {

        public static final String COMMAND_ALL                      = "tab.admin";
        public static final String COMMAND_RELOAD                   = "tab.reload";
        public static final String COMMAND_CPU                      = "tab.cpu";
        public static final String COMMAND_DEBUG                    = "tab.debug";
        public static final String COMMAND_GROUP_INFO               = "tab.groupinfo";
        public static final String COMMAND_GROUP_LIST               = "tab.grouplist";
        public static final String COMMAND_PARSE                    = "tab.parse";
        public static final String COMMAND_SETCOLLISION             = "tab.setcollision";
        public static final String COMMAND_AUTOCOMPLETE             = "tab.tabcomplete";
        public static final String COMMAND_DATA_REMOVE              = "tab.remove";
        public static final String COMMAND_PROPERTY_CHANGE_PREFIX   = "tab.change.";

        public static final String COMMAND_BOSSBAR_ANNOUNCE         = "tab.announce.bar";
        public static final String COMMAND_BOSSBAR_TOGGLE           = "tab.bossbar.toggle";
        public static final String COMMAND_BOSSBAR_TOGGLE_OTHER     = "tab.bossbar.toggle.other";

        public static final String COMMAND_MYSQL_DOWNLOAD           = "tab.mysql.download";
        public static final String COMMAND_MYSQL_UPLOAD             = "tab.mysql.upload";
        public static final String COMMAND_NAMETAG_TOGGLE           = "tab.nametag.toggle";
        public static final String COMMAND_NAMETAG_TOGGLE_OTHER     = "tab.nametag.toggle.other";
        public static final String COMMAND_NAMETAG_PREVIEW          = "tab.nametag.preview";
        public static final String COMMAND_NAMETAG_PREVIEW_OTHER    = "tab.nametag.preview.other";

        public static final String COMMAND_SCOREBOARD_ANNOUNCE      = "tab.announce.scoreboard";
        public static final String COMMAND_SCOREBOARD_TOGGLE        = "tab.scoreboard.toggle";
        public static final String COMMAND_SCOREBOARD_TOGGLE_OTHER  = "tab.scoreboard.toggle.other";
        public static final String COMMAND_SCOREBOARD_SHOW          = "tab.scoreboard.show";
        public static final String COMMAND_SCOREBOARD_SHOW_OTHER    = "tab.scoreboard.show.other";

        public static final String STAFF                            = "tab.staff";
        public static final String GROUP_PREFIX                     = "tab.group.";
        public static final String PER_WORLD_PLAYERLIST_BYPASS      = "tab.bypass";
        public static final String SEE_VANISHED                     = "tab.seevanished";
        public static final String SPECTATOR_BYPASS                 = "tab.spectatorbypass";

        public static final String TEST_PERMISSION                  = "tab.testpermission";
    }

    /**
     * Internal property names used to store text
     * under a specific key
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Property {

        public static final String HEADER = "header";
        public static final String FOOTER = "footer";

        public static final String TABPREFIX = "tabprefix";
        public static final String CUSTOMTABNAME = "customtabname";
        public static final String TABSUFFIX = "tabsuffix";

        public static final String TAGPREFIX = "tagprefix";
        public static final String CUSTOMTAGNAME = "customtagname";
        public static final String TAGSUFFIX = "tagsuffix";

        public static final String ABOVENAME = "abovename";
        public static final String NAMETAG = "nametag";
        public static final String BELOWNAME = "belowname";
    }

    /**
     * Feature names used to register features under in the
     * feature manager which they can be retrieved by.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Feature {

        //universal features
        public static final String HEADER_FOOTER = "HeaderFooter";
        public static final String PLAYER_LIST = "PlayerList";
        public static final String SPECTATOR_FIX = "SpectatorFix";
        public static final String YELLOW_NUMBER = "YellowNumber";
        public static final String BELOW_NAME = "BelowName";
        public static final String BELOW_NAME_TEXT = "BelowNameText";
        public static final String SCOREBOARD = "ScoreBoard";
        public static final String SORTING = "sorting";
        public static final String LAYOUT = "layout";
        public static final String LAYOUT_LATENCY = "layout-latency";
        public static final String PIPELINE_INJECTION = "injection";
        public static final String BOSS_BAR = "BossBar";
        public static final String NAME_TAGS = "NameTag16";
        public static final String NAME_TAGS_COLLISION = "NameTagCollision";
        public static final String NAME_TAGS_VISIBILITY = "NameTagVisibility";
        public static final String GROUP_MANAGER = "GroupManager";
        public static final String PLACEHOLDER_MANAGER = "PlaceholderManager";
        public static final String UNLIMITED_NAME_TAGS = "NameTagX";
        public static final String UNLIMITED_NAME_TAGS_PACKET_LISTENER = "nametagx-packet";
        public static final String UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER = "nametagx-vehicle";
        public static final String PING_SPOOF = "PingSpoof";

        //Bukkit only
        public static final String PER_WORLD_PLAYER_LIST = "PerWorldPlayerList";

        //BungeeCord only
        public static final String REDIS_BUNGEE = "RedisBungee";
        public static final String GLOBAL_PLAYER_LIST = "GlobalPlayerList";
        public static final String GLOBAL_PLAYER_LIST_LATENCY = "GlobalPlayerList-Latency";

        //additional info displayed in cpu command
        public static final String PACKET_DESERIALIZING = "Packet deserializing";
        public static final String NICK_COMPATIBILITY = "Nick";

        public static String scoreboardLine(String line) {
            return "scoreboard-" + line;
        }

        public static String scoreboardLine(String scoreboard, int index) {
            return "scoreboard-line-" + scoreboard + "-" + index;
        }

        public static String scoreboardScore(String scoreboard, int index) {
            return "scoreboard-score-" + scoreboard + "-" + index;
        }

        public static String layout(String name) {
            return "layout-" + name;
        }

        public static String layoutSlot(String layout, int slot) {
            return "layout-" + layout + "-slot-" + slot;
        }

        public static String bossBarTitle(String name) {
            return "BossBar-title-" + name;
        }

        public static String bossBarProgress(String name) {
            return "BossBar-progress-" + name;
        }

        public static String bossBarColor(String color) {
            return "BossBar-color-" + color;
        }

        public static String bossBarStyle(String style) {
            return "BossBar-style-" + style;
        }
    }

    /**
     * All internal placeholders offered by the plugin
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Placeholder {

        // Universal
        public static final String DISPLAY_NAME = "%displayname%";
        public static final String GROUP = "%group%";
        public static final String VANISHED = "%vanished%";
        public static final String WORLD = "%world%";
        public static final String WORLD_ONLINE = "%worldonline%";
        public static final String SERVER = "%server%";
        public static final String SERVER_ONLINE = "%serveronline%";
        public static final String PLAYER = "%player%";
        public static final String TIME = "%time%";
        public static final String DATE = "%date%";
        public static final String PING = "%ping%";
        public static final String PLAYER_VERSION = "%player-version%";
        public static final String PLAYER_VERSION_ID = "%player-version-id%";
        public static final String MEMORY_USED = "%memory-used%";
        public static final String MEMORY_MAX = "%memory-max%";
        public static final String MEMORY_USED_GB = "%memory-used-gb%";
        public static final String MEMORY_MAX_GB = "%memory-max-gb%";
        public static final String ONLINE = "%online%";
        public static final String STAFF_ONLINE = "%staffonline%";
        public static final String NON_STAFF_ONLINE = "%nonstaffonline%";
        public static final String LUCKPERMS_PREFIX = "%luckperms-prefix%";
        public static final String LUCKPERMS_SUFFIX = "%luckperms-suffix%";
        public static final String COUNTDOWN = "%countdown%";
        public static final String GAMEMODE = "%gamemode%";

        // Placeholders for internal use
        public static final String COLLISION = "%collision%";
        public static final String INVISIBLE = "%invisible%";
        public static final String VEHICLE = "%vehicle%";

        // Bukkit only
        public static final String TPS = "%tps%";
        public static final String MSPT = "%mspt%";
        public static final String HEALTH = "%health%";

        public static final int MINIMUM_REFRESH_INTERVAL = 50;
        public static final int RETURN_TIME_WARN_THRESHOLD = 50;

        public static String condition(String name) {
            return "%condition:" + name + "%";
        }

        public static String animation(String name) {
            return "%animation:" + name + "%";
        }

        public static String globalPlayerListGroup(String group) {
            return "%playerlist-group_" + group + "%";
        }
    }

    /**
     * TAB's custom metrics charts
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MetricsChart {

        public static final String PERMISSION_SYSTEM = "permission_system";
        public static final String GLOBAL_PLAYER_LIST_ENABLED = "global_playerlist_enabled";
        public static final String SERVER_VERSION = "server_version";
        public static final String UNLIMITED_NAME_TAG_MODE_ENABLED = "unlimited_nametag_mode_enabled";

    }
}