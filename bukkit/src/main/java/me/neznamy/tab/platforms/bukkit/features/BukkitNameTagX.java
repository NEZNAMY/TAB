package me.neznamy.tab.platforms.bukkit.features;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.DataWatcher;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The core class for unlimited NameTag mode on Bukkit
 */
public class BukkitNameTagX extends BackendNameTagX implements Listener {

    public BukkitNameTagX(@NotNull JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent e) {
        sneak(e.getPlayer().getUniqueId(), e.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        respawn(e.getPlayer().getUniqueId());
    }

    @Override
    public double getDistance(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        Location loc1 = ((Player) player1.getPlayer()).getLocation();
        Location loc2 = ((Player) player2.getPlayer()).getLocation();
        return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
    }

    @Override
    public boolean areInSameWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        return ((Player) player1.getPlayer()).getWorld() == ((Player) player2.getPlayer()).getWorld();
    }

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return ((Player)viewer.getPlayer()).canSee((Player) target.getPlayer());
    }

    @Override
    public void unregisterListener() {
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    @NotNull
    public List<Integer> getPassengers(@NotNull Object entity) {
        Entity vehicle = (Entity) entity;
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 11) {
            return vehicle.getPassengers().stream().map(Entity::getEntityId).collect(Collectors.toList());
        } else {
            if (vehicle.getPassenger() != null) {
                return Collections.singletonList(vehicle.getPassenger().getEntityId());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    @Nullable
    public Object getVehicle(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).getVehicle();
    }

    @Override
    public int getEntityId(@NotNull Object entity) {
        return ((Entity)entity).getEntityId();
    }

    @Override
    @NotNull
    public String getEntityType(@NotNull Object entity) {
        return ((Entity) entity).getType().toString().toLowerCase();
    }

    @Override
    public boolean isSneaking(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).isSneaking();
    }

    @Override
    public boolean isSwimming(@NotNull TabPlayer player) {
        Player p = (Player) player.getPlayer();
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 14 && p.getPose() == Pose.SWIMMING) return true;
        return TAB.getInstance().getServerVersion().getMinorVersion() == 13 && p.isSwimming();
    }

    @Override
    public boolean isGliding(@NotNull TabPlayer player) {
        return TAB.getInstance().getServerVersion().getMinorVersion() >= 9 && ((Player)player.getPlayer()).isGliding();
    }

    @Override
    public boolean isSleeping(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).isSleeping();
    }

    @Override
    @NotNull
    public Object getArmorStandType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public double getX(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).getLocation().getX();
    }

    @Override
    public double getY(@NotNull Object entity) {
        return ((Entity)entity).getLocation().getY();
    }

    @Override
    public double getZ(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).getLocation().getZ();
    }

    @Override
    @NotNull
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        DataWatcher datawatcher = new DataWatcher();
        datawatcher.setEntityFlags(flags);
        datawatcher.setCustomName(displayName, viewer.getVersion());
        datawatcher.setCustomNameVisible(nameVisible);
        datawatcher.setArmorStandFlags((byte)16);
        return datawatcher;
    }

    @Override
    public void runInEntityScheduler(@NotNull Object entity, @NotNull Runnable task) {
        ((BukkitPlatform)TAB.getInstance().getPlatform()).runEntityTask((Entity) entity, task);
    }

    @Override
    public boolean isDead(@NotNull TabPlayer player) {
        return ((BukkitTabPlayer)player).getPlayer().isDead();
    }
}