package me.neznamy.tab.shared.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Handler for "/tab cpu" subcommand
 */
public class CpuCommand extends SubCommand {

	private final DecimalFormat decimal3 = new DecimalFormat("#.###");

	private static final char LINE_CHAR = (char)9553;
	private static final String SEPARATOR = "&8&l" + LINE_CHAR + "&8&m                                                    ";

	/**
	 * Constructs new instance
	 */
	public CpuCommand() {
		super("cpu", TabConstants.Permission.COMMAND_CPU);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TAB tab = TAB.getInstance();
		Map<String, Float> placeholders = tab.getCPUManager().getPlaceholderUsage();
		double placeholdersTotal = placeholders.values().stream().mapToDouble(Float::floatValue).sum();

		Map<String, Float> bridgePlaceholders = tab.getCPUManager().getBridgeUsage();
		double bridgePlaceholdersTotal = bridgePlaceholders.values().stream().mapToDouble(Float::floatValue).sum();

		Map<String, Map<String, Float>> features = tab.getCPUManager().getFeatureUsage();
		double featuresTotal = 0;
		for (Map<String, Float> map : features.values()) {
			featuresTotal += map.values().stream().mapToDouble(Float::floatValue).sum();
		}

		sendMessage(sender, " ");
		sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6CPU stats from the last 10 seconds");
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Top 5 placeholders:");
		printPlaceholders(sender, tab.getCPUManager().getPlaceholderUsage());
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Some internal separately measured methods:");
		printMethods(sender);
		sendMessage(sender, SEPARATOR);
		if (!tab.getCPUManager().getBridgeUsage().isEmpty()) {
			sendMessage(sender, "&8&l" + LINE_CHAR + " &6Top 5 placeholders on Bukkit servers:");
			printPlaceholders(sender, tab.getCPUManager().getBridgeUsage());
			sendMessage(sender, SEPARATOR);
		}
		if (sender != null) {
			sendToPlayer(sender, features);
		} else {
			sendToConsole(features);
		}
		sendMessage(sender, SEPARATOR);
		if (sender != null) {
			sendPacketCountToPlayer(sender);
		} else {
			sendPacketCountToConsole();
		}
		sendMessage(sender, String.format("&8&l%s &6&lPlaceholders Total: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(placeholdersTotal), 10, 5)));
		if (!tab.getCPUManager().getBridgeUsage().isEmpty()) {
			sendMessage(sender, String.format("&8&l%s &6&lBukkit bridge placeholders Total: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(bridgePlaceholdersTotal), 10, 5)));
		}
		sendMessage(sender, String.format("&8&l%s &6&lPlugin internals: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(featuresTotal-placeholdersTotal), 10, 5)));
		sendMessage(sender, String.format("&8&l%s &6&lTotal: &e&l%s%%", LINE_CHAR, colorize(decimal3.format(featuresTotal + bridgePlaceholdersTotal), 10, 5)));
		sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}

	private void printPlaceholders(TabPlayer sender, Map<String, Float> map) {
		int printCounter = 0;
		for (Entry<String, Float> entry : map.entrySet()) {
			if (printCounter++ == 5) break;
			String refresh = "";
			Placeholder p = TAB.getInstance().getPlaceholderManager().getPlaceholder(entry.getKey());
			if (p != null && !p.isTriggerMode()) refresh = " &8(" + p.getRefresh() + ")&7";
			String colorized = entry.getKey().startsWith("%sync:") ? "&c" + decimal3.format(entry.getValue()) : colorize(decimal3.format(entry.getValue()), 1, 0.3f);
			sendMessage(sender, String.format("&8&l%s &7%s - %s%%", LINE_CHAR, entry.getKey() + refresh, colorized));
		}
	}
	
	private void printMethods(TabPlayer sender) {
		for (Entry<String, Float> entry : TAB.getInstance().getCPUManager().getMethodUsage().entrySet()) {
			sendMessage(sender, String.format("&8&l%s &7%s: %s%%", LINE_CHAR, entry.getKey(), colorize(decimal3.format(entry.getValue()), 5, 2)));
		}
	}

	public void sendToConsole(Map<String, Map<String, Float>> features) {
		TAB.getInstance().getPlatform().sendConsoleMessage("&8&l" + LINE_CHAR + " &6Features:", true);
		for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
			double featureTotal = entry.getValue().values().stream().mapToDouble(Float::floatValue).sum();
			String core = String.format("&8&l%s &7%s &7(%s%%&7):", LINE_CHAR, entry.getKey(), colorize(decimal3.format(featureTotal), 5, 1));
			List<String> messages = new ArrayList<>();
			for (Entry<String, Float> type : entry.getValue().entrySet()){
				messages.add(String.format("&8&l%s     &7%s - %s%%", LINE_CHAR, type.getKey(), colorize(decimal3.format(type.getValue()), 5, 1)));
			}
			TAB.getInstance().getPlatform().sendConsoleMessage(core, true);
			for (String message : messages) {
				TAB.getInstance().getPlatform().sendConsoleMessage(message, true);
			}
		}
	}

	public void sendToPlayer(TabPlayer sender, Map<String, Map<String, Float>> features) {
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Features (hover with cursor for more info):");
		for (Entry<String, Map<String, Float>> entry : features.entrySet()) {
			double featureTotal = entry.getValue().values().stream().mapToDouble(Float::floatValue).sum();
			String core = String.format("&8&l%s &7%s &7(%s%%&7):", LINE_CHAR, entry.getKey(), colorize(decimal3.format(featureTotal), 5, 1));
			List<String> messages = new ArrayList<>();
			for (Entry<String, Float> type : entry.getValue().entrySet()){
				messages.add("&3" + type.getKey() + " - " + colorize(decimal3.format(type.getValue()), 5, 1) + "%");
			}
			IChatBaseComponent message = new IChatBaseComponent(EnumChatFormat.color(core));
			message.getModifier().onHoverShowText(new IChatBaseComponent(EnumChatFormat.color(String.join("\n", messages))));
			sender.sendMessage(message);
		}
	}
	
	public void sendPacketCountToConsole() {
		Map<String, AtomicInteger> packets = TAB.getInstance().getCPUManager().getSentPackets();
		List<String> messages = new ArrayList<>();
		for (Entry<String, AtomicInteger> entry : packets.entrySet()) {
			messages.add("&8&l" + LINE_CHAR + "     &7" + entry.getKey() + " - " + entry.getValue());
		}
		TAB.getInstance().getPlatform().sendConsoleMessage("&8&l" + LINE_CHAR + " &r&7Packets sent by the plugin: " + packets.values().stream().mapToInt(AtomicInteger::get).sum(), true);
		for (String message : messages) {
			TAB.getInstance().getPlatform().sendConsoleMessage(message, true);
		}
	}
	
	public void sendPacketCountToPlayer(TabPlayer sender) {
		Map<String, AtomicInteger> packets = TAB.getInstance().getCPUManager().getSentPackets();
		List<String> messages = new ArrayList<>();
		for (Entry<String, AtomicInteger> entry : packets.entrySet()) {
			messages.add("&3" + entry.getKey() + " - " + entry.getValue());
		}
		IChatBaseComponent message = new IChatBaseComponent(EnumChatFormat.color("&8&l" + LINE_CHAR + " &r&7Packets sent by the plugin (hover for more info): " + packets.values().stream().mapToInt(AtomicInteger::get).sum()));
		message.getModifier().onHoverShowText(new IChatBaseComponent(EnumChatFormat.color(String.join("\n", messages))));
		sender.sendMessage(message);
	}

	/**
	 * Returns colored usage from provided usage
	 * @param usage - usage
	 * @return colored usage
	 */
	private String colorize(String usage, float threshold1, float threshold2) {
		float percent = Float.parseFloat(usage.replace(",", "."));
		if (percent > threshold1) return "&c" + usage;
		if (percent > threshold2) return "&e" + usage;
		return "&a" + usage;
	}
}