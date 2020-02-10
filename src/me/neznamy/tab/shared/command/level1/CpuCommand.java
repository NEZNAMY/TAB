package me.neznamy.tab.shared.command.level1;

import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class CpuCommand extends SubCommand {

	public CpuCommand() {
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		sendMessage(sender, " ");
		sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l║ &6TAB CPU STATS FROM THE LAST MINUTE");
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6Placeholders:");
		Map<String, Float> placeholders = Shared.cpu.getPlaceholderCPU();
		for (Entry<String, Float> entry : placeholders.entrySet()) {
			if (entry.getValue() > 0.01) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - " + colorizePlaceholder(Shared.decimal3.format(entry.getValue())) + "%");
		}
		float placeholdersTotal = 0;
		for (Float time : placeholders.values()) placeholdersTotal += time;
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6Feature specific:");
		Map<String, Float> features = Shared.cpu.getFeatureCPU();
		for (Entry<String, Float> entry : features.entrySet()) {
			sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - " + colorizeFeature(Shared.decimal3.format(entry.getValue())) + "%");
		}
		float featuresTotal = 0;
		for (Float time : features.values()) featuresTotal += time;
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6&lPlaceholders Total: &a&l" + Shared.decimal3.format(placeholdersTotal) + "%");
		sendMessage(sender, "&8&l║ &6&lPlugin Total: &e&l" + Shared.decimal3.format(featuresTotal) + "%");
		sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}
	private static String colorizePlaceholder(String value) {
		float f = Float.parseFloat(value.replace(",", "."));
		if (f > 1) return "&c" + value;
		if (f > 0.3) return "&e" + value;
		return "&a" + value;
	}
	private static String colorizeFeature(String value) {
		float f = Float.parseFloat(value.replace(",", "."));
		if (f > 5) return "&c" + value;
		if (f > 1) return "&e" + value;
		return "&a" + value;
	}
	@Override
	public Object complete(ITabPlayer sender, String currentArgument) {
		return null;
	}
}