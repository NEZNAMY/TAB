package me.neznamy.tab.platforms.velocity;

import java.util.Map.Entry;

import com.velocitypowered.api.proxy.ProxyServer;

import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Velocity registry to register velocity-only placeholders
 */
public class VelocityPlaceholderRegistry implements PlaceholderRegistry {

	private ProxyServer server;
	
	public VelocityPlaceholderRegistry(ProxyServer server) {
		this.server = server;
	}
	@Override
	public void registerPlaceholders() {
		Placeholders.registerPlaceholder(new ServerConstant("%maxplayers%") {
			public String get() {
				return server.getConfiguration().getShowMaxPlayers()+"";
			}
		});
		for (Entry<String, String> servers : server.getConfiguration().getServers().entrySet()) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%online_" + servers.getKey() + "%", 1000) {
				public String get() {
					return server.getServer(servers.getKey()).get().getPlayersConnected().size()+"";
				}
			});
		}
	}
}