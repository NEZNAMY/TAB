package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.backend.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Custom class for holding data used in PacketPlayOutEntityTeleport minecraft packet.
 */
public class PacketPlayOutEntityTeleportStorage {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field ENTITY_ID;
    public static Field X;
    public static Field Y;
    public static Field Z;
    public static Field YAW;
    public static Field PITCH;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws NoSuchMethodException {
        ENTITY_ID = nms.getFields(CLASS, int.class).get(0);
        YAW = nms.getFields(CLASS, byte.class).get(0);
        PITCH = nms.getFields(CLASS, byte.class).get(1);
        if (nms.getMinorVersion() >= 17) {
            CONSTRUCTOR = CLASS.getConstructor(nms.Entity);
        } else {
            CONSTRUCTOR = CLASS.getConstructor();
        }
        if (nms.getMinorVersion() >= 9) {
            X = nms.getFields(CLASS, double.class).get(0);
            Y = nms.getFields(CLASS, double.class).get(1);
            Z = nms.getFields(CLASS, double.class).get(2);
        } else {
            X = nms.getFields(CLASS, int.class).get(1);
            Y = nms.getFields(CLASS, int.class).get(2);
            Z = nms.getFields(CLASS, int.class).get(3);
        }
    }

    /**
     * Converts this class into NMS packet
     *
     * @return  NMS packet
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public static Object build(int entityId, Location location) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        Object nmsPacket;
        if (nms.getMinorVersion() >= 17) {
            nmsPacket = CONSTRUCTOR.newInstance(nms.dummyEntity);
        } else {
            nmsPacket = CONSTRUCTOR.newInstance();
        }
        ENTITY_ID.set(nmsPacket, entityId);
        if (nms.getMinorVersion() >= 9) {
            X.set(nmsPacket, location.getX());
            Y.set(nmsPacket, location.getY());
            Z.set(nmsPacket, location.getZ());
        } else {
            X.set(nmsPacket, floor(location.getX()*32));
            Y.set(nmsPacket, floor(location.getY()*32));
            Z.set(nmsPacket, floor(location.getZ()*32));
        }
        YAW.set(nmsPacket, (byte) (location.getYaw()/360*256));
        PITCH.set(nmsPacket, (byte) (location.getPitch()/360*256));
        return nmsPacket;
    }

    private static int floor(double paramDouble) {
        int i = (int)paramDouble;
        return paramDouble < i ? i - 1 : i;
    }
}