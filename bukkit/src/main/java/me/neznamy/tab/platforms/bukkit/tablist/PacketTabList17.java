package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.TriFunctionWithException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TabList handler for 1.7- servers using packets.
 */
public class PacketTabList17 extends TabListBase {

    private static TriFunctionWithException<String, Boolean, Integer, Object> newPacket;
    private static PacketSender packetSender;

    /** Because entries are identified by names and not uuids on 1.7- */
    @NotNull
    private final Map<UUID, String> userNames = new HashMap<>();

    @NotNull
    private final Map<UUID, String> displayNames = new HashMap<>();

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public PacketTabList17(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    /**
     * Attempts to load all required NMS classes, fields and methods.
     * If anything fails, throws an exception.
     *
     * @throws  ReflectiveOperationException
     *          If something goes wrong
     */
    public static void load() throws ReflectiveOperationException {
        Class<?> PlayerInfoClass = BukkitReflection.getClass("PacketPlayOutPlayerInfo", "Packet201PlayerInfo");
        try {
            Constructor<?> newPlayerInfo = PlayerInfoClass.getConstructor(String.class, boolean.class, int.class);
            newPacket = newPlayerInfo::newInstance;
        } catch (NoSuchMethodException e) {
            // 1.7.10 spigot with protocol hack
            Constructor<?> newPlayerInfo = PlayerInfoClass.getConstructor();
            Field USERNAME = ReflectionUtils.getField(PlayerInfoClass, "username");
            Field ACTION = ReflectionUtils.getField(PlayerInfoClass, "action");
            Field PING = ReflectionUtils.getField(PlayerInfoClass, "ping");
            newPacket = (name, addOrUpdate, latency) -> {
                Object packet = newPlayerInfo.newInstance();
                USERNAME.set(packet, name);
                ACTION.set(packet, addOrUpdate ? 3 : 4);
                PING.set(packet, latency);
                return packet;
            };
        }
        packetSender = new PacketSender();
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NotNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), newPacket.apply(displayNames.get(entry), false, 0));
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    @SneakyThrows
    public void updateDisplayName(@NotNull UUID entry, @Nullable TabComponent displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), newPacket.apply(displayNames.get(entry), false, 0));
        addEntry(new Entry(entry, userNames.get(entry), null, 0, 0, displayName));
    }

    @Override
    @SneakyThrows
    public void updateLatency(@NotNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), newPacket.apply(displayNames.get(entry), true, latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        // Added in 1.8
    }

    @Override
    @SneakyThrows
    public void addEntry(@NotNull Entry entry) {
        String name = entry.getDisplayName() == null ? entry.getName() : entry.getDisplayName().toLegacyText();
        if (name.length() > Limitations.MAX_DISPLAY_NAME_LENGTH_1_7) name = name.substring(0, Limitations.MAX_DISPLAY_NAME_LENGTH_1_7);
        packetSender.sendPacket(player.getPlayer(), newPacket.apply(name, true, entry.getLatency()));
        userNames.put(entry.getUniqueId(), entry.getName());
        displayNames.put(entry.getUniqueId(), name);
    }
}
