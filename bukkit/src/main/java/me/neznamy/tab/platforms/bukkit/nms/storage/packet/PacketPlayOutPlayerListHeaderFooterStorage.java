package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PacketPlayOutPlayerListHeaderFooterStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field HEADER;
    public static Field FOOTER;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        if (nms.getMinorVersion() < 8) return;
        if (nms.getMinorVersion() >= 17) {
            CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, nms.IChatBaseComponent);
        } else {
            CONSTRUCTOR = CLASS.getConstructor();
            HEADER = nms.getFields(CLASS, nms.IChatBaseComponent).get(0);
            FOOTER = nms.getFields(CLASS, nms.IChatBaseComponent).get(1);
        }
    }

    public static Object build(IChatBaseComponent header, IChatBaseComponent footer, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        if (nms.getMinorVersion() < 8) return null;
        if (CONSTRUCTOR.getParameterCount() == 2) {
            return CONSTRUCTOR.newInstance(nms.toNMSComponent(header, clientVersion), nms.toNMSComponent(footer, clientVersion));
        }
        Object nmsPacket = CONSTRUCTOR.newInstance();
        HEADER.set(nmsPacket, nms.toNMSComponent(header, clientVersion));
        FOOTER.set(nmsPacket, nms.toNMSComponent(footer, clientVersion));
        return nmsPacket;
    }
}