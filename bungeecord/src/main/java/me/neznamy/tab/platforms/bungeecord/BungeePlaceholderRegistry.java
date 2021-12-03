package me.neznamy.tab.platforms.bungeecord;

import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyPlaceholderRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * BungeeCord registry to register BungeeCord-only placeholders
 */
public class BungeePlaceholderRegistry extends ProxyPlaceholderRegistry {

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		super.registerPlaceholders(manager);
		manager.registerPlayerPlaceholder("%displayname%", 500, p -> ((ProxiedPlayer) p.getPlayer()).getDisplayName());
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			manager.registerServerPlaceholder("%online_" + server.getKey() + "%", 1000, () -> {
				int count = 0;
				for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
					if (p.getServer() != null && p.getServer().equals(server.getValue().getName()) && !p.isVanished()) count++;
				}
				return count;
			});
		}
	}
}