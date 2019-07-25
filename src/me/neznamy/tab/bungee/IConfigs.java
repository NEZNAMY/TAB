package me.neznamy.tab.bungee;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.BossBar;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.HeaderFooter;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.TabObjective;
import me.neznamy.tab.shared.BossBar.BossBarFrame;
import me.neznamy.tab.shared.BossBar.BossBarLine;
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
		Configs.disabledHeaderFooter = Configs.config.getStringList("disable-features-in-servers.header-footer", Lists.newArrayList("disabledserver"));
		Configs.disabledTablistNames = Configs.config.getStringList("disable-features-in-servers.tablist-names", Lists.newArrayList("disabledserver"));
		Configs.disabledNametag = Configs.config.getStringList("disable-features-in-servers.nametag", Lists.newArrayList("disabledserver"));
		Configs.disabledTablistObjective = Configs.config.getStringList("disable-features-in-servers.tablist-objective", Lists.newArrayList("disabledserver"));
		Configs.disabledBossbar = Configs.config.getStringList("disable-features-in-servers.bossbar", Lists.newArrayList("disabledserver"));
	}
	public static void loadBossbar() throws Exception {
		Configs.bossbar = new ConfigurationFile("bungeebossbar.yml", "bossbar.yml");
		BossBar.enable = Configs.bossbar.getBoolean("enabled", false);
		Configs.bossbarToggleCommand = Configs.bossbar.getString("bossbar-toggle-command", "/bossbar");
		BossBar.refresh = Configs.bossbar.getInt("refresh-interval", 1000);
		BossBar.lines.clear();
		if (Configs.bossbar.getConfigurationSection("bars") != null) {
			for (String bar : Configs.bossbar.getConfigurationSection("bars").keySet()){
				List<BossBarFrame> frames = new ArrayList<BossBarFrame>();
				for (String frame : Configs.bossbar.getConfigurationSection("bars." + bar + ".frames").keySet()){
					String style = Configs.bossbar.getString("bars." + bar + ".frames." + frame + ".style");
					String color = Configs.bossbar.getString("bars." + bar + ".frames." + frame + ".color");
					String progress = Configs.bossbar.getString("bars." + bar + ".frames." + frame + ".progress");
					String message = Configs.bossbar.getString("bars." + bar + ".frames." + frame + ".text");
					frames.add(new BossBarFrame(style, color, progress, message));
				}
				if (!frames.isEmpty()) BossBar.lines.add(new BossBarLine(Configs.bossbar.getInt("bars." + bar + ".refresh", 1000), frames));
			}
		}
	}
}