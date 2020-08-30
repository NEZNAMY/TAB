package me.neznamy.tab.shared.command.level1;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;

/**
 * Handler for "/tab cpu" subcommand
 */
public class CpuCommand extends SubCommand {

	private DecimalFormat decimal3 = new DecimalFormat("#.###");
	
	public CpuCommand() {
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		Map<String, Float> placeholders = Shared.cpu.getPlaceholderUsage();
		float placeholdersTotal = 0;
		for (Float time : placeholders.values()) placeholdersTotal += time;
		
		Map<String, Float> bridgeplaceholders = Shared.cpu.getBridgeUsage();
		float bridgeplaceholdersTotal = 0;
		for (Float time : bridgeplaceholders.values()) bridgeplaceholdersTotal += time;
		
		Map<TabFeature, Map<UsageType, Float>> features = Shared.cpu.getFeatureUsage();
		float featuresTotal = 0;
		for (Map<UsageType, Float> map : features.values()) {
			for (Float time : map.values()) {
				featuresTotal += time;
			}
		}
		
		sendMessage(sender, " ");
		sendMessage(sender, "&8&l•&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l• &6CPU stats from the last minute");
		sendMessage(sender, "&8&l•&8&m                                                    ");
		sendMessage(sender, "&8&l• &6Placeholders:");
		for (Entry<String, Float> entry : placeholders.entrySet()) {
			String refresh = "";
			if (!entry.getKey().toString().startsWith("%rel_")) {
				Placeholder p = Placeholders.getPlaceholder(entry.getKey()+"");
				if (p != null) refresh = " &8(" + p.cooldown + ")&7";
			} else {
				RelationalPlaceholder rel = Placeholders.getRelationalPlaceholder(entry.getKey()+"");
				if (rel != null) refresh = " &8(" + rel.refresh + ")&7";
			}
			sendMessage(sender, "&8&l• &7" + entry.getKey() + refresh + " - " + colorizePlaceholder(decimal3.format(entry.getValue())) + "%");
		}
		sendMessage(sender, "&8&l•&8&m                                                    ");
		if (Shared.platform.getSeparatorType().equals("server")) {
			sendMessage(sender, "&8&l• &6Placeholder usage on Bukkit servers:");
			for (Entry<String, Float> entry : bridgeplaceholders.entrySet()) {
				sendMessage(sender, "&8&l• &7" + entry.getKey() + " - " + colorizePlaceholder(decimal3.format(entry.getValue())) + "%");
			}
			sendMessage(sender, "&8&l•&8&m                                                    ");
		}
		sendMessage(sender, "&8&l• &6Features:");
		for (Entry<TabFeature, Map<UsageType, Float>> entry : features.entrySet()) {
			sendMessage(sender, "&8&l• &7" + entry.getKey() + ":");
			for (Entry<UsageType, Float> type : entry.getValue().entrySet()){
				sendMessage(sender, "&8&l•     &7" + type.getKey() + " - " + colorizeFeature(decimal3.format(type.getValue())) + "%");
			}
		}
		sendMessage(sender, "&8&l•&8&m                                                    ");
		sendMessage(sender, "&8&l• &6&lPlaceholders Total: &a&l" + colorizeTotalUsage(decimal3.format(placeholdersTotal)) + "%");
		if (Shared.platform.getSeparatorType().equals("server")) sendMessage(sender, "&8&l• &6&lBukkit bridge placeholders Total: &a&l" + colorizeTotalUsage(decimal3.format(bridgeplaceholdersTotal)) + "%");
		sendMessage(sender, "&8&l• &6&lPlugin internals: &a&l" + colorizeTotalUsage(decimal3.format(featuresTotal-placeholdersTotal)) + "%");
		sendMessage(sender, "&8&l• &6&lTotal: &e&l" + colorizeTotalUsage(decimal3.format(featuresTotal + bridgeplaceholdersTotal)) + "%");
		sendMessage(sender, "&8&l•&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}
	private String colorizePlaceholder(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 1) return "&c" + usage;
		if (percent > 0.3) return "&e" + usage;
		return "&a" + usage;
	}
	private String colorizeFeature(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 5) return "&c" + usage;
		if (percent > 1) return "&e" + usage;
		return "&a" + usage;
	}
	private String colorizeTotalUsage(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 10) return "&c" + usage;
		if (percent > 5) return "&e" + usage;
		return "&a" + usage;
	}
}