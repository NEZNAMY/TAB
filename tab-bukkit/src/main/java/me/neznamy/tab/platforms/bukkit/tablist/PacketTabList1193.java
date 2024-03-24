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

/**
 * TabList handler for 1.19.3+ servers using packets.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketTabList1193 extends PacketTabList18 {

    /** Map of actions to prevent creating new EnumSet on each packet send */
    private static final Map<Action, EnumSet<?>> actionToEnumSet = new EnumMap<>(Action.class);

    private static Enum actionAddPlayer;
    private static Enum actionUpdateDisplayName;
    private static Enum actionUpdateLatency;

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

        actionAddPlayer = Enum.valueOf(ActionClass, Action.ADD_PLAYER.name());
        actionUpdateDisplayName = Enum.valueOf(ActionClass, Action.UPDATE_DISPLAY_NAME.name());
        actionUpdateLatency = Enum.valueOf(ActionClass, Action.UPDATE_LATENCY.name());
        Enum actionUpdateGameMode = Enum.valueOf(ActionClass, Action.UPDATE_GAME_MODE.name());

        actionToEnumSet.put(Action.ADD_PLAYER, EnumSet.allOf(ActionClass));
        actionToEnumSet.put(Action.UPDATE_GAME_MODE, EnumSet.of(actionUpdateGameMode));
        actionToEnumSet.put(Action.UPDATE_DISPLAY_NAME, EnumSet.of(actionUpdateDisplayName));
        actionToEnumSet.put(Action.UPDATE_LATENCY, EnumSet.of(actionUpdateLatency));
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
        Object packet = newPlayerInfo.newInstance(actionToEnumSet.get(action), Collections.emptyList());
        PLAYERS.set(packet, Collections.singletonList(newPlayerInfoData.newInstance(
                id,
                action == Action.ADD_PLAYER ? createProfile(id, name, skin) : null,
                true,
                latency,
                gameModes[gameMode],
                displayName,
                null
        )));
        return packet;
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (!(PlayerInfoClass.isInstance(packet))) return;
        EnumSet<?> actions = (EnumSet<?>) ACTION.get(packet);
        List<Object> updatedList = new ArrayList<>();
        boolean rewritePacket = false;
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            boolean rewriteEntry = false;
            UUID id = (UUID) PlayerInfoData_UUID.get(nmsData);
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            Object displayName = PlayerInfoData_DisplayName.get(nmsData);
            int latency = PlayerInfoData_Latency.getInt(nmsData);
            if (actions.contains(actionUpdateDisplayName)) {
                Object expectedName = getExpectedDisplayName(id);
                if (expectedName != null && expectedName != displayName) {
                    displayName = expectedName;
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(actionUpdateLatency)) {
                int newLatency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, latency);
                if (newLatency != latency) {
                    latency = newLatency;
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(actionAddPlayer)) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
            // 1.19.3 is using records, which do not allow changing final fields, need to rewrite the list entirely
            updatedList.add(rewriteEntry ? newPlayerInfoData.newInstance(
                    id,
                    profile,
                    PlayerInfoData_Listed.getBoolean(nmsData),
                    latency,
                    PlayerInfoData_GameMode.get(nmsData),
                    displayName,
                    PlayerInfoData_RemoteChatSession.get(nmsData)) : nmsData);
        }
        if (rewritePacket) PLAYERS.set(packet, updatedList);
    }
}
