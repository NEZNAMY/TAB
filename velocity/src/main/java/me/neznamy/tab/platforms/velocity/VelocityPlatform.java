package me.neznamy.tab.platforms.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.scoreboard.ObjectiveEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scoreboard.ScoreboardManager;
import lombok.Getter;
import me.neznamy.tab.platforms.velocity.features.VelocityRedisSupport;
import me.neznamy.tab.platforms.velocity.hook.VelocityPremiumVanishHook;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Velocity implementation of Platform
 */
@Getter
public class VelocityPlatform extends ProxyPlatform {

    @NotNull
    private final VelocityTAB plugin;

    /** Flag tracking presence of Velocity Scoreboard API */
    private boolean scoreboardAPI;

    /** Plugin message channel */
    private final MinecraftChannelIdentifier MCI = MinecraftChannelIdentifier.from(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);

    /** Logger for components */
    private static final ComponentLogger logger = ComponentLogger.logger("TAB");

    /**
     * Constructs new instance with given plugin reference.
     *
     * @param   plugin
     *          Plugin instance
     */
    public VelocityPlatform(@NotNull VelocityTAB plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().isLoaded("velocity-scoreboard-api")) {
            try {
                ScoreboardManager.getInstance();
                scoreboardAPI = true;
                plugin.getServer().getEventManager().register(plugin, ObjectiveEvent.Display.class, e -> {
                    TAB tab = TAB.getInstance();
                    if (tab.isPluginDisabled()) return;
                    tab.getCPUManager().runTask(() -> {
                        TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
                        if (player != null) tab.getFeatureManager().onDisplayObjective(player, e.getNewSlot().ordinal(), e.getObjective().getName());
                    });
                });
                plugin.getServer().getEventManager().register(plugin, ObjectiveEvent.Unregister.class, e -> {
                    TAB tab = TAB.getInstance();
                    if (tab.isPluginDisabled()) return;
                    tab.getCPUManager().runTask(() -> {
                        TabPlayer player = tab.getPlayer(e.getPlayer().getUniqueId());
                        if (player != null) tab.getFeatureManager().onObjective(player, Scoreboard.ObjectiveAction.UNREGISTER, e.getObjective().getName());
                    });
                });
            } catch (IllegalStateException ignored) {
                // Scoreboard API failed to enable due to an error
            }
        } else {
            logInfo(TabComponent.fromColoredText(EnumChatFormat.RED + "As of version 5.0.0, TAB no longer uses TAB-Bridge to encode scoreboard packets on Velocity. " +
                    "Instead, it uses a custom made plugin that adds scoreboard API directly to Velocity, which offers better performance and reliability. " +
                    "You can download the plugin from https://github.com/NEZNAMY/VelocityScoreboardAPI/releases/. " +
                    "Until then, the following features will not work: scoreboard-teams, belowname-objective, playerlist-objective, scoreboard"));
        }
        if (plugin.getServer().getPluginManager().isLoaded("premiumvanish")) {
            PremiumVanishHook.setInstance(new VelocityPremiumVanishHook());
        }
    }

    @Override
    public void loadPlayers() {
        for (Player p : plugin.getServer().getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(this, p));
        }
    }

    @Override
    @Nullable
    public RedisSupport getRedisSupport() {
        if (ReflectionUtils.classExists("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI") &&
                RedisBungeeAPI.getRedisBungeeApi() != null) {
            return new VelocityRedisSupport(plugin);
        }
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        logger.info(message.toAdventure(ProtocolVersion.LATEST_KNOWN_VERSION));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        logger.warn(message.toAdventure(ProtocolVersion.LATEST_KNOWN_VERSION));
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Velocity] " + plugin.getServer().getVersion().getName() + " - " + plugin.getServer().getVersion().getVersion();
    }

    @Override
    public void registerListener() {
        plugin.getServer().getEventManager().register(plugin, new VelocityEventListener());
    }

    @Override
    public void registerCommand() {
        CommandManager cmd = plugin.getServer().getCommandManager();
        cmd.register(cmd.metaBuilder(TabConstants.COMMAND_PROXY).build(), new VelocityTabCommand());
    }

    @Override
    public void startMetrics() {
        plugin.getMetricsFactory().make(plugin, TabConstants.BSTATS_PLUGIN_ID_VELOCITY)
                .addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED,
                () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getDataFolder().toFile();
    }

    @Override
    @NotNull
    public Component convertComponent(@NotNull TabComponent component, boolean modern) {
        return AdventureHook.toAdventureComponent(component, modern);
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        if (scoreboardAPI) {
            return new VelocityScoreboard((VelocityTabPlayer) player);
        } else {
            return new DummyScoreboard(player);
        }
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new AdventureBossBar(player);
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        return new VelocityTabList((VelocityTabPlayer) player);
    }

    @Override
    public boolean supportsNumberFormat() {
        return true;
    }

    @Override
    public boolean supportsListOrder() {
        return false; // TODO when they add API
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return null;
    }

    @Override
    public void registerChannel() {
        plugin.getServer().getChannelRegistrar().register(MCI);
    }
}
