package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

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
	public void loadFeatures() {
		super.loadFeatures();
		for (Player p : server.getAllPlayers()) {
			TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
		}
	}
	
	@Override
	public void sendConsoleMessage(String message, boolean translateColors) {
		Preconditions.checkNotNull(message, "message");
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
	public PacketBuilder getPacketBuilder() {
		return packetBuilder;
	}

	@Override
	public String getPluginVersion(String plugin) {
		Preconditions.checkNotNull(plugin, "plugin");
		Optional<PluginContainer> pl = server.getPluginManager().getPlugin(plugin.toLowerCase(Locale.US));
		return pl.flatMap(pluginContainer -> pluginContainer.getDescription().getVersion()).orElse(null);
	}
}