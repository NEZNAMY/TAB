package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

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

    public static Object change(String objective, String player, int score) {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            if (nms.getMinorVersion() >= 13) {
                return CONSTRUCTOR_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, "CHANGE"), objective, player, score);
            }
            Object scoreboardScore = newScoreboardScore.newInstance(nms.emptyScoreboard, nms.newScoreboardObjective(objective), player);
            ScoreboardScore_setScore.invoke(scoreboardScore, score);
            if (nms.getMinorVersion() >= 8) {
                return CONSTRUCTOR.newInstance(scoreboardScore);
            }
            return CONSTRUCTOR.newInstance(scoreboardScore, 0);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object remove(String objective, String player) {
        try {
            if (NMSStorage.getInstance().getMinorVersion() >= 13) {
                return CONSTRUCTOR_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, "REMOVE"), objective, player, 0);
            }
            return CONSTRUCTOR_String.newInstance(player);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
