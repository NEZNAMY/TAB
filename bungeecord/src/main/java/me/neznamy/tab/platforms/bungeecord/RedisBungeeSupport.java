package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import lombok.AllArgsConstructor;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * Redis implementation for BungeeCord
 */
@AllArgsConstructor
public class RedisBungeeSupport extends RedisSupport implements Listener {

    /** Plugin reference for registering listener */
    private final Plugin plugin;

    @Override
    public void load() {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
        RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(TabConstants.REDIS_CHANNEL_NAME);
        super.load();
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