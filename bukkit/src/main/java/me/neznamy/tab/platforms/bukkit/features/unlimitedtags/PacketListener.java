package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The packet listening part for securing proper functionality of armor stands.
 * Bukkit events are too unreliable and delayed/ahead which causes de-sync
 * if trying to listen to move event.
 * For entering/leaving tracking range there are no events and
 * periodic / move-triggered distance checks would cause high CPU usage.
 */
public class PacketListener extends TabFeature {

    /** Reference to the main feature */
    private final BukkitNameTagX nameTagX;

    /** A player map by entity id, used for better performance */
    private final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();
    
    /** Reference to NMS storage for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    /**
     * Constructs new instance with given parameter
     *
     * @param   nameTagX
     *          main feature
     */
    public PacketListener(BukkitNameTagX nameTagX) {
        super(nameTagX.getFeatureName(), null);
        this.nameTagX = nameTagX;
    }

    @Override
    public void load() {
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            entityIdMap.put(((Player) all.getPlayer()).getEntityId(), all);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        entityIdMap.put(((Player) connectedPlayer.getPlayer()).getEntityId(), connectedPlayer);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        entityIdMap.remove(((Player) disconnectedPlayer.getPlayer()).getEntityId());
    }

    @Override
    public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
        if (sender.getVersion().getMinorVersion() == 8 && nms.PacketPlayInUseEntity.isInstance(packet)) {
            int entityId = nms.PacketPlayInUseEntity_ENTITY.getInt(packet);
            TabPlayer attacked = null;
            for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
                if (all.isLoaded() && nameTagX.getArmorStandManager(all).hasArmorStandWithID(entityId)) {
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
        if (!receiver.isLoaded() || nameTagX.isDisabledPlayer(receiver) || nameTagX.getDisabledUnlimitedPlayers().contains(receiver)) return;
        if (nms.PacketPlayOutEntity.isInstance(packet) && !nms.PacketPlayOutEntityLook.isInstance(packet)) { //ignoring head rotation only packets
            onEntityMove(receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutEntityTeleport.isInstance(packet)) {
            onEntityMove(receiver, nms.PacketPlayOutEntityTeleport_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
            onEntitySpawn(receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
        } else if (nms.PacketPlayOutEntityDestroy.isInstance(packet)) {
            if (nms.getMinorVersion() >= 17) {
                Object entities = nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet);
                if (entities instanceof List) {
                    onEntityDestroy(receiver, (List<Integer>) entities);
                } else {
                    //1.17.0
                    onEntityDestroy(receiver, (int) entities);
                }
            } else {
                onEntityDestroy(receiver, (int[]) nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet));
            }
        }
    }

    /**
     * Processes entity move packet. If entity ID belongs to a player,
     * armor stands of that player are teleported to player who received the packet.
     * If it belongs to a vehicle carrying a player, that player's armor stands are
     * teleported as well.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          entity that moved
     */
    private void onEntityMove(TabPlayer receiver, int entityId) {
        TabPlayer pl = entityIdMap.get(entityId);
        List<Entity> vehicleList;
        if (pl != null) {
            //player moved
            if (nameTagX.isPlayerDisabled(pl) || !pl.isLoaded()) return;
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE,
                    () -> nameTagX.getArmorStandManager(pl).teleport(receiver));
        } else if ((vehicleList = nameTagX.getVehicleManager().getVehicles().get(entityId)) != null){
            //a vehicle carrying something moved
            for (Entity entity : vehicleList) {
                TabPlayer passenger = entityIdMap.get(entity.getEntityId());
                if (passenger != null && nameTagX.getArmorStandManager(passenger) != null) {
                    TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER,
                            () -> nameTagX.getArmorStandManager(passenger).teleport(receiver));
                }
            }
        }
    }

    /**
     * Processes named entity spawn packet and spawns armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          spawned entity
     */
    private void onEntitySpawn(TabPlayer receiver, int entityId) {
        TabPlayer spawnedPlayer = entityIdMap.get(entityId);
        if (spawnedPlayer != null && spawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(spawnedPlayer)) {
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN,
                    () -> nameTagX.getArmorStandManager(spawnedPlayer).spawn(receiver));
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entities
     *          de-spawned entities
     */
    private void onEntityDestroy(TabPlayer receiver, List<Integer> entities) {
        for (int entity : entities) {
            onEntityDestroy(receiver, entity);
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entities
     *          de-spawned entities
     */
    private void onEntityDestroy(TabPlayer receiver, int... entities) {
        for (int entity : entities) {
            onEntityDestroy(receiver, entity);
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entity
     *          de-spawned entity
     */
    private void onEntityDestroy(TabPlayer receiver, int entity) {
        TabPlayer deSpawnedPlayer = entityIdMap.get(entity);
        if (deSpawnedPlayer != null && deSpawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(deSpawnedPlayer)) {
            BukkitArmorStandManager asm = nameTagX.getArmorStandManager(deSpawnedPlayer);
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY,
                    () -> asm.destroy(receiver));
        }
    }
}