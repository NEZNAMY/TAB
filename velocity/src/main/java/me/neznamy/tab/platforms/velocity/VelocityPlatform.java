package me.neznamy.tab.platforms.velocity;

import java.io.File;
import java.util.Optional;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.TAB;
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

	//instance of ProxyServer
	private final ProxyServer server;
	
	private final PacketBuilder packetBuilder = new PacketBuilder();
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of ProxyServer
	 */
	public VelocityPlatform(ProxyServer server, PluginMessageHandler plm) {
		super(plm);
		this.server = server;
	}
	
	@Override
	public PermissionPlugin detectPermissionPlugin() {
		Optional<PluginContainer> luckperms = server.getPluginManager().getPlugin("luckperms");
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			return new VaultBridge();
		} else if (luckperms.isPresent()) {
			return new LuckPerms(luckperms.get().getDescription().getVersion().orElse("null"));
		} else {
			return new VaultBridge();
		}
	}
	
	@Override
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		new UniversalPlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
		super.loadFeatures();
		for (Player p : server.getAllPlayers()) {
			tab.addPlayer(new VelocityTabPlayer(p));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		server.getConsoleCommandSource().sendMessage(Identity.nil(), Component.text(translateColors ? EnumChatFormat.color(message) : message));
	}
	
	@Override
	public String getServerVersion() {
		return server.getVersion().getName() + " v" + server.getVersion().getVersion();
	}

	@Override
	public File getDataFolder() {
		return new File("plugins" + File.separatorChar + "TAB");
	}

	@Override
	public void callLoadEvent() {}
	
	@Override
	public void callLoadEvent(TabPlayer player) {}

	@Override
	public int getMaxPlayers() {
		return server.getConfiguration().getShowMaxPlayers();
	}

	@Override
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}

	@Override
	public boolean isProxy() {
		return true;
	}
	
	@Override
	public boolean isPluginEnabled(String plugin) {
		return server.getPluginManager().getPlugin(plugin).isPresent();
	}
}