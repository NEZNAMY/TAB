package me.neznamy.tab.shared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUManager;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.PacketBuilder;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * Universal variable and method storage
 */
public class TAB {
	
	//plugin instance
	private static TAB instance;
	
	//version of plugin
	public static final String PLUGIN_VERSION = "2.9.2";

	//player data
	private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();
	
	//the command
	private TabCommand command;

	//command used if plugin is disabled due to a broken configuration file
	private final DisabledCommand disabledCommand = new DisabledCommand();
	
	//platform interface
	private Platform platform;
	
	//cpu manager
	private CPUManager cpu;
	
	//error manager
	private ErrorManager errorManager;
	
	//permission plugin interface
	private PermissionPlugin permissionPlugin;
	
	//feature manager
	private FeatureManager featureManager;
	
	//name of broken configuration file filled on load and used in disabledCommand
	private String brokenFile = "-";
	
	//platform-specific packet builder
	private PacketBuilder packetBuilder;
	
	private Configs configuration;
	
	private boolean debugMode;
	
	private boolean disabled;
	
	private PlaceholderManager placeholderManager;
	
	//server version, always using latest on proxies
	private ProtocolVersion serverVersion;

	public TAB(Platform platform, PacketBuilder packetBuilder, ProtocolVersion serverVersion) {
		this.platform = platform;
		this.packetBuilder = packetBuilder;
		this.serverVersion = serverVersion;
	}
	
	/**
	 * Returns true if this compilation is premium, false if not
	 * @return true if this is premium version, false if not
	 */
	public boolean isPremium() {
		return false;
	}
	
	/**
	 * Returns all players
	 * @return all players
	 */
	public Collection<TabPlayer> getPlayers(){
		return data.values();
	}
	
	/**
	 * Returns player by name
	 * @param name - exact name of player
	 * @return the player
	 */
	public TabPlayer getPlayer(String name) {
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
	public TabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}
	
	/**
	 * Returns player by tablist uuid. This is required due to Velocity as player uuid and tablist uuid do ont match there
	 * @param tablistId - tablist id of player
	 * @return the player or null if not found
	 */
	public TabPlayer getPlayerByTablistUUID(UUID tablistId) {
		for (TabPlayer p : data.values()) {
			if (p.getTablistUUID().equals(tablistId)) return p;
		}
		return null;
	}
	
	/**
	 * Sends console message with tab prefix and specified message and color
	 * @param color - color to use
	 * @param message - message to send
	 */
	public void print(char color, String message) {
		platform.sendConsoleMessage("&" + color + "[TAB] " + message, true);
	}
	
	/**
	 * Sends a console message with debug prefix if debug is enabled in config
	 * @param message - message to be sent into console
	 */
	public void debug(String message) {
		if (isDebugMode()) platform.sendConsoleMessage("&9[TAB DEBUG] " + message, true);
	}
	
	/**
	 * Loads the entire plugin
	 */
	public String load() {
		try {
			long time = System.currentTimeMillis();
			this.errorManager = new ErrorManager(this);
			cpu = new CPUManager(errorManager);
			featureManager = new FeatureManager(this);
			configuration = new Configs(this);
			configuration.loadFiles();
			setPermissionPlugin(platform.detectPermissionPlugin());
			placeholderManager = new PlaceholderManager(this);
			featureManager.registerFeature("placeholders", placeholderManager);
			platform.loadFeatures();
			setCommand(new TabCommand(this));
			featureManager.load();
			getPlayers().forEach(p -> ((ITabPlayer)p).markAsLoaded());
			errorManager.printConsoleWarnCount();
			print('a', "Enabled in " + (System.currentTimeMillis()-time) + "ms");
			platform.callLoadEvent();
			disabled = false;
			return configuration.getTranslation().getString("reloaded");
		} catch (YAMLException e) {
			print('c', "Did not enable due to a broken configuration file.");
			disabled = true;
			return configuration.getReloadFailedMessage().replace("%file%", brokenFile);
		} catch (Exception e) {
			errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
			disabled = true;
			return "&cFailed to enable due to an internal plugin error. Check console for more info.";
		}
	}
	
	/**
	 * Properly unloads the entire plugin
	 */
	public void unload() {
		if (disabled) return;
		disabled = true;
		try {
			long time = System.currentTimeMillis();
			cpu.cancelAllTasks();
			featureManager.unload();
			data.clear();
			platform.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms", true);
		} catch (Exception e) {
			errorManager.criticalError("Failed to disable", e);
		}
	}
	
	public void addPlayer(TabPlayer player) {
		data.put(player.getUniqueId(), player);
	}
	
	public void removePlayer(TabPlayer player) {
		data.remove(player.getUniqueId());
	}
	
	public static TAB getInstance() {
		return instance;
	}
	
	public static void setInstance(TAB instance) {
		TAB.instance = instance;
	}
	
	public FeatureManager getFeatureManager() {
		return featureManager;
	}
	
	public Platform getPlatform() {
		return platform;
	}
	
	public CPUManager getCPUManager() {
		return cpu;
	}
	
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}
	
	public ErrorManager getErrorManager() {
		return errorManager;
	}

	public PermissionPlugin getPermissionPlugin() {
		return permissionPlugin;
	}

	public void setPermissionPlugin(PermissionPlugin permissionPlugin) {
		this.permissionPlugin = permissionPlugin;
	}

	public String getPluginVersion() {
		return PLUGIN_VERSION;
	}
	
	public Configs getConfiguration() {
		return configuration;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public PlaceholderManager getPlaceholderManager() {
		return placeholderManager;
	}

	public TabCommand getCommand() {
		return command;
	}

	public void setCommand(TabCommand command) {
		this.command = command;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	public String getBrokenFile() {
		return brokenFile;
	}

	public void setBrokenFile(String brokenFile) {
		this.brokenFile = brokenFile;
	}

	public DisabledCommand getDisabledCommand() {
		return disabledCommand;
	}
	
	public ProtocolVersion getServerVersion() {
		return serverVersion;
	}

}