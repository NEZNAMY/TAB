package me.neznamy.tab.platforms.velocity.features;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import com.velocitypowered.api.event.Subscribe;
import lombok.AllArgsConstructor;
import me.neznamy.tab.platforms.velocity.VelocityTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

/**
 * RedisBungee implementation for Velocity
 */
@AllArgsConstructor
public class VelocityRedisSupport extends ProxySupport {

    /** Plugin reference for registering listener */
    @NotNull
    private final VelocityTAB plugin;

    /**
     * Listens to messages coming from other proxies.
     *
     * @param   e
     *          Message event
     */
    @Subscribe
    public void onMessage(PubSubMessageEvent e) {
        if (!e.getChannel().equals(TabConstants.PROXY_CHANNEL_NAME)) return;
        processMessage(e.getMessage());
    }

    @Override
    public void register() {
        plugin.getServer().getEventManager().register(plugin, this);
        try {
            RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(TabConstants.PROXY_CHANNEL_NAME);
        } catch (NullPointerException e) {
            // java.lang.NullPointerException: Cannot invoke "com.imaginarycode.minecraft.redisbungee.api.PubSubListener.addChannel(String[])"
            // because the return value of "com.imaginarycode.minecraft.redisbungee.api.RedisBungeePlugin.getPubSubListener()" is null
            TAB.getInstance().getErrorManager().redisBungeeRegisterFail(e);
        }
    }

    @Override
    public void unregister() {
        plugin.getServer().getEventManager().unregisterListener(plugin, this);
        RedisBungeeAPI.getRedisBungeeApi().unregisterPubSubChannels(TabConstants.PROXY_CHANNEL_NAME);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        try {
            RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(TabConstants.PROXY_CHANNEL_NAME, message);
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().redisBungeeMessageSendFail(e);
        }
    }
}