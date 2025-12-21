package me.neznamy.tab.platforms.bungeecord.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import me.neznamy.tab.platforms.bungeecord.BungeeTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * RedisBungee implementation for BungeeCord
 */
public class BungeeRedisSupport extends ProxySupport implements Listener {

    /** Plugin reference for registering listener */
    @NotNull
    private final BungeeTAB plugin;

    /**
     * Constructs new instance with given parameters
     *
     * @param   plugin
     *          Plugin reference
     * @param   channelName
     *          Name of the messaging channel
     */
    public BungeeRedisSupport(@NotNull BungeeTAB plugin, @NotNull String channelName) {
        super(channelName);
        this.plugin = plugin;
    }

    /**
     * Listens to messages coming from other proxies.
     *
     * @param   e
     *          Message event
     */
    @EventHandler
    public void onMessage(@NotNull PubSubMessageEvent e) {
        if (!e.getChannel().equals(getChannelName())) return;
        processMessage(e.getMessage());
    }

    @Override
    public void register() {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
        try {
            RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(getChannelName());
        } catch (NullPointerException e) {
            // java.lang.NullPointerException: Cannot invoke "com.imaginarycode.minecraft.redisbungee.api.PubSubListener.addChannel(String[])"
            // because the return value of "com.imaginarycode.minecraft.redisbungee.api.RedisBungeePlugin.getPubSubListener()" is null
            TAB.getInstance().getErrorManager().redisBungeeRegisterFail(e);
        }
    }

    @Override
    public void unregister() {
        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
        RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(getChannelName());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        try {
            RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(getChannelName(), message);
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().redisBungeeMessageSendFail(e);
        }
    }
}