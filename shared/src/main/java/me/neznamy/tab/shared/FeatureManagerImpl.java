package me.neznamy.tab.shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import me.neznamy.tab.api.FeatureManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * Feature registration which offers calls to all features
 * and measures how long it took them to process
 */
public class FeatureManagerImpl implements FeatureManager {

    /** Map of all registered feature where key is feature's identifier */
    private final Map<String, TabFeature> features = new LinkedHashMap<>();

    /** All registered features in an array to avoid memory allocations on iteration */
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
    public void refresh(TabPlayer refreshed, boolean force) {
        for (TabFeature f : values) {
            if (f instanceof Refreshable) ((Refreshable)f).refresh(refreshed, force);
        }
    }

    public int onGameModeChange(TabPlayer packetReceiver, UUID id, int gameMode) {
        for (TabFeature f : values) {
            if (!(f instanceof GameModeListener)) continue;
            long time = System.nanoTime();
            gameMode = ((GameModeListener) f).onGameModeChange(packetReceiver, id, gameMode);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
        return gameMode;
    }

    public int onLatencyChange(TabPlayer packetReceiver, UUID id, int latency) {
        for (TabFeature f : values) {
            if (!(f instanceof LatencyListener)) continue;
            long time = System.nanoTime();
            latency = ((LatencyListener)f).onLatencyChange(packetReceiver, id, latency);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
        return latency;
    }

    public IChatBaseComponent onDisplayNameChange(TabPlayer packetReceiver, UUID id, IChatBaseComponent displayName) {
        for (TabFeature f : values) {
            if (!(f instanceof DisplayNameListener)) continue;
            long time = System.nanoTime();
            displayName = ((DisplayNameListener) f).onDisplayNameChange(packetReceiver, id, displayName);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
        return displayName;
    }

    public void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        for (TabFeature f : values) {
            if (!(f instanceof EntryAddListener)) continue;
            long time = System.nanoTime();
            ((EntryAddListener) f).onEntryAdd(packetReceiver, id, name);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime() - time);
        }
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
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

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
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
        ((ITabPlayer)connectedPlayer).markAsLoaded(true);
        TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
        if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
            MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
            users.load(connectedPlayer);
        }
    }

    @Override
    public void onWorldChange(UUID playerUUID, String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.getWorld();
        ((ITabPlayer)changed).setWorld(to);
        if (!changed.isLoaded()) return; // Plugin message came back on reload too quickly and player is not loaded yet
        for (TabFeature f : values) {
            if (!(f instanceof WorldSwitchListener)) continue;
            long time = System.nanoTime();
            ((WorldSwitchListener) f).onWorldChange(changed, from, to);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.WORLD)).updateValue(changed, to);
    }

    @Override
    public void onServerChange(UUID playerUUID, String to) {
        TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
        if (changed == null) return;
        String from = changed.getServer();
        ((ITabPlayer)changed).setServer(to);
        ((ProxyTabPlayer)changed).sendJoinPluginMessage();
        if (!isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION) || changed.getVersion().getMinorVersion() < 8)
            onLoginPacket(changed);
        for (TabFeature f : values) {
            if (!(f instanceof ServerSwitchListener)) continue;
            long time = System.nanoTime();
            ((ServerSwitchListener) f).onServerChange(changed, from, to);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.SERVER_SWITCH, System.nanoTime()-time);
        }
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.SERVER)).updateValue(changed, to);
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
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
     * Calls onPacketReceive(TabPlayer, Object) on all features
     * 
     * @param   sender
     *          packet sender
     * @param   packet
     *          IN packet coming from player
     * @return  {@code true} if packet should be cancelled, {@code false} if not
     */
    public boolean onPacketReceive(TabPlayer sender, Object packet) {
        boolean cancel = false;
        for (TabFeature f : values) {
            if (!(f instanceof PacketReceiveListener)) continue;
            long time = System.nanoTime();
            try {
                if (((PacketReceiveListener)f).onPacketReceive(sender, packet)) cancel = true;
            } catch (ReflectiveOperationException e) {
                TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
            }
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.RAW_PACKET_IN, System.nanoTime()-time);
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
    public void onPacketSend(TabPlayer receiver, Object packet) {
        for (TabFeature f : values) {
            if (!(f instanceof PacketSendListener)) continue;
            long time = System.nanoTime();
            try {
                ((PacketSendListener)f).onPacketSend(receiver, packet);
            } catch (ReflectiveOperationException e) {
                TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
            }
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.RAW_PACKET_OUT, System.nanoTime()-time);
        }
    }

    /**
     * Calls onLoginPacket(TabPlayer) on all features
     *
     * @param   packetReceiver
     *          player who received the packet
     */
    public void onLoginPacket(TabPlayer packetReceiver) {
        ((TabScoreboard)packetReceiver.getScoreboard()).clearRegisteredObjectives();
        for (TabFeature f : values) {
            if (!(f instanceof LoginPacketListener)) continue;
            long time = System.nanoTime();
            ((LoginPacketListener)f).onLoginPacket(packetReceiver);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_JOIN_GAME, System.nanoTime()-time);
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
    public void onDisplayObjective(TabPlayer packetReceiver, int slot, String objective) {
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
    public void onObjective(TabPlayer packetReceiver, int action, String objective) {
        for (TabFeature f : values) {
            if (!(f instanceof ObjectiveListener)) continue;
            long time = System.nanoTime();
            ((ObjectiveListener)f).onObjective(packetReceiver, action, objective);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
        }
    }

    public void onVanishStatusChange(TabPlayer player) {
        for (TabFeature f : values) {
            if (!(f instanceof VanishListener)) continue;
            long time = System.nanoTime();
            ((VanishListener)f).onVanishStatusChange(player);
            TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.VANISH_CHANGE, System.nanoTime()-time);
        }
    }

    @Override
    public void registerFeature(String featureName, TabFeature featureHandler) {
        if (featureName == null || featureHandler == null) return;
        features.put(featureName, featureHandler);
        values = features.values().toArray(new TabFeature[0]);
    }

    @Override
    public void unregisterFeature(String featureName) {
        features.remove(featureName);
        values = features.values().toArray(new TabFeature[0]);
    }

    @Override
    public boolean isFeatureEnabled(String name) {
        return features.containsKey(name);
    }

    @Override
    public TabFeature getFeature(String name) {
        return features.get(name);
    }
}