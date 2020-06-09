package me.neznamy.tab.shared;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.cpu.CPUManager;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.CustomPacketFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.IChatBaseComponent.TextColor;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Shared {

	public static final String DECODER_NAME = "TABReader";
	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final String pluginVersion = "2.7.8-pre2";

	public static final Map<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static final Map<Integer, ITabPlayer> entityIdMap = new ConcurrentHashMap<Integer, ITabPlayer>();
	
	public static final Map<String, Object> features = new ConcurrentHashMap<String, Object>();
	public static final Map<String, CustomPacketFeature> custompacketfeatures = new ConcurrentHashMap<String, CustomPacketFeature>();
	public static final Map<String, RawPacketFeature> rawpacketfeatures = new ConcurrentHashMap<String, RawPacketFeature>();
	public static final Map<String, Loadable> loadableFeatures = new ConcurrentHashMap<String, Loadable>();
	public static final Map<String, JoinEventListener> joinListeners = new ConcurrentHashMap<String, JoinEventListener>();
	public static final Map<String, QuitEventListener> quitListeners = new ConcurrentHashMap<String, QuitEventListener>();
	public static final Map<String, WorldChangeListener> worldChangeListeners = new ConcurrentHashMap<String, WorldChangeListener>();
	public static final Map<String, CommandListener> commandListeners = new ConcurrentHashMap<String, CommandListener>();
	
	public static boolean disabled;
	public static MainClass mainClass;
	public static String separatorType;
	public static CPUManager featureCpu;
	public static CPUManager placeholderCpu;
	public static CPUManager bukkitBridgePlaceholderCpu;
	public static ErrorManager errorManager;
	public static TabCommand command;
	
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
		IChatBaseComponent message = new IChatBaseComponent("TAB v" + pluginVersion).setColor(new TextColor(EnumChatFormat.DARK_AQUA)).onHoverShowText(Placeholders.colorChar + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
		message.addExtra(new IChatBaseComponent(" by _NEZNAMY_").setColor(new TextColor(EnumChatFormat.BLACK)));
		to.sendCustomPacket(new PacketPlayOutChat(message));
	}
	public static void load(boolean inject) {
		try {
			long time = System.currentTimeMillis();
			disabled = false;
			errorManager = new ErrorManager();
			featureCpu = new CPUManager();
			placeholderCpu = new CPUManager();
			bukkitBridgePlaceholderCpu = new CPUManager();
			Configs.loadFiles();
			mainClass.loadFeatures(inject);
			loadableFeatures.values().forEach(f -> f.load());
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
			featureCpu.cancelAllTasks();
			placeholderCpu.cancelAllTasks();
			bukkitBridgePlaceholderCpu.cancelAllTasks();
			loadableFeatures.values().forEach(f -> f.unload());
			loadableFeatures.clear();
			custompacketfeatures.clear();
			rawpacketfeatures.clear();
			joinListeners.clear();
			quitListeners.clear();
			worldChangeListeners.clear();
			features.clear();
			data.clear();
			entityIdMap.clear();
			mainClass.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			errorManager.criticalError("Failed to disable", e);
		}
	}
	public static void registerFeature(String featureName, Object featureHandler) {
		features.put(featureName, featureHandler);
		if (featureHandler instanceof Loadable) {
			loadableFeatures.put(featureName, (Loadable) featureHandler);
		}
		if (featureHandler instanceof CustomPacketFeature) {
			custompacketfeatures.put(featureName, (CustomPacketFeature) featureHandler);
		}
		if (featureHandler instanceof RawPacketFeature) {
			rawpacketfeatures.put(featureName, (RawPacketFeature) featureHandler);
		}
		if (featureHandler instanceof JoinEventListener) {
			joinListeners.put(featureName, (JoinEventListener) featureHandler);
		}
		if (featureHandler instanceof QuitEventListener) {
			quitListeners.put(featureName, (QuitEventListener) featureHandler);
		}
		if (featureHandler instanceof WorldChangeListener) {
			worldChangeListeners.put(featureName, (WorldChangeListener) featureHandler);
		}
		if (featureHandler instanceof CommandListener) {
			commandListeners.put(featureName, (CommandListener) featureHandler);
		}
	}
}