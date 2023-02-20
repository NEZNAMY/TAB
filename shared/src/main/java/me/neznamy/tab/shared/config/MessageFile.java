package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class MessageFile extends YamlConfigurationFile {

    @Getter private final String announceCommandUsage = getString("announce-command-usage", "Usage: /tab announce <type> <name> <length>\nCurrently supported types: &lbar, scoreboard");
    @Getter private final String bossBarNotEnabled = getString("bossbar-feature-not-enabled", "&cThis command requires the bossbar feature to be enabled.");
    @Getter private final String bossBarAnnounceCommandUsage = getString("bossbar-announce-command-usage", "Usage: /tab announce bar <bar name> <length>");
    @Getter private final String bossBarAlreadyAnnounced = getString("bossbar-already-announced", "&cThis bossbar is already being announced");
    @Getter private final String parseCommandUsage = getString("parse-command-usage", "Usage: /tab parse <player> <placeholder>");
    @Getter private final String sendCommandUsage = getString("send-command-usage", "Usage: /tab send <type> <player> <bar name> <length>\nCurrently supported types: &lbar");
    @Getter private final String sendBarCommandUsage = getString("send-bar-command-usage", "Usage: /tab send bar <player> <bar name> <length>");
    @Getter private final String teamFeatureRequired = getString("team-feature-required", "This command requires scoreboard teams feature enabled");
    @Getter private final String collisionCommandUsage = getString("collision-command-usage", "Usage: /tab setcollision <player> <true/false>");
    @Getter private final String noPermission = getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
    @Getter private final String commandOnlyFromGame = getString("command-only-from-game", "&cThis command must be ran from the game");
    @Getter private final String unlimitedNametagModeNotEnabled = getString("unlimited-nametag-mode-not-enabled", "&c[TAB] Warning! To make this feature work, you need to enable unlimited-nametag-mode in the config!");
    @Getter private final String scoreboardFeatureNotEnabled = getString("scoreboard-feature-not-enabled", "&4This command requires the scoreboard feature to be enabled.");
    @Getter private final String scoreboardAnnounceCommandUsage = getString("scoreboard-announce-command-usage", "Usage: /tab announce scoreboard <scoreboard name> <length>");
    @Getter private final String nametagPreviewOn = getString("nametag-preview-on", "&7Preview mode &aactivated&7.");
    @Getter private final String nametagPreviewOff = getString("nametag-preview-of", "&7Preview mode &3deactivated&7.");
    @Getter private final String reloadSuccess = getString("reload-success", "&3[TAB] Successfully reloaded");
    @Getter private final String reloadFailBrokenFile = getString("reload-fail-file", "&3[TAB] &4Failed to reload, file %file% has broken syntax. Check console for more info.");
    @Getter private final String scoreboardOn = getString("scoreboard-toggle-on", "&2Scoreboard enabled");
    @Getter private final String scoreboardOff = getString("scoreboard-toggle-off", "&7Scoreboard disabled");
    @Getter private final String bossBarOn = getString("bossbar-toggle-on", "&2Bossbar is now visible");
    @Getter private final String bossBarOff = getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
    @Getter private final String scoreboardShowUsage = getString("scoreboard-show-usage", "Usage: /tab scoreboard show <scoreboard> [player]");
    @Getter private final String bossBarNotMarkedAsAnnouncement = getString("bossbar-not-marked-as-announcement", "&cThis bossbar is not marked as an announcement bar and is therefore " +
            "already displayed permanently (if display condition is met)");
    @Getter private final List<String> helpMenu = getStringList("help-menu", Arrays.asList("&m                                                                                "
            ," &8>> &3&l/tab reload"
            ,"    &7Reloads plugin and config"
            ," &8>> &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>"
            ,"    &7Do &8/tab group/player &7to show properties"
            ," &8>> &3&l/tab nametag preview"
            ,"    &7Shows your nametag for yourself, for testing purposes"
            ," &8>> &3&l/tab announce bar &3<name> &9<seconds>"
            ,"    &7Temporarily displays bossbar to all players"
            ," &8>> &3&l/tab parse <player> <placeholder> "
            ,"    &7Test if a placeholder works"
            ," &8>> &3&l/tab debug [player]"
            ,"    &7displays debug information about player"
            ," &8>> &3&l/tab cpu"
            ,"    &7shows CPU usage of the plugin"
            ," &8>> &3&l/tab group/player <name> remove"
            ,"    &7Clears all data about player/group"
            ,"&m                                                                                "));
    @Getter private final List<String> mySQLHelpMenu = getStringList("mysql-help-menu", Arrays.asList(
            "/tab mysql upload - uploads data from files to mysql",
            "/tab mysql download - downloads data from mysql to files"
    ));
    @Getter private final String mySQLFailNotEnabled = getString("mysql-fail-not-enabled", "&cCannot download/upload data from/to MySQL, because it's disabled.");
    @Getter private final String mySQLFailError = getString("mysql-fail-error", "MySQL download failed due to an error. Check console for more info.");
    @Getter private final String mySQLDownloadSuccess = getString("mysql-download-success", "&aMySQL data downloaded successfully.");
    @Getter private final String mySQLUploadSuccess = getString("mysql-upload-success", "&aMySQL data uploaded successfully.");
    @Getter private final List<String> nameTagHelpMenu = getStringList("nametag-help-menu", Arrays.asList(
            "/tab nametag preview [player] - toggles armor stand preview mode",
            "/tab nametag toggle [player] - toggles nametags on all players for command sender"
    ));
    @Getter private final String nameTagFeatureNotEnabled = getString("nametag-feature-not-enabled", "&cThis command requires nametag feature to be enabled.");
    @Getter private final String nameTagsHidden = getString("nametags-hidden", "&aNametags of all players were hidden to you");
    @Getter private final String nameTagsShown = getString("nametags-shown", "&aNametags of all players were shown to you");
    @Getter private final String armorStandsDisabledCannotPreview = getString("armorstands-disabled-cannot-use-preview", "&cYour armor stands are disabled, therefore you cannot use preview feature");

    public MessageFile() throws YAMLException, IOException {
        super(MessageFile.class.getClassLoader().getResourceAsStream("messages.yml"), new File(TAB.getInstance().getDataFolder(), "messages.yml"));
    }

    public String getBossBarNotFound(String name) {
        return getString("bossbar-not-found", "&cNo bossbar found with the name \"%name%\"").replace("%name%", name);
    }

    public String getGroupDataRemoved(String group) {
        return getString("group-data-removed", "&3[TAB] All data has been successfully removed from group &e%group%").replace("%group%", group);
    }

    public String getGroupValueAssigned(String property, String value, String group) {
        return getString("group-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to group &e%group%")
                .replace("%property%", property).replace("%value%", value).replace("%group%", group);
    }

    public String getGroupValueRemoved(String property, String group) {
        return getString("group-value-removed", "&3[TAB] %property% has been successfully removed from group &e%group%")
                .replace("%property%", property).replace("%group%", group);
    }

    public String getPlayerDataRemoved(String player) {
        return getString("user-data-removed", "&3[TAB] All data has been successfully removed from player &e%player%").replace("%player%", player);
    }

    public String getPlayerValueAssigned(String property, String value, String player) {
        return getString("user-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to player &e%player%")
                .replace("%property%", property).replace("%value%", value).replace("%player%", player);
    }

    public String getPlayerValueRemoved(String property, String player) {
        return getString("user-value-removed", "&3[TAB] %property% has been successfully removed from player &e%player%")
                .replace("%property%", property).replace("%player%", player);
    }

    public String getPlayerNotFound(String name) {
        return getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
    }

    public String getInvalidNumber(String input) {
        return getString("invalid-number", "\"%input%\" is not a number!").replace("%input%", input);
    }

    public String getScoreboardNotFound(String name) {
        return getString("scoreboard-not-found", "&cNo scoreboard found with the name \"%name%\"").replace("%name%", name);
    }

    public String getBossBarAnnouncementSuccess(String bar, int length) {
        return getString("bossbar-announcement-success", "&aAnnouncing bossbar &6%bossbar% &afor %length% seconds.")
                .replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }

    public String getBossBarSendSuccess(String player, String bar, int length) {
        return getString("bossbar-send-success", "&aSending bossbar &6%bossbar% &ato player &6%player% &afor %length% seconds.")
                .replace("%player%", player).replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
    }
}