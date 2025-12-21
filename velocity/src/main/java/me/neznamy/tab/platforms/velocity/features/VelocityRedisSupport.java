package me.neznamy.tab.platforms.velocity.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import com.velocitypowered.api.event.Subscribe;
import me.neznamy.tab.platforms.velocity.VelocityTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

/**
 * RedisBungee implementation for Velocity
 */
public class VelocityRedisSupport extends ProxySupport {

    /** Plugin reference for registering listener */
    @NotNull
    private final VelocityTAB plugin;

    /**
     * Constructs new instance with given parameters
     *
     * @param   plugin
     *          Plugin reference
     * @param   channelName
     *          Name of the messaging channel
     */
    public VelocityRedisSupport(@NotNull VelocityTAB plugin, @NotNull String channelName) {
        super(channelName);
        this.plugin = plugin;
    }

    /**
     * Listens to messages coming from other proxies.
     *
     * @param   e
     *          Message event
     */
    @Subscribe
    public void onMessage(PubSubMessageEvent e) {
        if (!e.getChannel().equals(getChannelName())) return;
        processMessage(e.getMessage());
    }

    @Override
    public void register() {
        plugin.getServer().getEventManager().register(plugin, this);
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
        plugin.getServer().getEventManager().unregisterListener(plugin, this);
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