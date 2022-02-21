package me.neznamy.tab.platforms.bungeecord;

import java.io.File;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.platforms.bungeecord.event.TabLoadEvent;
import me.neznamy.tab.platforms.bungeecord.event.TabPlayerLoadEvent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PluginMessageHandler;
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
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		if (tab.getConfiguration().isPipelineInjection())
			tab.getFeatureManager().registerFeature(TabConstants.Feature.PIPELINE_INJECTION, new BungeePipelineInjector());
		tab.getPlaceholderManager().registerPlayerPlaceholder("%displayname%", 500, p -> ((ProxiedPlayer) p.getPlayer()).getDisplayName());
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
		Preconditions.checkNotNull(message, "message");
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
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}

	@Override
	public String getPluginVersion(String plugin) {
		Preconditions.checkNotNull(plugin, "plugin");
		Plugin pl = ProxyServer.getInstance().getPluginManager().getPlugin(plugin);
		return pl == null ? null : pl.getDescription().getVersion();
	}
}