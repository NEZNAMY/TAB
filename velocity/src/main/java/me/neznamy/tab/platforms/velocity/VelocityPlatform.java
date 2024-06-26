package me.neznamy.tab.platforms.velocity;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import me.neznamy.tab.platforms.velocity.features.VelocityRedisSupport;
import me.neznamy.tab.platforms.velocity.hook.VelocityPremiumVanishHook;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    @NotNull
    private final VelocityTAB plugin;

    /** Logger for components */
    private static final ComponentLogger logger = ComponentLogger.logger("TAB");

    /** Plugin message channel */
    @Getter
    private final MinecraftChannelIdentifier MCI = MinecraftChannelIdentifier.from(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);

    /**
     * Constructs new instance with given plugin reference.
     *
     * @param   plugin
     *          Plugin instance
     */
    public VelocityPlatform(VelocityTAB plugin) {
        this.plugin = plugin;
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
        logger.info(AdventureHook.toAdventureComponent(message, true));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        logger.warn(Component.text("").color(NamedTextColor.RED).append(AdventureHook.toAdventureComponent(message, true)));
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
    public Component convertComponent(@NotNull TabComponent component, boolean modern) {
        return AdventureHook.toAdventureComponent(component, modern);
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
