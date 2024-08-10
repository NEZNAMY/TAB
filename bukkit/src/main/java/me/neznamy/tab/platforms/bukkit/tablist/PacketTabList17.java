package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.NonNull;
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
public class PacketTabList17 extends TabListBase<String> {

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
    public PacketTabList17(@NonNull BukkitTabPlayer player) {
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
    public void removeEntry0(@NonNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player, newPacket.apply(displayNames.get(entry), false, 0));
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    @SneakyThrows
    public void updateDisplayName(@NonNull UUID entry, @Nullable String displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player, newPacket.apply(displayNames.get(entry), false, 0));
        addEntry(entry, userNames.get(entry), null, false, 0, 0, displayName);
    }

    @Override
    @SneakyThrows
    public void updateLatency(@NonNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player, newPacket.apply(displayNames.get(entry), true, latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        // Added in 1.8
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    @SneakyThrows
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable String displayName) {
        String display = displayName == null ? name : displayName;
        packetSender.sendPacket(player, newPacket.apply(display, true, latency));
        userNames.put(id, name);
        displayNames.put(id, display);
    }

    @Override
    public String toComponent(@NonNull TabComponent component) {
        String name = component.toLegacyText();
        if (name.length() > Limitations.MAX_DISPLAY_NAME_LENGTH_1_7) name = name.substring(0, Limitations.MAX_DISPLAY_NAME_LENGTH_1_7);
        return name;
    }
}
