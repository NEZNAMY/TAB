package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * EntityView implementation for Bukkit using packets.
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class PacketEntityView implements EntityView {

    /** PacketPlayOutEntityDestroy */
    private static Class<?> EntityDestroyClass;
    private static Constructor<?> newEntityDestroy;
    private static Field EntityDestroy_Entities;

    /** PacketPlayOutEntityMetadata */
    private static Constructor<?> newEntityMetadata;
    
    /** PacketPlayOutEntityTeleport */
    private static Class<?> EntityTeleportClass;
    private static Constructor<?> newEntityTeleport;
    private static Field EntityTeleport_EntityId;
    private static Field EntityTeleport_X;
    private static Field EntityTeleport_Y;
    private static Field EntityTeleport_Z;

    /** PacketPlayOutSpawnEntityLiving */
    private static Class<?> SpawnEntityClass;
    private static Constructor<?> newSpawnEntity;

    /** 1.17+ */
    private static Object Vec3D_Empty;
    private static Object EntityTypes_ARMOR_STAND;

    /** 1.16.5- */
    private static Field SpawnEntity_EntityId;
    private static Field SpawnEntity_EntityType;
    private static Field SpawnEntity_UUID;
    private static Field SpawnEntity_X;
    private static Field SpawnEntity_Y;
    private static Field SpawnEntity_Z;
    private static Field SpawnEntity_DataWatcher;
    private static final EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);

    /** Other entity packets */
    private static Class<?> PacketPlayOutEntity;
    private static Field PacketPlayOutEntity_ENTITYID;
    private static Class<?> PacketPlayOutEntityLook;
    private static Class<?> PacketPlayOutNamedEntitySpawn;
    private static Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    private static Class<?> ClientboundBundlePacket;
    private static Constructor<?> newClientboundBundlePacket;
    private static Field ClientboundBundlePacket_packets;

    private static Object dummyEntity;

    private static PacketSender packetSender;

    @Getter
    private static boolean available;

    /** Player this view belongs to */
    private final BukkitTabPlayer player;

    /**
     * Loads all required classes and fields and throws Exception if something went wrong
     *
     * @throws  ReflectiveOperationException
     *          If something fails
     */
    public static void load() throws ReflectiveOperationException {
        loadEntityMetadata();
        loadEntityDestroy();
        loadEntityTeleport();
        loadEntityMove();
        loadEntitySpawn();
        if (BukkitReflection.is1_19_4Plus()) {
            ClientboundBundlePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundBundlePacket");
            newClientboundBundlePacket = ClientboundBundlePacket.getConstructor(Iterable.class);
            ClientboundBundlePacket_packets = ReflectionUtils.getOnlyField(ClientboundBundlePacket.getSuperclass(), Iterable.class);
        }
        packetSender = new PacketSender();
        available = true;
    }

    private static void loadEntityMetadata() throws ReflectiveOperationException {
        // Class
        Class<?> entityMetadataClass = BukkitReflection.getClass("network.protocol.game.ClientboundSetEntityDataPacket",
                "network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");

        // Constructor
        if (BukkitReflection.is1_19_3Plus()) {
            newEntityMetadata = entityMetadataClass.getConstructor(int.class, List.class);
        } else {
            newEntityMetadata = entityMetadataClass.getConstructor(int.class, DataWatcher.DataWatcher, boolean.class);
        }
    }

    private static void loadEntityDestroy() throws ReflectiveOperationException {
        // Class
        EntityDestroyClass = BukkitReflection.getClass("network.protocol.game.ClientboundRemoveEntitiesPacket",
                "network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy", "Packet29DestroyEntity");

        // Constructor
        try {
            newEntityDestroy = EntityDestroyClass.getConstructor(int[].class);
        } catch (NoSuchMethodException e) {
            //1.17.0
            newEntityDestroy = EntityDestroyClass.getConstructor(int.class);
        }

        // Field
        EntityDestroy_Entities = ReflectionUtils.getOnlyField(EntityDestroyClass);
    }

    private static void loadEntityTeleport() throws ReflectiveOperationException {
        // Class
        EntityTeleportClass = BukkitReflection.getClass("network.protocol.game.ClientboundTeleportEntityPacket",
                "network.protocol.game.PacketPlayOutEntityTeleport", "PacketPlayOutEntityTeleport", "Packet34EntityTeleport");

        // Constructor
        if (BukkitReflection.getMinorVersion() >= 17) {
            newEntityTeleport = EntityTeleportClass.getConstructor(BukkitReflection.getClass("world.entity.Entity"));
        } else {
            newEntityTeleport = EntityTeleportClass.getConstructor();
        }

        // Fields
        EntityTeleport_EntityId = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(0);
        if (BukkitReflection.getMinorVersion() >= 9) {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(0);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(1);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(2);
        } else {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(1);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(2);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(3);
        }

        // Dummy armor stand for constructor
        if (BukkitReflection.getMinorVersion() >= 17) {
            Class<?> world = BukkitReflection.getClass("world.level.Level", "world.level.World", "World");
            Class<?> entityArmorStand = BukkitReflection.getClass("world.entity.decoration.ArmorStand",
                    "world.entity.decoration.EntityArmorStand", "EntityArmorStand");
            Constructor<?> newEntityArmorStand = entityArmorStand.getConstructor(world, double.class, double.class, double.class);
            Method World_getHandle = BukkitReflection.getBukkitClass("CraftWorld").getMethod("getHandle");
            dummyEntity = newEntityArmorStand.newInstance(World_getHandle.invoke(Bukkit.getWorlds().get(0)), 0, 0, 0);
        }
    }

    private static void loadEntityMove() throws ReflectiveOperationException {
        // Classes
        PacketPlayOutEntity = BukkitReflection.getClass("network.protocol.game.ClientboundMoveEntityPacket",
                "network.protocol.game.PacketPlayOutEntity", "PacketPlayOutEntity", "Packet30Entity");
        PacketPlayOutEntityLook = BukkitReflection.getClass("network.protocol.game.ClientboundMoveEntityPacket$Rot",
                "network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntity$PacketPlayOutEntityLook",
                "PacketPlayOutEntityLook", "Packet32EntityLook");

        // Field
        PacketPlayOutEntity_ENTITYID = ReflectionUtils.getFields(PacketPlayOutEntity, int.class).get(0);
    }

    /**
     * Loads required NMS classes, fields and methods. If it fails, exception is thrown.
     *
     * @throws  ReflectiveOperationException
     *          If anything fails
     */
    private static void loadEntitySpawn() throws ReflectiveOperationException {
        SpawnEntityClass = BukkitReflection.getClass("network.protocol.game.ClientboundAddEntityPacket",
                "network.protocol.game.PacketPlayOutSpawnEntity", "PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
        SpawnEntity_EntityId = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(0);
        if (BukkitReflection.getMinorVersion() >= 17) {
            Class<?> Vec3D = BukkitReflection.getClass("world.phys.Vec3", "world.phys.Vec3D");
            Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
            Class<?> EntityTypes = BukkitReflection.getClass("world.entity.EntityType", "world.entity.EntityTypes");
            if (BukkitReflection.getMinorVersion() >= 19) {
                EntityTypes_ARMOR_STAND = ReflectionUtils.getField(EntityTypes, "ARMOR_STAND", "d").get(null);
            } else {
                EntityTypes_ARMOR_STAND = ReflectionUtils.getField(EntityTypes, "ARMOR_STAND", "c", "f_20529_").get(null); // Mohist 1.18.2
            }
            if (BukkitReflection.getMinorVersion() >= 19) {
                newSpawnEntity = SpawnEntityClass.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D, double.class);
            } else {
                newSpawnEntity = SpawnEntityClass.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D);
            }
        } else {
            newSpawnEntity = SpawnEntityClass.getConstructor();
            if (BukkitReflection.getMinorVersion() >= 9) {
                SpawnEntity_UUID = ReflectionUtils.getOnlyField(SpawnEntityClass, UUID.class);
                SpawnEntity_X = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(0);
                SpawnEntity_Y = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(1);
                SpawnEntity_Z = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(2);
            } else {
                SpawnEntity_X = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(2);
                SpawnEntity_Y = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(3);
                SpawnEntity_Z = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(4);
            }
            SpawnEntity_EntityType = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(1);
            if (BukkitReflection.getMinorVersion() <= 14) {
                SpawnEntity_DataWatcher = ReflectionUtils.getOnlyField(SpawnEntityClass, DataWatcher.DataWatcher);
            }
        }
        if (BukkitReflection.getMinorVersion() >= 13) {
            entityIds.put(EntityType.ARMOR_STAND, 1);
        } else {
            entityIds.put(EntityType.WITHER, 64);
            if (BukkitReflection.getMinorVersion() >= 8) {
                entityIds.put(EntityType.ARMOR_STAND, 30);
            }
        }
        if (!BukkitReflection.is1_20_2Plus()) {
            PacketPlayOutNamedEntitySpawn = BukkitReflection.getClass("network.protocol.game.ClientboundAddPlayerPacket",
                    "network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
            PacketPlayOutNamedEntitySpawn_ENTITYID = ReflectionUtils.getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        }
    }

    private static int floor(double paramDouble) {
        int i = (int) (paramDouble*32);
        return paramDouble < i ? i - 1 : i;
    }

    @SneakyThrows
    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location l, @NotNull EntityData data) {
        int minorVersion = BukkitReflection.getMinorVersion();
        if (minorVersion >= 19) {
            List<Object> packets = new ArrayList<>();
            packets.add(newSpawnEntity.newInstance(entityId, id, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty, 0.0d));
            packets.add(createEntityMetadata(entityId, data));
            if (BukkitReflection.is1_19_4Plus()) {
                // Send bundle packet to avoid rare flicker when frame is rendered between packets
                packetSender.sendPacket(player.getPlayer(), newClientboundBundlePacket.newInstance(packets));
            } else {
                for (Object packet : packets) {
                    packetSender.sendPacket(player.getPlayer(), packet);
                }
            }
            return;
        }
        if (minorVersion >= 17) {
            packetSender.sendPacket(player.getPlayer(), newSpawnEntity.newInstance(entityId, id, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty));
        } else {
            Object nmsPacket = newSpawnEntity.newInstance();
            SpawnEntity_EntityId.set(nmsPacket, entityId);
            if (minorVersion <= 14) {
                SpawnEntity_DataWatcher.set(nmsPacket, data.build());
            }
            if (minorVersion >= 9) {
                SpawnEntity_UUID.set(nmsPacket, id);
                SpawnEntity_X.set(nmsPacket, l.getX());
                SpawnEntity_Y.set(nmsPacket, l.getY());
                SpawnEntity_Z.set(nmsPacket, l.getZ());
            } else {
                SpawnEntity_X.set(nmsPacket, floor(l.getX()));
                SpawnEntity_Y.set(nmsPacket, floor(l.getY()));
                SpawnEntity_Z.set(nmsPacket, floor(l.getZ()));
            }
            SpawnEntity_EntityType.set(nmsPacket, entityIds.get((EntityType) entityType));
            packetSender.sendPacket(player.getPlayer(), nmsPacket);
        }
        if (BukkitReflection.getMinorVersion() >= 15) {
            updateEntityMetadata(entityId, data);
        }
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        packetSender.sendPacket(player.getPlayer(), createEntityMetadata(entityId, data));
    }

    @SneakyThrows
    private Object createEntityMetadata(int entityId, @NotNull EntityData data) {
        if (newEntityMetadata.getParameterCount() == 2) {
            //1.19.3+
            return newEntityMetadata.newInstance(entityId, DataWatcher.DataWatcher_packDirty.invoke(data.build()));
        } else {
            return newEntityMetadata.newInstance(entityId, data.build(), true);
        }
    }

    @SneakyThrows
    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        Object nmsPacket;
        if (BukkitReflection.getMinorVersion() >= 17) {
            nmsPacket = newEntityTeleport.newInstance(dummyEntity);
        } else {
            nmsPacket = newEntityTeleport.newInstance();
        }
        EntityTeleport_EntityId.set(nmsPacket, entityId);
        if (BukkitReflection.getMinorVersion() >= 9) {
            EntityTeleport_X.set(nmsPacket, location.getX());
            EntityTeleport_Y.set(nmsPacket, location.getY());
            EntityTeleport_Z.set(nmsPacket, location.getZ());
        } else {
            EntityTeleport_X.set(nmsPacket, floor(location.getX()));
            EntityTeleport_Y.set(nmsPacket, floor(location.getY()));
            EntityTeleport_Z.set(nmsPacket, floor(location.getZ()));
        }
        packetSender.sendPacket(player.getPlayer(), nmsPacket);
    }

    @SneakyThrows
    @Override
    public void destroyEntities(int... entities) {
        if (newEntityDestroy.getParameterTypes()[0] != int.class) {
            packetSender.sendPacket(player.getPlayer(), newEntityDestroy.newInstance(new Object[]{entities}));
        } else {
            //1.17.0 Mojank
            for (int entity : entities) {
                packetSender.sendPacket(player.getPlayer(), newEntityDestroy.newInstance(entity));
            }
        }
    }

    @Override
    public boolean isDestroyPacket(@NotNull Object packet) {
        return EntityDestroyClass.isInstance(packet);
    }

    @Override
    public boolean isTeleportPacket(@NotNull Object packet) {
        return EntityTeleportClass.isInstance(packet);
    }

    @Override
    public boolean isNamedEntitySpawnPacket(@NotNull Object packet) {
        if (BukkitReflection.is1_20_2Plus()) {
            return SpawnEntityClass.isInstance(packet);
        } else {
            return PacketPlayOutNamedEntitySpawn.isInstance(packet);
        }
    }

    @Override
    public boolean isMovePacket(@NotNull Object packet) {
        return PacketPlayOutEntity.isInstance(packet);
    }

    @Override
    public boolean isLookPacket(@NotNull Object packet) {
        return PacketPlayOutEntityLook.isInstance(packet);
    }

    @Override
    @SneakyThrows
    public int getTeleportEntityId(@NotNull Object teleportPacket) {
        return EntityTeleport_EntityId.getInt(teleportPacket);
    }

    @Override
    @SneakyThrows
    public int getMoveEntityId(@NotNull Object movePacket) {
        return PacketPlayOutEntity_ENTITYID.getInt(movePacket);
    }

    @Override
    @SneakyThrows
    public int getSpawnedPlayer(@NotNull Object playerSpawnPacket) {
        if (BukkitReflection.is1_20_2Plus()) {
            return SpawnEntity_EntityId.getInt(playerSpawnPacket);
        } else {
            return PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(playerSpawnPacket);
        }
    }

    @Override
    @SneakyThrows
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        Object entities = EntityDestroy_Entities.get(destroyPacket);
        if (BukkitReflection.getMinorVersion() >= 17) {
            if (entities instanceof List) {
                return ((List<Integer>)entities).stream().mapToInt(i -> i).toArray();
            } else {
                //1.17.0
                return new int[]{(int) entities};
            }
        } else {
            return (int[]) entities;
        }
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return BukkitReflection.is1_19_4Plus() && ClientboundBundlePacket.isInstance(packet);
    }

    @Override
    @SneakyThrows
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return (Iterable<Object>) ClientboundBundlePacket_packets.get(bundlePacket);
    }
}
