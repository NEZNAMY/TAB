package me.neznamy.tab.platforms.bungeecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
		List<Placeholder> placeholders = new ArrayList<>();
		placeholders.add(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (!all.isVanished() || p.hasPermission("tab.seevanished")) count++;
				}
				return String.valueOf(count);
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff() && (!all.isVanished() || p.hasPermission("tab.seevanished"))) count++;
				}
				return String.valueOf(count);
			}
		});
		placeholders.add(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((ProxiedPlayer) p.getPlayer()).getDisplayName();
			}
		});
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			placeholders.add(new ServerPlaceholder("%online_" + server.getKey() + "%", 1000) {
				public String get() {
					return String.valueOf(server.getValue().getPlayers().size());
				}
			});
			placeholders.add(new ServerPlaceholder("%canseeonline_" + server.getKey() + "%", 1000) {
				public String get() {
					int count = server.getValue().getPlayers().size();
					for (ProxiedPlayer p : server.getValue().getPlayers()) {
						if (((BungeeTabPlayer)TAB.getInstance().getPlayer(p.getUniqueId())).isVanished()) count--;
					}
					return String.valueOf(count);
				}
			});
		}
		return placeholders;
	}
}