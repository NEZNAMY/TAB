package me.neznamy.tab.platforms.bukkit.nms;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PacketEntityView {

    /** PacketPlayOutEntityDestroy */
    public static Class<?> EntityDestroyClass;
    private static Constructor<?> newEntityDestroy;
    public static Field EntityDestroy_Entities;

    /** PacketPlayOutEntityMetadata */
    private static Constructor<?> newEntityMetadata;
    
    /** PacketPlayOutEntityTeleport */
    public static Class<?> EntityTeleportClass;
    private static Constructor<?> newEntityTeleport;
    public static Field EntityTeleport_EntityId;
    private static Field EntityTeleport_X;
    private static Field EntityTeleport_Y;
    private static Field EntityTeleport_Z;

    /** PacketPlayOutSpawnEntityLiving */
    private static Constructor<?> newSpawnEntity;

    /** 1.17+ */
    private static Class<?> Vec3D;
    private static Class<?> EntityTypes;
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
    public static Class<?> PacketPlayOutEntity;
    public static Field PacketPlayOutEntity_ENTITYID;
    public static Class<?> PacketPlayOutEntityLook;
    public static Class<?> PacketPlayOutNamedEntitySpawn;
    public static Field PacketPlayOutNamedEntitySpawn_ENTITYID;

    /** Player this view belongs to */
    private final BukkitTabPlayer player;

    /**
     * Loads all required classes and fields and throws Exception if something went wrong
     *
     * @throws  ReflectiveOperationException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws ReflectiveOperationException {
        Class<?> spawnEntityClass;
        Class<?> entityMetadataClass;
        if (nms.minorVersion >= 17) {
            EntityTeleportClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport");
            EntityDestroyClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
            entityMetadataClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata");
            Vec3D = Class.forName("net.minecraft.world.phys.Vec3D");
            EntityTypes = Class.forName("net.minecraft.world.entity.EntityTypes");
            spawnEntityClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity");
            PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity");
            PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
            PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn");
        } else {
            spawnEntityClass = nms.getLegacyClass("PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
            EntityTeleportClass = nms.getLegacyClass("PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
            entityMetadataClass = nms.getLegacyClass("PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
            EntityDestroyClass = nms.getLegacyClass("PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
            PacketPlayOutEntity = nms.getLegacyClass("PacketPlayOutEntity", "Packet30Entity");
            PacketPlayOutEntityLook = nms.getLegacyClass("PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
            PacketPlayOutNamedEntitySpawn = nms.getLegacyClass("PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
        }
        
        EntityDestroy_Entities = ReflectionUtils.getOnlyField(EntityDestroyClass);
        try {
            newEntityDestroy = EntityDestroyClass.getConstructor(int[].class);
        } catch (NoSuchMethodException e) {
            //1.17.0
            newEntityDestroy = EntityDestroyClass.getConstructor(int.class);
        }
        
        if (nms.is1_19_3Plus()) {
            newEntityMetadata = entityMetadataClass.getConstructor(int.class, List.class);
        } else {
            newEntityMetadata = entityMetadataClass.getConstructor(int.class, DataWatcher.CLASS, boolean.class);
        }

        EntityTeleport_EntityId = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(0);
        if (nms.getMinorVersion() >= 17) {
            newEntityTeleport = EntityTeleportClass.getConstructor(nms.Entity);
        } else {
            newEntityTeleport = EntityTeleportClass.getConstructor();
        }
        if (nms.getMinorVersion() >= 9) {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(0);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(1);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, double.class).get(2);
        } else {
            EntityTeleport_X = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(1);
            EntityTeleport_Y = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(2);
            EntityTeleport_Z = ReflectionUtils.getFields(EntityTeleportClass, int.class).get(3);
        }

        if (nms.minorVersion >= 13) {
            entityIds.put(EntityType.ARMOR_STAND, 1);
        } else {
            entityIds.put(EntityType.WITHER, 64);
            if (nms.minorVersion >= 8) {
                entityIds.put(EntityType.ARMOR_STAND, 30);
            }
        }

        if (nms.minorVersion >= 19) {
            EntityTypes_ARMOR_STAND = EntityTypes.getDeclaredField("d").get(null);
            newSpawnEntity = spawnEntityClass.getConstructor(int.class, java.util.UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D, double.class);
            Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
        } else if (nms.minorVersion >= 17) {
            EntityTypes_ARMOR_STAND = EntityTypes.getDeclaredField("c").get(null);
            newSpawnEntity = spawnEntityClass.getConstructor(int.class, UUID.class, double.class, double.class, double.class, float.class, float.class, EntityTypes, int.class, Vec3D);
            Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
        } else {
            newSpawnEntity = spawnEntityClass.getConstructor();
            SpawnEntity_EntityId = ReflectionUtils.getFields(spawnEntityClass, int.class).get(0);
            if (nms.minorVersion >= 9) {
                SpawnEntity_UUID = ReflectionUtils.getOnlyField(spawnEntityClass, UUID.class);
                SpawnEntity_X = ReflectionUtils.getFields(spawnEntityClass, double.class).get(0);
                SpawnEntity_Y = ReflectionUtils.getFields(spawnEntityClass, double.class).get(1);
                SpawnEntity_Z = ReflectionUtils.getFields(spawnEntityClass, double.class).get(2);
            } else {
                SpawnEntity_X = ReflectionUtils.getFields(spawnEntityClass, int.class).get(2);
                SpawnEntity_Y = ReflectionUtils.getFields(spawnEntityClass, int.class).get(3);
                SpawnEntity_Z = ReflectionUtils.getFields(spawnEntityClass, int.class).get(4);
            }
            SpawnEntity_EntityType = ReflectionUtils.getFields(spawnEntityClass, int.class).get(1);
            if (nms.minorVersion <= 14) {
                SpawnEntity_DataWatcher = ReflectionUtils.getOnlyField(spawnEntityClass, DataWatcher.CLASS);
            }
        }
        PacketPlayOutEntity_ENTITYID = ReflectionUtils.getFields(PacketPlayOutEntity, int.class).get(0);
        PacketPlayOutNamedEntitySpawn_ENTITYID = ReflectionUtils.getFields(PacketPlayOutNamedEntitySpawn, int.class).get(0);
    }

    private static int floor(double paramDouble) {
        int i = (int)paramDouble;
        return paramDouble < i ? i - 1 : i;
    }

    @SneakyThrows
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location l, @NotNull EntityData data) {
        int minorVersion = NMSStorage.getInstance().getMinorVersion();
        if (minorVersion >= 19) {
            player.sendPacket(newSpawnEntity.newInstance(entityId, id, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty, 0d));
        } else if (minorVersion >= 17) {
            player.sendPacket(newSpawnEntity.newInstance(entityId, id, l.getX(), l.getY(), l.getZ(), 0, 0, EntityTypes_ARMOR_STAND, 0, Vec3D_Empty));
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
                SpawnEntity_X.set(nmsPacket, floor(l.getX()*32));
                SpawnEntity_Y.set(nmsPacket, floor(l.getY()*32));
                SpawnEntity_Z.set(nmsPacket, floor(l.getZ()*32));
            }
            SpawnEntity_EntityType.set(nmsPacket, entityIds.get((EntityType) entityType));
            player.sendPacket(nmsPacket);
        }
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 15) {
            updateEntityMetadata(entityId, data);
        }
    }

    @SneakyThrows
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        if (newEntityMetadata.getParameterCount() == 2) {
            //1.19.3+
            player.sendPacket(newEntityMetadata.newInstance(entityId, DataWatcher.packDirty.invoke(data.build())));
        } else {
            player.sendPacket(newEntityMetadata.newInstance(entityId, data.build(), true));
        }
    }

    @SneakyThrows
    public void teleportEntity(int entityId, @NotNull Location location) {
        NMSStorage nms = NMSStorage.getInstance();
        Object nmsPacket;
        if (nms.getMinorVersion() >= 17) {
            nmsPacket = newEntityTeleport.newInstance(nms.dummyEntity);
        } else {
            nmsPacket = newEntityTeleport.newInstance();
        }
        EntityTeleport_EntityId.set(nmsPacket, entityId);
        if (nms.getMinorVersion() >= 9) {
            EntityTeleport_X.set(nmsPacket, location.getX());
            EntityTeleport_Y.set(nmsPacket, location.getY());
            EntityTeleport_Z.set(nmsPacket, location.getZ());
        } else {
            EntityTeleport_X.set(nmsPacket, floor(location.getX()*32));
            EntityTeleport_Y.set(nmsPacket, floor(location.getY()*32));
            EntityTeleport_Z.set(nmsPacket, floor(location.getZ()*32));
        }
        player.sendPacket(nmsPacket);
    }

    @SneakyThrows
    public void destroyEntities(int... entities) {
        if (newEntityDestroy.getParameterTypes()[0] != int.class) {
            player.sendPacket(newEntityDestroy.newInstance(new Object[]{entities}));
        } else {
            //1.17.0 Mojank
            for (int entity : entities) {
                player.sendPacket(newEntityDestroy.newInstance(entity));
            }
        }
    }
}
