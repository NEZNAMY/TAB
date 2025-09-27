package me.neznamy.tab.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * A class containing various constants used everywhere in the plugin
 * to allow easier overview and modification to prevent inconsistencies.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TabConstants {

    public static final String NO_GROUP = "NONE";
    public static final String DEFAULT_GROUP = "_DEFAULT_";
    public static final String PLUGIN_MESSAGE_CHANNEL_NAME = "tab:bridge-6";
    public static final String PROXY_CHANNEL_NAME = ProjectVariables.PLUGIN_NAME + "-2";
    public static final String PIPELINE_HANDLER_NAME = ProjectVariables.PLUGIN_NAME;

    public static final int BSTATS_PLUGIN_ID_BUKKIT = 5304;
    public static final int BSTATS_PLUGIN_ID_BUNGEE = 10535;
    public static final int BSTATS_PLUGIN_ID_SPONGE = 17732;
    public static final int BSTATS_PLUGIN_ID_VELOCITY = 10533;

    public static final int MAX_LOG_SIZE = 16 * 1024 * 1024;

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
        public static final String PROXY_MESSAGE = "Proxy Message processing";

        public static final String PLUGIN_MESSAGE_DECODE = "Decoding message";
        public static final String PLUGIN_MESSAGE_PROCESS = "Processing message";
        public static final String PLUGIN_MESSAGE_ENCODE = "Encoding message";
        public static final String PLUGIN_MESSAGE_SEND = "Sending message";

        /** Periodic task checking for current and expected display names of all entries */
        public static final String ANTI_OVERRIDE_TABLIST_PERIODIC = "Tablist anti override (periodic task)";

        public static final String PERIODIC_TASK = "Periodic task";

        public static final String PING_CHANGE = "Processing ping change";
        public static final String NICK_PLUGIN_COMPATIBILITY = "Compatibility with nick plugins";
        public static final String BYTE_BUF = "ByteBuf";
        public static final String PACKET_LOGIN = "Login packet";
        public static final String SCOREBOARD_PACKET_CHECK = "Checking for other plugins";
        public static final String PROXY_RELOAD = "Processing reload from another proxy";
        public static final String GROUP_CHANGE = "Processing group change";

        // Placeholders
        public static final String PLACEHOLDER_REFRESH_INIT = "Phase #1 - Preparing for request";
        public static final String PLACEHOLDER_REQUEST = "Phase #2 - Requesting new values";
        public static final String PLACEHOLDER_SAVE = "Phase #3 - Saving results";

        // Other
        public static final String GAMEMODE_CHANGE = "Processing gamemode change";
        public static final String TABLIST_CLEAR = "TabList entry re-add";
        public static final String VANISH_CHANGE = "Vanish status change";
        public static final String DISABLE_CONDITION_CHANGE = "Refreshing disable condition";
        public static final String NICKNAME_CHANGE_PROCESS = "Processing nickname change";
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
        public static final String COMMAND_NAMETAG_VISIBILITY       = "tab.nametag.visibility";
        public static final String COMMAND_NAMETAG_VISIBILITY_OTHER = "tab.nametag.visibility.other";
        public static final String COMMAND_NAMETAG_VIEW             = "tab.nametag.view";
        public static final String COMMAND_NAMETAG_VIEW_OTHER       = "tab.nametag.view.other";

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
        public static final String YELLOW_NUMBER_TEXT = "YellowNumberText";
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
        public static final String PLACEHOLDER_MANAGER = "PlaceholderManager";
        public static final String PING_SPOOF = "PingSpoof";
        public static final String PROXY_SUPPORT = "ProxySupport";

        //Bukkit only
        public static final String PER_WORLD_PLAYER_LIST = "PerWorldPlayerList";

        //BungeeCord only
        public static final String GLOBAL_PLAYER_LIST = "GlobalPlayerList";

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

        public static String design(String name) {
            return "design-" + name;
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
        public static final String UUID = "%uuid%";
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
        public static final String LUCKPERMS_PREFIXES = "%luckperms-prefixes%";
        public static final String LUCKPERMS_SUFFIX = "%luckperms-suffix%";
        public static final String LUCKPERMS_SUFFIXES = "%luckperms-suffixes%";
        public static final String LUCKPERMS_WEIGHT = "%luckperms-weight%";
        public static final String GAMEMODE = "%gamemode%";
        public static final String BEDROCK = "%bedrock%";

        // Placeholders for internal use
        public static final String COLLISION = "%collision%";
        public static final String INVISIBLE = "%invisible%";

        // Backend only
        public static final String TPS = "%tps%";
        public static final String MSPT = "%mspt%";
        public static final String HEALTH = "%health%";
        public static final String DEATHS = "%deaths%";

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

        public static String bossbarAnnounceLeft(@NotNull String name) {
            return "%bossbar_announce_time_left_" + name + "%";
        }

        public static String bossbarAnnounceTotal(@NotNull String name) {
            return "%bossbar_announce_time_total_" + name + "%";
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

    }
}