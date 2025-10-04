package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.tab.platforms.bungeecord.features.BungeeRedisSupport;
import me.neznamy.tab.platforms.bungeecord.hook.BungeePremiumVanishHook;
import me.neznamy.tab.platforms.bungeecord.injection.BungeePipelineInjector;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1193;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList17;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList18;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.ObjectInfo;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.DummyBossBar;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.PerformanceUtil;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.objects.PlayerObject;
import net.md_5.bungee.api.chat.objects.SpriteObject;
import net.md_5.bungee.api.chat.player.Profile;
import net.md_5.bungee.api.chat.player.Property;
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
    public void registerPlaceholders() {
        super.registerPlaceholders();
        for (String serverName : ProxyServer.getInstance().getConfig().getServers().keySet()) {
            Server server = Server.byName(serverName);
            TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder("%online_" + serverName + "%", 1000, () -> {
                int count = 0;
                for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                    if (player.server == server && !player.isVanished()) count++;
                }
                return PerformanceUtil.toString(count);
            });
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
    public BaseComponent @NotNull [] convertComponent(@NotNull TabComponent component) {
        return new BaseComponent[] {
                createComponent(component, ProtocolVersion.V1_21_9),
                createComponent(component, ProtocolVersion.V1_16),
                createComponent(component, ProtocolVersion.V1_8),
        };
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
        return pickCorrectComponent(component.convert(), version);
    }

    /**
     * Picks correct component from the given array based on client version.
     *
     * @param   components
     *          Array of 3 components for 1.21.9+, 1.16-1.21.8 and below 1.16
     * @param   version
     *          Client version
     * @return  Correct component for the specified client version
     */
    @NotNull
    public BaseComponent pickCorrectComponent(@NotNull BaseComponent[] components, @NotNull ProtocolVersion version) {
        if (version.getNetworkId() >= ProtocolVersion.V1_21_9.getNetworkId()) {
            return components[0];
        } else if (version.getNetworkId() >= ProtocolVersion.V1_16.getNetworkId()) {
            return components[1];
        } else {
            return components[2];
        }
    }

    /**
     * Creates a bungee component using the given TAB component and target client version.
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Client version to create the component for
     * @return  Converted component
     */
    @NotNull
    private BaseComponent createComponent(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        // Component type
        BaseComponent bComponent;
        if (component instanceof TabTextComponent) {
            bComponent = new TextComponent(((TabTextComponent) component).getText());
        } else if (component instanceof TabTranslatableComponent) {
            bComponent = new TranslatableComponent(((TabTranslatableComponent) component).getKey());
        } else if (component instanceof TabKeybindComponent) {
            bComponent = new KeybindComponent(((TabKeybindComponent) component).getKeybind());
        } else if (component instanceof TabObjectComponent) {
            if (version.getNetworkId() >= ProtocolVersion.V1_21_9.getNetworkId()) {
                ObjectInfo info = ((TabObjectComponent) component).getContents();
                if (info instanceof TabAtlasSprite) {
                    bComponent = new ObjectComponent(new SpriteObject(((TabAtlasSprite) info).getAtlas(), ((TabAtlasSprite) info).getSprite()));
                } else if (info instanceof TabPlayerSprite) {
                    bComponent = new ObjectComponent(new PlayerObject(new Profile(
                            ((TabPlayerSprite) info).getName(),
                            ((TabPlayerSprite) info).getId(),
                            ((TabPlayerSprite) info).getSkin() == null ? new Property[0] : new Property[] {
                                    new Property("textures", ((TabPlayerSprite) info).getSkin().getValue(), ((TabPlayerSprite) info).getSkin().getSignature())
                            }
                    ), ((TabPlayerSprite) info).isShowHat()));
                } else {
                    throw new IllegalStateException("Unexpected object component type: " + info.getClass().getName());
                }
            } else {
                bComponent = new TextComponent(component.toLegacyText());
            }
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        TabStyle modifier = component.getModifier();
        if (modifier.getColor() != null) {
            if (version.getMinorVersion() >= 16) {
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
            bComponent.addExtra(createComponent(extra, version));
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
        if (player.getVersionId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            return new BungeeTabList1193((BungeeTabPlayer) player);
        } else if (player.getVersionId() >= ProtocolVersion.V1_8.getNetworkId()) {
            return new BungeeTabList18((BungeeTabPlayer) player);
        } else {
            return new BungeeTabList17((BungeeTabPlayer) player);
        }
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
