package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.command.SubCommand;

public class CpuCommand extends SubCommand {

	public CpuCommand() {
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		int dataSize = Shared.cpuHistory.size();
		sendMessage(sender, " ");
		sendMessage(sender, "&8&l&m╔             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l║ &6TAB CPU STATS FROM THE LAST MINUTE");
		sendMessage(sender, "&8&l&m╠                                       ");
		sendMessage(sender, "&8&l║ &6Placeholders using over 0.01%:");
		Map<String, Long> placeholders = sortByValue(getPlaceholderCpu(dataSize));
		for (Entry<String, Long> entry : placeholders.entrySet()) {
			if (entry.getValue()/dataSize > 100000) sendMessage(sender, "&8&l║ &7" + entry.getKey() + " - &a" + colorizePlaceholder(Shared.decimal3.format((float)entry.getValue()/dataSize/10000000)) + "%");
		}
		long placeholdersTotal = 0;
		for (Long time : placeholders.values()) placeholdersTotal += time;
		sendMessage(sender, "&8&l&m╠                                       ");
		sendMessage(sender, "&8&l║ &6Feature specific:");
		Map<Feature, Long> features = sortByValue(getFeatureCpu(dataSize));
		for (Entry<Feature, Long> entry : features.entrySet()) {
			sendMessage(sender, "&8&l║ &7" + entry.getKey().toString() + " - &a" + colorizeFeature(Shared.decimal3.format((float)entry.getValue()/dataSize/10000000)) + "%");
		}
		long featuresTotal = 0;
		for (Long time : features.values()) featuresTotal += time;
		sendMessage(sender, "&8&l&m╠                                       ");
		sendMessage(sender, "&8&l║ &7&lPLACEHOLDERS TOTAL: &a&l" + Shared.decimal3.format((float)placeholdersTotal/dataSize/10000000) + "%");
		sendMessage(sender, "&8&l║ &7&lPLUGIN TOTAL: &e&l" + Shared.decimal3.format((float)featuresTotal/dataSize/10000000) + "%");
		sendMessage(sender, "&8&l&m╚             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
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
	private static HashMap<Feature, Long> getFeatureCpu(int history) {
		HashMap<Feature, Long> values = new HashMap<Feature, Long>();
		for (int i=0; i<Shared.cpuHistory.size(); i++) {
			for (Entry<Feature, Long> entry : Shared.cpuHistory.get(i).getValues().entrySet()) {
				Feature feature = entry.getKey();
				if (!values.containsKey(feature)) values.put(feature, 0L);
				values.put(feature, values.get(feature)+entry.getValue());
			}
		}
		return values;
	}
	private static HashMap<String, Long> getPlaceholderCpu(int history) {
		HashMap<String, Long> values = new HashMap<String, Long>();
		for (ConcurrentHashMap<String, Long> sample : Shared.placeholderCpuHistory) {
			for (Entry<String, Long> placeholder : sample.entrySet()) {
				if (!values.containsKey(placeholder.getKey())) values.put(placeholder.getKey(), 0L);
				values.put(placeholder.getKey(), values.get(placeholder.getKey()) + placeholder.getValue());
			}
		}
		return values;
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