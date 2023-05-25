package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BungeeCord implementation of Platform
 */
@AllArgsConstructor
public class BungeePlatform extends ProxyPlatform {

    @NotNull private final BungeeTAB plugin;

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        if (ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI") &&
                RedisBungeeAPI.getRedisBungeeApi() != null) {
            return new RedisBungeeSupport(plugin);
        }
        return null;
    }

    @Override
    public void sendConsoleMessage(@NotNull IChatBaseComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public String getServerVersionInfo() {
        return "[BungeeCord] " + plugin.getProxy().getName() + " - " + plugin.getProxy().getVersion();
    }

    @Override
    public void loadPlayers() {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            TAB.getInstance().addPlayer(new BungeeTabPlayer(p));
        }
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return new BungeePipelineInjector();
    }
}