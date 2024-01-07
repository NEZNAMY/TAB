package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Class storing NMS fields and methods of DisplayObjective packet.
 */
public class DisplayPacketData {

    private final Class<?> DisplayObjectiveClass;
    private final Constructor<?> newDisplayObjective;
    private final Field DisplayObjective_OBJECTIVE_NAME;
    private final Object[] displaySlots;
    private final FunctionWithException<Object, Integer> packetToSlot;

    /**
     * Constructs new instance and loads all required NMS fields.
     *
     * @throws  ReflectiveOperationException
     *          If something fails
     */
    public DisplayPacketData() throws ReflectiveOperationException {
        Class<?> ScoreboardObjective = BukkitReflection.getClass("world.scores.Objective", "world.scores.ScoreboardObjective", "ScoreboardObjective");
        DisplayObjectiveClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetDisplayObjectivePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardDisplayObjective", // Bukkit 1.17+
                "PacketPlayOutScoreboardDisplayObjective", // Bukkit 1.7 - 1.16.5
                "Packet208SetScoreboardDisplayObjective" // Bukkit 1.5 - 1.6.4
        );
        DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(DisplayObjectiveClass, String.class);
        if (BukkitReflection.is1_20_2Plus()) {
            Class<?> DisplaySlot = BukkitReflection.getClass("world.scores.DisplaySlot");
            displaySlots = (Object[]) DisplaySlot.getDeclaredMethod("values").invoke(null);
            Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, DisplaySlot);
            newDisplayObjective = DisplayObjectiveClass.getConstructor(DisplaySlot, ScoreboardObjective);
            packetToSlot = packet -> ((Enum<?>)DisplayObjective_POSITION.get(packet)).ordinal();
        } else {
            displaySlots = new Object[]{0, 1, 2};
            Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, int.class);
            newDisplayObjective = DisplayObjectiveClass.getConstructor(int.class, ScoreboardObjective);
            packetToSlot = DisplayObjective_POSITION::getInt;
        }
    }

    /**
     * Creates a packet for setting display slot.
     *
     * @param   slot
     *          Objective slot
     * @param   objective
     *          Objective to display
     * @return  Packet for setting display slot
     */
    @SneakyThrows
    public Object setDisplaySlot(int slot, @NotNull Object objective) {
        return newDisplayObjective.newInstance(displaySlots[slot], objective);
    }

    /**
     * Returns {@code true} if packet is display objective packet, {@code false} if not.
     *
     * @param   packet
     *          Packet to check
     * @return  {@code true} if is, {@code false} if not
     */
    public boolean isDisplayObjective(@NotNull Object packet) {
        return DisplayObjectiveClass.isInstance(packet);
    }

    /**
     * Processes display objective packet.
     *
     * @param   player
     *          Player who received the packet
     * @param   packet
     *          Display objective packet received
     */
    @SneakyThrows
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player, packetToSlot.apply(packet),
                (String) DisplayObjective_OBJECTIVE_NAME.get(packet));
    }
}
