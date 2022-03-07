package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The core class for unlimited NameTag mode
 */
public class BukkitNameTagX extends NameTagX {

    //bukkit event listener
    private final EventListener eventListener = new EventListener(this);

    private final VehicleRefresher vehicleManager = new VehicleRefresher(this);

    /**
     * Constructs new instance with given parameters and loads config options
     * @param plugin - plugin instance
     */
    public BukkitNameTagX(JavaPlugin plugin) {
        super(BukkitArmorStandManager::new);
        Bukkit.getPluginManager().registerEvents(eventListener, plugin);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, new PacketListener(this));
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager);
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

    private void startVisibilityRefreshTask() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, this, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY, () -> {

            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
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
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, connectedPlayer);
            spawnArmorStands(connectedPlayer, viewer);
        }
    }

    @Override
    public boolean isOnBoat(TabPlayer player) {
        return vehicleManager != null && vehicleManager.isOnBoat(player);
    }

    private void spawnArmorStands(TabPlayer viewer, TabPlayer target) {
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
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            spawnArmorStands(viewer, player);
        }
    }

    @Override
    public void pauseArmorStands(TabPlayer player) {
        if (isPlayerDisabled(player)) return;
        getArmorStandManager(player).destroy();
    }

    @Override
    public void updateNameTagVisibilityView(TabPlayer player) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            getArmorStandManager(all).updateVisibility(true);
        }
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        super.onQuit(disconnectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
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
    }

    /**
     * Returns flat distance between two players ignoring Y value
     * @param player1 - first player
     * @param player2 - second player
     * @return flat distance in blocks
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

    public VehicleRefresher getVehicleManager() {
        return vehicleManager;
    }


}