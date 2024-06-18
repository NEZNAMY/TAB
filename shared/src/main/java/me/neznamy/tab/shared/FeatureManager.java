package me.neznamy.tab.shared;

import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.Unload;
import me.neznamy.tab.shared.task.FeatureTasks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Feature registration which offers calls to all features
 * and measures how long it took them to process
 */
public class FeatureManager {

    /** Map of all registered feature where key is feature's identifier */
    private final Map<String, TabFeature> features = new LinkedHashMap<>();

    /** All registered features in an array to avoid memory allocations on iteration */
    @NotNull
    private TabFeature[] values = new TabFeature[0];

    /** Flag tracking presence of a feature listening to latency change for faster check with better performance */
    private boolean hasLatencyChangeListener;

    /** Flag tracking presence of a feature listening to command preprocess for faster check with better performance */
    private boolean hasCommandListener;

    /** Commands features listen to */
    private final List<String> listeningCommands = new ArrayList<>();

    /**
     * Calls load() on all features.
     * This function is called on plugin startup.
     */
    public void load() {
        for (TabFeature f : values) {
            if (!(f instanceof Loadable)) continue;
            FeatureTasks.Load task = new FeatureTasks.Load((Loadable) f);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
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
            FeatureTasks.Unload task = new FeatureTasks.Unload((UnLoadable) f);
            if (f instanceof CustomThreaded) {
                ThreadExecutor thread = ((CustomThreaded) f).getCustomThread();
                thread.execute(() -> {
                    task.run();
                    thread.shutdown();
                });
            } else {
                task.run();
            }
        }
        for (TabFeature f : values) {
            f.deactivate();
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().unregisterExpansion();
        if (TAB.getInstance().getPlatform() instanceof ProxyPlatform) {
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                ((ProxyTabPlayer)player).sendPluginMessage(new Unload());
            }
        }
    }

