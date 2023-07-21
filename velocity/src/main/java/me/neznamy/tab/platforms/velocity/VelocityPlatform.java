package me.neznamy.tab.platforms.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.velocity.features.VelocityRedisSupport;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Velocity implementation of Platform
 */
@RequiredArgsConstructor
public class VelocityPlatform extends ProxyPlatform {

    @NotNull private final VelocityTAB plugin;

    @Override
    public void loadPlayers() {
        for (Player p : plugin.getServer().getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
        }
    }

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        if (ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI") &&
                RedisBungeeAPI.getRedisBungeeApi() != null) {
            return new VelocityRedisSupport(plugin);
        }
        return null;
    }

    @Override
    public void logInfo(@NotNull IChatBaseComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        plugin.getLogger().warn(EnumChatFormat.RED.getFormat() + message.toLegacyText());
    }

    @Override
    public String getServerVersionInfo() {
        return "[Velocity] " + plugin.getServer().getVersion().getName() + " - " + plugin.getServer().getVersion().getVersion();
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return null;
    }
}
