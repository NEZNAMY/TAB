package me.neznamy.tab.shared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.packets.*;
import me.neznamy.tab.shared.placeholders.*;

public class Shared {

	public static final String DECODER_NAME = "TABReader";
	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final String pluginVersion = "2.7.5-pre2";

	public static final Map<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static final Map<Integer, ITabPlayer> entityIdMap = new ConcurrentHashMap<Integer, ITabPlayer>();
	
	public static final Map<String, SimpleFeature> features = new ConcurrentHashMap<String, SimpleFeature>();
	public static final Map<String, CustomPacketFeature> custompacketfeatures = new ConcurrentHashMap<String, CustomPacketFeature>();
	public static final Map<String, RawPacketFeature> rawpacketfeatures = new ConcurrentHashMap<String, RawPacketFeature>();

	public static boolean disabled;
	public static MainClass mainClass;
	public static String separatorType;
	public static CPUManager cpu;
	public static ErrorManager errorManager;
	
	public static String brokenFile = "-";

	public static Collection<ITabPlayer> getPlayers(){
		return data.values();
	}
	public static ITabPlayer getPlayer(String name) {
		for (ITabPlayer p : data.values()) {
			if (p.getName().equals(name)) return p;
		}
		return null;
	}
	public static ITabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}
	public static ITabPlayer getPlayerByTablistUUID(UUID tablistId) {
		for (ITabPlayer p : data.values()) {
			if (p.getTablistId().toString().equals(tablistId.toString())) {
				return p;
			}
		}
		return null;
	}
	
	public static void print(char color, String message) {
		mainClass.sendConsoleMessage("&" + color + "[TAB] " + message);
	}
	public static void debug(String message) {
		if (Configs.SECRET_debugMode) mainClass.sendConsoleMessage("&7[TAB DEBUG] " + message);
	}
	public static void sendPluginInfo(ITabPlayer to) {
		if (Premium.is() && !to.hasPermission("tab.admin")) return;
		IChatBaseComponent message = new IChatBaseComponent("TAB v" + pluginVersion).setColor(EnumChatFormat.DARK_AQUA).onHoverShowText(Placeholders.colorChar + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
		message.addExtra(new IChatBaseComponent(" by _NEZNAMY_").setColor(EnumChatFormat.BLACK));
		to.sendCustomPacket(new PacketPlayOutChat(message));
	}
	public static void load(boolean inject) {
		try {
			long time = System.currentTimeMillis();
			disabled = false;
			cpu = new CPUManager();
			errorManager = new ErrorManager();
			Configs.loadFiles();
			mainClass.loadFeatures(inject);
			features.values().forEach(f -> f.load());
			errorManager.printConsoleWarnCount();
			print('a', "Enabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (ParserException | ScannerException e) {
			print('c', "Did not enable due to a broken configuration file.");
			disabled = true;
		} catch (Throwable e) {
			errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
			disabled = true;
		}
	}
	public static void unload() {
		try {
			if (disabled) return;
			long time = System.currentTimeMillis();
			cpu.cancelAllTasks();
			features.values().forEach(f -> f.unload());
			features.clear();
			custompacketfeatures.clear();
			rawpacketfeatures.clear();
			data.clear();
			entityIdMap.clear();
			mainClass.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			errorManager.criticalError("Failed to disable", e);
		}
	}
	public static void registerFeature(String featureName, Object featureHandler) {
		if (featureHandler instanceof SimpleFeature) {
			features.put(featureName, (SimpleFeature) featureHandler);
		}
		if (featureHandler instanceof CustomPacketFeature) {
			custompacketfeatures.put(featureName, (CustomPacketFeature) featureHandler);
		}
		if (featureHandler instanceof RawPacketFeature) {
			rawpacketfeatures.put(featureName, (RawPacketFeature) featureHandler);
		}
	}
}