package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
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

    private static Constructor<?> newPlayerInfo;
    private static Field USERNAME;
    private static Field ACTION;
    private static Field PING;
    private static boolean protocolHack;
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
            newPlayerInfo = PlayerInfoClass.getConstructor(String.class, boolean.class, int.class);
        } catch (NoSuchMethodException e) {
            // 1.7.10 spigot with protocol back
            protocolHack = true;
            newPlayerInfo = PlayerInfoClass.getConstructor();
            USERNAME = ReflectionUtils.getField(PlayerInfoClass, "username");
            ACTION = ReflectionUtils.getField(PlayerInfoClass, "action");
            PING = ReflectionUtils.getField(PlayerInfoClass, "ping");
        }
        packetSender = new PacketSender();
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), createPacket(displayNames.get(entry), false, 0));
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), createPacket(displayNames.get(entry), false, 0));
        addEntry(new Entry(entry, userNames.get(entry), null, 0, 0, displayName));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        packetSender.sendPacket(player.getPlayer(), createPacket(displayNames.get(entry), true, latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        // Added in 1.8
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        String name = entry.getDisplayName() == null ? entry.getName() : entry.getDisplayName().toLegacyText();
        if (name.length() > Limitations.MAX_DISPLAY_NAME_LENGTH_1_7) name = name.substring(0, Limitations.MAX_DISPLAY_NAME_LENGTH_1_7);
        packetSender.sendPacket(player.getPlayer(), createPacket(name, true, entry.getLatency()));
        userNames.put(entry.getUniqueId(), entry.getName());
        displayNames.put(entry.getUniqueId(), name);
    }

    @SneakyThrows
    private Object createPacket(@NotNull String name, boolean addOrUpdate, int latency) {
        if (!protocolHack) {
            return newPlayerInfo.newInstance(name, addOrUpdate, latency);
        } else {
            Object packet = newPlayerInfo.newInstance();
            USERNAME.set(packet, name);
            ACTION.set(packet, addOrUpdate ? 3 : 4);
            PING.set(packet, latency);
            return packet;
        }
    }
}
