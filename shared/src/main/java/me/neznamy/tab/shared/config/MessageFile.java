package me.neznamy.tab.shared.config;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Getter
public class MessageFile extends YamlConfigurationFile {

    private final String bossBarNotEnabled = getString("bossbar-feature-not-enabled", "&cThis command requires the bossbar feature to be enabled.");
    private final String bossBarAnnounceCommandUsage = getString("bossbar-announce-command-usage", "Usage: /tab bossbar announce <bar name> <length>");
    private final String bossBarAlreadyAnnounced = getString("bossbar-already-announced", "&cThis bossbar is already being announced");
    private final String parseCommandUsage = getString("parse-command-usage", "Usage: /tab parse <player> <placeholder>");
    private final String sendCommandUsage = getString("send-command-usage", "Usage: /tab send <type> <player> <bar name> <length>\nCurrently supported types: &lbar");
    private final String sendBarCommandUsage = getString("send-bar-command-usage", "Usage: /tab send bar <player> <bar name> <length>");
    private final String teamFeatureRequired = getString("team-feature-required", "This command requires scoreboard teams feature enabled");
    private final String collisionCommandUsage = getString("collision-command-usage", "Usage: /tab setcollision <player> <true/false>");
    private final String noPermission = getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
    private final String commandOnlyFromGame = getString("command-only-from-game", "&cThis command must be ran from the game");
    private final String scoreboardFeatureNotEnabled = getString("scoreboard-feature-not-enabled", "&4This command requires the scoreboard feature to be enabled.");
    private final String scoreboardAnnounceCommandUsage = getString("scoreboard-announce-command-usage", "Usage: /tab scoreboard announce <scoreboard name> <length>");
    private final String reloadSuccess = getString("reload-success", "&3[TAB] Successfully reloaded");
    private final String reloadFailBrokenFile = getString("reload-fail-file", "&3[TAB] &4Failed to reload, file %file% has broken syntax. Check console for more info.");
    private final String scoreboardOn = getString("scoreboard-toggle-on", "&2Scoreboard enabled");
    private final String scoreboardOff = getString("scoreboard-toggle-off", "&7Scoreboard disabled");
    private final String bossBarOn = getString("bossbar-toggle-on", "&2Bossbar is now visible");
    private final String bossBarOff = getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
    private final String scoreboardShowUsage = getString("scoreboard-show-usage", "Usage: /tab scoreboard show <scoreboard> [player]");
    private final String bossBarNotMarkedAsAnnouncement = getString("bossbar-not-marked-as-announcement", "&cThis bossbar is not marked as an announcement bar and is therefore " +
            "already displayed permanently (if display condition is met)");
    private final List<String> helpMenu = getStringList("help-menu", Arrays.asList("&m                                                                                "
            ," &8>> &3&l/tab reload"
            ,"    &7Reloads plugin and config"
            ," &8>> &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>"
            ,"    &7Do &8/tab group/player &7to show properties"
            ," &8>> &3&l/tab parse <player> <placeholder> "
            ,"    &7Test if a placeholder works"
            ," &8>> &3&l/tab debug [player]"
            ,"    &7displays debug information about player"
            ," &8>> &3&l/tab cpu"
            ,"    &7shows CPU usage of the plugin"
            ," &8>> &3&l/tab group/player <name> remove"
            ,"    &7Clears all data about player/group"
            ," &8>> &3&l/tab nametag"
            ,"    &7Nametag-related commands"
            ,"&m                                                                                "));
    private final List<String> mySQLHelpMenu = getStringList("mysql-help-menu", Arrays.asList(
            "/tab mysql upload - uploads data from files to mysql",
            "/tab mysql download - downloads data from mysql to files"
    ));
    private final String mySQLFailNotEnabled = getString("mysql-fail-not-enabled", "&cCannot download/upload data from/to MySQL, because it's disabled.");
    private final String mySQLFailError = getString("mysql-fail-error", "MySQL download / upload failed due to an error. Check console for more info.");
    private final String mySQLDownloadSuccess = getString("mysql-download-success", "&aMySQL data downloaded successfully.");
    private final String mySQLUploadSuccess = getString("mysql-upload-success", "&aMySQL data uploaded successfully.");
    private final List<String> scoreboardHelpMenu = getStringList("scoreboard-help-menu", Arrays.asList(
            "/tab scoreboard [on/off/toggle] [player] [options]",
            "/tab scoreboard show <name> [player]",
            "/tab scoreboard announce <name> <length>"
    ));
    private final List<String> bossbarHelpMenu = getStringList("bossbar-help-menu", Arrays.asList(
            "/tab bossbar [on/off/toggle] [player] [options]",
            "/tab bossbar send <name> [player]",
            "/tab bossbar announce <name> <length>"
    ));

