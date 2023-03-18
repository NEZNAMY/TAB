package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

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

    public static Object buildSilent(int action, String objectiveName, String title, boolean hearts, ProtocolVersion clientVersion) {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            if (nms.getMinorVersion() >= 13) {
                return CONSTRUCTOR.newInstance(newScoreboardObjective.newInstance(null, objectiveName, null,
                        nms.toNMSComponent(IChatBaseComponent.optimizedComponent(title), clientVersion),
                        asDisplayType(hearts)), action);
            }
            Object nmsPacket = CONSTRUCTOR.newInstance();
            OBJECTIVE_NAME.set(nmsPacket, objectiveName);
            DISPLAY_NAME.set(nmsPacket, title);
            if (nms.getMinorVersion() >= 8) {
                RENDER_TYPE.set(nmsPacket, asDisplayType(hearts));
            }
            METHOD.set(nmsPacket, action);
            return nmsPacket;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object asDisplayType(boolean hearts) {
        return Enum.valueOf(EnumScoreboardHealthDisplay, hearts ? "HEARTS" : "INTEGER");
    }
}
