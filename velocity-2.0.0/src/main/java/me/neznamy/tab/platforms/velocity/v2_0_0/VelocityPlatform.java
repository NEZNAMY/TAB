package me.neznamy.tab.platforms.velocity.v2_0_0;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.connection.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.VelocityTABLoadEvent;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform implements Platform {

	//instance of proxyserver
	private ProxyServer server;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of proxyserver
	 */
	public VelocityPlatform(ProxyServer server) {
		this.server = server;
	}
	
	@Override
	public PermissionPlugin detectPermissionPlugin() {
		if (server.pluginManager().getPlugin("luckperms").isPresent()) {
			return new LuckPerms(server.pluginManager().getPlugin("luckperms").get().description().version().get());
		} else {
			return new VaultBridge(Main.plm);
		}
	}
	
	@Override
	public void loadFeatures() throws Exception{
		TAB tab = TAB.getInstance();
		tab.getPlaceholderManager().addRegistry(new VelocityPlaceholderRegistry(server));
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		if (tab.getConfiguration().pipelineInjection) tab.getFeatureManager().registerFeature("injection", new VelocityPipelineInjector(tab));
		if (tab.getConfiguration().config.getBoolean("change-nametag-prefix-suffix", true)) tab.getFeatureManager().registerFeature("nametag16", new NameTag(tab));
		loadUniversalFeatures();
		if (tab.getConfiguration().config.getBoolean("global-playerlist.enabled", false)) 	tab.getFeatureManager().registerFeature("globalplayerlist", new GlobalPlayerlist(tab));
		for (Player p : server.connectedPlayers()) {
			tab.addPlayer(new VelocityTabPlayer(p));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		server.consoleCommandSource().sendMessage(Identity.nil(), Component.text(translateColors ? message.replace('&', '\u00a7') : message));
	}
	
	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			String plugin = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (plugin.equals("some")) return;
			TAB.getInstance().debug("Detected used PlaceholderAPI placeholder " + identifier);
			PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
			int refresh = pl.defaultRefresh;
			if (pl.playerPlaceholderRefreshIntervals.containsKey(identifier)) refresh = pl.playerPlaceholderRefreshIntervals.get(identifier);
			if (pl.serverPlaceholderRefreshIntervals.containsKey(identifier)) refresh = pl.serverPlaceholderRefreshIntervals.get(identifier);
			TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
				public String get(TabPlayer p) {
					Main.plm.requestPlaceholder(p, identifier);
					return lastValue.get(p.getName());
				}
			});
			return;
		}
	}
	
	@Override
	public void convertConfig(ConfigurationFile config) {
		convertUniversalOptions(config);
		if (config.getName().equals("config.yml")) {
			if (config.getObject("global-playerlist") instanceof Boolean) {
				rename(config, "global-playerlist", "global-playerlist.enabled");
				config.set("global-playerlist.spy-servers", Arrays.asList("spyserver1", "spyserver2"));
				Map<String, List<String>> serverGroups = new HashMap<String, List<String>>();
				serverGroups.put("lobbies", Arrays.asList("lobby1", "lobby2"));
				serverGroups.put("group2", Arrays.asList("server1", "server2"));
				config.set("global-playerlist.server-groups", serverGroups);
				config.set("global-playerlist.display-others-as-spectators", false);
				TAB.getInstance().print('2', "Converted old global-playerlist section to new one in config.yml.");
			}
			rename(config, "tablist-objective-value", "yellow-number-in-tablist");
		}
	}
	
	@Override
	public String getServerVersion() {
		return server.version().getName() + " v" + server.version().getVersion();
	}

	@Override
	public String getSeparatorType() {
		return "server";
	}

	@Override
	public File getDataFolder() {
		return new File("plugins" + File.separatorChar + "TAB");
	}

	@Override
	public void callLoadEvent() {
		server.eventManager().fire(new VelocityTABLoadEvent());
	}

	@Override
	public int getMaxPlayers() {
		return server.configuration().getShowMaxPlayers();
	}
}