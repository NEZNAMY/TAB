package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.tab.platforms.bungeecord.features.BungeeRedisSupport;
import me.neznamy.tab.platforms.bungeecord.hook.BungeePremiumVanishHook;
import me.neznamy.tab.platforms.bungeecord.hook.BungeeSayanVanishHook;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.*;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.hook.SayanVanishHook;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * BungeeCord implementation of Platform
 */
public class BungeePlatform extends ProxyPlatform {

    @NotNull
    private final BungeeTAB plugin;

    /**
     * Constructs new instance with given plugin instance.
     *
     * @param   plugin
     *          Plugin instance
     */
    public BungeePlatform(@NotNull BungeeTAB plugin) {
        this.plugin = plugin;
        if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
            PremiumVanishHook.setInstance(new BungeePremiumVanishHook(this));
        }
        if (ProxyServer.getInstance().getPluginManager().getPlugin("SayanVanish") != null) {
            SayanVanishHook.setInstance(new BungeeSayanVanishHook());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void loadPlayers() {
        try {
            ProxyConfig config = ProxyServer.getInstance().getConfig();
            if ((boolean) config.getClass().getMethod("isDisableTabListRewrite").invoke(config)) {
                logWarn(new SimpleComponent("Waterfall's \"disable_tab_list_rewrite: true\" option may cause " +
                        "the plugin to not work correctly. Disable it to avoid issues."));
            }
        } catch (Exception e) {
            // Not waterfall
        }
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
    public void logInfo(@NotNull TabComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        plugin.getLogger().warning(EnumChatFormat.RED + message.toLegacyText());
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
        new Metrics(plugin, TabConstants.BSTATS_PLUGIN_ID_BUNGEE).addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED,
                () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder();
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

    @Override
    public BaseComponent convertComponent(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent) return new TextComponent(component.toLegacyText());
        StructuredComponent iComponent = (StructuredComponent) component;
        TextComponent textComponent = new TextComponent(iComponent.getText());
        ChatModifier modifier = iComponent.getModifier();
        if (modifier.getColor() != null) {
            if (modern) {
                textComponent.setColor(ChatColor.of("#" + modifier.getColor().getHexCode()));
            } else {
                textComponent.setColor(ChatColor.of(modifier.getColor().getLegacyColor().name()));
            }
        }

        if (modifier.isBold()) textComponent.setBold(true);
        if (modifier.isItalic()) textComponent.setItalic(true);
        if (modifier.isObfuscated()) textComponent.setObfuscated(true);
        if (modifier.isStrikethrough()) textComponent.setStrikethrough(true);
        if (modifier.isUnderlined()) textComponent.setUnderlined(true);

        textComponent.setFont(modifier.getFont());

        if (!iComponent.getExtra().isEmpty()) {
            List<BaseComponent> list = new ArrayList<>();
            for (StructuredComponent extra : iComponent.getExtra()) {
                list.add(convertComponent(extra, modern));
            }
            textComponent.setExtra(list);
        }

        return textComponent;
    }
}
