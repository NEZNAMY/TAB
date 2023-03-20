package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.feature.PacketSendListener;
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
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The core class for unlimited NameTag mode on Bukkit
 */
@RequiredArgsConstructor
public class BukkitNameTagX extends BackendNameTagX implements PacketSendListener {

    /** Reference to NMS storage for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    /** Bukkit event listener */
    private final EventListener eventListener = new EventListener(this);

    /** Plugin reference */
    private final JavaPlugin plugin;

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(eventListener, plugin);
        super.load();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
        if (receiver.getVersion().getMinorVersion() < 8) return;
        if (!receiver.isLoaded() || isDisabledPlayer(receiver) || getDisabledUnlimitedPlayers().contains(receiver)) return;
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
    public double getDistance(TabPlayer player1, TabPlayer player2) {
        Location loc1 = ((Player) player1.getPlayer()).getLocation();
        Location loc2 = ((Player) player2.getPlayer()).getLocation();
        return Math.sqrt(Math.pow(loc1.getX()-loc2.getX(), 2) + Math.pow(loc1.getZ()-loc2.getZ(), 2));
    }

    @Override
    public boolean areInSameWorld(TabPlayer player1, TabPlayer player2) {
        return ((Player) player1.getPlayer()).getWorld() == ((Player) player1.getPlayer()).getWorld();
    }

    @Override
    public boolean canSee(TabPlayer viewer, TabPlayer target) {
        return ((Player)viewer.getPlayer()).canSee((Player)target.getPlayer());
    }

    @Override
    public void unregisterListener() {
        HandlerList.unregisterAll(eventListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Integer> getPassengers(Object entity) {
        Entity vehicle = (Entity) entity;
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 11) {
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
    public void registerVehiclePlaceholder() {
        TabAPI.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.VEHICLE, 100, p -> {
            Entity v = ((Player)p.getPlayer()).getVehicle();
            //There's a bug in Bukkit 1.19.3 throwing NPE on .toString(), use default toString implementation
            return v == null ? "" : v.getClass().getName() + "@" + Integer.toHexString(v.hashCode());
        });
    }

    @Override
    public Object getVehicle(TabPlayer player) {
        return ((Player)player.getPlayer()).getVehicle();
    }

    @Override
    public int getEntityId(Object entity) {
        return ((Entity)entity).getEntityId();
    }

    @Override
    public String getEntityType(Object entity) {
        return ((Entity) entity).getType().toString().toLowerCase();
    }

    @Override
    public boolean isSneaking(TabPlayer player) {
        return ((Player)player.getPlayer()).isSneaking();
    }

    @Override
    public boolean isSwimming(TabPlayer player) {
        Player p = (Player) player.getPlayer();
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 14 && p.getPose() == Pose.SWIMMING) return true;
        return TabAPI.getInstance().getServerVersion().getMinorVersion() == 13 && p.isSwimming();
    }

    @Override
    public boolean isGliding(TabPlayer player) {
        return TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9 && ((Player)player.getPlayer()).isGliding();
    }

    @Override
    public boolean isSleeping(TabPlayer player) {
        return ((Player)player.getPlayer()).isSleeping();
    }

    @Override
    public Object getArmorStandType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public double getX(TabPlayer player) {
        return ((Player)player.getPlayer()).getLocation().getX();
    }

    @Override
    public double getY(Object entity) {
        return ((Entity)entity).getLocation().getY();
    }

    @Override
    public double getZ(TabPlayer player) {
        return ((Player)player.getPlayer()).getLocation().getZ();
    }

    @Override
    public EntityData createDataWatcher(TabPlayer viewer, byte flags, String displayName, boolean nameVisible) {
        DataWatcher datawatcher = new DataWatcher();
        datawatcher.getHelper().setEntityFlags(flags);
        datawatcher.getHelper().setCustomName(displayName, viewer.getVersion());
        datawatcher.getHelper().setCustomNameVisible(nameVisible);
        datawatcher.getHelper().setArmorStandFlags((byte)16);
        return datawatcher;
    }
}