package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.platforms.bungeecord.features.BungeeRedisSupport;
import me.neznamy.tab.platforms.bungeecord.hook.BungeePremiumVanishHook;
import me.neznamy.tab.platforms.bungeecord.injection.BungeePipelineInjector;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1193;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList17;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList18;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.DummyBossBar;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.cache.Cache;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

/**
 * BungeeCord implementation of Platform
 */
public class BungeePlatform extends ProxyPlatform {

    @NotNull
    private final BungeeTAB plugin;

    /** Cache for legacy components for <1.16 players, as we need 2 different components for each tab component */
    private final Cache<TabComponent, BaseComponent> legacyComponentCache = new Cache<>("Bungee legacy component cache", 1000, tab -> createComponent(tab, false));

    /**
     * Constructs new instance with given plugin instance.
     *
     * @param   plugin
     *          Plugin instance
     */
    public BungeePlatform(@NotNull BungeeTAB plugin) {
        this.plugin = plugin;
        if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
            new BungeePremiumVanishHook(this).register();
        }
    }

    @Override
    public void loadPlayers() {
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            TAB.getInstance().addPlayer(new BungeeTabPlayer(this, p));
        }
    }

    @Override
    @Nullable
    public ProxySupport getProxySupport(@NotNull String plugin) {
        if (plugin.equalsIgnoreCase("RedisBungee")) {
            if (ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI") &&
                    RedisBungeeAPI.getRedisBungeeApi() != null) {
                return new BungeeRedisSupport(this.plugin);
            }
        }
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        plugin.getLogger().warning("§c" + message.toLegacyText());
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
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new BungeeTabCommand(getCommand()));
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
    @NotNull
    public BaseComponent convertComponent(@NotNull TabComponent component) {
        return createComponent(component, true);
    }

    /**
     * Transforms the TAB component into a bungee component depending on player's version.
     *
     * @param   component
     *          Component to transform
     * @param   version
     *          Version to transform the component to
     * @return  Bungee component for the specified client version
     */
    @NotNull
    public BaseComponent transformComponent(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        if (version.getMinorVersion() >= 16) {
            return component.convert();
        } else {
            // Convert color to legacy for <1.16 players
            return legacyComponentCache.get(component);
        }
    }

    /**
     * Creates a bungee component using the given TAB component and modern flag for an RGB/legacy color decision.
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          {@code true} if colors should be as RGB, {@code false} if legacy
     * @return  Converted component
     */
    @NotNull
    private BaseComponent createComponent(@NotNull TabComponent component, boolean modern) {
        // Component type
        BaseComponent bComponent;
        if (component instanceof TextComponent) {
            bComponent = new net.md_5.bungee.api.chat.TextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            bComponent = new net.md_5.bungee.api.chat.TranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            bComponent = new net.md_5.bungee.api.chat.KeybindComponent(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) {
            if (modern) {
                bComponent.setColor(ChatColor.of("#" + modifier.getColor().getHexCode()));
            } else {
                bComponent.setColor(ChatColor.of(modifier.getColor().getLegacyColor().name()));
            }
        }
        bComponent.setShadowColor(modifier.getShadowColor() == null ? null : new Color(
                (modifier.getShadowColor() >> 16) & 0xFF,
                (modifier.getShadowColor() >> 8) & 0xFF,
                (modifier.getShadowColor()) & 0xFF,
                (modifier.getShadowColor() >> 24) & 0xFF
        ));
        bComponent.setBold(modifier.getBold());
        bComponent.setItalic(modifier.getItalic());
        bComponent.setObfuscated(modifier.getObfuscated());
        bComponent.setStrikethrough(modifier.getStrikethrough());
        bComponent.setUnderlined(modifier.getUnderlined());
        bComponent.setFont(modifier.getFont());

        // Extra
        for (TabComponent extra : component.getExtra()) {
            bComponent.addExtra(createComponent(extra, modern));
        }

        return bComponent;
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new BungeeScoreboard((BungeeTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        if (player.getVersion().getMinorVersion() >= 9) {
            return new BungeeBossBar((BungeeTabPlayer) player);
        } else {
            return new DummyBossBar();
        }
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            return new BungeeTabList1193((BungeeTabPlayer) player);
        } else if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_8.getNetworkId()) {
            return new BungeeTabList18((BungeeTabPlayer) player);
        } else {
            return new BungeeTabList17((BungeeTabPlayer) player);
        }
    }

    @Override
    public boolean supportsNumberFormat() {
        return true;
    }

    @Override
    public boolean supportsListOrder() {
        return true;
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
    }

    @Override
    @NotNull
    public String getCommand() {
        return "btab";
    }
}
