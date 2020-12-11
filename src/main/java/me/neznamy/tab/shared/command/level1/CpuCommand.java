package me.neznamy.tab.shared.command.level1;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Handler for "/tab cpu" subcommand
 */
public class CpuCommand extends SubCommand {

	private final DecimalFormat decimal3 = new DecimalFormat("#.###");
	
	private final char LINE_CHAR = (char)9553;
	private final String SEPARATOR = "&8&l" + LINE_CHAR + "&8&m                                                    ";
	private final String HEADER_FOOTER = "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ";
	private final String TITLE = "&8&l" + LINE_CHAR + " &6CPU stats from the last 10 seconds";
	private final String PLACEHOLDERS_TITLE = "&8&l" + LINE_CHAR + " &6Placeholders using more than 0.1%:";
	private final String PLACEHOLDER_LINE = "&8&l" + LINE_CHAR + " &7%identifier% - %usage%%";
	private final String BUKKIT_BRIDGE_TITLE = "&8&l" + LINE_CHAR + " &6Placeholder usage on Bukkit servers:";
	private final String BUKKIT_BRIDGE_LINE = "&8&l" + LINE_CHAR + " &7%identifier% - %usage%%";
	private final String FEATURE_NAME = "&8&l" + LINE_CHAR + " &7%name% (%usage%%&7):";
	private final String FEATURE_LINE = "&8&l" + LINE_CHAR + "     &7%category% - %usage%%";
	private final String THREADS = "&8&l" + LINE_CHAR + " &7Threads created by the plugin (active/total): &7%n%";
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
			Placeholder p = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(entry.getKey()+"");
			if (p != null) refresh = " &8(" + p.getRefresh() + ")&7";
			sendMessage(sender, PLACEHOLDER_LINE.replace("%identifier%", entry.getKey() + refresh).replace("%usage%", colorizePlaceholder(decimal3.format(entry.getValue()))));
		}
		sendMessage(sender, "&8&l" + LINE_CHAR + " &8Last refresh: &6" + (System.currentTimeMillis()-((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).lastSuccessfulRefresh) + "ms ago");
		sendMessage(sender, SEPARATOR);
		if (Shared.platform.getSeparatorType().equals("server")) {
			sendMessage(sender, BUKKIT_BRIDGE_TITLE);
			for (Entry<String, Float> entry : bridgeplaceholders.entrySet()) {
				if (entry.getValue() < 0.1) continue;
				sendMessage(sender, BUKKIT_BRIDGE_LINE.replace("%identifier%", entry.getKey()).replace("%usage%", colorizePlaceholder(decimal3.format(entry.getValue()))));
			}
			sendMessage(sender, SEPARATOR);
		}
		if (sender != null) {
			sendMessage(sender, "&8&l" + LINE_CHAR + " &6Features (hover with cursor for more info):");
		} else {
			Shared.platform.sendConsoleMessage("&8&l" + LINE_CHAR + " &6Features:", true);
		}
		for (Entry<TabFeature, Map<UsageType, Float>> entry : features.entrySet()) {
			float featureTotal = 0;
			for (Float f : entry.getValue().values()) {
				featureTotal += f;
			}
			String core = FEATURE_NAME.replace("%name%", entry.getKey().toString()).replace("%usage%", colorizeFeature(decimal3.format(featureTotal)));
			List<String> messages = new ArrayList<String>();
			for (Entry<UsageType, Float> type : entry.getValue().entrySet()){
				if (sender != null) {
					//player
					messages.add("&3" + type.getKey().toString() + " - " + colorizeFeature(decimal3.format(type.getValue())) + "%");
				} else {
					//console
					messages.add(FEATURE_LINE.replace("%category%", type.getKey().toString()).replace("%usage%", colorizeFeature(decimal3.format(type.getValue()))));
				}
			}
			if (sender != null) {
				//player
				IChatBaseComponent message = new IChatBaseComponent(Placeholders.color(core));
				message.onHoverShowText(Placeholders.color(String.join("\n", messages)));
				sender.sendCustomPacket(new PacketPlayOutChat(message));
			} else {
				Shared.platform.sendConsoleMessage(core, true);
				for (String message : messages) {
					Shared.platform.sendConsoleMessage(message, true);
				}
			}
		}
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, THREADS.replace("%n%", Shared.cpu.getThreadCount()));
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