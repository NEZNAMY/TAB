package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUManager;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.IChatBaseComponent.TextColor;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Shared {

	public static final String DECODER_NAME = "TABReader";
	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final String pluginVersion = "2.8.4-pre5";

	public static final Map<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static final Map<Integer, ITabPlayer> entityIdMap = new ConcurrentHashMap<Integer, ITabPlayer>();
	public static final TabCommand command = new TabCommand();
	
	public static Map<String, Object> features = new ConcurrentHashMap<String, Object>();
	public static List<PlayerInfoPacketListener> playerInfoListeners = new ArrayList<PlayerInfoPacketListener>();
	public static List<RawPacketFeature> rawpacketfeatures = new ArrayList<RawPacketFeature>();
	public static List<Loadable> loadableFeatures = new ArrayList<Loadable>();
	public static List<JoinEventListener> joinListeners = new ArrayList<JoinEventListener>();
	public static List<QuitEventListener> quitListeners = new ArrayList<QuitEventListener>();
	public static List<WorldChangeListener> worldChangeListeners = new ArrayList<WorldChangeListener>();
	public static List<CommandListener> commandListeners = new ArrayList<CommandListener>();
	public static List<Refreshable> refreshables = new ArrayList<Refreshable>();
	
	public static boolean disabled;
	public static PlatformMethods platform;
	public static CPUManager featureCpu;
	public static CPUManager placeholderCpu;
	public static CPUManager bukkitBridgePlaceholderCpu;
	public static ErrorManager errorManager;
	public static PermissionPlugin permissionPlugin;
	
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
		platform.sendConsoleMessage("&" + color + "[TAB] " + message);
	}
	public static void debug(String message) {
		if (Configs.SECRET_debugMode) platform.sendConsoleMessage("&9[TAB DEBUG] " + message);
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
			permissionPlugin = platform.detectPermissionPlugin();
			platform.loadFeatures(inject);
			loadableFeatures.forEach(f -> f.load());
			getPlayers().forEach(p -> p.onJoinFinished = true);
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
			CPUManager.cancelAllTasks();
			loadableFeatures.forEach(f -> f.unload());
			loadableFeatures = new ArrayList<>();
			playerInfoListeners = new ArrayList<>();
			rawpacketfeatures = new ArrayList<>();
			joinListeners = new ArrayList<>();
			quitListeners = new ArrayList<>();
			worldChangeListeners = new ArrayList<>();
			commandListeners = new ArrayList<>();
			refreshables = new ArrayList<>();
			features = new HashMap<>();
			data.clear();
			entityIdMap.clear();
			platform.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms");
		} catch (Throwable e) {
			errorManager.criticalError("Failed to disable", e);
		}
	}
	public static void registerFeature(String featureName, Object featureHandler) {
		features.put(featureName, featureHandler);
		if (featureHandler instanceof Loadable) {
			loadableFeatures.add((Loadable) featureHandler);
		}
		if (featureHandler instanceof PlayerInfoPacketListener) {
			playerInfoListeners.add((PlayerInfoPacketListener) featureHandler);
		}
		if (featureHandler instanceof RawPacketFeature) {
			rawpacketfeatures.add((RawPacketFeature) featureHandler);
		}
		if (featureHandler instanceof JoinEventListener) {
			joinListeners.add((JoinEventListener) featureHandler);
		}
		if (featureHandler instanceof QuitEventListener) {
			quitListeners.add((QuitEventListener) featureHandler);
		}
		if (featureHandler instanceof WorldChangeListener) {
			worldChangeListeners.add((WorldChangeListener) featureHandler);
		}
		if (featureHandler instanceof CommandListener) {
			commandListeners.add((CommandListener) featureHandler);
		}
		if (featureHandler instanceof Refreshable) {
			refreshables.add((Refreshable) featureHandler);
		}
	}
}