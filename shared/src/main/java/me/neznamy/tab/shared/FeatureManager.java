package me.neznamy.tab.shared;

import com.saicone.delivery4j.broker.RabbitMQBroker;
import com.saicone.delivery4j.broker.RedisBroker;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.belowname.BelowName;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.header.HeaderFooter;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.pingspoof.PingSpoof;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.playerlistobjective.YellowNumber;
import me.neznamy.tab.shared.features.proxy.ProxyMessengerSupport;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.Unload;
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
            ((Loadable) f).load();
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
        // Shut down and then unload sync to prevent load running before old unload ends on reloads
        for (TabFeature f : values) {
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().shutdown();
            }
            if (f instanceof UnLoadable) {
                ((UnLoadable) f).unload();
            }
        }
        for (TabFeature f : values) {
            f.deactivate();
        }
        long time = System.currentTimeMillis();
        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            player.getScoreboard().clear();
            player.getBossBar().clear();
        }
        TAB.getInstance().debug("Unregistered all scoreboard teams, objectives and boss bars for all players in " + (System.currentTimeMillis()-time) + "ms");
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((GroupListener) f).onGroupChange(player), f.getFeatureName(), CpuUsageCategory.GROUP_CHANGE);
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((GameModeListener) f).onGameModeChange(player), f.getFeatureName(), CpuUsageCategory.GAMEMODE_CHANGE);
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((QuitListener) f).onQuit(disconnectedPlayer), f.getFeatureName(), CpuUsageCategory.PLAYER_QUIT);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        TAB.getInstance().removePlayer(disconnectedPlayer);
        removeforcedDisplayName(disconnectedPlayer.getTablistId());
        TAB.getInstance().debug("Player quit of " + disconnectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");

        ProxySupport proxy = getFeature(TabConstants.Feature.PROXY_SUPPORT);
        if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(disconnectedPlayer.getUniqueId());
            if (proxyPlayer != null && proxyPlayer.getConnectionState() == ProxyPlayer.ConnectionState.QUEUED) {
                onJoin(proxyPlayer);
            }
        }
    }

    private void removeforcedDisplayName(@NotNull UUID id) {
        ProxySupport proxy = getFeature(TabConstants.Feature.PROXY_SUPPORT);
        if (proxy != null) {
            ProxyPlayer proxyPlayer = proxy.getProxyPlayers().get(id);
            if (proxyPlayer != null) return; // Proxy player is already connected, do not remove anymore
        }

        if (TAB.getInstance().getPlayer(id) != null) return; // Real player connected in the meantime, do not remove anymore

        // Player is actually not online anymore, remove to avoid memory leak
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            ((TrackedTabList<?>)all.getTabList()).getForcedDisplayNames().remove(id);
            ((TrackedTabList<?>)all.getTabList()).getForcedGameModes().remove(id);
        }
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> ((JoinListener) f).onJoin(connectedPlayer), f.getFeatureName(), CpuUsageCategory.PLAYER_JOIN);
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
     *          New world
     */
    public void onWorldChange(@NotNull UUID playerUUID, @NotNull World to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        World from = changed.world;
        changed.world = to;
        for (TabFeature f : values) {
            if (!(f instanceof WorldSwitchListener)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((WorldSwitchListener) f).onWorldChange(changed, from, to), f.getFeatureName(), CpuUsageCategory.WORLD_SWITCH);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.WORLD)).updateValue(changed, to.getName());
    }

    /**
     * Processed server switch and forwards it to all features.
     *
     * @param   playerUUID
     *          UUID of player who switched server
     * @param   to
     *          New server name
     */
    public void onServerChange(@NotNull UUID playerUUID, @NotNull Server to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        Server from = changed.server;
        changed.server = to;
        ((ProxyTabPlayer)changed).sendJoinPluginMessage();
        ((TrackedTabList<?>)changed.getTabList()).resendHeaderFooter();
        for (TabFeature f : values) {
            if (!(f instanceof ServerSwitchListener)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ServerSwitchListener) f).onServerChange(changed, from, to), f.getFeatureName(), CpuUsageCategory.SERVER_SWITCH);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.SERVER)).updateValue(changed, to.getName());
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
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), CpuUsageCategory.COMMAND_PREPROCESS, System.nanoTime()-time);
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((DisplayObjectiveListener) f).onDisplayObjective(packetReceiver, slot, objective), f.getFeatureName(), CpuUsageCategory.SCOREBOARD_PACKET_CHECK);
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ObjectiveListener) f).onObjective(packetReceiver, action, objective), f.getFeatureName(), CpuUsageCategory.SCOREBOARD_PACKET_CHECK);
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((VanishListener) f).onVanishStatusChange(player), f.getFeatureName(), CpuUsageCategory.VANISH_CHANGE);
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
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), CpuUsageCategory.NICK_PLUGIN_COMPATIBILITY, System.nanoTime() - time);
        }
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
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((TabListClearListener) f).onTabListClear(packetReceiver), f.getFeatureName(), CpuUsageCategory.TABLIST_CLEAR);
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
    public void onProxyLoadRequest() {
        for (TabFeature f : values) {
            if (!(f instanceof ProxyFeature)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ProxyFeature) f).onProxyLoadRequest(), f.getFeatureName(), CpuUsageCategory.PROXY_RELOAD);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Handles proxy player join and forwards it to all features.
     *
     * @param   connectedPlayer
     *          Player who joined
     */
    public void onJoin(@NotNull ProxyPlayer connectedPlayer) {
        // Delay to let server remove the player from tablist, and then we can potentially add the player back
        // No delay could cause the server to send tablist remove packet of real player AFTER we sent add, removing the player
        TAB.getInstance().getCpu().getProcessingThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (connectedPlayer.getConnectionState() == ProxyPlayer.ConnectionState.DISCONNECTED) return; // Player immediately disconnected in the meantime
            connectedPlayer.setConnectionState(ProxyPlayer.ConnectionState.CONNECTED);
            for (TabFeature f : values) {
                if (!(f instanceof ProxyFeature)) continue;
                TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                        () -> ((ProxyFeature) f).onJoin(connectedPlayer), f.getFeatureName(), CpuUsageCategory.PLAYER_JOIN);
                if (f instanceof CustomThreaded) {
                    ((CustomThreaded) f).getCustomThread().execute(task);
                } else {
                    task.run();
                }
            }
            if (connectedPlayer.isVanished()) {
                onVanishStatusChange(connectedPlayer);
            }
        }, getFeature(TabConstants.Feature.PROXY_SUPPORT).getFeatureName(), CpuUsageCategory.PLAYER_JOIN), 200);
    }

    /**
     * Handles proxy player server switch and forwards it to all features.
     *
     * @param   player
     *          Player who joined
     */
    public void onServerSwitch(@NotNull ProxyPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof ProxyFeature)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ProxyFeature) f).onServerSwitch(player), f.getFeatureName(), CpuUsageCategory.SERVER_SWITCH);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Handles proxy player quit and forwards it to all features.
     *
     * @param   disconnectedPlayer
     *          Player who left
     */
    public void onQuit(@NotNull ProxyPlayer disconnectedPlayer) {
        disconnectedPlayer.setConnectionState(ProxyPlayer.ConnectionState.DISCONNECTED);
        for (TabFeature f : values) {
            if (!(f instanceof ProxyFeature)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ProxyFeature) f).onQuit(disconnectedPlayer), f.getFeatureName(), CpuUsageCategory.PLAYER_QUIT);
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        removeforcedDisplayName(disconnectedPlayer.getTablistId());
    }

    /**
     * Forwards vanish status change to all features.
     *
     * @param   player
     *          Player whose vanish status changed
     */
    public void onVanishStatusChange(@NotNull ProxyPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof ProxyFeature)) continue;
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(),
                    () -> ((ProxyFeature) f).onVanishStatusChange(player), f.getFeatureName(), CpuUsageCategory.VANISH_CHANGE);
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
        Config config = TAB.getInstance().getConfiguration().getConfig();
        FeatureManager featureManager = TAB.getInstance().getFeatureManager();

        // Load the feature first, because it will be processed in main thread (to make it run before feature threads)
        if (config.isEnableProxySupport()) {
            String type = config.getConfig().getString("proxy-support.type", "PLUGIN");
            ProxySupport proxy = null;
            if (type.equalsIgnoreCase("PLUGIN")) {
                String plugin = config.getConfig().getString("proxy-support.plugin.name", "RedisBungee");
                proxy = TAB.getInstance().getPlatform().getProxySupport(plugin);
            } else if (type.equalsIgnoreCase("REDIS")) {
                String url = config.getConfig().getString("proxy-support.redis.url", "redis://:password@localhost:6379/0");
                proxy = new ProxyMessengerSupport("Redis", () -> RedisBroker.of(url));
            } else if (type.equalsIgnoreCase("RABBITMQ")) {
                String exchange = config.getConfig().getString("proxy-support.rabbitmq.exchange", "plugin");
                String url = config.getConfig().getString("proxy-support.rabbitmq.url", "amqp://guest:guest@localhost:5672/%2F");
                proxy = new ProxyMessengerSupport("RabbitMQ", () -> RabbitMQBroker.of(url, exchange));
            }
            if (proxy != null) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.PROXY_SUPPORT, proxy);
        }

        if (config.isPipelineInjection()) {
            PipelineInjector inj = TAB.getInstance().getPlatform().createPipelineInjector();
            if (inj != null) featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, inj);
        }

        if (config.getPerWorldPlayerList() != null) {
            TabFeature pwp = TAB.getInstance().getPlatform().getPerWorldPlayerList(config.getPerWorldPlayerList());
            if (pwp != null) featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, pwp);
        }
        if (config.getBossbar() != null) {
            featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl(config.getBossbar()));
        }
        if (config.getPingSpoof() != null) {
            featureManager.registerFeature(TabConstants.Feature.PING_SPOOF, new PingSpoof(config.getPingSpoof()));
        }
        if (config.getHeaderFooter() != null) {
            featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter(config.getHeaderFooter()));
        }
        if (config.isPreventSpectatorEffect()) {
            featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());
        }
        if (config.getScoreboard() != null) {
            featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl(config.getScoreboard()));
        }
        if (config.getPlayerlistObjective() != null) {
            featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, new YellowNumber(config.getPlayerlistObjective()));
        }
        if (config.getBelowname() != null) {
            featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, new BelowName(config.getBelowname()));
        }
        if (config.getSorting() != null) {
            featureManager.registerFeature(TabConstants.Feature.SORTING, new Sorting(config.getSorting()));
        }
        if (config.getTablistFormatting() != null) {
            featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, new PlayerList(config.getTablistFormatting()));
        }

        // Must be loaded after: Sorting
        if (config.getTeams() != null) {
            featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag(config.getTeams()));
        }

        // Must be loaded after: Sorting, PlayerList
        if (config.getLayout() != null) {
            featureManager.registerFeature(TabConstants.Feature.LAYOUT, new LayoutManagerImpl(config.getLayout()));
        }

        // Must be loaded after: PlayerList
        if (config.getGlobalPlayerList() != null) {
            featureManager.registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, new GlobalPlayerList(config.getGlobalPlayerList()));
        }

        featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility());
    }
}