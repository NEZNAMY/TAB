package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutScoreboardScoreStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR_1_13;
    public static Constructor<?> CONSTRUCTOR_String;
    public static Constructor<?> CONSTRUCTOR;
    public static Class<?> ScoreboardScore;
    public static Constructor<?> newScoreboardScore;
    public static Method ScoreboardScore_setScore;
    public static Class<Enum> EnumScoreboardAction;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        newScoreboardScore = ScoreboardScore.getConstructor(nms.Scoreboard, PacketPlayOutScoreboardObjectiveStorage.ScoreboardObjective, String.class);
        if (nms.getMinorVersion() >= 13) {
            CONSTRUCTOR_1_13 = CLASS.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
        } else {
            CONSTRUCTOR_String = CLASS.getConstructor(String.class);
            if (nms.getMinorVersion() >= 8) {
                CONSTRUCTOR = CLASS.getConstructor(ScoreboardScore);
            } else {
                CONSTRUCTOR = CLASS.getConstructor(ScoreboardScore, int.class);
            }
        }
    }

    public static Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        if (nms.getMinorVersion() >= 13) {
            return CONSTRUCTOR_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, packet.getAction().toString()), packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
        }
        if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
            return CONSTRUCTOR_String.newInstance(packet.getPlayer());
        }
        Object score = newScoreboardScore.newInstance(nms.emptyScoreboard, nms.newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
        ScoreboardScore_setScore.invoke(score, packet.getScore());
        if (nms.getMinorVersion() >= 8) {
            return CONSTRUCTOR.newInstance(score);
        }
        return CONSTRUCTOR.newInstance(score, 0);
    }
}
