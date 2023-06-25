package me.neznamy.tab.platforms.bukkit.features;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.PacketSendListener;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutEntityDestroyStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutEntityTeleportStorage;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
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
public class BukkitNameTagX extends BackendNameTagX implements Listener, PacketSendListener {

    /** Reference to NMS storage for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    public BukkitNameTagX(@NotNull JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent e) {
        TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
        if (p == null || isPlayerDisabled(p)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PLAYER_SNEAK,
                () -> getArmorStandManager(p).sneak(e.isSneaking()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        TabPlayer respawned = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
        if (respawned == null || isPlayerDisabled(respawned)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PLAYER_RESPAWN,
                () -> getArmorStandManager(respawned).teleport());
    }

    @SuppressWarnings("unchecked")
    @Override
    @SneakyThrows
    public void onPacketSend(@NotNull TabPlayer receiver, @NotNull Object packet) {
        if (receiver.getVersion().getMinorVersion() < 8) return;
        if (!receiver.isLoaded() || getDisableChecker().isDisabledPlayer(receiver) || getUnlimitedDisableChecker().isDisabledPlayer(receiver)) return;
        if (nms.PacketPlayOutEntity.isInstance(packet) && !nms.PacketPlayOutEntityLook.isInstance(packet)) { //ignoring head rotation only packets
            packetListener.onEntityMove((BackendTabPlayer) receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
        } else if (PacketPlayOutEntityTeleportStorage.CLASS.isInstance(packet)) {
            packetListener.onEntityMove((BackendTabPlayer) receiver, PacketPlayOutEntityTeleportStorage.ENTITY_ID.getInt(packet));
        } else if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
            packetListener.onEntitySpawn((BackendTabPlayer) receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
        } else if (PacketPlayOutEntityDestroyStorage.CLASS.isInstance(packet)) {
            if (nms.getMinorVersion() >= 17) {
                Object entities = PacketPlayOutEntityDestroyStorage.ENTITIES.get(packet);
                if (entities instanceof List) {
                    packetListener.onEntityDestroy((BackendTabPlayer) receiver, (List<Integer>) entities);
                } else {
                    //1.17.0
                    packetListener.onEntityDestroy((BackendTabPlayer) receiver, (int) entities);
                }
            } else {
                packetListener.onEntityDestroy((BackendTabPlayer) receiver, (int[]) PacketPlayOutEntityDestroyStorage.ENTITIES.get(packet));
            }
        }
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
    public @NotNull List<Integer> getPassengers(@NotNull Object entity) {
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
    public @Nullable Object getVehicle(@NotNull TabPlayer player) {
        return ((Player)player.getPlayer()).getVehicle();
    }

    @Override
    public int getEntityId(@NotNull Object entity) {
        return ((Entity)entity).getEntityId();
    }

    @Override
    public @NotNull String getEntityType(@NotNull Object entity) {
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
    public @NotNull Object getArmorStandType() {
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
    public EntityData createDataWatcher(@NotNull TabPlayer viewer, byte flags, @NotNull String displayName, boolean nameVisible) {
        DataWatcher datawatcher = new DataWatcher();
        datawatcher.getHelper().setEntityFlags(flags);
        datawatcher.getHelper().setCustomName(displayName, viewer.getVersion());
        datawatcher.getHelper().setCustomNameVisible(nameVisible);
        datawatcher.getHelper().setArmorStandFlags((byte)16);
        return datawatcher;
    }

    @Override
    public void runInEntityScheduler(Object entity, Runnable task) {
        ((BukkitPlatform)TAB.getInstance().getPlatform()).runEntityTask((Entity) entity, task);
    }
}