package me.neznamy.tab.shared;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUManager;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.IChatBaseComponent.TextColor;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Universal variable and method storage
 */
public class Shared {

	public static final String DECODER_NAME = "TABReader";
	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final String pluginVersion = "2.8.5-pre6";

	public static final Map<UUID, ITabPlayer> data = new ConcurrentHashMap<UUID, ITabPlayer>();
	public static final Map<Integer, ITabPlayer> entityIdMap = new ConcurrentHashMap<Integer, ITabPlayer>();
	public static final TabCommand command = new TabCommand();
	
	public static boolean disabled;
	public static PlatformMethods platform;
	public static CPUManager cpu;
	public static ErrorManager errorManager;
	public static PermissionPlugin permissionPlugin;
	public static FeatureManager featureManager;
	
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
			if (p.getUniqueId().toString().equals(tablistId.toString())) return p;
			if (p.getOfflineId().toString().equals(tablistId.toString())) return p;
		}
		return null;
	}
	
	public static void print(char color, String message) {
		platform.sendConsoleMessage("&" + color + "[TAB] " + message,true);
	}
	public static void debug(String message) {
		if (Configs.SECRET_debugMode) platform.sendConsoleMessage("&9[TAB DEBUG] " + message, true);
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
			cpu = new CPUManager();
			featureManager = new FeatureManager();
			Configs.loadFiles();
			permissionPlugin = platform.detectPermissionPlugin();
			platform.loadFeatures(inject);
			featureManager.load();
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
			cpu.cancelAllTasks();
			featureManager.unload();
			data.clear();
			entityIdMap.clear();
			platform.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms", true);
		} catch (Throwable e) {
			errorManager.criticalError("Failed to disable", e);
		}
	}
}