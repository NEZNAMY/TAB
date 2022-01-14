package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class MessageFile extends YamlConfigurationFile {

	public MessageFile() throws YAMLException, IOException {
		super(MessageFile.class.getClassLoader().getResourceAsStream("messages.yml"), new File(TAB.getInstance().getPlatform().getDataFolder(), "messages.yml"));
	}

	public String getAnnounceCommandUsage() {
		return getString("announce-command-usage", "Usage: /tab announce <type> <name> <length>\nCurrently supported types: &lbar, scoreboard");
	}

	public String getBossBarNotEnabled() {
		return getString("bossbar-feature-not-enabled", "&cThis command requires the bossbar feature to be enabled.");
	}

	public String getBossBarAnnounceCommandUsage() {
		return getString("bossbar-announce-command-usage", "Usage: /tab announce bar <bar name> <length>");
	}

	public String getBossBarNotFound(String name) {
		return getString("bossbar-not-found", "&cNo bossbar found with the name \"%name%\"").replace("%name%", name);
	}

	public String getBossBarAlreadyAnnounced() {
		return getString("bossbar-already-announced", "&cThis bossbar is already being announced");
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

	public String getParseCommandUsage() {
		return getString("parse-command-usage", "Usage: /tab parse <player> <placeholder>");
	}

	public String getSendCommandUsage() {
		return getString("send-command-usage", "Usage: /tab send <type> <player> <bar name> <length>\nCurrently supported types: &lbar");
	}

	public String getSendBarCommandUsage() {
		return getString("send-bar-command-usage", "Usage: /tab send bar <player> <bar name> <length>");
	}

	public String getTeamFeatureRequired() {
		return getString("team-feature-required", "This command requires scoreboard teams feature enabled");
	}

	public String getCollisionCommandUsage() {
		return getString("collision-command-ussage", "Usage: /tab setcollision <player> <true/false>");
	}

	public String getNoPermission() {
		return getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
	}

	public String getCommandOnlyFromGame() {
		return getString("command-only-from-game", "&cThis command must be ran from the game");
	}

	public String getPlayerNotFound(String name) {
		return getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
	}

	public String getUnlimitedNametagModeNotEnabled() {
		return getString("unlimited-nametag-mode-not-enabled", "&c[TAB] Warning! To make this feature work, you need to enable unlimited-nametag-mode in the config!");
	}

	public String getInvalidNumber(String input) {
		return getString("invalid-number", "\"%input%\" is not a number!").replace("%input%", input);
	}

	public String getScoreboardFeatureNotEnabled() {
		return getString("scoreboard-feature-not-enabled", "&4This command requires the scoreboard feature to be enabled.");
	}

	public String getScoreboardAnnounceCommandUsage() {
		return getString("scoreboard-announce-command-usage", "Usage: /tab announce scoreboard <scoreboard name> <length>");
	}

	public String getScoreboardNotFound(String name) {
		return getString("scoreboard-not-found", "&cNo scoreboard found with the name \"%name%\"").replace("%name%", name);
	}

	public String getNametagPreviewOn() {
		return getString("nametag-preview-on", "&7Preview mode &aactivated&7.");
	}

	public String getNametagPreviewOff() {
		return getString("nametag-preview-of", "&7Preview mode &3deactivated&7.");
	}

	public String getReloadSuccess() {
		return getString("reload-success", "&3[TAB] Successfully reloaded");
	}

	//keeping %file% to replace later since message file may not be available
	public String getReloadFailBrokenFile() {
		return getString("reload-fail-file", "&3[TAB] &4Failed to reload, file %file% has broken syntax. Check console for more info.");
	}

	public String getScoreboardOn() {
		return getString("scoreboard-toggle-on", "&2Scoreboard enabled");
	}

	public String getScoreboardOff() {
		return getString("scoreboard-toggle-off", "&7Scoreboard disabled");
	}

	public String getBossBarOn() {
		return getString("bossbar-toggle-on", "&2Bossbar is now visible");
	}

	public String getBossBarOff() {
		return getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
	}

	public String getScoreboardShowUsage() {
		return getString("scoreboard-show-usage", "Usage: /tab scoreboard show <scoreboard> [player]");
	}

	public String getBossBarNotMarkedAsAnnouncement() {
		return getString("bossbar-not-marked-as-announcement", "&cThis bossbar is not marked as an announcement bar and is therefore " +
				"already displayed permanently (if display condition is met)");
	}
	
	public String getBossBarAnnouncementSuccess(String bar, int length) {
		return getString("bossbar-announcement-success", "&aAnnouncing bossbar &6%bossbar% &afor %length% seconds.")
				.replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
	}

	public String getBossBarSendSuccess(String player, String bar, int length) {
		return getString("bossbar-send-success", "&aSending bossbar &6%bossbar% &ato player &6%player% &afor %length% seconds.")
				.replace("%player%", player).replace("%bossbar%", bar).replace("%length%", String.valueOf(length));
	}

	public List<String> getHelpMenu() {
		return getStringList("help-menu", Arrays.asList("&m                                                                                "
				," &8>> &3&l/tab reload"
				,"    &7Reloads plugin and config"
				," &8>> &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>"
				,"    &7Do &8/tab group/player &7to show properties"
				," &8>> &3&l/tab ntpreview"
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
	}
}