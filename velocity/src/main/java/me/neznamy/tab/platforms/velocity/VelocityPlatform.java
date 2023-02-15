package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

import java.util.Locale;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    @Getter private final PipelineInjector pipelineInjector = null;
    @Getter private final RedisSupport redisSupport = null;
    @Getter private final PacketBuilder packetBuilder = new PacketBuilder();

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
}