package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.util.ReflectionUtils;

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
        newScoreboardObjective = ReflectionUtils.getOnlyConstructor(ScoreboardObjective);
        OBJECTIVE_NAME = ReflectionUtils.getFields(CLASS, String.class).get(0);
        List<Field> list = ReflectionUtils.getFields(CLASS, int.class);
        METHOD = list.get(list.size()-1);
        if (nms.getMinorVersion() >= 8) {
            RENDER_TYPE = ReflectionUtils.getOnlyField(CLASS, EnumScoreboardHealthDisplay);
        }
        if (nms.getMinorVersion() >= 13) {
            CONSTRUCTOR = CLASS.getConstructor(ScoreboardObjective, int.class);
            DISPLAY_NAME = ReflectionUtils.getOnlyField(CLASS, nms.IChatBaseComponent);
        } else {
            CONSTRUCTOR = CLASS.getConstructor();
            DISPLAY_NAME = ReflectionUtils.getFields(CLASS, String.class).get(1);
        }
    }

    @SneakyThrows
    public static Object build(int action, String objectiveName, String title, boolean hearts, ProtocolVersion clientVersion) {
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
    }

    private static Object asDisplayType(boolean hearts) {
        return Enum.valueOf(EnumScoreboardHealthDisplay, hearts ? "HEARTS" : "INTEGER");
    }
}
