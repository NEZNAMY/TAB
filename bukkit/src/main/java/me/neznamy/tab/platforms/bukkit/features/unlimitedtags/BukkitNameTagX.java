package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStandManager;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The core class for unlimited NameTag mode on Bukkit
 */
public class BukkitNameTagX extends BackendNameTagX {

    /** Reference to NMS storage for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    /** Bukkit event listener */
    private final EventListener eventListener = new EventListener(this);

    /**
     * Constructs new instance with given parameter, loads config options, registers events
     * and registers sub-features.
     *
     * @param   plugin
     *          plugin instance
     */
    public BukkitNameTagX(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(eventListener, plugin);
    }

    @Override
    public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
        if (sender.getVersion().getMinorVersion() == 8 && nms.PacketPlayInUseEntity.isInstance(packet)) {
            int entityId = nms.PacketPlayInUseEntity_ENTITY.getInt(packet);
            TabPlayer attacked = null;
            for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
                if (all.isLoaded() && getArmorStandManager(all).hasArmorStandWithID(entityId)) {
                    attacked = all;
                    break;
                }
            }
            if (attacked != null && attacked != sender) {
                nms.setField(packet, nms.PacketPlayInUseEntity_ENTITY, ((Player) attacked.getPlayer()).getEntityId());
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
        if (receiver.getVersion().getMinorVersion() < 8) return;
        if (!receiver.isLoaded() || isDisabledPlayer(receiver) || getDisabledUnlimitedPlayers().contains(receiver)) return;
        if (nms.PacketPlayOutEntity.isInstance(packet) && !nms.PacketPlayOutEntityLook.isInstance(packet)) { //ignoring head rotation only packets
            packetListener.onEntityMove(receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutEntityTeleport.isInstance(packet)) {
            packetListener.onEntityMove(receiver, nms.PacketPlayOutEntityTeleport_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
            packetListener.onEntitySpawn(receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutEntityDestroy.isInstance(packet)) {
            if (nms.getMinorVersion() >= 17) {
                Object entities = nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet);
                if (entities instanceof List) {
                    packetListener.onEntityDestroy(receiver, (List<Integer>) entities);
                } else {
                    //1.17.0
                    packetListener.onEntityDestroy(receiver, (int) entities);
                }
            } else {
                packetListener.onEntityDestroy(receiver, (int[]) nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet));
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
    public int getEntityId(TabPlayer player) {
        return ((Player) player.getPlayer()).getEntityId();
    }

    @Override
    public String getEntityType(Object entity) {
        return ((Entity)entity).getType().toString();
    }

    @Override
    public ArmorStand createArmorStand(BackendArmorStandManager feature, TabPlayer owner, String lineName, double yOffset, boolean staticOffset) {
        return new BukkitArmorStand(this, feature, owner, lineName, yOffset, staticOffset);
    }
}