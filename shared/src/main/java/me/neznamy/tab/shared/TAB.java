package me.neznamy.tab.shared;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.HeaderFooterManager;
import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TablistFormatManager;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.AlignedPlayerlist;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * Universal variable and method storage
 */
public class TAB extends TabAPI {

	//plugin instance
	private static TAB instance;

	//version of plugin
	public static final String PLUGIN_VERSION = "3.0.0-pre4";

	//player data
	private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();
	
	//player array to reduce memory allocation when iterating
	private TabPlayer[] players = new TabPlayer[0];

	//the command
	private TabCommand command;

	//command used if plugin is disabled due to a broken configuration file
	private final DisabledCommand disabledCommand = new DisabledCommand();

	//platform interface
	private final Platform platform;

	//cpu manager
	private CpuManager cpu;

	//error manager
	private ErrorManager errorManager;

	//feature manager
	private FeatureManagerImpl featureManager;

	private Configs configuration;

	private boolean debugMode;

	private boolean disabled;

	private PlaceholderManagerImpl placeholderManager;

	//server version, always using latest on proxies
	private final ProtocolVersion serverVersion;
	
	private GroupManager groupManager;
	
	private boolean floodgate;

	public TAB(Platform platform, ProtocolVersion serverVersion) {
		this.platform = platform;
		this.serverVersion = serverVersion;
		TabAPI.setInstance(this);
		try {
			Class.forName("org.geysermc.floodgate.api.FloodgateApi");
			floodgate = true;
		} catch (ClassNotFoundException e) {
			//plugin not installed
		}
	}

