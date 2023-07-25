package me.neznamy.tab.shared;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.alignedplayerlist.AlignedPlayerList;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Feature registration which offers calls to all features
 * and measures how long it took them to process
 */
public class FeatureManager {

    /** Map of all registered feature where key is feature's identifier */
    private final Map<String, TabFeature> features = new LinkedHashMap<>();

    /** All registered features in an array to avoid memory allocations on iteration */
    @NotNull
    @Getter private TabFeature[] values = new TabFeature[0];

    /**
     * Calls load() on all features.
     * This function is called on plugin startup.
     */
    public void load() {
        for (TabFeature f : values) {
            if (!(f instanceof Loadable)) continue;
            long time = System.currentTimeMillis();
            ((Loadable) f).load();
            TAB.getInstance().debug("Feature " + f.getClass().getSimpleName() + " processed load in " + (System.currentTimeMillis()-time) + "ms");
        }
        if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
            MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) users.load(p);
        }
    }

    /**
     * Calls unload() on all features.
     * This function is called on plugin disable.
     */
    public void unload() {
        for (TabFeature f : values) {
            if (!(f instanceof UnLoadable)) continue;
            long time = System.currentTimeMillis();
            ((UnLoadable) f).unload();
            TAB.getInstance().debug("Feature " + f.getClass().getSimpleName() + " processed unload in " + (System.currentTimeMillis()-time) + "ms");
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().unregister();
        if (TAB.getInstance().getPlatform() instanceof ProxyPlatform) {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                ((ProxyTabPlayer)player).sendPluginMessage("Unload");
            }
        }
    }

    /**
     * Calls refresh(TabPlayer, boolean) on all features
     * 
     * @param   refreshed
     *          player to refresh
     * @param   force
     *          whether refresh should be forced or not
     */
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        for (TabFeature f : values) {
            if (f instanceof Refreshable) ((Refreshable)f).refresh(refreshed, force);
        }
    }

    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof GameModeListener)) continue;
            long time = System.nanoTime();
            ((GameModeListener) f).onGameModeChange(player);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
    }

    public IChatBaseComponent onDisplayNameChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id) {
        IChatBaseComponent newDisplayName = null;
        for (TabFeature f : values) {
            if (!(f instanceof DisplayNameListener)) continue;
            long time = System.nanoTime();
            IChatBaseComponent value = ((DisplayNameListener) f).onDisplayNameChange(packetReceiver, id);
            if (value != null) newDisplayName = value;
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
        return newDisplayName;
    }

    public void onQuit(@Nullable TabPlayer disconnectedPlayer) {
        if (disconnectedPlayer == null) return;
        long millis = System.currentTimeMillis();
        for (TabFeature f : values) {
            if (!(f instanceof QuitListener)) continue;
            long time = System.nanoTime();
            ((QuitListener)f).onQuit(disconnectedPlayer);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PLAYER_QUIT, System.nanoTime()-time);
        }
        TAB.getInstance().removePlayer(disconnectedPlayer);
        TAB.getInstance().debug("Player quit of " + disconnectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
    }

    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (!connectedPlayer.isOnline()) return;
        long millis = System.currentTimeMillis();
        TAB.getInstance().addPlayer(connectedPlayer);
        for (TabFeature f : values) {
            if (!(f instanceof JoinListener)) continue;
            long time = System.nanoTime();
            ((JoinListener)f).onJoin(connectedPlayer);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
            TAB.getInstance().debug("Feature " + f.getClass().getSimpleName() + " processed player join in " + (System.nanoTime()-time)/1000000 + "ms");

        }
        connectedPlayer.markAsLoaded(true);
        TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
        if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
            MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
            users.load(connectedPlayer);
        }
    }

    public void onWorldChange(@NotNull UUID playerUUID, @NotNull String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.getWorld();
        changed.setWorld(to);
        for (TabFeature f : values) {
            if (!(f instanceof WorldSwitchListener)) continue;
            long time = System.nanoTime();
            ((WorldSwitchListener) f).onWorldChange(changed, from, to);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.WORLD)).updateValue(changed, to);
    }

    public void onServerChange(@NotNull UUID playerUUID, @NotNull String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.getServer();
        changed.setServer(to);
        changed.getScoreboard().clearRegisteredObjectives();
        ((ProxyTabPlayer)changed).sendJoinPluginMessage();
        for (TabFeature f : values) {
            if (!(f instanceof ServerSwitchListener)) continue;
            long time = System.nanoTime();
            ((ServerSwitchListener) f).onServerChange(changed, from, to);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.SERVER_SWITCH, System.nanoTime()-time);
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.SERVER)).updateValue(changed, to);
    }

    public boolean onCommand(@Nullable TabPlayer sender, @NotNull String command) {
        if (sender == null) return false;
        boolean cancel = false;
        for (TabFeature f : values) {
            if (!(f instanceof CommandListener)) continue;
            long time = System.nanoTime();
            if (((CommandListener)f).onCommand(sender, command)) cancel = true;
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.COMMAND_PREPROCESS, System.nanoTime()-time);
        }
        return cancel;
    }

    /**
     * Calls onPacketSend(TabPlayer, Object) on all features
     * 
     * @param   receiver
     *          packet receiver
     * @param   packet
     *          OUT packet coming from the server
     */
    public void onPacketSend(@NotNull TabPlayer receiver, @NotNull Object packet) {
        for (TabFeature f : values) {
            if (!(f instanceof PacketSendListener)) continue;
            long time = System.nanoTime();
            ((PacketSendListener)f).onPacketSend(receiver, packet);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.RAW_PACKET_OUT, System.nanoTime()-time);
        }
    }

    /**
     * Calls onDisplayObjective(...) on all features
     *
     * @param   packetReceiver
     *          player who received the packet
     * @param   slot
     *          Objective slot
     * @param   objective
     *          Objective name
     */
    public void onDisplayObjective(@NotNull TabPlayer packetReceiver, int slot, @NotNull String objective) {
        for (TabFeature f : values) {
            if (!(f instanceof DisplayObjectiveListener)) continue;
            long time = System.nanoTime();
            ((DisplayObjectiveListener)f).onDisplayObjective(packetReceiver, slot, objective);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
        }
    }

    /**
     * Calls onObjective(TabPlayer, PacketPlayOutScoreboardObjective) on all features
     *
     * @param   packetReceiver
     *          player who received the packet
     * @param   action
     *          Packet action
     * @param   objective
     *          Objective name
     */
    public void onObjective(@NotNull TabPlayer packetReceiver, int action, @NotNull String objective) {
        for (TabFeature f : values) {
            if (!(f instanceof ObjectiveListener)) continue;
            long time = System.nanoTime();
            ((ObjectiveListener)f).onObjective(packetReceiver, action, objective);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
        }
    }

    public void onVanishStatusChange(@NotNull TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof VanishListener)) continue;
            long time = System.nanoTime();
            ((VanishListener)f).onVanishStatusChange(player);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.VANISH_CHANGE, System.nanoTime()-time);
        }
    }

    public void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        for (TabFeature f : values) {
            if (!(f instanceof EntryAddListener)) continue;
            long time = System.nanoTime();
            ((EntryAddListener)f).onEntryAdd(packetReceiver, id, name);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
    }

    public void registerFeature(@NotNull String featureName, @NotNull TabFeature featureHandler) {
        features.put(featureName, featureHandler);
        values = features.values().toArray(new TabFeature[0]);
        if (featureHandler instanceof VanishListener) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VANISHED));
        }
        if (featureHandler instanceof GameModeListener) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.GAMEMODE));
        }
    }

    public void unregisterFeature(@NotNull String featureName) {
        features.remove(featureName);
        values = features.values().toArray(new TabFeature[0]);
    }

    public boolean isFeatureEnabled(@NotNull String name) {
        return features.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends TabFeature> T getFeature(@NotNull String name) {
        return (T) features.get(name);
    }

    /**
     * Loads features from config
     */
    public void loadFeaturesFromConfig() {
        Configs configuration = TAB.getInstance().getConfiguration();
        FeatureManager featureManager = TAB.getInstance().getFeatureManager();
        int minorVersion = TAB.getInstance().getServerVersion().getMinorVersion();

        if (configuration.getConfig().getBoolean("bossbar.enabled", false)) {
            featureManager.registerFeature(TabConstants.Feature.BOSS_BAR,
                    minorVersion >= 9 ? new BossBarManagerImpl() : TAB.getInstance().getPlatform().getLegacyBossBar());
        }

        if (configuration.getConfig().getBoolean("header-footer.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter());

        if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());

        if (configuration.isPipelineInjection()) {
            PipelineInjector inj = TAB.getInstance().getPlatform().createPipelineInjector();
            if (inj != null) featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, inj);
        }

        if (configuration.getConfig().getBoolean("scoreboard.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl());

        if (configuration.getConfig().getBoolean("per-world-playerlist.enabled", false)) {
            TabFeature pwp = TAB.getInstance().getPlatform().getPerWorldPlayerList();
            if (pwp != null) featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, pwp);
            if (configuration.getConfig().getBoolean("layout.enabled", false)) {
                TAB.getInstance().getMisconfigurationHelper().bothPerWorldPlayerListAndLayoutEnabled();
            }
        }

        if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, new YellowNumber());

        if (configuration.getConfig().getBoolean("belowname-objective.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, new BelowName());

        // No requirements, but due to chicken vs egg, the feature uses NameTags, Layout and RedisBungee
        if (configuration.getConfig().getBoolean("scoreboard-teams.enabled", true) ||
                configuration.getConfig().getBoolean("layout.enabled", false)) {
            featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting());
        }

        // Must be loaded after: Sorting
        if (configuration.getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            if (configuration.getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false) && minorVersion >= 8) {
                NameTag unlimited = TAB.getInstance().getPlatform().getUnlimitedNameTags();
                if (unlimited instanceof NameTagX) {
                    featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, unlimited);
                } else {
                    featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, unlimited);
                }
            } else {
                featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
            }
        }

        // Must be loaded after: Sorting
        if (minorVersion >= 8 && configuration.getConfig().getBoolean("layout.enabled", false)) {
            featureManager.registerFeature(TabConstants.Feature.LAYOUT, new LayoutManagerImpl());
            if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true)) {
                TAB.getInstance().getMisconfigurationHelper().layoutBreaksYellowNumber();
            }
            if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false)) {
                TAB.getInstance().getMisconfigurationHelper().layoutIncludesPreventSpectatorEffect();
            }
        }

        // Must be loaded after: Layout
        if (minorVersion >= 8 && configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true)) {
            if (configuration.getConfig().getBoolean("tablist-name-formatting.align-tabsuffix-on-the-right", false)) {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new AlignedPlayerList());
            } else {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new PlayerList());
            }
        }

        // Must be loaded after: PlayerList
        if (configuration.getConfig().getBoolean("global-playerlist.enabled", false) &&
                TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY) {
            featureManager.registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, new GlobalPlayerList());
            if (configuration.getConfig().getBoolean("layout.enabled", false)) {
                TAB.getInstance().getMisconfigurationHelper().bothGlobalPlayerListAndLayoutEnabled();
            }
        }

        // Must be loaded after: Global PlayerList, PlayerList, NameTags, YellowNumber, BelowName
        RedisSupport redis = TAB.getInstance().getPlatform().getRedisSupport();
        if (redis != null) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, redis);

        featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility());
    }
}