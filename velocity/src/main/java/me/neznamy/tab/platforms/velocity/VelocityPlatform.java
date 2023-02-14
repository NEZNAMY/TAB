package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    @Override
    public String getPluginVersion(String plugin) {
        return Main.getInstance().getServer().getPluginManager().getPlugin(plugin.toLowerCase(Locale.US))
                .flatMap(pluginContainer -> pluginContainer.getDescription().getVersion()).orElse(null);
    }

    @Override
    public void loadPlayers() {
        for (Player p : Main.getInstance().getServer().getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
        }
    }

    @Override
    public @Nullable PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        return null;
    }

    @Override
    public PacketBuilder createPacketBuilder() {
        return new PacketBuilder();
    }
}