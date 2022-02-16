package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.Map.Entry;

/**
 * Redis implementation for BungeeCord
 */
public class RedisBungeeSupport extends RedisSupport implements Listener {

	/**
	 * Constructs new instance, registers listeners and overrides placeholders
	 *
	 * @param	plugin
	 * 			plugin to register event listener for
	 */
	public RedisBungeeSupport(Plugin plugin) {
		ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
		RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(TabConstants.REDIS_CHANNEL_NAME);
		for (Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
			TAB.getInstance().getPlaceholderManager().registerServerPlaceholder("%online_" + server.getKey() + "%", 1000, () ->
					Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(all -> all.getServer().equals(server.getValue().getName()) && !all.isVanished()).count() +
							redisPlayers.values().stream().filter(all -> all.getServer().equals(server.getValue().getName()) && !all.isVanished()).count());
		}
	}

	@EventHandler
	public void onMessage(PubSubMessageEvent e) {
		if (!e.getChannel().equals(TabConstants.REDIS_CHANNEL_NAME)) return;
		processMessage(e.getMessage());
	}

	@Override
	public void unregister() {
		ProxyServer.getInstance().getPluginManager().unregisterListener(this);
		RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(TabConstants.REDIS_CHANNEL_NAME);
	}

	@Override
	public void sendMessage(String message) {
		RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(TabConstants.REDIS_CHANNEL_NAME, message);
	}
}