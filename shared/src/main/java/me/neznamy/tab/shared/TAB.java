package me.neznamy.tab.shared;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.event.EventBusImpl;
import me.neznamy.tab.shared.event.impl.TabLoadEventImpl;
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
import me.neznamy.tab.shared.features.AlignedPlayerList;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Universal variable and method storage
 */
public class TAB extends TabAPI {

	//plugin instance
	private static TAB instance;

	//version of plugin
	public static final String PLUGIN_VERSION = "@plugin_version@";

	//player data
	private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();
	private final Map<UUID, TabPlayer> playersByTablistId = new ConcurrentHashMap<>();
	
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

	private EventBusImpl eventBus;

	//error manager
	private ErrorManager errorManager;

	//feature manager
	private FeatureManagerImpl featureManager;

	private Configs configuration;

	private boolean debugMode;

	private boolean disabled;

	private PlaceholderManagerImpl placeholderManager;

	//server version, always using the latest on proxies
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
		} catch (ClassNotFoundException | IllegalStateException e) {
			//plugin not installed
		}
		try {
			eventBus = new EventBusImpl();
		} catch (NoSuchMethodError e) {
			//1.7.10 or lower
		}
	}

	@Override
	public TabPlayer[] getOnlinePlayers(){
		return players;
	}

	/**
	 * Returns player by TabList UUID. This is required due to Velocity as player uuid and TabList uuid do not match there
	 * @param tabListId - TabList id of player
	 * @return the player or null if not found
	 */
	public TabPlayer getPlayerByTablistUUID(UUID tabListId) {
		return playersByTablistId.get(tabListId);
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
			this.errorManager = new ErrorManager();
			cpu = new CpuManager(errorManager);
			featureManager = new FeatureManagerImpl();
			configuration = new Configs(this);
			configuration.loadFiles();
			placeholderManager = new PlaceholderManagerImpl();
			cpu.registerPlaceholder();
			featureManager.registerFeature(TabConstants.Feature.PLACEHOLDER_MANAGER, placeholderManager);
			groupManager = new GroupManager(platform.detectPermissionPlugin());
			featureManager.registerFeature(TabConstants.Feature.GROUP_MANAGER, groupManager);
			platform.loadFeatures();
			command = new TabCommand(this);
			featureManager.load();
			for (TabPlayer p : players) ((ITabPlayer)p).markAsLoaded(false);
			cpu.enable();
			if (eventBus != null) eventBus.fire(TabLoadEventImpl.getInstance());
			platform.callLoadEvent();
			disabled = false;
			print('a', "Enabled in " + (System.currentTimeMillis()-time) + "ms");
			return configuration.getMessages().getReloadSuccess();
		} catch (YAMLException e) {
			print('c', "Did not enable due to a broken configuration file.");
			kill();
			return configuration.getReloadFailedMessage().replace("%file%", "-"); //recode soon
		} catch (Exception e) {
			errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
			kill();
			return "&cFailed to enable due to an internal plugin error. Check console for more info.";
		}
	}

	/**
	 * Properly unloads the entire plugin
	 */
	public void unload() {
		if (disabled) return;
		try {
			long time = System.currentTimeMillis();
			if (configuration.getMysql() != null) configuration.getMysql().closeConnection();
			featureManager.unload();
			platform.sendConsoleMessage("&a[TAB] Disabled in " + (System.currentTimeMillis()-time) + "ms", true);
		} catch (Exception e) {
			errorManager.criticalError("Failed to disable", e);
		}
		kill();
	}

	private void kill() {
		disabled = true;
		data.clear();
		players = new TabPlayer[0];
		cpu.cancelAllTasks();
	}

	/**
	 * Loads universal features present on all platforms with the same configuration
	 */
	public void loadUniversalFeatures() {
		if (configuration.getConfig().getBoolean("header-footer.enabled", true))
			featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter());
		if (configuration.isRemoveGhostPlayers())
			featureManager.registerFeature(TabConstants.Feature.GHOST_PLAYER_FIX, new GhostPlayerFix());
		if (serverVersion.getMinorVersion() >= 8 && configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true)) {
			if (configuration.getConfig().getBoolean("tablist-name-formatting.align-tabsuffix-on-the-right", false)) {
				featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new AlignedPlayerList());
			} else {
				featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new PlayerList());
			}
		}
		if (configuration.getConfig().getBoolean("ping-spoof.enabled", false))
			featureManager.registerFeature(TabConstants.Feature.PING_SPOOF, new PingSpoof());
		if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true))
			featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, new YellowNumber());
		if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false))
			featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());
		if (configuration.getConfig().getBoolean("belowname-objective.enabled", true))
			featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, new BelowName());
		if (configuration.getConfig().getBoolean("scoreboard.enabled", false))
			featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl());
		if (serverVersion.getMinorVersion() >= 8 && configuration.getLayout().getBoolean("enabled", false)) {
			if (getTeamManager() == null) {
				//sorting is disabled, but layout needs team names
				featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting(null));
			}
			featureManager.registerFeature(TabConstants.Feature.LAYOUT, new LayoutManager());
		}
		featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility());
	}

	public void addPlayer(TabPlayer player) {
		data.put(player.getUniqueId(), player);
		playersByTablistId.put(player.getTablistUUID(), player);
		players = data.values().toArray(new TabPlayer[0]);
	}

	public void removePlayer(TabPlayer player) {
		data.remove(player.getUniqueId());
		playersByTablistId.remove(player.getTablistUUID());
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

	@Override
	public EventBusImpl getEventBus() {
		return eventBus;
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
		return (BossBarManager) featureManager.getFeature(TabConstants.Feature.BOSS_BAR);
	}

	@Override
	public ScoreboardManager getScoreboardManager() {
		return (ScoreboardManager) featureManager.getFeature(TabConstants.Feature.SCOREBOARD);
	}

	@Override
	public TeamManager getTeamManager() {
		if (featureManager.isFeatureEnabled(TabConstants.Feature.NAME_TAGS)) return (NameTag) featureManager.getFeature(TabConstants.Feature.NAME_TAGS);
		return (NameTag) featureManager.getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
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
		return (HeaderFooterManager) featureManager.getFeature(TabConstants.Feature.HEADER_FOOTER);
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
		return (TablistFormatManager) featureManager.getFeature(TabConstants.Feature.PLAYER_LIST);
	}
}
