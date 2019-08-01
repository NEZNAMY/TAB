package me.neznamy.tab.bungee;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.HeaderFooter;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.TabObjective;
import me.neznamy.tab.shared.TabObjective.TabObjectiveType;

public class IConfigs {

	public static void loadConfig() throws Exception{
		Configs.config = new ConfigurationFile("bungeeconfig.yml", "config.yml");
		TabObjective.customValue = Configs.config.getString("tablist-objective-value", "%ping%");
		TabObjective.type = (TabObjective.customValue.length() == 0) ? TabObjectiveType.NONE : TabObjectiveType.CUSTOM;
		Playerlist.refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		Playerlist.enable = Configs.config.getBoolean("change-tablist-prefix-suffix", true);
		NameTag16.enable = Configs.config.getBoolean("change-nametag-prefix-suffix", true);
		NameTag16.refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		HeaderFooter.refresh = Configs.config.getInt("header-footer-refresh-interval-milliseconds", 50);
	}
}