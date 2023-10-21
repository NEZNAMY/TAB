package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.GameModeListener;
import me.neznamy.tab.shared.features.types.PacketSendListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class BackendNameTagX extends NameTagX implements GameModeListener, PacketSendListener {

    /** Vehicle manager reference */
    @Getter private final VehicleRefresher vehicleManager = new VehicleRefresher(this);

    /** Packet Listener reference */
    protected final PacketListener packetListener = new PacketListener(this);

    public BackendNameTagX() {
        super(BackendArmorStandManager::new);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, packetListener);
    }

    /**
     * Starts task checking for player visibility to hide armor stands of invisible players.
     */
    private void startVisibilityRefreshTask() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, featureName, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY, () -> {

            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (isPlayerDisabled(p)) continue;
                getArmorStandManager(p).updateVisibility(false);
            }
        });
    }

    @Override
    public BackendArmorStandManager getArmorStandManager(@NotNull TabPlayer player) {
        return (BackendArmorStandManager) armorStandManagerMap.get(player);
    }

    @Override
    public void load() {
        super.load();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (isPlayerDisabled(all)) continue;
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                spawnArmorStands(viewer, all);
            }
        }
        startVisibilityRefreshTask();
    }

    @Override
    public void unload() {
        super.unload();
        unregisterListener();
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        super.onJoin(connectedPlayer);
        if (isPlayerDisabled(connectedPlayer)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, connectedPlayer);
            spawnArmorStands(connectedPlayer, viewer);
        }
    }

    @Override
    public boolean isOnBoat(@NotNull TabPlayer player) {
        return vehicleManager != null && vehicleManager.isOnBoat(player);
    }

    /**
     * Spawns armor stands of target player to viewer if all requirements are met.
     * These include players being in the same world, distance being less than 48 blocks
     * and target player being visible to viewer.
     *
     * @param   viewer
     *          Player viewing armor stands
     * @param   target
     *          Target player with armor stands
     */
    private void spawnArmorStands(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        if (target == viewer || isPlayerDisabled(target) || isDead(target)) return;
        if (!areInSameWorld(viewer, target)) return;
        if (getDistance(viewer, target) <= 48 && canSee(viewer, target) && !target.isVanished())
            getArmorStandManager(target).spawn((BackendTabPlayer) viewer);
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).unregisterPlayer((BackendTabPlayer) disconnectedPlayer);
        }
        armorStandManagerMap.get(disconnectedPlayer).destroy();
        armorStandManagerMap.remove(disconnectedPlayer); // WeakHashMap doesn't clear this due to value referencing the key
    }

    @Override
    public void resumeArmorStands(@NotNull TabPlayer player) {
        if (isPlayerDisabled(player)) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, player);
        }
    }

    @Override
    public void setNameTagPreview(@NotNull TabPlayer player, boolean status) {
        if (status) {
            getArmorStandManager(player).spawn((BackendTabPlayer) player);
        } else {
            getArmorStandManager(player).destroy((BackendTabPlayer) player);
        }
    }

    @Override
    public void pauseArmorStands(@NotNull TabPlayer player) {
        getArmorStandManager(player).destroy();
    }

    @Override
    public void updateNameTagVisibilityView(@NotNull TabPlayer player) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).updateVisibility(true);
        }
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        super.onWorldChange(p, from, to);
        if (isPreviewingNameTag(p)) {
            getArmorStandManager(p).spawn((BackendTabPlayer) p);
        }
        //for some reason this is needed for some users
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getWorld().equals(from)) {
                getArmorStandManager(p).destroy((BackendTabPlayer) viewer);
            }
        }
    }

    @Override
    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            getArmorStandManager(player).updateMetadata((BackendTabPlayer) viewer);
        }
    }

    @Override
    public void onPacketSend(@NotNull TabPlayer receiver, @NotNull Object packet) {
        if (receiver.getVersion().getMinorVersion() < 8) return;
        if (!receiver.isLoaded() || getDisableChecker().isDisabledPlayer(receiver) || getUnlimitedDisableChecker().isDisabledPlayer(receiver)) return;
        BackendTabPlayer player = (BackendTabPlayer) receiver;
        if (player.getEntityView().isMovePacket(packet) && !player.getEntityView().isLookPacket(packet)) { //ignoring head rotation only packets
            packetListener.onEntityMove(player, player.getEntityView().getMoveEntityId(packet));
        } else if (player.getEntityView().isTeleportPacket(packet)) {
            packetListener.onEntityMove(player, player.getEntityView().getTeleportEntityId(packet));
        } else if (player.getEntityView().isNamedEntitySpawnPacket(packet)) {
            packetListener.onEntitySpawn(player, player.getEntityView().getSpawnedPlayer(packet));
        } else if (player.getEntityView().isDestroyPacket(packet)) {
            packetListener.onEntityDestroy(player, player.getEntityView().getDestroyedEntities(packet));
        }
    }

    public void sneak(UUID playerUUID, boolean sneaking) {
        TabPlayer p = TAB.getInstance().getPlayer(playerUUID);
        if (p == null || isPlayerDisabled(p)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PLAYER_SNEAK,
                () -> getArmorStandManager(p).sneak(sneaking));
    }

    public void respawn(UUID playerUUID) {
        TabPlayer respawned = TAB.getInstance().getPlayer(playerUUID);
        if (respawned == null || isPlayerDisabled(respawned)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PLAYER_RESPAWN,
                () -> getArmorStandManager(respawned).teleport());
    }

    public int getEntityId(@NotNull TabPlayer player) {
        return getEntityId(player.getPlayer());
    }

    /**
     * Returns flat distance between two players ignoring Y value
     *
     * @param   player1
     *          first player
     * @param   player2
     *          second player
     * @return  flat distance in blocks
     */
    public abstract double getDistance(@NotNull TabPlayer player1, @NotNull TabPlayer player2);

    public abstract boolean areInSameWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2);

    public abstract boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target);

    public abstract void unregisterListener();

    public abstract @NotNull List<Integer> getPassengers(@NotNull Object vehicle);

    public abstract @Nullable Object getVehicle(@NotNull TabPlayer player);

    public abstract int getEntityId(@NotNull Object entity);

    public abstract @NotNull String getEntityType(@NotNull Object entity);

    public abstract boolean isSneaking(@NotNull TabPlayer player);

    public abstract boolean isSwimming(@NotNull TabPlayer player);

    public abstract boolean isGliding(@NotNull TabPlayer player);

    public abstract boolean isSleeping(@NotNull TabPlayer player);

    public abstract @NotNull Object getArmorStandType();

    public abstract double getX(@NotNull TabPlayer player);

    public abstract double getY(@NotNull Object entity);

    public abstract double getZ(@NotNull TabPlayer player);

    public abstract EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible);

    public abstract void runInEntityScheduler(Object entity, Runnable task);

    public abstract boolean isDead(TabPlayer player);
}
