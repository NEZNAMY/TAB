package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
		Map<String, Long> placeholders = sortByValue(Shared.cpu.getPlaceholderCPU());
		for (Entry<String, Long> entry : placeholders.entrySet()) {
			if (entry.getValue()/Shared.cpu.getHistory() > 100000) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - &a" + colorizePlaceholder(Shared.decimal3.format((float)entry.getValue()/Shared.cpu.getHistory()/10000000)) + "%");
		}
		long placeholdersTotal = 0;
		for (Long time : placeholders.values()) placeholdersTotal += time;
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6Feature specific:");
		Map<String, Long> features = sortByValue(Shared.cpu.getFeatureCPU());
		for (Entry<String, Long> entry : features.entrySet()) {
			sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - &a" + colorizeFeature(Shared.decimal3.format((float)entry.getValue()/Shared.cpu.getHistory()/10000000)) + "%");
		}
		long featuresTotal = 0;
		for (Long time : features.values()) featuresTotal += time;
		sendMessage(sender, "&8&l║&8&m                                                    ");
		sendMessage(sender, "&8&l║ &6&lPlaceholders Total: &a&l" + Shared.decimal3.format((float)placeholdersTotal/Shared.cpu.getHistory()/10000000) + "%");
		sendMessage(sender, "&8&l║ &6&lPlugin Total: &e&l" + Shared.decimal3.format((float)featuresTotal/Shared.cpu.getHistory()/10000000) + "%");
		sendMessage(sender, "&8&l║&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}
	private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());
		Map<K, V> result = new LinkedHashMap<>();
		for (int i=list.size()-1; i>=0; i--) {
			Entry<K, V> entry = list.get(i);
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
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