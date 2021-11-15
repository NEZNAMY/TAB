package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.IOException;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;

public class MessageFile {

	private ConfigurationFile file;
	
	public MessageFile() throws YAMLException, IOException {
		file = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("messages.yml"), new File(TAB.getInstance().getPlatform().getDataFolder(), "messages.yml"));
	}
	
	public String getAnnounceCommandUsage() {
		return file.getString("announce-command-usage", "Usage: /tab announce <type> <name> <length>\nCurrently supported types: &lbar, scoreboard");
	}
	
	public String getBossBarNotEnabled() {
		return file.getString("bossbar-feature-not-enabled", "&cThis command requires the bossbar feature to be enabled.");
	}
	
	public String getBossBarAnnounceCommandUsage() {
		return file.getString("bossbar-announce-command-usage", "Usage: /tab announce bar <bar name> <length>");
	}
	
	public String getBossBarNotFound(String name) {
		return file.getString("bossbar-not-found", "&cNo bossbar found with the name \"%name%\"").replace("%name%", name);
	}
	
	public String getBossBarAlreadyAnnounced() {
		return file.getString("bossbar-already-announced", "&cThis bossbar is already being announced");
	}
	
	public String getGroupDataRemoved(String group) {
		return file.getString("group-data-removed", "&3[TAB] All data has been successfully removed from group &e%group%").replace("%group%", group);
	}
	
	public String getGroupValueAssigned(String property, String value, String group) {
		return file.getString("group-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to group &e%group%")
				.replace("%property%", property).replace("%value%", value).replace("%group%", group);
	}
	
	public String getGroupValueRemoved(String property, String group) {
		return file.getString("group-value-removed", "&3[TAB] %property% has been successfully removed from group &e%group%")
				.replace("%property%", property).replace("%group%", group);
	}
	
	public String getPlayerDataRemoved(String player) {
		return file.getString("user-data-removed", "&3[TAB] All data has been successfully removed from player &e%player%").replace("%player%", player);
	}
	
	public String getPlayerValueAssigned(String property, String value, String player) {
		return file.getString("user-value-assigned", "&3[TAB] %property% '&r%value%&r&3' has been successfully assigned to player &e%player%")
				.replace("%property%", property).replace("%value%", value).replace("%player%", player);
	}
	
	public String getPlayerValueRemoved(String property, String player) {
		return file.getString("group-value-removed", "&3[TAB] %property% has been successfully removed from player &e%player%")
				.replace("%property%", property).replace("%player%", player);
	}
	
	public String getParseCommandUsage() {
		return file.getString("parse-command-usage", "Usage: /tab parse <player> <placeholder>");
	}
	
	public String getSendCommandUsage() {
		return file.getString("send-command-usage", "Usage: /tab send <type> <player> <bar name> <length>\nCurrently supported types: &lbar");
	}
	
	public String getSendBarCommandUsage() {
		return file.getString("send-bar-command-usage", "Usage: /tab send bar <player> <bar name> <length>");
	}
	
	public String getTeamFeatureRequired() {
		return file.getString("team-feature-required", "This command requires scoreboard teams feature enabled");
	}
	
	public String getCollisionCommandUsage() {
		return file.getString("collision-command-ussage", "Usage: /tab setcollision <player> <true/false>");
	}
	
	public String getNoPermission() {
		return file.getString("no-permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
	}
	
	public String getCommandOnlyFromGame() {
		return file.getString("command-only-from-game", "&cThis command must be ran from the game");
	}
	
	public String getPlayerNotFound(String name) {
		return file.getString("player-not-online", "&cNo online player found with the name \"%player%\"").replace("%player%", name);
	}
	
	public String getUnlimitedNametagModeNotEnabled() {
		return file.getString("unlimited-nametag-mode-not-enabled", "&c[TAB] Warning! To make this feature work, you need to enable unlimited-nametag-mode in the config!");
	}
	
	public String getInvalidNumber(String input) {
		return file.getString("invalid-number", "\"%input%\" is not a number!").replace("%input%", input);
	}
	
	public String getScoreboardFeatureNotEnabled() {
		return file.getString("scoreboard-feature-not-enabled", "&4This command requires the scoreboard feature to be enabled.");
	}
	
	public String getScoreboardAnnounceCommandUsage() {
		return file.getString("scoreboard-announce-command-usage", "Usage: /tab announce scoreboard <scoreboard name> <length>");
	}
	
	public String getScoreboardNotFound(String name) {
		return file.getString("scoreboard-not-found", "&cNo scoreboard found with the name \"%name%\"").replace("%name%", name);
	}
	
	public String getNametagPreviewOn() {
		return file.getString("nametag-preview-on", "&7Preview mode &aactivated&7.");
	}
	
	public String getNametagPreviewOff() {
		return file.getString("nametag-preview-of", "&7Preview mode &3deactivated&7.");
	}
	
	public String getReloadSuccess() {
		return file.getString("reload-success", "&3[TAB] Successfully reloaded");
	}
	
	//keeping %file% to replace later since message file may not be available
	public String getReloadFailBrokenFile() {
		return file.getString("reload-fail-file", "&3[TAB] &4Failed to reload, file %file% has broken syntax. Check console for more info.");
	}
	
	public String getScoreboardOn() {
		return file.getString("scoreboard-toggle-on", "&2Scoreboard enabled");
	}
	
	public String getScoreboardOff() {
		return file.getString("scoreboard-toggle-off", "&7Scoreboard disabled");
	}
	
	public String getBossBarOn() {
		return file.getString("bossbar-toggle-on", "&2Bossbar is now visible");
	}
	
	public String getBossBarOff() {
		return file.getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
	}
}