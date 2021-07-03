package me.neznamy.tab.platforms.bungeecord;

import java.io.File;

import me.neznamy.tab.api.event.BungeeTABLoadEvent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.UltraPermissions;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Bungeecord implementation of Platform
 */
public class BungeePlatform extends ProxyPlatform {

	//instance of plugin
	private Plugin plugin;
	
	/**
	 * Constructs new instance with given parameter
	 * @param plugin - main class
	 */
	public BungeePlatform(Plugin plugin, PluginMessageHandler plm) {
		super(plm);
		this.plugin = plugin;
	}
	
	@Override
	public PermissionPlugin detectPermissionPlugin() {
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			return new VaultBridge(plm);
		} else if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
			return new LuckPerms(ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms").getDescription().getVersion());
		} else if (ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions") != null) {
			return new UltraPermissions(ProxyServer.getInstance().getPluginManager().getPlugin("UltraPermissions").getDescription().getVersion());
		} else {
			return new VaultBridge(plm);
		}
	}

	@Override
	public void loadFeatures() throws IllegalAccessException {
		TAB tab = TAB.getInstance();
		tab.getPlaceholderManager().addRegistry(new BungeePlaceholderRegistry());
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		if (tab.getConfiguration().isPipelineInjection()) tab.getFeatureManager().registerFeature("injection", new BungeePipelineInjector(tab));
		if (tab.getConfiguration().getConfig().getBoolean("change-nametag-prefix-suffix", true)) tab.getFeatureManager().registerFeature("nametag16", new NameTag(tab));
		loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("ping-spoof.enabled", false)) tab.getFeatureManager().registerFeature("pingspoof", new PingSpoof());
		if (tab.getConfiguration().getConfig().getString("yellow-number-in-tablist", "%ping%").length() > 0) tab.getFeatureManager().registerFeature("tabobjective", new TabObjective(tab));
		if (tab.getConfiguration().getConfig().getBoolean("do-not-move-spectators", false)) tab.getFeatureManager().registerFeature("spectatorfix", new SpectatorFix());
		if (tab.getConfiguration().getConfig().getBoolean("classic-vanilla-belowname.enabled", true)) tab.getFeatureManager().registerFeature("belowname", new BelowName(tab));
		if (tab.getConfiguration().getPremiumConfig() != null && tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.enabled", false)) tab.getFeatureManager().registerFeature("scoreboard", new ScoreboardManager(tab));
		if (tab.getConfiguration().getConfig().getBoolean("global-playerlist.enabled", false)) 	tab.getFeatureManager().registerFeature("globalplayerlist", new GlobalPlayerlist(tab));
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			tab.addPlayer(new BungeeTabPlayer(p, plm));
		}
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void sendConsoleMessage(String message, boolean translateColors) {
		ProxyServer.getInstance().getConsole().sendMessage(translateColors ? message.replace('&', '\u00a7') : message);
	}
	
	@Override
	public String getServerVersion() {
		return ProxyServer.getInstance().getVersion();
	}

	@Override
	public String getSeparatorType() {
		return "server";
	}

	@Override
	public File getDataFolder() {
		return plugin.getDataFolder();
	}

	@Override
	public void callLoadEvent() {
		ProxyServer.getInstance().getPluginManager().callEvent(new BungeeTABLoadEvent());
	}

	@Override
	public int getMaxPlayers() {
		return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
	}
	
	@Override
	public String getConfigName() {
		return "bungeeconfig.yml";
	}
}