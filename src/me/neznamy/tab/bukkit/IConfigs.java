package me.neznamy.tab.bukkit;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.HeaderFooter;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.TabObjective;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;

public class IConfigs{

	public static void loadConfig() throws Exception {
		Configs.config = new ConfigurationFile("bukkitconfig.yml", "config.yml");
		boolean changeNameTag = Configs.config.getBoolean("change-nametag-prefix-suffix", true);
		Playerlist.enable = Configs.config.getBoolean("change-tablist-prefix-suffix", true);
		NameTag16.refresh = NameTagX.refresh = (Configs.config.getInt("nametag-refresh-interval-ticks", 20)*50);
		Playerlist.refresh = (Configs.config.getInt("tablist-refresh-interval-ticks", 20)*50);
		boolean unlimitedTags = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false);
		Configs.modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", true);
		HeaderFooter.refresh = (Configs.config.getInt("header-footer-refresh-interval-ticks", 1)*50);
		//resetting booleans if this is a plugin reload to avoid chance of both modes being loaded at the same time
		NameTagX.enable = false;
		NameTag16.enable = false;
		if (changeNameTag) {
			if (unlimitedTags) {
				NameTagX.enable = true;
				Configs.unlimitedTags = true;
			} else {
				NameTag16.enable = true;
			}
		}
		try{
			TabObjective.type = TabObjectiveType.valueOf(Configs.config.getString("tablist-objective", "PING").toUpperCase());
		} catch (Exception e) {
			Shared.startupWarn("\"§e" + Configs.config.getString("tablist-objective", "PING") + "§c\" is not a valid type of tablist-objective. Valid options are: §ePING, HEARTS, CUSTOM and NONE §cfor disabling the feature.");
			TabObjective.type = TabObjectiveType.NONE;
		}
		TabObjective.customValue = Configs.config.getString("tablist-objective-custom-value", "%ping%");
		Placeholders.noFaction = Configs.config.getString("placeholders.faction-no", "&2Wilderness");
		Placeholders.yesFaction = Configs.config.getString("placeholders.faction-yes", "<%value%>");
		Placeholders.noTag = Configs.config.getString("placeholders.deluxetag-no", "&oNo Tag :(");
		Placeholders.yesTag = Configs.config.getString("placeholders.deluxetag-yes", "< %value% >");
		Placeholders.noAfk = Configs.config.getString("placeholders.afk-no", "");
		Placeholders.yesAfk = Configs.config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
	}
	public static void loadAdvancedConfig() throws Exception {
		Configs.advancedconfig = new ConfigurationFile("advancedconfig.yml");
		PerWorldPlayerlist.enabled = Configs.advancedconfig.getBoolean("per-world-playerlist", false);
		PerWorldPlayerlist.allowBypass = Configs.advancedconfig.getBoolean("allow-pwp-bypass-permission", false);
		PerWorldPlayerlist.ignoredWorlds = Configs.advancedconfig.getStringList("ignore-pwp-in-worlds", Lists.newArrayList("ignoredworld", "spawn"));
		Configs.sortByNickname = Configs.advancedconfig.getBoolean("sort-players-by-nickname", false);
		Configs.sortByPermissions = Configs.advancedconfig.getBoolean("sort-players-by-permissions", false);
		Configs.fixPetNames = Configs.advancedconfig.getBoolean("fix-pet-names", false);
		Configs.usePrimaryGroup = Configs.advancedconfig.getBoolean("use-primary-group", true);
		Configs.primaryGroupFindingList = Configs.advancedconfig.getStringList("primary-group-finding-list", Lists.newArrayList("Owner", "Admin", "Helper", "default"));
	}
}