    // ------------------
    // Nametags
    // ------------------

    private final List<String> nameTagHelpMenu = getStringList("nametag.help-menu", Arrays.asList(
            "/tab nametag <show/hide/toggle> [player] [-s] - Toggles nametag of specified player",
            "/tab nametag <showview/hideview/toggleview> [player] [viewer] [-s] - Toggles nametag VIEW of specified player on other player(s)"
    ));
    private final String nameTagFeatureNotEnabled = getString("nametag.feature-not-enabled", "&cThis command requires nametag feature to be enabled.");
    private final String nameTagViewHidden = getString("nametag.view-hidden", "&aNametags of all players were hidden to you");
    private final String nameTagViewShown = getString("nametag.view-shown", "&aNametags of all players were shown to you");
    private final String nameTagTargetHidden = getString("nametag.player-hidden", "&aYour nametag was hidden");
    private final String nameTagTargetShown = getString("nametag.player-shown", "&aYour nametag was shown");
    private final String nameTagNoArgFromConsole = getString("nametag.no-arg-from-console", "&cYou need to specify player if running this command from the console");

    public MessageFile() throws IOException {
        super(MessageFile.class.getClassLoader().getResourceAsStream("config/messages.yml"), new File(TAB.getInstance().getDataFolder(), "messages.yml"));
    }

    @NotNull
    public String getBossBarNotFound(@NonNull String name) {
        return getString("bossbar-not-found", "&cNo bossbar found with the name \"%name%\"").replace("%name%", name);
    }

    @NotNull
    public String getGroupDataRemoved(@NonNull String group) {
        return getString("group-data-removed", "&3[TAB] All data has been successfully removed from group &e%group%").replace("%group%", group);
    }

    @NotNull
    public String getGroupValueAssigned(@NonNull String property, @NonNull String value, @NonNull String group) {
        return getString("group-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to group &e%group%")
                .replace("%property%", property).replace("%value%", value).replace("%group%", group);
    }

    @NotNull
    public String getGroupValueRemoved(@NonNull String property, @NonNull String group) {
        return getString("group-value-removed", "&3[TAB] %property% has been successfully removed from group &e%group%")
                .replace("%property%", property).replace("%group%", group);
    }

    @NotNull
    public String getPlayerDataRemoved(@NonNull String player) {
        return getString("user-data-removed", "&3[TAB] All data has been successfully removed from player &e%player%").replace("%player%", player);
    }

    @NotNull
    public String getPlayerValueAssigned(@NonNull String property, @NonNull String value, @NonNull String player) {
        return getString("user-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to player &e%player%")
                .replace("%property%", property).replace("%value%", value).replace("%player%", player);
    }

    @NotNull
    public String getPlayerValueRemoved(@NonNull String property, @NonNull String player) {
        return getString("user-value-removed", "&3[TAB] %property% has been successfully removed from player &e%player%")
                .replace("%property%", property).replace("%player%", player);
    }

    @NotNull
    public String getPlayerNotFound(@NonNull String name) {
        return getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
    }

    @NotNull
    public String getInvalidNumber(@NonNull String input) {
        return getString("invalid-number", "\"%input%\" is not a number!").replace("%input%", input);
    }

    @NotNull
    public String getScoreboardNotFound(@NonNull String name) {
        return getString("scoreboard-not-found", "&cNo scoreboard found with the name \"%name%\"").replace("%name%", name);
    }

    @NotNull
    public String getBossBarAnnouncementSuccess(@NonNull String bar, int length) {
        return getString("bossbar-announcement-success", "&aAnnouncing bossbar &6%bossbar% &afor %length% seconds.")
                .replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }

    @NotNull
    public String getBossBarSendSuccess(@NonNull String player, @NonNull String bar, int length) {
        return getString("bossbar-send-success", "&aSending bossbar &6%bossbar% &ato player &6%player% &afor %length% seconds.")
                .replace("%player%", player).replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }
}