package me.neznamy.tab.platforms.bungeecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
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
	public List<Placeholder> registerPlaceholders() {
		List<Placeholder> placeholders = new ArrayList<Placeholder>();
		if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
			placeholders.add(new ServerPlaceholder("%canseeonline%", 1000) {
				public String get() {
					return TAB.getInstance().getPlayers().size() - BungeeVanishAPI.getInvisiblePlayers().size()+"";
				}
			});
			placeholders.add(new ServerPlaceholder("%canseestaffonline%", 1000) {
				public String get() {
					int count = 0;
					for (TabPlayer all : TAB.getInstance().getPlayers()) {
						if (!((BungeeTabPlayer)all).isVanished() && all.isStaff()) count++;
					}
					return count+"";
				}
			});
		}
		placeholders.add(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((ProxiedPlayer) p.getPlayer()).getDisplayName();
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			placeholders.add(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return server.getValue().getPlayers().size()+"";
				}
			});
			placeholders.add(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (((BungeeTabPlayer)TAB.getInstance().getPlayer(p.getUniqueId())).isVanished()) count--;
					}
					return count+"";
				}
			});
		}
		return placeholders;
	}
}