    /**
     * Calls onGroupChange to all features implementing {@link GroupListener}.
     *
     * @param   player
     *          player with new group
     */
    public void onGroupChange(@NotNull TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof GroupListener)) continue;
            FeatureTasks.GroupChange task = new FeatureTasks.GroupChange((GroupListener) f, player);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Forwards gamemode change to all enabled features.
     *
     * @param   player
     *          Player whose gamemode has changed.
     */
    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof GameModeListener)) continue;
            FeatureTasks.GameModeChange task = new FeatureTasks.GameModeChange((GameModeListener) f, player);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Forwards player quit to all features
     *
     * @param   disconnectedPlayer
     *          Player who left
     */
    public void onQuit(@Nullable TabPlayer disconnectedPlayer) {
        if (disconnectedPlayer == null) return;
        disconnectedPlayer.markOffline();
        long millis = System.currentTimeMillis();
        for (TabFeature f : values) {
            if (!(f instanceof QuitListener)) continue;
            FeatureTasks.Quit task = new FeatureTasks.Quit((QuitListener) f, disconnectedPlayer);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        TAB.getInstance().removePlayer(disconnectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList<?, ?>)all.getTabList()).removeExpectedDisplayName(disconnectedPlayer.getTablistId());
        }
        TAB.getInstance().debug("Player quit of " + disconnectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
    }

    /**
     * Handles player join and forwards it to all features.
     *
     * @param   connectedPlayer
     *          Player who joined
     */
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        long millis = System.currentTimeMillis();
        TAB.getInstance().addPlayer(connectedPlayer);
        for (TabFeature f : values) {
            if (!(f instanceof JoinListener)) continue;
            FeatureTasks.Join task = new FeatureTasks.Join((JoinListener) f, connectedPlayer);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        connectedPlayer.markAsLoaded(true);
        TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
        if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
            MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
            users.load(connectedPlayer);
        }
    }

    /**
     * Processed world change and forwards it to all features.
     *
     * @param   playerUUID
     *          UUID of player who switched worlds
     * @param   to
     *          New world name
     */
    public void onWorldChange(@NotNull UUID playerUUID, @NotNull String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.world;
        changed.world = to;
        for (TabFeature f : values) {
            if (!(f instanceof WorldSwitchListener)) continue;
            FeatureTasks.WorldSwitch change = new FeatureTasks.WorldSwitch((WorldSwitchListener) f, changed, from, to);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(change);
            } else {
                change.run();
            }
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.WORLD)).updateValue(changed, to);
    }

    /**
     * Processed server switch and forwards it to all features.
     *
     * @param   playerUUID
     *          UUID of player who switched server
     * @param   to
     *          New server name
     */
    public void onServerChange(@NotNull UUID playerUUID, @NotNull String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.server;
        changed.server = to;
        ((ProxyTabPlayer)changed).sendJoinPluginMessage();
        for (TabFeature f : values) {
            if (!(f instanceof ServerSwitchListener)) continue;
            FeatureTasks.ServerSwitch change = new FeatureTasks.ServerSwitch((ServerSwitchListener) f, changed, from, to);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(change);
            } else {
                change.run();
            }
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.SERVER)).updateValue(changed, to);
    }

    /**
     * Forwards command event to all features. Returns {@code true} if the event
     * should be cancelled, {@code false} if not.
     *
     * @param   sender
     *          Command sender
     * @param   command
     *          Executed command
     * @return  {@code true} if event should be cancelled, {@code false} if not.
     */
    public boolean onCommand(@Nullable TabPlayer sender, @NotNull String command) {
        if (!hasCommandListener || sender == null) return false;
        if (!listeningCommands.contains(command)) return false;
        boolean cancel = false;
        for (TabFeature f : values) {
            if (!(f instanceof CommandListener)) continue;
            long time = System.nanoTime();
            if (((CommandListener)f).onCommand(sender, command)) cancel = true;
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), TabConstants.CpuUsageCategory.COMMAND_PREPROCESS, System.nanoTime()-time);
        }
        return cancel;
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
            FeatureTasks.OnDisplayObjective task = new FeatureTasks.OnDisplayObjective((DisplayObjectiveListener) f, packetReceiver, slot, objective);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
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
            FeatureTasks.OnObjective task = new FeatureTasks.OnObjective((ObjectiveListener) f, packetReceiver, action, objective);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Forwards vanish status change to all features.
     *
     * @param   player
     *          Player whose vanish status changed
     */
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof VanishListener)) continue;
            FeatureTasks.VanishStatus task = new FeatureTasks.VanishStatus((VanishListener) f, player);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Forwards entry add to all features.
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of the entry
     * @param   name
     *          Player name of the entry
     */
    public void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        for (TabFeature f : values) {
            if (!(f instanceof EntryAddListener)) continue;
            long time = System.nanoTime();
            ((EntryAddListener)f).onEntryAdd(packetReceiver, id, name);
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), TabConstants.CpuUsageCategory.NICK_PLUGIN_COMPATIBILITY, System.nanoTime() - time);
        }
    }

    /**
     * Forwards latency change to all features and returns new latency to use.
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of player whose ping changed
     * @param   latency
     *          Latency in the packet
     * @return  New latency to use
     */
    public int onLatencyChange(TabPlayer packetReceiver, UUID id, int latency) {
        if (!hasLatencyChangeListener) return latency;
        int newLatency = latency;
        for (TabFeature f : values) {
            if (!(f instanceof LatencyListener)) continue;
            long time = System.nanoTime();
            newLatency = ((LatencyListener)f).onLatencyChange(packetReceiver, id, newLatency);
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), TabConstants.CpuUsageCategory.PING_CHANGE, System.nanoTime() - time);
        }
        return newLatency;
    }

    /**
     * Forwards tablist clear to all enabled features.
     *
     * @param   packetReceiver
     *          Player whose tablist got cleared
     */
    public void onTabListClear(TabPlayer packetReceiver) {
        for (TabFeature f : values) {
            if (!(f instanceof TabListClearListener)) continue;
            FeatureTasks.TabListClear task = new FeatureTasks.TabListClear((TabListClearListener) f, packetReceiver);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Called when another proxy is reloaded to request all data again.
     */
    public void onRedisLoadRequest() {
        for (TabFeature f : values) {
            if (!(f instanceof RedisFeature)) continue;
            FeatureTasks.RedisReload task = new FeatureTasks.RedisReload((RedisFeature) f);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Handles redis player join and forwards it to all features.
     *
     * @param   connectedPlayer
     *          Player who joined
     */
    public void onJoin(@NotNull RedisPlayer connectedPlayer) {
        for (TabFeature f : values) {
            if (!(f instanceof RedisFeature)) continue;
            FeatureTasks.RedisJoin task = new FeatureTasks.RedisJoin((RedisFeature) f, connectedPlayer);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Handles redis player server switch and forwards it to all features.
     *
     * @param   connectedPlayer
     *          Player who joined
     */
    public void onServerSwitch(@NotNull RedisPlayer connectedPlayer) {
        for (TabFeature f : values) {
            if (!(f instanceof RedisFeature)) continue;
            FeatureTasks.RedisJoin task = new FeatureTasks.RedisJoin((RedisFeature) f, connectedPlayer);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Handles redis player quit and forwards it to all features.
     *
     * @param   disconnectedPlayer
     *          Player who left
     */
    public void onQuit(@NotNull RedisPlayer disconnectedPlayer) {
        for (TabFeature f : values) {
            if (!(f instanceof RedisFeature)) continue;
            FeatureTasks.RedisQuit task = new FeatureTasks.RedisQuit((RedisFeature) f, disconnectedPlayer);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Forwards vanish status change to all features.
     *
     * @param   player
     *          Player whose vanish status changed
     */
    public void onVanishStatusChange(@NotNull RedisPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof RedisFeature)) continue;
            FeatureTasks.RedisVanishStatus task = new FeatureTasks.RedisVanishStatus((RedisFeature) f, player);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Registers feature with given parameters.
     *
     * @param   featureName
     *          Name of feature to register as
     * @param   featureHandler
     *          Feature handler
     */
    public synchronized void registerFeature(@NotNull String featureName, @NotNull TabFeature featureHandler) {
        features.put(featureName, featureHandler);
        values = features.values().toArray(new TabFeature[0]);
        if (featureHandler instanceof VanishListener) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(TabConstants.Placeholder.VANISHED);
        }
        if (featureHandler instanceof GameModeListener) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(TabConstants.Placeholder.GAMEMODE);
        }
        if (featureHandler instanceof LatencyListener) {
            hasLatencyChangeListener = true;
        }
        if (featureHandler instanceof CommandListener) {
            hasCommandListener = true;
            listeningCommands.add(((CommandListener) featureHandler).getCommand());
        }
    }

    /**
     * Unregisters feature with given name.
     *
     * @param   featureName
     *          Name of the feature it was previously registered with.
     */
    public void unregisterFeature(@NotNull String featureName) {
        features.remove(featureName);
        values = features.values().toArray(new TabFeature[0]);
    }

    /**
     * Returns {@code true} if feature is enabled, {@code false} if not.
     *
     * @param   name
     *          Name of the feature
     * @return  {@code true} if enabled, {@code false} if not
     */
    public boolean isFeatureEnabled(@NotNull String name) {
        return features.containsKey(name);
    }

    /**
     * Returns feature by given name.
     *
     * @param   name
     *          Name of the feature
     * @return  Feature handler
     * @param   <T>
     *          class extending TabFeature
     */
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

        boolean pingSpoof          = configuration.getConfig().getBoolean("ping-spoof.enabled", false);
        boolean bossbar            = configuration.getConfig().getBoolean("bossbar.enabled", false);
        boolean headerFooter       = configuration.getConfig().getBoolean("header-footer.enabled", true);
        boolean spectatorFix       = configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false);
        boolean scoreboard         = configuration.getConfig().getBoolean("scoreboard.enabled", false);
        boolean perWorldPlayerList = configuration.getConfig().getBoolean("per-world-playerlist.enabled", false);
        boolean layout             = configuration.getConfig().getBoolean("layout.enabled", false);
        boolean yellowNumber       = configuration.getConfig().getBoolean("playerlist-objective.enabled", true);
        boolean belowName          = configuration.getConfig().getBoolean("belowname-objective.enabled", false);
        boolean teams              = configuration.getConfig().getBoolean("scoreboard-teams.enabled", true);
        boolean globalPlayerList   = configuration.getConfig().getBoolean("global-playerlist.enabled", false);
        boolean tablistFormatting  = configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true);

        if (perWorldPlayerList && layout) TAB.getInstance().getConfigHelper().startup().bothPerWorldPlayerListAndLayoutEnabled();
        if (yellowNumber && layout)       TAB.getInstance().getConfigHelper().startup().layoutBreaksYellowNumber();
        if (spectatorFix && layout)       TAB.getInstance().getConfigHelper().hint().layoutIncludesPreventSpectatorEffect();
        if (globalPlayerList && layout)   TAB.getInstance().getConfigHelper().startup().bothGlobalPlayerListAndLayoutEnabled();

        // Load the feature first, because it will be processed in main thread (to make it run before feature threads)
        if (configuration.isEnableRedisHook()) {
            RedisSupport redis = TAB.getInstance().getPlatform().getRedisSupport();
            if (redis != null) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, redis);
        }

        if (configuration.isPipelineInjection()) {
            PipelineInjector inj = TAB.getInstance().getPlatform().createPipelineInjector();
            if (inj != null) featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, inj);
        }

        if (perWorldPlayerList) {
            TabFeature pwp = TAB.getInstance().getPlatform().getPerWorldPlayerList();
            if (pwp != null) featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, pwp);
        }
        if (bossbar)      featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
        if (pingSpoof)    featureManager.registerFeature(TabConstants.Feature.PING_SPOOF, new PingSpoof());
        if (headerFooter) featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter());
        if (spectatorFix) featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());
        if (scoreboard)   featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl());
        if (yellowNumber) featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, new YellowNumber());
        if (belowName)    featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, new BelowName());
        if (teams || layout) featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting());
        if (tablistFormatting) featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new PlayerList());

        // Must be loaded after: Sorting
        if (teams) featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());

        // Must be loaded after: Sorting, PlayerList
        if (layout) featureManager.registerFeature(TabConstants.Feature.LAYOUT, new LayoutManagerImpl());

        // Must be loaded after: PlayerList
        if (globalPlayerList && TAB.getInstance().getPlatform().isProxy()) {
            featureManager.registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, new GlobalPlayerList());
        }

        featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility());
    }
}