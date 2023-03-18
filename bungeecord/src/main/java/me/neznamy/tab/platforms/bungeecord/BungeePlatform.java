package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * BungeeCord implementation of Platform
 */
@AllArgsConstructor
public class BungeePlatform extends ProxyPlatform {

    private final Plugin plugin;
    @Getter private final BungeePipelineInjector pipelineInjector = new BungeePipelineInjector();

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        if (ProxyServer.getInstance().getPluginManager().getPlugin(TabConstants.Plugin.REDIS_BUNGEE) != null) {
            if (RedisBungeeAPI.getRedisBungeeApi() != null) {
                return new RedisBungeeSupport(plugin);
            } else {
                TAB.getInstance().getErrorManager().criticalError("RedisBungee plugin was detected, but it returned null API instance. Disabling hook.", null);
            }
        }
        return null;
    }

    @Override
    public String getPluginVersion(String plugin) {
        Plugin pl = ProxyServer.getInstance().getPluginManager().getPlugin(plugin);
        return pl == null ? null : pl.getDescription().getVersion();
    }

    @Override
    public void loadPlayers() {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            TAB.getInstance().addPlayer(new BungeeTabPlayer(p));
        }
    }
}