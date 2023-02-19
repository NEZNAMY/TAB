package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutChatStorage {

    public static Class<?> CLASS;
    public static Class<Enum> ChatMessageTypeClass;
    public static Constructor<?> CONSTRUCTOR;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        if (nms.getMinorVersion() >= 19) {
            try {
                CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, boolean.class);
            } catch (NoSuchMethodException e) {
                //1.19.0
                CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, int.class);
            }
        } else if (nms.getMinorVersion() >= 16) {
            CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, ChatMessageTypeClass, UUID.class);
        } else if (nms.getMinorVersion() >= 12) {
            CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, ChatMessageTypeClass);
        } else if (nms.getMinorVersion() >= 8) {
            CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent, byte.class);
        } else if (nms.getMinorVersion() >= 7) {
            CONSTRUCTOR = CLASS.getConstructor(nms.IChatBaseComponent);
        }
    }

    public static Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        Object component = nms.toNMSComponent(packet.getMessage(), clientVersion);
        if (nms.getMinorVersion() >= 19)
            try {
                return CONSTRUCTOR.newInstance(component, packet.getType() == PacketPlayOutChat.ChatMessageType.GAME_INFO);
            } catch (Exception e) {
                //1.19.0
                return CONSTRUCTOR.newInstance(component, packet.getType().ordinal());
            }
        if (nms.getMinorVersion() >= 16)
            return CONSTRUCTOR.newInstance(component, Enum.valueOf(ChatMessageTypeClass, packet.getType().toString()), UUID.randomUUID());
        if (nms.getMinorVersion() >= 12)
            return CONSTRUCTOR.newInstance(component, Enum.valueOf(ChatMessageTypeClass, packet.getType().toString()));
        if (nms.getMinorVersion() >= 8)
            return CONSTRUCTOR.newInstance(component, (byte) packet.getType().ordinal());
        if (nms.getMinorVersion() == 7)
            return CONSTRUCTOR.newInstance(component);
        return packet;
    }
}
