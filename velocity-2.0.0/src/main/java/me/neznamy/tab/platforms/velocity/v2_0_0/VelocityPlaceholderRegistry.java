package me.neznamy.tab.platforms.velocity.v2_0_0;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Velocity registry to register velocity-only placeholders
 */
public class VelocityPlaceholderRegistry implements PlaceholderRegistry {

	//instance of proxyserver
	private ProxyServer server;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of proxyserver
	 */
	public VelocityPlaceholderRegistry(ProxyServer server) {
		this.server = server;
	}
	
	@Override
	public List<Placeholder> registerPlaceholders() {
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		for (RegisteredServer rServer : server.registeredServers()) {
			placeholders.add(new ServerPlaceholder("%online_" + rServer.serverInfo().name() + "%", 1000) {
				public String get() {
					return String.valueOf(rServer.connectedPlayers().size());
				}
			});
		}
		return placeholders;
	}
}