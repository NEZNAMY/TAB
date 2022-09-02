package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The core class for unlimited NameTag mode on Bukkit
 */
public class BukkitNameTagX extends NameTagX {

    /** Bukkit event listener */
    private final EventListener eventListener = new EventListener(this);

    /** Vehicle manager reference */
    private final VehicleRefresher vehicleManager = new VehicleRefresher(this);

    /**
     * Constructs new instance with given parameter, loads config options, registers events
     * and registers sub-features.
     *
     * @param   plugin
     *          plugin instance
     */
    public BukkitNameTagX(JavaPlugin plugin) {
        super(BukkitArmorStandManager::new);
        Bukkit.getPluginManager().registerEvents(eventListener, plugin);
        TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, new PacketListener(this));
        TabAPI.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager);
    }

    @Override
    public void load() {
        super.load();
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            if (isPlayerDisabled(all)) continue;
            for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
                spawnArmorStands(viewer, all);
            }
        }
        startVisibilityRefreshTask();
    }

    /**
     * Starts task checking for player visibility to hide armor stands of invisible players.
     */
    private void startVisibilityRefreshTask() {
        TabAPI.getInstance().getThreadManager().startRepeatingMeasuredTask(500, this, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY, () -> {

            for (TabPlayer p : TabAPI.getInstance().getOnlinePlayers()) {
                if (isPlayerDisabled(p)) continue;
                getArmorStandManager(p).updateVisibility(false);
            }
        });
    }

    @Override
    public void unload() {
        super.unload();
        HandlerList.unregisterAll(eventListener);
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
        if (((Player) viewer.getPlayer()).getWorld() != ((Player) target.getPlayer()).getWorld()) return;
        if (getDistance(viewer, target) <= 48 && ((Player)viewer.getPlayer()).canSee((Player)target.getPlayer()) && !target.isVanished())
            getArmorStandManager(target).spawn(viewer);
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean status) {
        if (status) {
            getArmorStandManager(player).spawn(player);
        } else {
            getArmorStandManager(player).destroy(player);
        }
    }

    @Override
    public void resumeArmorStands(TabPlayer player) {
        if (isPlayerDisabled(player)) return;
        for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, player);
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
    public void onQuit(TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).unregisterPlayer(disconnectedPlayer);
        }
        armorStandManagerMap.get(disconnectedPlayer).destroy();
        armorStandManagerMap.remove(disconnectedPlayer); // WeakHashMap doesn't clear this due to value referencing the key
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
            getArmorStandManager(p).spawn(p);
        }
        //for some reason this is needed for some users
        for (TabPlayer viewer : TabAPI.getInstance().getOnlinePlayers()) {
            if (viewer.getWorld().equals(from)) {
                getArmorStandManager(p).destroy(viewer);
            }
        }
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
    private double getDistance(TabPlayer player1, TabPlayer player2) {
        Location loc1 = ((Player) player1.getPlayer()).getLocation();
        Location loc2 = ((Player) player2.getPlayer()).getLocation();
        return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
    }

    @Override
    public BukkitArmorStandManager getArmorStandManager(TabPlayer player) {
        return (BukkitArmorStandManager) armorStandManagerMap.get(player);
    }

    /**
     * Returns {@link #vehicleManager}
     * @return  {@link #vehicleManager}
     */
    public VehicleRefresher getVehicleManager() {
        return vehicleManager;
    }
}