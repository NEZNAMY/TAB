package me.neznamy.tab.platforms.bungeecord;

import java.util.Map.Entry;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import me.neznamy.tab.shared.proxy.ProxyPlaceholderRegistry;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Bungeecord registry to register bungeecord-only placeholders
 */
public class BungeePlaceholderRegistry extends ProxyPlaceholderRegistry {

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		super.registerPlaceholders(manager);
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((ProxiedPlayer) p.getPlayer()).getDisplayName();
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			manager.registerServerPlaceholder(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return String.valueOf(server.getValue().getPlayers().size());
				}
			});
			manager.registerServerPlaceholder(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (((BungeeTabPlayer)TAB.getInstance().getPlayer(p.getUniqueId())).isVanished()) count--;
					}
					return String.valueOf(count);
				}
			});
		}
	}
}