	@Override
	public TabPlayer[] getOnlinePlayers(){
		return players;
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
	@Override
	public void debug(String message) {
		if (debugMode) platform.sendConsoleMessage("&9[TAB DEBUG] " + message, true);
	}

	/**
	 * Loads the entire plugin
	 */
	public String load() {
		try {
			long time = System.currentTimeMillis();
			this.errorManager = new ErrorManager(this);
			cpu = new CpuManager(errorManager);
			featureManager = new FeatureManagerImpl();
			configuration = new Configs(this);
			configuration.loadFiles();
			placeholderManager = new PlaceholderManagerImpl();
			cpu.registerPlaceholder();
			featureManager.registerFeature("placeholders", placeholderManager);
			groupManager = new GroupManager(platform.detectPermissionPlugin());
			featureManager.registerFeature("groups", groupManager);
			platform.loadFeatures();
			command = new TabCommand(this);
			featureManager.load();
			for (TabPlayer p : players) ((ITabPlayer)p).markAsLoaded();
			errorManager.printConsoleWarnCount();
			print('a', "Enabled in " + (System.currentTimeMillis()-time) + "ms");
			platform.callLoadEvent();
			disabled = false;
			return configuration.getMessages().getReloadSuccess();
		} catch (YAMLException e) {
			print('c', "Did not enable due to a broken configuration file.");
			disabled = true;
			return configuration.getReloadFailedMessage().replace("%file%", "-"); //recode soon
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
			if (configuration.getMysql() != null) configuration.getMysql().closeConnection();
			featureManager.unload();
			data.clear();
			platform.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms", true);
		} catch (Exception e) {
			data.clear();
			errorManager.criticalError("Failed to disable", e);
		}
	}

	/**
	 * Loads universal features present on all platforms with the same configuration
	 */
	public void loadUniversalFeatures() {
		if (configuration.getConfig().getBoolean("header-footer.enabled", true)) featureManager.registerFeature("headerfooter", new HeaderFooter());
		if (configuration.isRemoveGhostPlayers()) featureManager.registerFeature("ghostplayerfix", new GhostPlayerFix());
		if (serverVersion.getMinorVersion() >= 8 && configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true)) {
			if (configuration.getConfig().getBoolean("tablist-name-formatting.align-tabsuffix-on-the-right", false)) {
				featureManager.registerFeature("playerlist", new AlignedPlayerlist());
			} else {
				featureManager.registerFeature("playerlist", new Playerlist());
			}
		}
		if (configuration.getConfig().getBoolean("ping-spoof.enabled", false)) featureManager.registerFeature("pingspoof", new PingSpoof());
		if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true)) featureManager.registerFeature("tabobjective", new YellowNumber());
		if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false)) featureManager.registerFeature("spectatorfix", new SpectatorFix());
		if (configuration.getConfig().getBoolean("belowname-objective.enabled", true)) featureManager.registerFeature("belowname", new BelowName());
		if (configuration.getConfig().getBoolean("scoreboard.enabled", false)) featureManager.registerFeature("scoreboard", new ScoreboardManagerImpl());
		if (serverVersion.getMinorVersion() >= 8 && configuration.getLayout().getBoolean("enabled", false)) {
			if (getTeamManager() == null) {
				//sorting is disabled, but layout needs team names
				featureManager.registerFeature("sorting", new Sorting(null));
			}
			featureManager.registerFeature("layout", new LayoutManager());
		}
		featureManager.registerFeature("nick", new NickCompatibility());
		if (platform.isProxy()) {
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				((ProxyTabPlayer)all).getPluginMessageHandler().requestAttribute(all, "world");
			}
		}
	}

	public void addPlayer(TabPlayer player) {
		data.put(player.getUniqueId(), player);
		players = data.values().toArray(new TabPlayer[0]);
	}

	public void removePlayer(TabPlayer player) {
		data.remove(player.getUniqueId());
		players = data.values().toArray(new TabPlayer[0]);
	}

	public static TAB getInstance() {
		return instance;
	}

	public static void setInstance(TAB instance) {
		TAB.instance = instance;
	}

	@Override
	public FeatureManagerImpl getFeatureManager() {
		return featureManager;
	}

	public Platform getPlatform() {
		return platform;
	}

	public CpuManager getCPUManager() {
		return cpu;
	}

	public ErrorManager getErrorManager() {
		return errorManager;
	}

	public Configs getConfiguration() {
		return configuration;
	}

	@Override
	public ProtocolVersion getServerVersion() {
		return serverVersion;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public TabCommand getCommand() {
		return command;
	}

	public void setDebugMode(boolean debug) {
		debugMode = debug;
	}

	public DisabledCommand getDisabledCommand() {
		return disabledCommand;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	@Override
	public BossBarManager getBossBarManager() {
		return (BossBarManager) featureManager.getFeature("bossbar");
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		return (ScoreboardManager) featureManager.getFeature("scoreboard");
	}

	@Override
	public TeamManager getTeamManager() {
		if (featureManager.isFeatureEnabled("nametag16")) return (NameTag) featureManager.getFeature("nametag16");
		return (NameTag) featureManager.getFeature("nametagx");
	}

	@Override
	public PlaceholderManagerImpl getPlaceholderManager() {
		return placeholderManager;
	}

	@Override
	public TabPlayer getPlayer(String name) {
		for (TabPlayer p : data.values()) {
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		return null;
	}

	@Override
	public TabPlayer getPlayer(UUID uniqueId) {
		return data.get(uniqueId);
	}

	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		platform.sendConsoleMessage(message, translateColors);
	}

	@Override
	public HeaderFooterManager getHeaderFooterManager() {
		return (HeaderFooterManager) featureManager.getFeature("headerfooter");
	}

	@Override
	public ConfigurationFile getPlayerCache() {
		return configuration.getPlayerDataFile();
	}

	@Override
	public ConfigurationFile getConfig() {
		return configuration.getConfig();
	}
	
	@Override
	public CpuManager getThreadManager() {
		return getCPUManager();
	}

	@Override
	public PropertyConfiguration getGroups() {
		return configuration.getGroups();
	}

	@Override
	public PropertyConfiguration getUsers() {
		return configuration.getUsers();
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public boolean isFloodgateInstalled() {
		return floodgate;
	}

	@Override
	public void logError(String message, Throwable t) {
		errorManager.printError(message, t);
	}

	@Override
	public TablistFormatManager getTablistFormatManager() {
		return (TablistFormatManager) featureManager.getFeature("playerlist");
	}
}