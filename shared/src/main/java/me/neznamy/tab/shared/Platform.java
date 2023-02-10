package me.neznamy.tab.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.api.FeatureManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.alignedplayerlist.AlignedPlayerList;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls
 */
@AllArgsConstructor
public abstract class Platform {

    /** Platform's packet builder implementation */
    @Getter private final PacketBuilder packetBuilder;

    public void sendConsoleMessage(String message, boolean translateColors) {
        Object logger = TAB.getInstance().getLogger();
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        } else if (logger instanceof Logger) {
            ((Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        }
    }

    /**
     * Detects permission plugin and returns it's representing object
     *
     * @return  the interface representing the permission hook
     */
    public abstract PermissionPlugin detectPermissionPlugin();

    /**
     * Loads features
     */
    public void loadFeatures() {
        Configs configuration = TAB.getInstance().getConfiguration();
        FeatureManager featureManager = TAB.getInstance().getFeatureManager();

        if (configuration.isPipelineInjection() && TAB.getInstance().getServerVersion().getMinorVersion() >= 8) {
            PipelineInjector inj = getPipelineInjector();
            if (inj != null) featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, inj);
        }

        if (configuration.getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting());
            if (configuration.getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)
                    && TAB.getInstance().getServerVersion().getMinorVersion() >= 8) {
                featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, getUnlimitedNametags());
            } else {
                featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
            }
        }

        if (configuration.getConfig().getBoolean("bossbar.enabled", false)) {
            if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
                featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
            } else {
                featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, getLegacyBossBar());
            }
        }

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9 &&
                configuration.getConfig().getBoolean("fix-pet-names.enabled", false)) {
            TabFeature petFix = getPetFix();
            if (petFix != null) featureManager.registerFeature(TabConstants.Feature.PET_FIX, getPetFix());
        }

        if (configuration.getConfig().getBoolean("header-footer.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter());

        if (configuration.isRemoveGhostPlayers())
            featureManager.registerFeature(TabConstants.Feature.GHOST_PLAYER_FIX, new GhostPlayerFix());

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8 && configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true)) {
            if (configuration.getConfig().getBoolean("tablist-name-formatting.align-tabsuffix-on-the-right", false)) {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new AlignedPlayerList());
            } else {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new PlayerList());
            }
        }

        if (configuration.getConfig().getBoolean("ping-spoof.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.PING_SPOOF, new PingSpoof());

        if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, new YellowNumber());

        if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());

        if (configuration.getConfig().getBoolean("belowname-objective.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, new BelowName());

        if (configuration.getConfig().getBoolean("scoreboard.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl());

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8 && configuration.getLayout().getBoolean("enabled", false)) {
            if (TAB.getInstance().getTeamManager() == null) {
                //sorting is disabled, but layout needs team names
                featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting());
            }
            featureManager.registerFeature(TabConstants.Feature.LAYOUT, new LayoutManager());
        }

        if (configuration.getConfig().getBoolean("placeholders.register-tab-expansion", false)) {
            TAB.getInstance().getPlaceholderManager().setTabExpansion(getTabExpansion());
        }

        if (configuration.getConfig().getBoolean("global-playerlist.enabled", false)) {
            TabFeature global = getGlobalPlayerlist();
            if (global != null) featureManager.registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, getGlobalPlayerlist());
        }

        if (configuration.getConfig().getBoolean("per-world-playerlist.enabled", false)) {
            TabFeature pwp = getPerWorldPlayerlist();
            if (pwp != null) featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, pwp);
        }

        RedisSupport redis = getRedisSupport();
        if (redis != null) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, redis);

        featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility());
    }

    public BossBarManagerImpl getLegacyBossBar() {
        return new BossBarManagerImpl();
    }

    /**
     * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder}
     * to handle this unknown placeholder (typically a PAPI placeholder)
     *
     * @param   identifier
     *          placeholder's identifier
     */
    public abstract void registerUnknownPlaceholder(String identifier);

    /**
     * Performs platform-specific plugin manager call and returns the result.
     * If plugin is not installed, returns {@code null}.
     *
     * @param   plugin
     *          Plugin to check version of
     * @return  Version string if plugin is installed, {@code null} if not
     */
    public abstract String getPluginVersion(String plugin);

    /**
     * Creates instance for all online players and adds them to the plugin
     */
    public abstract void loadPlayers();

    /**
     * Registers all placeholders, including universal and platform-specific ones
     */
    public abstract void registerPlaceholders();

    public abstract @Nullable PipelineInjector getPipelineInjector();

    public abstract NameTag getUnlimitedNametags();

    public abstract TabExpansion getTabExpansion();

    public abstract @Nullable TabFeature getPetFix();

    public abstract @Nullable TabFeature getGlobalPlayerlist();

    public abstract @Nullable RedisSupport getRedisSupport();

    public abstract @Nullable TabFeature getPerWorldPlayerlist();

}