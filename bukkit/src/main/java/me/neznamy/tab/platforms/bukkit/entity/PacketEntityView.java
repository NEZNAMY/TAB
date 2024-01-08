package me.neznamy.tab.platforms.bukkit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * EntityView implementation for Bukkit using packets.
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class PacketEntityView implements EntityView {

    /** Version with entity metadata being split from spawn packet */
    private static final int SPLIT_METADATA_VERSION = 15;

    private static PacketSender packetSender;

    /** PacketPlayOutEntityDestroy */
    private static Class<?> EntityDestroyClass;
    private static BiConsumerWithException<BukkitTabPlayer, int[]> destroyEntities;
    private static FunctionWithException<Object, int[]> getDestroyedEntities;

    /** PacketPlayOutEntityMetadata */
    private static BiFunctionWithException<Integer, EntityData, Object> newEntityMetadata;
    
    /** PacketPlayOutEntityTeleport */
    private static BiFunctionWithException<Integer, Location, Object> newEntityTeleport;
    private static Class<?> EntityTeleportClass;
    private static Field EntityTeleport_EntityId;

    /** PacketPlayOutSpawnEntityLiving */
    private static QuintFunction<Integer, UUID, Object, Location, EntityData, Object> newSpawnEntity;

    /** Other entity packets */
    private static Class<?> PacketPlayOutEntity;
    private static Field PacketPlayOutEntity_ENTITYID;
    private static Class<?> PacketPlayOutEntityLook;
    private static Class<?> PacketPlayOutNamedEntitySpawn;
    private static Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    private static Constructor<?> newClientboundBundlePacket;
    private static Field ClientboundBundlePacket_packets;
    private static Function<Object, Boolean> isBundlePacket = packet -> false;
    private static BiConsumerWithException<BukkitTabPlayer, Iterable<?>> sendAsBundle = (player, packets) -> {
        for (Object packet : packets) {
            packetSender.sendPacket(player.getPlayer(), packet);
        }
    };

    @Getter
    private static boolean available;

    /** Player this view belongs to */
    private final BukkitTabPlayer player;

    /**
     * Loads all required classes and fields and marks class as available.
     * If something goes wrong, a warning is printed and class is not marked as available.
     */
    public static void tryLoad() {
        try {
            DataWatcher.load();
            loadEntityMetadata();
            loadEntityDestroy();
            loadEntityTeleport();
            loadEntityMove();
            loadEntitySpawn();
            if (BukkitReflection.is1_19_4Plus()) {
                Class<?> ClientboundBundlePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundBundlePacket");
                newClientboundBundlePacket = ClientboundBundlePacket.getConstructor(Iterable.class);
                ClientboundBundlePacket_packets = ReflectionUtils.getOnlyField(ClientboundBundlePacket.getSuperclass(), Iterable.class);
                isBundlePacket = ClientboundBundlePacket::isInstance;
                sendAsBundle = (player, packets) -> packetSender.sendPacket(player.getPlayer(), newClientboundBundlePacket.newInstance(packets));
            }
            packetSender = new PacketSender();
            available = true;
        } catch (ReflectiveOperationException e) {
            List<String> missingFeatures = new ArrayList<>();
            if (BukkitReflection.getMinorVersion() >= 8) {
                missingFeatures.add("Unlimited nametag mode not working and being replaced with regular nametags");
            }
            if (BukkitReflection.getMinorVersion() <= 8) {
                missingFeatures.add("BossBar feature not working");
            }
            BukkitUtils.compatibilityError(e, "sending entity packets", null, missingFeatures.toArray(new String[0]));
        }
    }

    @SuppressWarnings("JavaReflectionInvocation")
    private static void loadEntityMetadata() throws ReflectiveOperationException {
        Class<?> entityMetadataClass = BukkitReflection.getClass("network.protocol.game.ClientboundSetEntityDataPacket",
                "network.protocol.game.PacketPlayOutEntityMetadata", "PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
        if (BukkitReflection.is1_19_3Plus()) {
            Constructor<?> constructor = entityMetadataClass.getConstructor(int.class, List.class);
            newEntityMetadata = (entityId, data) -> constructor.newInstance(entityId, DataWatcher.DataWatcher_packDirty.invoke(data.build()));
        } else {
            Constructor<?> constructor = entityMetadataClass.getConstructor(int.class, DataWatcher.DataWatcher, boolean.class);
            newEntityMetadata = (entityId, data) -> constructor.newInstance(entityId, data.build(), true);
        }
    }

    private static void loadEntityDestroy() throws ReflectiveOperationException {
        EntityDestroyClass = BukkitReflection.getClass("network.protocol.game.ClientboundRemoveEntitiesPacket",
                "network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
        Field entities = ReflectionUtils.getOnlyField(EntityDestroyClass);
        try {
            Constructor<?> constructor = EntityDestroyClass.getConstructor(int[].class);
            destroyEntities = (player, ids) -> packetSender.sendPacket(player.getPlayer(), constructor.newInstance(new Object[]{ids}));
            if (BukkitReflection.getMinorVersion() >= 17) {
                getDestroyedEntities = packet -> ((List<Integer>) entities.get(packet)).stream().mapToInt(i -> i).toArray();
            } else {
                getDestroyedEntities = packet -> (int[]) entities.get(packet);
            }
        } catch (NoSuchMethodException e) {
            //1.17.0 Mojank
            Constructor<?> constructor = EntityDestroyClass.getConstructor(int.class);
            destroyEntities = (player, ids) -> { for (int entity : ids) packetSender.sendPacket(player.getPlayer(), constructor.newInstance(entity));};
            getDestroyedEntities = packet -> new int[]{entities.getInt(packet)};
        }
    }

    private static void loadEntityTeleport() throws ReflectiveOperationException {
        EntityTeleportClass = BukkitReflection.getClass("network.protocol.game.ClientboundTeleportEntityPacket",
                "network.protocol.game.PacketPlayOutEntityTeleport", "PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
        Callable<Object> newPacket;
        if (BukkitReflection.getMinorVersion() >= 17) {
            // Dummy armor stand for constructor
            Class<?> world = BukkitReflection.getClass("world.level.Level", "world.level.World", "World");
            Class<?> entityArmorStand = BukkitReflection.getClass("world.entity.decoration.ArmorStand",
                    "world.entity.decoration.EntityArmorStand", "EntityArmorStand");
            Constructor<?> newEntityArmorStand = entityArmorStand.getConstructor(world, double.class, double.class, double.class);
            Method World_getHandle = BukkitReflection.getBukkitClass("CraftWorld").getMethod("getHandle");
            Object dummyEntity = newEntityArmorStand.newInstance(World_getHandle.invoke(Bukkit.getWorlds().get(0)), 0, 0, 0);

            Constructor<?> constructor = EntityTeleportClass.getConstructor(BukkitReflection.getClass("world.entity.Entity"));
            newPacket = () -> constructor.newInstance(dummyEntity);
        } else {
            Constructor<?> constructor = EntityTeleportClass.getConstructor();
            newPacket = constructor::newInstance;
        }

        EntityTeleport_EntityId = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(0);
        Field EntityTeleport_X;
        Field EntityTeleport_Y;
        Field EntityTeleport_Z;
        if (BukkitReflection.getMinorVersion() >= 9) {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(0);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(1);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(2);
        } else {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(1);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(2);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(3);
        }
        newEntityTeleport = (entityId, location) -> {
            Object packet = newPacket.call();
            EntityTeleport_EntityId.set(packet, entityId);
            EntityTeleport_X.set(packet, toPosition(location.getX()));
            EntityTeleport_Y.set(packet, toPosition(location.getY()));
            EntityTeleport_Z.set(packet, toPosition(location.getZ()));
            return packet;
        };
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
        int minorVersion = BukkitReflection.getMinorVersion();

        // Entity ids
        EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);
        if (minorVersion >= 13) {
            entityIds.put(EntityType.ARMOR_STAND, 1);
        } else {
            entityIds.put(EntityType.WITHER, 64);
            if (minorVersion >= 8) {
                entityIds.put(EntityType.ARMOR_STAND, 30);
            }
        }

        Class<?> SpawnEntityClass = BukkitReflection.getClass("network.protocol.game.ClientboundAddEntityPacket",
                "network.protocol.game.PacketPlayOutSpawnEntity", "PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
        Field SpawnEntity_EntityId = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(0);
        if (BukkitReflection.is1_20_2Plus()) {
            PacketPlayOutNamedEntitySpawn = SpawnEntityClass;
            PacketPlayOutNamedEntitySpawn_ENTITYID = SpawnEntity_EntityId;
        } else {
            PacketPlayOutNamedEntitySpawn = BukkitReflection.getClass("network.protocol.game.ClientboundAddPlayerPacket",
                    "network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
            PacketPlayOutNamedEntitySpawn_ENTITYID = ReflectionUtils.getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
        }
        Constructor<?> constructor;

        // 1.17+ constructor using Spawn entity packet
        if (minorVersion >= 17) {
            Class<?> Vec3D = BukkitReflection.getClass("world.phys.Vec3", "world.phys.Vec3D");
            Object Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
            Class<?> EntityTypes = BukkitReflection.getClass("world.entity.EntityType", "world.entity.EntityTypes");
            if (minorVersion >= 19) {
                Object EntityTypes_ARMOR_STAND = ReflectionUtils.getField(EntityTypes, "ARMOR_STAND", "d").get(null);
                constructor = SpawnEntityClass.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D, double.class);
                newSpawnEntity = (id, uuid, type, l, data) -> constructor.newInstance(id, uuid, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty, 0.0d);
            } else {
                Object EntityTypes_ARMOR_STAND = ReflectionUtils.getField(EntityTypes, "ARMOR_STAND", "c", "f_20529_").get(null); // Mohist 1.18.2
                constructor = SpawnEntityClass.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D);
                newSpawnEntity = (id, uuid, type, l, data) -> constructor.newInstance(id, uuid, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty);
            }
            return;
        }

        // 1.16- constructor using spawn entity living packet
        constructor = SpawnEntityClass.getConstructor();
        @Nullable Field SpawnEntity_UUID;
        Field SpawnEntity_X;
        Field SpawnEntity_Y;
        Field SpawnEntity_Z;
        @Nullable Field SpawnEntity_DataWatcher;
        Field SpawnEntity_EntityType = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(1);
        if (minorVersion >= 9) {
            SpawnEntity_UUID = ReflectionUtils.getOnlyField(SpawnEntityClass, UUID.class);
            SpawnEntity_X = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(0);
            SpawnEntity_Y = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(1);
            SpawnEntity_Z = ReflectionUtils.getFields(SpawnEntityClass, double.class).get(2);
        } else {
            SpawnEntity_UUID = null;
            SpawnEntity_X = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(2);
            SpawnEntity_Y = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(3);
            SpawnEntity_Z = ReflectionUtils.getFields(SpawnEntityClass, int.class).get(4);
        }
        if (minorVersion < SPLIT_METADATA_VERSION) {
            SpawnEntity_DataWatcher = ReflectionUtils.getOnlyField(SpawnEntityClass, DataWatcher.DataWatcher);
        } else {
            SpawnEntity_DataWatcher = null;
        }
        newSpawnEntity = (id, uuid, type, l, data) -> {
            Object nmsPacket = constructor.newInstance();
            SpawnEntity_EntityId.set(nmsPacket, id);
            if (minorVersion < SPLIT_METADATA_VERSION) SpawnEntity_DataWatcher.set(nmsPacket, data.build());
            if (minorVersion >= 9) SpawnEntity_UUID.set(nmsPacket, uuid);
            SpawnEntity_X.set(nmsPacket, toPosition(l.getX()));
            SpawnEntity_Y.set(nmsPacket, toPosition(l.getY()));
            SpawnEntity_Z.set(nmsPacket, toPosition(l.getZ()));
            SpawnEntity_EntityType.set(nmsPacket, entityIds.get((EntityType) type));
            return nmsPacket;
        };
    }

    private static Object toPosition(double paramDouble) {
        if (BukkitReflection.getMinorVersion() >= 9) {
            return paramDouble;
        } else {
            int i = (int) (paramDouble*32);
            return paramDouble < i ? i - 1 : i;
        }
    }

    @SneakyThrows
    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location l, @NotNull EntityData data) {
        if (BukkitReflection.getMinorVersion() >= SPLIT_METADATA_VERSION) {
            sendAsBundle.accept(player, Arrays.asList(
                    newSpawnEntity.apply(entityId, id, entityType, l, null),
                    newEntityMetadata.apply(entityId, data)
            ));
        } else {
            packetSender.sendPacket(player.getPlayer(), newSpawnEntity.apply(entityId, id, entityType, l, data));
        }
    }

    @Override
    @SneakyThrows
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        packetSender.sendPacket(player.getPlayer(), newEntityMetadata.apply(entityId, data));
    }

    @SneakyThrows
    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        packetSender.sendPacket(player.getPlayer(), newEntityTeleport.apply(entityId, location));
    }

    @SneakyThrows
    @Override
    public void destroyEntities(int... entities) {
        destroyEntities.accept(player, entities);
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
        return PacketPlayOutNamedEntitySpawn.isInstance(packet);
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
        return PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(playerSpawnPacket);
    }

    @Override
    @SneakyThrows
    public int[] getDestroyedEntities(@NotNull Object destroyPacket) {
        return getDestroyedEntities.apply(destroyPacket);
    }

    @Override
    public boolean isBundlePacket(@NotNull Object packet) {
        return isBundlePacket.apply(packet);
    }

    @Override
    @SneakyThrows
    public Iterable<Object> getPackets(@NotNull Object bundlePacket) {
        return (Iterable<Object>) ClientboundBundlePacket_packets.get(bundlePacket);
    }
}
