package me.neznamy.tab.platforms.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.velocity.features.VelocityRedisSupport;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@RequiredArgsConstructor
public class VelocityPlatform extends ProxyPlatform {

    private final VelocityTAB plugin;
    private final ProxyServer server;

    @Override
    public String getPluginVersion(String plugin) {
        return server.getPluginManager().getPlugin(plugin.toLowerCase(Locale.US))
                .flatMap(pluginContainer -> pluginContainer.getDescription().getVersion()).orElse(null);
    }

    @Override
    public void loadPlayers() {
        for (Player p : server.getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
        }
    }

    @Override
    public @Nullable PipelineInjector getPipelineInjector() { return null; }

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        if (getPluginVersion(TabConstants.Plugin.REDIS_BUNGEE) != null && RedisBungeeAPI.getRedisBungeeApi() != null) {
            return new VelocityRedisSupport(plugin, server);
        }
        return null;
    }
}
