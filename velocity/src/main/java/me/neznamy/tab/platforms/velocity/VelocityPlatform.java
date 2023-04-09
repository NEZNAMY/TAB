package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@RequiredArgsConstructor
public class VelocityPlatform extends ProxyPlatform {

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
    public @Nullable RedisSupport getRedisSupport() { return null; }
}
