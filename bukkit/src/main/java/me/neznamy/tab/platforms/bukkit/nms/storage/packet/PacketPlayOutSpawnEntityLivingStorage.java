package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.UUID;

/**
 * Custom class for holding data used in PacketPlayOutSpawnEntityLiving minecraft packet.
 */
public class PacketPlayOutSpawnEntityLivingStorage {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;

    /** 1.17+ */
    public static Class<?> Vec3D;
    public static Class<?> EntityTypes;
    public static Object Vec3D_Empty;
    public static Object EntityTypes_ARMOR_STAND;

    /** 1.16.5- */
    public static Field ENTITY_ID;
    public static Field ENTITY_TYPE;
    public static Field YAW;
    public static Field PITCH;
    public static Field UUID;
    public static Field X;
    public static Field Y;
    public static Field Z;
    public static Field DATA_WATCHER;
    public static final EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws NoSuchMethodException, IllegalAccessException {
        int minorVersion = nms.getMinorVersion();
        if (minorVersion >= 13) {
            entityIds.put(EntityType.ARMOR_STAND, 1);
        } else {
            entityIds.put(EntityType.WITHER, 64);
            if (minorVersion >= 8) {
                entityIds.put(EntityType.ARMOR_STAND, 30);
            }
        }

        if (minorVersion >= 19) {
            CONSTRUCTOR = CLASS.getConstructor(int.class, UUID.class, double.class, double.class, double.class,
                    float.class, float.class, EntityTypes, int.class, Vec3D, double.class);
            Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
        } else if (minorVersion >= 17) {
            CONSTRUCTOR = CLASS.getConstructor(int.class, UUID.class, double.class, double.class, double.class,
                    float.class, float.class, EntityTypes, int.class, Vec3D);
            Vec3D_Empty = ReflectionUtils.getOnlyField(Vec3D, Vec3D).get(null);
        } else {
            CONSTRUCTOR = CLASS.getConstructor();
            ENTITY_ID = ReflectionUtils.getFields(CLASS, int.class).get(0);
            YAW = ReflectionUtils.getFields(CLASS, byte.class).get(0);
            PITCH = ReflectionUtils.getFields(CLASS, byte.class).get(1);
            if (minorVersion >= 9) {
                UUID = ReflectionUtils.getOnlyField(CLASS, UUID.class);
                X = ReflectionUtils.getFields(CLASS, double.class).get(0);
                Y = ReflectionUtils.getFields(CLASS, double.class).get(1);
                Z = ReflectionUtils.getFields(CLASS, double.class).get(2);
            } else {
                X = ReflectionUtils.getFields(CLASS, int.class).get(2);
                Y = ReflectionUtils.getFields(CLASS, int.class).get(3);
                Z = ReflectionUtils.getFields(CLASS, int.class).get(4);
            }
            ENTITY_TYPE = ReflectionUtils.getFields(CLASS, int.class).get(1);
            if (minorVersion <= 14) {
                DATA_WATCHER = ReflectionUtils.getOnlyField(CLASS, DataWatcher.CLASS);
            }
        }
    }

    /**
     * Converts this class into NMS packet
     *
     * @return  NMS packet
     */
    @SneakyThrows
    public static Object build(int id, UUID uuid, Object entityType, Location l, EntityData data) {
        int minorVersion = NMSStorage.getInstance().getMinorVersion();
        if (minorVersion >= 19) {
            return CONSTRUCTOR.newInstance(id, uuid, l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), EntityTypes_ARMOR_STAND, 0, Vec3D_Empty, 0d);
        }
        if (minorVersion >= 17) {
            return CONSTRUCTOR.newInstance(id, uuid, l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), EntityTypes_ARMOR_STAND, 0, Vec3D_Empty);
        }
        Object nmsPacket = CONSTRUCTOR.newInstance();
        ENTITY_ID.set(nmsPacket, id);
        YAW.set(nmsPacket, (byte)(l.getYaw() * 256.0f / 360.0f));
        PITCH.set(nmsPacket, (byte)(l.getPitch() * 256.0f / 360.0f));
        if (minorVersion <= 14) {
            DATA_WATCHER.set(nmsPacket, data.build());
        }
        if (minorVersion >= 9) {
            UUID.set(nmsPacket, uuid);
            X.set(nmsPacket, l.getX());
            Y.set(nmsPacket, l.getY());
            Z.set(nmsPacket, l.getZ());
        } else {
            X.set(nmsPacket, floor(l.getX()*32));
            Y.set(nmsPacket, floor(l.getY()*32));
            Z.set(nmsPacket, floor(l.getZ()*32));
        }
        ENTITY_TYPE.set(nmsPacket, entityIds.get((EntityType) entityType));
        return nmsPacket;
    }

    private static int floor(double paramDouble) {
        int i = (int)paramDouble;
        return paramDouble < i ? i - 1 : i;
    }
}