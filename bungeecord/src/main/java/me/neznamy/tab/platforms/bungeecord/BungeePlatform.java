package me.neznamy.tab.platforms.bungeecord;

import java.io.File;
import java.util.List;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.platforms.bungeecord.event.TabLoadEvent;
import me.neznamy.tab.platforms.bungeecord.event.TabPlayerLoadEvent;
import me.neznamy.tab.platforms.bungeecord.redisbungee.RedisBungeeSupport;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.permission.UltraPermissions;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BungeeCord implementation of Platform
 */
public class BungeePlatform extends ProxyPlatform {

	//instance of plugin
	private final Plugin plugin;
	
	private final BungeePacketBuilder packetBuilder = new BungeePacketBuilder();
	
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
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		if (tab.getConfiguration().isPipelineInjection())
			tab.getFeatureManager().registerFeature(TabConstants.Feature.PIPELINE_INJECTION, new BungeePipelineInjector());
		new BungeePlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
		super.loadFeatures();
		if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null) {
			if (RedisBungeeAPI.getRedisBungeeApi() != null) {
				tab.getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, new RedisBungeeSupport(plugin));
			} else {
				TAB.getInstance().getErrorManager().criticalError("RedisBungee plugin was detected, but it returned null API instance. Disabling hook.", null);
			}
		}
		for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			tab.addPlayer(new BungeeTabPlayer(p));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(translateColors ? EnumChatFormat.color(message) : message));
	}
	
	@Override
	public String getServerVersion() {
		return ProxyServer.getInstance().getVersion();
	}

	@Override
	public File getDataFolder() {
		return plugin.getDataFolder();
	}

	@Override
	public void callLoadEvent() {
		ProxyServer.getInstance().getPluginManager().callEvent(new TabLoadEvent());
	}
	
	@Override
	public void callLoadEvent(TabPlayer player) {
		ProxyServer.getInstance().getPluginManager().callEvent(new TabPlayerLoadEvent(player));
	}

	@Override
	public int getMaxPlayers() {
		return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
	}

	@Override
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}
	
	@Override
	public Object getSkin(List<String> properties) {
		String[][] array = new String[1][3];
		array[0][0] = "textures";
		array[0][1] = properties.get(0);
		array[0][2] = properties.get(1);
		return array;
	}

	@Override
	public boolean isProxy() {
		return true;
	}
	
	@Override
	public boolean isPluginEnabled(String plugin) {
		return ProxyServer.getInstance().getPluginManager().getPlugin(plugin) != null;
	}
}