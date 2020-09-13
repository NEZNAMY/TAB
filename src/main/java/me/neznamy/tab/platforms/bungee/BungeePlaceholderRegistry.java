package me.neznamy.tab.platforms.bungee;

import java.util.Map.Entry;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Bungeecord registry to register bungeecord-only placeholders
 */
public class BungeePlaceholderRegistry implements PlaceholderRegistry {

	@Override
	public void registerPlaceholders() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseeonline%", 1000) {
				public String get() {
					return Shared.getPlayers().size() - BungeeVanishAPI.getInvisiblePlayers().size()+"";
				}
			});
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseestaffonline%", 1000) {
				public String get() {
					int count = 0;
					for (ITabPlayer all : Shared.getPlayers()) {
						if (!all.isVanished() && all.isStaff()) count++;
					}
					return count+"";
				}
			});
		}
		Placeholders.registerPlaceholder(new ServerPlaceholder("%maxplayers%", -1) {
			public String get() {
				return ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%displayname%", 500) {
			public String get(ITabPlayer p) {
				return ((ProxiedPlayer) p.getPlayer()).getDisplayName();
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return server.getValue().getPlayers().size()+"";
				}
			});
			Placeholders.registerPlaceholder(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (Shared.getPlayer(p.getUniqueId()).isVanished()) count--;
					}
					return count+"";
				}
			});
		}
	}
}