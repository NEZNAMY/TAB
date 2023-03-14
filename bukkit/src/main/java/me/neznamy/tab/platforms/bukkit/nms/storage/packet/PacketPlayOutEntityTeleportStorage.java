package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityTeleport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Custom class for holding data used in PacketPlayOutEntityTeleport minecraft packet.
 */
public class PacketPlayOutEntityTeleportStorage implements TabPacket {

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
    public static Object build(PacketPlayOutEntityTeleport packet) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        Object nmsPacket;
        if (nms.getMinorVersion() >= 17) {
            nmsPacket = CONSTRUCTOR.newInstance(nms.dummyEntity);
        } else {
            nmsPacket = CONSTRUCTOR.newInstance();
        }
        ENTITY_ID.set(nmsPacket, packet.getEntityId());
        if (nms.getMinorVersion() >= 9) {
            X.set(nmsPacket, packet.getX());
            Y.set(nmsPacket, packet.getY());
            Z.set(nmsPacket, packet.getZ());
        } else {
            X.set(nmsPacket, floor(packet.getX()*32));
            Y.set(nmsPacket, floor(packet.getY()*32));
            Z.set(nmsPacket, floor(packet.getZ()*32));
        }
        YAW.set(nmsPacket, (byte) (packet.getYaw()/360*256));
        PITCH.set(nmsPacket, (byte) (packet.getPitch()/360*256));
        return nmsPacket;
    }

    private static int floor(double paramDouble) {
        int i = (int)paramDouble;
        return paramDouble < i ? i - 1 : i;
    }
}