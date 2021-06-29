package me.neznamy.tab.platforms.velocity.v1_1_0;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
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
		List<Placeholder> placeholders = new ArrayList<>();
		for (RegisteredServer rServer : server.getAllServers()) {
			placeholders.add(new ServerPlaceholder("%online_" + rServer.getServerInfo().getName() + "%", 1000) {
				public String get() {
					return String.valueOf(rServer.getPlayersConnected().size());
				}
			});
		}
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
		return placeholders;
	}
}