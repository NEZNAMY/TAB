package me.neznamy.tab.shared;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUManager;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.rgb.TextColor;

/**
 * Universal variable and method storage
 */
public class Shared {

	//name of the pipeline decoder injected in netty
	public static final String DECODER_NAME = "TABReader";
	
	//name of plugin messaging channel
	public static final String CHANNEL_NAME = "tab:placeholders";
	
	//version of plugin
	public static final String pluginVersion = "2.8.8-pre12";

	//player data
	public static final Map<UUID, TabPlayer> data = new ConcurrentHashMap<UUID, TabPlayer>();
	
	//player data by entityId, used by unlimited nametag mode for better performance
	public static final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<Integer, TabPlayer>();
	
	//the command
	public static final TabCommand command = new TabCommand();
	
	//command used if plugin is disabled due to a broken configuration file
	public static final DisabledCommand disabledCommand = new DisabledCommand();
	
	//if plugin is disabled due to a broken configuration file or not
	public static boolean disabled;
	
	//platform interface
	public static PlatformMethods platform;
	
	//cpu manager
	public static CPUManager cpu;
	
	//error manager
	public static ErrorManager errorManager;
	
	//permission plugin interface
	public static PermissionPlugin permissionPlugin;
	
	//feature manager
	public static FeatureManager featureManager;
	
	//name of broken configuration file filled on load and used in disabledCommand
	public static String brokenFile = "-";

	/**
	 * Returns all players
	 * @return all players
	 */
	public static Collection<TabPlayer> getPlayers(){
		return data.values();
	}
	
	/**
	 * Returns player by name
	 * @param name - exact name of player
	 * @return the player
	 */
	public static TabPlayer getPlayer(String name) {
		for (TabPlayer p : data.values()) {
			if (p.getName().equals(name)) return p;
		}
		return null;
	}
	
	/**
	 * Returns player by uuid
	 * @param uniqueId - player uuid
	 * @return the player
	 */
	public static TabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}
	
	/**
	 * Returns player by tablist uuid. This is required due to Velocity as player uuid and tablist uuid do ont match there
	 * @param tablistId - tablist id of player
	 * @return the player or null if not found
	 */
	public static TabPlayer getPlayerByTablistUUID(UUID tablistId) {
		for (TabPlayer p : data.values()) {
			if (p.getUniqueId().toString().equals(tablistId.toString())) return p;
			if (p.getOfflineUUID().equals(tablistId.toString())) return p;
		}
		return null;
	}
	
	/**
	 * Sends console message with tab prefix and specified message and color
	 * @param color - color to use
	 * @param message - message to send
	 */
	public static void print(char color, String message) {
		platform.sendConsoleMessage("&" + color + "[TAB] " + message,true);
	}
	
	/**
	 * Sends a console message with debug prefix if debug is enabled in config
	 * @param message - message to be sent into console
	 */
	public static void debug(String message) {
		if (Configs.SECRET_debugMode) platform.sendConsoleMessage("&9[TAB DEBUG] " + message, true);
	}
	
	/**
	 * Sends credit message to players
	 * @param to - player to send message to
	 */
	public static void sendPluginInfo(TabPlayer to) {
		if (Premium.is() && !to.hasPermission("tab.admin")) return;
		IChatBaseComponent message = new IChatBaseComponent("TAB v" + pluginVersion).setColor(TextColor.of(EnumChatFormat.DARK_AQUA)).onHoverShowText(Placeholders.colorChar + "aClick to visit plugin's spigot page").onClickOpenUrl("https://www.spigotmc.org/resources/57806/");
		message.addExtra(new IChatBaseComponent(" by _NEZNAMY_").setColor(TextColor.of(EnumChatFormat.BLACK)));
		to.sendCustomPacket(new PacketPlayOutChat(message));
	}
	
	/**
	 * Loads the entire plugin
	 * @param inject - if players should be injected or not
	 */
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
			getPlayers().forEach(p -> p.markAsLoaded());
			errorManager.printConsoleWarnCount();
			print('a', "Enabled in " + (System.currentTimeMillis()-time) + "ms");
			platform.callLoadEvent();
		} catch (YAMLException e) {
			print('c', "Did not enable due to a broken configuration file.");
			disabled = true;
		} catch (Throwable e) {
			errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
			disabled = true;
		}
	}
	
	/**
	 * Properly unloads the entire plugin
	 */
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