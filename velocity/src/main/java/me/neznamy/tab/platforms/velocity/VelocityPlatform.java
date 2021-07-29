package me.neznamy.tab.platforms.velocity;

import java.io.File;
import java.util.Optional;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.GlobalPlayerlist;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

	//instance of proxyserver
	private ProxyServer server;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of proxyserver
	 */
	public VelocityPlatform(ProxyServer server, PluginMessageHandler plm) {
		super(plm);
		this.server = server;
	}
	
	@Override
	public PermissionPlugin detectPermissionPlugin() {
		Optional<PluginContainer> luckperms = server.getPluginManager().getPlugin("luckperms");
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			return new VaultBridge(plm);
		} else if (luckperms.isPresent()) {
			return new LuckPerms(luckperms.get().getDescription().getVersion().get());
		} else {
			return new VaultBridge(plm);
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
			tab.addPlayer(new VelocityTabPlayer(p, plm));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		server.getConsoleCommandSource().sendMessage(Identity.nil(), Component.text(translateColors ? message.replace('&', '\u00a7') : message));
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