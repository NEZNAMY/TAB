package me.neznamy.tab.shared.command.level1;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class CpuCommand extends SubCommand {

	private DecimalFormat decimal3 = new DecimalFormat("#.###");
	
	public CpuCommand() {
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		Map<Object, Float> placeholders = Shared.placeholderCpu.getUsage();
		float placeholdersTotal = 0;
		for (Float time : placeholders.values()) placeholdersTotal += time;
		
		Map<Object, Float> bridgeplaceholders = Shared.bukkitBridgePlaceholderCpu.getUsage();
		float bridgeplaceholdersTotal = 0;
		for (Float time : bridgeplaceholders.values()) bridgeplaceholdersTotal += time;
		
		Map<Object, Float> features = Shared.featureCpu.getUsage();
		float featuresTotal = 0;
		for (Float time : features.values()) featuresTotal += time;
		
		sendMessage(sender, " ");
		sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l║ &6CPU stats from the last minute");
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6Placeholders:");
		for (Entry<Object, Float> entry : placeholders.entrySet()) {
			if (entry.getValue() > 0.05) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - " + colorizePlaceholder(decimal3.format(entry.getValue())) + "%");
		}
		sendMessage(sender, "&8&l║&8&m                                                    ");
		if (Shared.separatorType.equals("server")) {
			sendMessage(sender, "&8&l║ &6Placeholder usage on Bukkit servers:");
			for (Entry<Object, Float> entry : bridgeplaceholders.entrySet()) {
				if (entry.getValue() > 0.05) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - " + colorizePlaceholder(decimal3.format(entry.getValue())) + "%");
			}
			sendMessage(sender, "&8&l║&8&m                                                    ");
		}
		sendMessage(sender, "&8&l║ &6Features:");
		for (Entry<Object, Float> entry : features.entrySet()) {
			sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - " + colorizeFeature(decimal3.format(entry.getValue())) + "%");
		}
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6&lPlaceholders Total: &a&l" + colorizeTotalUsage(decimal3.format(placeholdersTotal)) + "%");
		if (Shared.separatorType.equals("server")) sendMessage(sender, "&8&l║ &6&lBukkit bridge placeholders Total: &a&l" + colorizeTotalUsage(decimal3.format(bridgeplaceholdersTotal)) + "%");
		sendMessage(sender, "&8&l║ &6&lPlugin internals: &a&l" + colorizeTotalUsage(decimal3.format(featuresTotal-placeholdersTotal)) + "%");
		sendMessage(sender, "&8&l║ &6&lTotal: &e&l" + colorizeTotalUsage(decimal3.format(featuresTotal + bridgeplaceholdersTotal)) + "%");
		sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}
	private static String colorizePlaceholder(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 1) return "&c" + usage;
		if (percent > 0.3) return "&e" + usage;
		return "&a" + usage;
	}
	private static String colorizeFeature(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 5) return "&c" + usage;
		if (percent > 1) return "&e" + usage;
		return "&a" + usage;
	}
	private static String colorizeTotalUsage(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 10) return "&c" + usage;
		if (percent > 5) return "&e" + usage;
		return "&a" + usage;
	}
}