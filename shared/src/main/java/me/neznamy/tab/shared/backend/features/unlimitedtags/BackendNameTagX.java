package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;

import java.util.List;

public abstract class BackendNameTagX extends NameTagX {

    /** Vehicle manager reference */
    @Getter private final VehicleRefresher vehicleManager = new VehicleRefresher(this);

    /** Packet Listener reference */
    protected final PacketListener packetListener = new PacketListener(this);

    public BackendNameTagX() {
        super(BackendArmorStandManager::new);
        TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager);
        TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, packetListener);
        TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_GAMEMODE_LISTENER, new GameModeRefresher(this));
    }

    /**
     * Starts task checking for player visibility to hide armor stands of invisible players.
     */
    private void startVisibilityRefreshTask() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, this, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY, () -> {

            for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
                if (isPlayerDisabled(p)) continue;
                getArmorStandManager(p).updateVisibility(false);
            }
        });
    }

    @Override
    public BackendArmorStandManager getArmorStandManager(TabPlayer player) {
        return (BackendArmorStandManager) armorStandManagerMap.get(player);
    }

    @Override
    public void load() {
        TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%gamemode%", 500, TabPlayer::getGamemode);
        super.load();
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            if (isPlayerDisabled(all)) continue;
            for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
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
    public void onJoin(TabPlayer connectedPlayer) {
        super.onJoin(connectedPlayer);
        if (isPlayerDisabled(connectedPlayer)) return;
        for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, connectedPlayer);
            spawnArmorStands(connectedPlayer, viewer);
        }
    }

    @Override
    public boolean isOnBoat(TabPlayer player) {
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
    private void spawnArmorStands(TabPlayer viewer, TabPlayer target) {
        if (viewer.getVersion().getMinorVersion() < 8) return;
        if (target == viewer || isPlayerDisabled(target)) return;
        if (!areInSameWorld(viewer, target)) return;
        if (getDistance(viewer, target) <= 48 && canSee(viewer, target) && !target.isVanished())
            getArmorStandManager(target).spawn((BackendTabPlayer) viewer);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).unregisterPlayer((BackendTabPlayer) disconnectedPlayer);
        }
        armorStandManagerMap.get(disconnectedPlayer).destroy();
        armorStandManagerMap.remove(disconnectedPlayer); // WeakHashMap doesn't clear this due to value referencing the key
    }

    @Override
    public void resumeArmorStands(TabPlayer player) {
        if (isPlayerDisabled(player)) return;
        for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, player);
        }
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean status) {
        if (status) {
            getArmorStandManager(player).spawn((BackendTabPlayer) player);
        } else {
            getArmorStandManager(player).destroy((BackendTabPlayer) player);
        }
    }

    @Override
    public void pauseArmorStands(TabPlayer player) {
        getArmorStandManager(player).destroy();
    }

    @Override
    public void updateNameTagVisibilityView(TabPlayer player) {
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).updateVisibility(true);
        }
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        super.onWorldChange(p, from, to);
        if (isUnlimitedDisabled(p.getServer(), to)) {
            getDisabledUnlimitedPlayers().add(p);
            updateTeamData(p);
        } else if (getDisabledUnlimitedPlayers().remove(p)) {
            updateTeamData(p);
        }
        if (isPreviewingNametag(p)) {
            getArmorStandManager(p).spawn((BackendTabPlayer) p);
        }
        //for some reason this is needed for some users
        for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
            if (viewer.getWorld().equals(from)) {
                getArmorStandManager(p).destroy((BackendTabPlayer) viewer);
            }
        }
    }

    public int getEntityId(TabPlayer player) {
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
    public abstract double getDistance(TabPlayer player1, TabPlayer player2);

    public abstract boolean areInSameWorld(TabPlayer player1, TabPlayer player2);

    public abstract boolean canSee(TabPlayer viewer, TabPlayer target);

    public abstract void unregisterListener();

    public abstract List<Integer> getPassengers(Object vehicle);

    public abstract void registerVehiclePlaceholder();

    public abstract Object getVehicle(TabPlayer player);

    public abstract int getEntityId(Object entity);

    public abstract String getEntityType(Object entity);

    public abstract boolean isSneaking(TabPlayer player);

    public abstract boolean isSwimming(TabPlayer player);

    public abstract boolean isGliding(TabPlayer player);

    public abstract boolean isSleeping(TabPlayer player);

    public abstract Object getArmorStandType();

    public abstract double getX(TabPlayer player);

    public abstract double getY(Object entity);

    public abstract double getZ(TabPlayer player);

    public abstract EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible);
}
