package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.AllArgsConstructor;
import me.neznamy.tab.platforms.bungeecord.features.BungeeRedisSupport;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.stream.Collectors;

/**
 * BungeeCord implementation of Platform
 */
@AllArgsConstructor
public class BungeePlatform extends ProxyPlatform<BaseComponent> {

    @NotNull
    private final BungeeTAB plugin;

    /** Component cache for BungeeCord components */
    @NotNull
    private final ComponentCache<IChatBaseComponent, BaseComponent> bungeeCache =
            new ComponentCache<>(1000, this::toComponent0);

    @Override
    public void loadPlayers() {
        ViaVersionHook.getInstance().printProxyWarn();
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            TAB.getInstance().addPlayer(new BungeeTabPlayer(this, p));
        }
    }

    @Override
    @Nullable
    public RedisSupport getRedisSupport() {
        if (ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI") &&
                RedisBungeeAPI.getRedisBungeeApi() != null) {
            return new BungeeRedisSupport(plugin);
        }
        return null;
    }

    @Override
    public void logInfo(@NotNull IChatBaseComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        plugin.getLogger().warning(EnumChatFormat.RED.getFormat() + message.toLegacyText());
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[BungeeCord] " + plugin.getProxy().getName() + " - " + plugin.getProxy().getVersion();
    }

    @Override
    public void registerListener() {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new BungeeEventListener());
    }

    @Override
    public void registerCommand() {
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new BungeeTabCommand());
    }

    @Override
    public void startMetrics() {
        new Metrics(plugin, 10535).addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED,
                () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public BaseComponent toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion version) {
        return bungeeCache.get(component, version);
    }

    private BaseComponent toComponent0(IChatBaseComponent component, ProtocolVersion version) {
        ChatModifier modifier = component.getModifier();
        TextComponent textComponent = new TextComponent(component.getText());
        if (modifier.getColor() != null) textComponent.setColor(ChatColor.of(
                modifier.getColor().toString(version.getMinorVersion() >= 16)));
        if (modifier.isBold()) textComponent.setBold(true);
        if (modifier.isItalic()) textComponent.setItalic(true);
        if (modifier.isObfuscated()) textComponent.setObfuscated(true);
        if (modifier.isStrikethrough()) textComponent.setStrikethrough(true);
        if (modifier.isUnderlined()) textComponent.setUnderlined(true);
        if (modifier.getFont() != null) textComponent.setFont(modifier.getFont());
        if (!component.getExtra().isEmpty()) textComponent.setExtra(
                component.getExtra().stream().map(c -> toComponent0(c, version)).collect(Collectors.toList()));
        return textComponent;
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return new BungeePipelineInjector();
    }

    @Override
    public void registerChannel() {
        ProxyServer.getInstance().registerChannel(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);
    }
}
