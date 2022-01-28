package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;

import java.util.Arrays;

/**
 * Velocity registry to register velocity-only placeholders
 */
public class VelocityPlaceholderRegistry implements PlaceholderRegistry {

	//instance of ProxyServer
	private final ProxyServer server;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of ProxyServer
	 */
	public VelocityPlaceholderRegistry(ProxyServer server) {
		this.server = server;
	}
	
	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		for (RegisteredServer rServer : server.getAllServers()) {
			manager.registerServerPlaceholder("%online_" + rServer.getServerInfo().getName() + "%", 1000,
					() -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> p.getServer().equals(rServer.getServerInfo().getName()) && !p.isVanished()).count());
		}
	}
}
