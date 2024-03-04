package me.neznamy.tab.platforms.bukkit.tablist;

import com.mojang.authlib.GameProfile;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TabList handler for 1.19.3+ servers using packets.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketTabList1193 extends PacketTabList18 {

    private static Constructor<?> newRemovePacket;

    private static Field PlayerInfoData_UUID;
    private static Field PlayerInfoData_GameMode;
    private static Field PlayerInfoData_Listed;
    private static Field PlayerInfoData_RemoteChatSession;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public PacketTabList1193(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    /**
     * Attempts to load all required NMS classes, fields and methods.
     * If anything fails, throws an exception.
     *
     * @throws  ReflectiveOperationException
     *          If something goes wrong
     */
    public static void loadNew() throws ReflectiveOperationException {
        Class<Enum> EnumGamemodeClass = (Class<Enum>) BukkitReflection.getClass("world.level.GameType", "world.level.EnumGamemode");
        ActionClass = (Class<Enum>) BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action", // Mojang
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$a" // Bukkit
        );
        PlayerInfoClass = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoUpdatePacket");
        Class<?> playerInfoDataClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry", // Mojang
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$b" // Bukkit
        );

        newPlayerInfo = PlayerInfoClass.getConstructor(EnumSet.class, Collection.class);
        ACTION = ReflectionUtils.getOnlyField(PlayerInfoClass, EnumSet.class);

        loadSharedContent(playerInfoDataClass, EnumGamemodeClass);

        PlayerInfoData_Listed = ReflectionUtils.getOnlyField(playerInfoDataClass, boolean.class);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        Class<?> RemoteChatSession$Data = BukkitReflection.getClass("network.chat.RemoteChatSession$Data", "network.chat.RemoteChatSession$a");
        PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
        PlayerInfoData_UUID = ReflectionUtils.getOnlyField(playerInfoDataClass, UUID.class);
        newRemovePacket = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoRemovePacket").getConstructor(List.class);
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NonNull UUID entry) {
        packetSender.sendPacket(player.getPlayer(), newRemovePacket.newInstance(Collections.singletonList(entry)));
    }

    @SneakyThrows
    @NonNull
    @Override
    public Object createPacket(@NonNull Action action, @NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                               int latency, int gameMode, @Nullable Object displayName) {
        List<Object> players = new ArrayList<>();
        EnumSet<?> actions;
        if (action == Action.ADD_PLAYER) {
            actions = EnumSet.allOf(ActionClass);
        } else {
            actions = EnumSet.of(Enum.valueOf(ActionClass, action.name()));
        }
        Object packet = newPlayerInfo.newInstance(actions, Collections.emptyList());
        players.add(newPlayerInfoData.newInstance(
                id,
                createProfile(id, name, skin),
                true,
                latency,
                gameModes[gameMode],
                displayName,
                null
        ));
        PLAYERS.set(packet, players);
        return packet;
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (!(PlayerInfoClass.isInstance(packet))) return;
        List<String> actions = ((EnumSet<?>)ACTION.get(packet)).stream().map(Enum::name).collect(Collectors.toList());
        List<Object> updatedList = new ArrayList<>();
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            UUID id = (UUID) PlayerInfoData_UUID.get(nmsData);
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            Object displayName = PlayerInfoData_DisplayName.get(nmsData);
            int latency = PlayerInfoData_Latency.getInt(nmsData);
            if (actions.contains(Action.UPDATE_DISPLAY_NAME.name())) {
                Object expectedName = getExpectedDisplayName(id);
                if (expectedName != null) displayName = expectedName;
            }
            if (actions.contains(Action.UPDATE_LATENCY.name())) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, latency);
            }
            if (actions.contains(Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
            // 1.19.3 is using records, which do not allow changing final fields, need to rewrite the list entirely
            updatedList.add(newPlayerInfoData.newInstance(
                    id,
                    profile,
                    PlayerInfoData_Listed.getBoolean(nmsData),
                    latency,
                    PlayerInfoData_GameMode.get(nmsData),
                    displayName,
                    PlayerInfoData_RemoteChatSession.get(nmsData)));
        }
        PLAYERS.set(packet, updatedList);
    }
}
