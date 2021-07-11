package me.neznamy.tab.shared.command.level1;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholder;

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
		super("cpu", "tab.cpu");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TAB tab = TAB.getInstance();
		Map<String, Float> placeholders = tab.getCPUManager().getPlaceholderUsage();
		float placeholdersTotal = 0;
		for (Float time : placeholders.values()) placeholdersTotal += time;

		Map<String, Float> bridgeplaceholders = tab.getCPUManager().getBridgeUsage();
		float bridgeplaceholdersTotal = 0;
		for (Float time : bridgeplaceholders.values()) bridgeplaceholdersTotal += time;

		Map<Object, Map<UsageType, Float>> features = tab.getCPUManager().getFeatureUsage();
		float featuresTotal = 0;
		for (Map<UsageType, Float> map : features.values()) {
			for (Float time : map.values()) {
				featuresTotal += time;
			}
		}

		sendMessage(sender, " ");
		sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6CPU stats from the last 10 seconds");
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Top 5 placeholders:");
		int printCounter = 0;
		for (Entry<String, Float> entry : placeholders.entrySet()) {
			if (printCounter++ == 5) break;
			String refresh = "";
			Placeholder p = TAB.getInstance().getPlaceholderManager().getPlaceholder(entry.getKey());
			if (p != null) refresh = " &8(" + p.getRefresh() + ")&7";
			String colorized = entry.getKey().startsWith("%sync:") ? "&c" + decimal3.format(entry.getValue()) : colorize(decimal3.format(entry.getValue()), 1, 0.3f);
			sendMessage(sender, String.format("&8&l%s &7%s - %s%%", LINE_CHAR, entry.getKey() + refresh, colorized));
		}
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Some internal separately measured methods:");
		for (Entry<String, Float> entry : tab.getCPUManager().getMethodUsage().entrySet()) {
			sendMessage(sender, String.format("&8&l%s &7%s: %s%%", LINE_CHAR, entry.getKey(), colorize(decimal3.format(entry.getValue()), 5, 2)));
		}
		sendMessage(sender, SEPARATOR);
		if (tab.getPlatform().getSeparatorType().equals("server")) {
			sendMessage(sender, "&8&l" + LINE_CHAR + " &6Placeholder usage on Bukkit servers:");
			for (Entry<String, Float> entry : bridgeplaceholders.entrySet()) {
				if (entry.getValue() < 0.1) continue;
				sendMessage(sender, String.format("&8&l%s &7%s - %s%%", LINE_CHAR, entry.getKey(), colorize(decimal3.format(entry.getValue()), 1, 0.3f)));
			}
			sendMessage(sender, SEPARATOR);
		}
		if (sender != null) {
			sendToPlayer(sender, features);
		} else {
			sendToConsole(features);
		}
		sendMessage(sender, SEPARATOR);
		sendMessage(sender, String.format("&8&l%s &7Threads created by the plugin (active/total): &7%s", LINE_CHAR, tab.getCPUManager().getThreadCount()));
		if (sender != null) {
			sendPacketCountToPlayer(sender);
		} else {
			sendPacketCountToConsole();
		}
		sendMessage(sender, String.format("&8&l%s &6&lPlaceholders Total: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(placeholdersTotal), 10, 5)));
		if (tab.getPlatform().getSeparatorType().equals("server")) {
			sendMessage(sender, String.format("&8&l%s &6&lBukkit bridge placeholders Total: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(bridgeplaceholdersTotal), 10, 5)));
		}
		sendMessage(sender, String.format("&8&l%s &6&lPlugin internals: &a&l%s%%", LINE_CHAR, colorize(decimal3.format(featuresTotal-placeholdersTotal), 10, 5)));
		sendMessage(sender, String.format("&8&l%s &6&lTotal: &e&l%s%%", LINE_CHAR, colorize(decimal3.format(featuresTotal + bridgeplaceholdersTotal), 10, 5)));
		sendMessage(sender, "&8&l" + LINE_CHAR + "&8&m             &r&8&l[ &bTAB CPU Stats &8&l]&r&8&l&m             ");
		sendMessage(sender, " ");
	}

	public void sendToConsole(Map<Object, Map<UsageType, Float>> features) {
		TAB.getInstance().getPlatform().sendConsoleMessage("&8&l" + LINE_CHAR + " &6Features:", true);
		for (Entry<Object, Map<UsageType, Float>> entry : features.entrySet()) {
			float featureTotal = 0;
			for (Float f : entry.getValue().values()) {
				featureTotal += f;
			}
			String core = String.format("&8&l%s &7%s (%s%%&7):", LINE_CHAR, entry.getKey(), colorize(decimal3.format(featureTotal), 5, 1));
			List<String> messages = new ArrayList<>();
			for (Entry<UsageType, Float> type : entry.getValue().entrySet()){
				messages.add(String.format("&8&l%s     &7%s - %s%%", LINE_CHAR, type.getKey(), colorize(decimal3.format(type.getValue()), 5, 1)));
			}
			TAB.getInstance().getPlatform().sendConsoleMessage(core, true);
			for (String message : messages) {
				TAB.getInstance().getPlatform().sendConsoleMessage(message, true);
			}
		}
	}

	public void sendToPlayer(TabPlayer sender, Map<Object, Map<UsageType, Float>> features) {
		sendMessage(sender, "&8&l" + LINE_CHAR + " &6Features (hover with cursor for more info):");
		for (Entry<Object, Map<UsageType, Float>> entry : features.entrySet()) {
			float featureTotal = 0;
			for (Float f : entry.getValue().values()) {
				featureTotal += f;
			}
			String core = String.format("&8&l%s &7%s (%s%%&7):", LINE_CHAR, entry.getKey(), colorize(decimal3.format(featureTotal), 5, 1));
			List<String> messages = new ArrayList<>();
			for (Entry<UsageType, Float> type : entry.getValue().entrySet()){
				messages.add("&3" + type.getKey().toString() + " - " + colorize(decimal3.format(type.getValue()), 5, 1) + "%");
			}
			IChatBaseComponent message = new IChatBaseComponent(core.replace('&', '\u00a7'));
			message.onHoverShowText(String.join("\n", messages).replace('&', '\u00a7'));
			sender.sendMessage(message);
		}
	}
	
	public void sendPacketCountToConsole() {
		Map<Object, AtomicInteger> packets = TAB.getInstance().getCPUManager().getSentPackets();
		int count = 0;
		List<String> messages = new ArrayList<>();
		for (Entry<Object, AtomicInteger> entry : packets.entrySet()) {
			count += entry.getValue().get();
			messages.add("&8&l" + LINE_CHAR + "     &7" + entry.getKey().toString() + " - " + entry.getValue());
		}
		TAB.getInstance().getPlatform().sendConsoleMessage("&8&l" + LINE_CHAR + " &r&7Packets sent by the plugin: " + count, true);
		for (String message : messages) {
			TAB.getInstance().getPlatform().sendConsoleMessage(message, true);
		}
	}
	
	public void sendPacketCountToPlayer(TabPlayer sender) {
		Map<Object, AtomicInteger> packets = TAB.getInstance().getCPUManager().getSentPackets();
		int count = 0;
		List<String> messages = new ArrayList<>();
		for (Entry<Object, AtomicInteger> entry : packets.entrySet()) {
			count += entry.getValue().get();
			messages.add("&3" + entry.getKey().toString() + " - " + entry.getValue());
		}
		IChatBaseComponent message = new IChatBaseComponent(("&8&l" + LINE_CHAR + " &r&7Packets sent by the plugin (hover for more info): " + count).replace('&', '\u00a7'));
		message.onHoverShowText(String.join("\n", messages).replace('&', '\u00a7'));
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