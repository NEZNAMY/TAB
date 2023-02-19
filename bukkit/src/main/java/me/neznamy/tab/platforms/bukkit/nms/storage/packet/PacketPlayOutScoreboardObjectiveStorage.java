package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutScoreboardObjectiveStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field OBJECTIVE_NAME;
    public static Field METHOD;
    public static Field RENDER_TYPE;
    public static Field DISPLAY_NAME;

    public static Class<Enum> EnumScoreboardHealthDisplay;
    public static Class<?> ScoreboardObjective;
    public static Constructor<?> newScoreboardObjective;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        newScoreboardObjective = ScoreboardObjective.getConstructors()[0];
        OBJECTIVE_NAME = nms.getFields(CLASS, String.class).get(0);
        List<Field> list = nms.getFields(CLASS, int.class);
        METHOD = list.get(list.size()-1);
        if (nms.getMinorVersion() >= 8) {
            RENDER_TYPE = nms.getFields(CLASS, EnumScoreboardHealthDisplay).get(0);
        }
        if (nms.getMinorVersion() >= 13) {
            CONSTRUCTOR = CLASS.getConstructor(ScoreboardObjective, int.class);
            DISPLAY_NAME = nms.getFields(CLASS, nms.IChatBaseComponent).get(0);
        } else {
            CONSTRUCTOR = CLASS.getConstructor();
            DISPLAY_NAME = nms.getFields(CLASS, String.class).get(1);
        }
    }

    public static Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        String displayName = clientVersion.getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder().cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
        if (nms.getMinorVersion() >= 13) {
            return CONSTRUCTOR.newInstance(newScoreboardObjective.newInstance(null, packet.getObjectiveName(), null,
                            nms.toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion),
                            packet.getRenderType() == null ? null : Enum.valueOf(EnumScoreboardHealthDisplay, packet.getRenderType().toString())),
                    packet.getAction());
        }

        Object nmsPacket = CONSTRUCTOR.newInstance();
        OBJECTIVE_NAME.set(nmsPacket, packet.getObjectiveName());
        DISPLAY_NAME.set(nmsPacket, displayName);
        if (nms.getMinorVersion() >= 8 && packet.getRenderType() != null) {
            RENDER_TYPE.set(nmsPacket, Enum.valueOf(EnumScoreboardHealthDisplay, packet.getRenderType().toString()));
        }
        METHOD.set(nmsPacket, packet.getAction());
        return nmsPacket;
    }

    public static PacketPlayOutScoreboardObjective read(Object nmsPacket) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardObjective(METHOD.getInt(nmsPacket),
                (String) OBJECTIVE_NAME.get(nmsPacket), null,
                PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay.INTEGER
        );
    }
}
