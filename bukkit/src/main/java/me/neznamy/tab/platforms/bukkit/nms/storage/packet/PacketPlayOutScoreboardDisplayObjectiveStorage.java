package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PacketPlayOutScoreboardDisplayObjectiveStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field POSITION;
    public static Field OBJECTIVE_NAME;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        CONSTRUCTOR = CLASS.getConstructor(int.class, PacketPlayOutScoreboardObjectiveStorage.ScoreboardObjective);
        POSITION = nms.getFields(CLASS, int.class).get(0);
        OBJECTIVE_NAME = nms.getFields(CLASS, String.class).get(0);
    }

    public static Object buildSilent(int slot, String objective) {
        try {
            return CONSTRUCTOR.newInstance(slot, NMSStorage.getInstance().newScoreboardObjective(objective));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
