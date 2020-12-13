package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Velocity registry to register velocity-only placeholders
 */
public class VelocityPlaceholderRegistry implements PlaceholderRegistry {

	//instance of proxyserver
	private ProxyServer server;
	
	private static List<Placeholder> placeholders;
	
	/**
	 * Constructs new instance with given parameter
	 * @param server - instance of proxyserver
	 */
	public VelocityPlaceholderRegistry(ProxyServer server) {
		this.server = server;
	}
	
	@Override
	public List<Placeholder> registerPlaceholders() {
		placeholders = new ArrayList<Placeholder>();
		placeholders.add(new ServerPlaceholder("%maxplayers%", -1) {
			public String get() {
				return server.getConfiguration().getShowMaxPlayers()+"";
			}
		});
		for (Entry<String, String> servers : server.getConfiguration().getServers().entrySet()) {
			placeholders.add(new ServerPlaceholder("%online_" + servers.getKey() + "%", 1000) {
				public String get() {
					return server.getServer(servers.getKey()).get().getPlayersConnected().size()+"";
				}
			});
		}
		return placeholders;
	}
}