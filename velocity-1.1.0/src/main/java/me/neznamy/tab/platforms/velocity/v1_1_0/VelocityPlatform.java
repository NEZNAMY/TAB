package me.neznamy.tab.platforms.velocity.v1_1_0;

import java.io.File;
import java.util.Optional;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
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
		Optional<PluginContainer> luckperms = server.getPluginManager().getPlugin("luckperms");
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			return new VaultBridge(Main.getInstance().getPluginMessageHandler());
		} else if (luckperms.isPresent()) {
			return new LuckPerms(luckperms.get().getDescription().getVersion().get());
		} else {
			return new VaultBridge(Main.getInstance().getPluginMessageHandler());
		}
	}
	
	@Override
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		tab.getPlaceholderManager().addRegistry(new VelocityPlaceholderRegistry(server));
		tab.getPlaceholderManager().addRegistry(new UniversalPlaceholderRegistry());
		tab.getPlaceholderManager().registerPlaceholders();
		loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("global-playerlist.enabled", false)) 	tab.getFeatureManager().registerFeature("globalplayerlist", new GlobalPlayerlist(tab));
		for (Player p : server.getAllPlayers()) {
			tab.addPlayer(new VelocityTabPlayer(p));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		server.getConsoleCommandSource().sendMessage(Identity.nil(), Component.text(translateColors ? message.replace('&', '\u00a7') : message));
	}
	
	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.contains("_")) {
			String plugin = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (plugin.equals("some")) return;
			TAB.getInstance().debug("Detected used PlaceholderAPI placeholder " + identifier);
			PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
			int refresh = pl.getDefaultRefresh();
			if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getPlayerPlaceholderRefreshIntervals().get(identifier);
			if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getServerPlaceholderRefreshIntervals().get(identifier);
			TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
				public String get(TabPlayer p) {
					Main.getInstance().getPluginMessageHandler().requestPlaceholder(p, identifier);
					return getLastValues().get(p.getName());
				}
			});
		}
	}
	
	@Override
	public String getServerVersion() {
		return server.getVersion().getName() + " v" + server.getVersion().getVersion();
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
		//not needed here
	}

	@Override
	public int getMaxPlayers() {
		return server.getConfiguration().getShowMaxPlayers();
	}
	
	@Override
	public String getConfigName() {
		return "velocityconfig.yml";
	}
}