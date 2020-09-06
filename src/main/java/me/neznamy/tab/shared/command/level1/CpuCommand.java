package me.neznamy.tab.shared.command.level1;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
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

	private final DecimalFormat decimal3 = new DecimalFormat("#.###");
	
	private final char LINE_CHAR = (char)9553;
	private final String SEPARATOR = "&8&l" + LINE_CHAR + "&8&m                                                    ";
	private final String HEADER_FOOTER = "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ";
	private final String TITLE = "&8&l" + LINE_CHAR + " &6CPU stats from the last minute";
	private final String PLACEHOLDERS_TITLE = "&8&l" + LINE_CHAR + " &6Placeholders:";
	private final String PLACEHOLDER_LINE = "&8&l" + LINE_CHAR + " &7%identifier% - %usage%%";
	private final String BUKKIT_BRIDGE_TITLE = "&8&l" + LINE_CHAR + " &6Placeholder usage on Bukkit servers:";
	private final String BUKKIT_BRIDGE_LINE = "&8&l" + LINE_CHAR + " &7%identifier% - %usage%%";
	private final String FEATURES_TITLE = "&8&l" + LINE_CHAR + " &6Features:";
	private final String FEATURE_NAME = "&8&l" + LINE_CHAR + " &7%name%:";
	private final String FEATURE_LINE = "&8&l" + LINE_CHAR + "     &7%category% - %usage%%";
	private final String PLACEHOLDERS_TOTAL = "&8&l" + LINE_CHAR + " &6&lPlaceholders Total: &a&l%total%%";
	private final String BRIDGE_PLACEHOLDERS_TOTAL = "&8&l" + LINE_CHAR + " &6&lBukkit bridge placeholders Total: &a&l%total%%";
	private final String PLUGIN_INTERNALS = "&8&l" + LINE_CHAR + " &6&lPlugin internals: &a&l%total%%";
	private final String TOTAL = "&8&l" + LINE_CHAR + " &6&lTotal: &e&l%total%%";
	
	public CpuCommand() {
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
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
		sendMessage(sender, HEADER_FOOTER);
		sendMessage(sender, TITLE);
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, PLACEHOLDERS_TITLE);
		for (Entry<String, Float> entry : placeholders.entrySet()) {
			if (entry.getValue() < 0.1) continue;
			String refresh = "";
			if (!entry.getKey().toString().startsWith("%rel_")) {
				Placeholder p = Placeholders.getPlaceholder(entry.getKey()+"");
				if (p != null) refresh = " &8(" + p.cooldown + ")&7";
			} else {
				RelationalPlaceholder rel = Placeholders.getRelationalPlaceholder(entry.getKey()+"");
				if (rel != null) refresh = " &8(" + rel.refresh + ")&7";
			}
			sendMessage(sender, PLACEHOLDER_LINE.replace("%identifier%", entry.getKey() + refresh).replace("%usage%", colorizePlaceholder(decimal3.format(entry.getValue()))));
		}
		sendMessage(sender, SEPARATOR);
		if (Shared.platform.getSeparatorType().equals("server")) {
			sendMessage(sender, BUKKIT_BRIDGE_TITLE);
			for (Entry<String, Float> entry : bridgeplaceholders.entrySet()) {
				if (entry.getValue() < 0.1) continue;
				sendMessage(sender, BUKKIT_BRIDGE_LINE.replace("%identifier%", entry.getKey()).replace("%usage%", colorizePlaceholder(decimal3.format(entry.getValue()))));
			}
			sendMessage(sender, SEPARATOR);
		}
		sendMessage(sender, FEATURES_TITLE);
		for (Entry<TabFeature, Map<UsageType, Float>> entry : features.entrySet()) {
			sendMessage(sender, FEATURE_NAME.replace("%name%", entry.getKey().toString()));
			for (Entry<UsageType, Float> type : entry.getValue().entrySet()){
				sendMessage(sender, FEATURE_LINE.replace("%category%", type.getKey().toString()).replace("%usage%", colorizeFeature(decimal3.format(type.getValue()))));
			}
		}
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, PLACEHOLDERS_TOTAL.replace("%total%", colorizeTotalUsage(decimal3.format(placeholdersTotal))));
		if (Shared.platform.getSeparatorType().equals("server")) {
			sendMessage(sender, BRIDGE_PLACEHOLDERS_TOTAL.replace("%total%", colorizeTotalUsage(decimal3.format(bridgeplaceholdersTotal))));
		}
		sendMessage(sender, PLUGIN_INTERNALS.replace("%total%", colorizeTotalUsage(decimal3.format(featuresTotal-placeholdersTotal))));
		sendMessage(sender, TOTAL.replace("%total%", colorizeTotalUsage(decimal3.format(featuresTotal + bridgeplaceholdersTotal))));
		sendMessage(sender, HEADER_FOOTER);
		sendMessage(sender, " ");
	}
	
	/**
	 * Returns colored usage from provided usage
	 * @param usage - usage
	 * @return colored usage
	 */
	private String colorizePlaceholder(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 1) return "&c" + usage;
		if (percent > 0.3) return "&e" + usage;
		return "&a" + usage;
	}
	
	/**
	 * Returns colored usage from provided usage
	 * @param usage - usage
	 * @return colored usage
	 */
	private String colorizeFeature(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 5) return "&c" + usage;
		if (percent > 1) return "&e" + usage;
		return "&a" + usage;
	}
	
	/**
	 * Returns colored usage from provided usage
	 * @param usage - usage
	 * @return colored usage
	 */
	private String colorizeTotalUsage(String usage) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > 10) return "&c" + usage;
		if (percent > 5) return "&e" + usage;
		return "&a" + usage;
	}
}