package me.neznamy.tab.platforms.bukkit.tablist;

import com.mojang.authlib.GameProfile;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
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

    private static boolean v1_21_2Plus;

    private static Enum actionAddPlayer;
    private static Enum actionUpdateDisplayName;
    private static Enum actionUpdateLatency;

    private static Constructor<?> newRemovePacket;

    private static Field PlayerInfoData_UUID;
    private static Field PlayerInfoData_GameMode;
    private static Field PlayerInfoData_Listed;
    private static Field PlayerInfoData_ShowHat;
    private static Field PlayerInfoData_ListOrder;
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
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        Class<?> RemoteChatSession$Data = BukkitReflection.getClass("network.chat.RemoteChatSession$Data", "network.chat.RemoteChatSession$a");
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

        PlayerInfoData_Listed = ReflectionUtils.getFields(playerInfoDataClass, boolean.class).get(0);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
        PlayerInfoData_UUID = ReflectionUtils.getOnlyField(playerInfoDataClass, UUID.class);
        newRemovePacket = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoRemovePacket").getConstructor(List.class);

        actionAddPlayer = Enum.valueOf(ActionClass, Action.ADD_PLAYER.name());
        actionUpdateDisplayName = Enum.valueOf(ActionClass, Action.UPDATE_DISPLAY_NAME.name());
        actionUpdateLatency = Enum.valueOf(ActionClass, Action.UPDATE_LATENCY.name());

        actionToEnumSet.put(Action.ADD_PLAYER, EnumSet.allOf(ActionClass));
        actionToEnumSet.put(Action.UPDATE_GAME_MODE, EnumSet.of(Enum.valueOf(ActionClass, Action.UPDATE_GAME_MODE.name())));
        actionToEnumSet.put(Action.UPDATE_DISPLAY_NAME, EnumSet.of(actionUpdateDisplayName));
        actionToEnumSet.put(Action.UPDATE_LATENCY, EnumSet.of(actionUpdateLatency));
        actionToEnumSet.put(Action.UPDATE_LISTED, EnumSet.of(Enum.valueOf(ActionClass, Action.UPDATE_LISTED.name())));
        try {
            actionToEnumSet.put(Action.UPDATE_LIST_ORDER, EnumSet.of(Enum.valueOf(ActionClass, Action.UPDATE_LIST_ORDER.name())));
            PlayerInfoData_ListOrder = ReflectionUtils.getFields(playerInfoDataClass, int.class).get(1);
            v1_21_2Plus = true;
            if (BukkitReflection.is1_21_4Plus()) {
                // 1.21.4+
                actionToEnumSet.put(Action.UPDATE_HAT, EnumSet.of(Enum.valueOf(ActionClass, Action.UPDATE_HAT.name())));
                PlayerInfoData_ShowHat = ReflectionUtils.getFields(playerInfoDataClass, boolean.class).get(1);
                newPlayerInfoData = playerInfoDataClass.getConstructor(UUID.class, GameProfile.class, boolean.class, int.class,
                        EnumGamemodeClass, IChatBaseComponent, boolean.class, int.class, RemoteChatSession$Data);
            } else {
                // 1.21.2 - 1.21.3
                newPlayerInfoData = playerInfoDataClass.getConstructor(UUID.class, GameProfile.class, boolean.class, int.class,
                        EnumGamemodeClass, IChatBaseComponent, int.class, RemoteChatSession$Data);
            }
        } catch (Exception ignored) {
            // 1.21.1-, should have a better check
            newPlayerInfoData = playerInfoDataClass.getConstructor(UUID.class, GameProfile.class, boolean.class, int.class,
                    EnumGamemodeClass, IChatBaseComponent, RemoteChatSession$Data);
        }
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NonNull UUID entry) {
        packetSender.sendPacket(player, newRemovePacket.newInstance(Collections.singletonList(entry)));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        packetSender.sendPacket(player,
                createPacket(Action.UPDATE_LISTED, entry, "", null, listed, 0, 0, null, 0, false));
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        if (player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
            packetSender.sendPacket(player,
                    createPacket(Action.UPDATE_LIST_ORDER, entry, "", null, false, 0, 0, null, listOrder, false));
        }
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        if (player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
            packetSender.sendPacket(player,
                    createPacket(Action.UPDATE_HAT, entry, "", null, false, 0, 0, null, 0, showHat));
        }
    }

    @SneakyThrows
    @NotNull
    @Override
    public Object createPacket(@NonNull Action action, @NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                               boolean listed, int latency, int gameMode, @Nullable Object displayName, int listOrder, boolean showHat) {
        Object packet = newPlayerInfo.newInstance(actionToEnumSet.get(action), Collections.emptyList());
        PLAYERS.set(packet, Collections.singletonList(newPlayerInfoData(
                id,
                action == Action.ADD_PLAYER ? createProfile(id, name, skin) : null,
                listed,
                latency,
                gameModes[gameMode],
                displayName,
                showHat,
                listOrder,
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
            int listOrder = v1_21_2Plus ? PlayerInfoData_ListOrder.getInt(nmsData) : 0;
            boolean showHat = BukkitReflection.is1_21_4Plus() && PlayerInfoData_ShowHat.getBoolean(nmsData);
            if (actions.contains(actionUpdateDisplayName)) {
                Object expectedName = getExpectedDisplayNames().get(id);
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
            updatedList.add(rewriteEntry ? newPlayerInfoData(
                    id,
                    profile,
                    PlayerInfoData_Listed.getBoolean(nmsData),
                    latency,
                    PlayerInfoData_GameMode.get(nmsData),
                    displayName,
                    showHat,
                    listOrder,
                    PlayerInfoData_RemoteChatSession.get(nmsData)) : nmsData);
        }
        if (rewritePacket) PLAYERS.set(packet, updatedList);
    }

    @NotNull
    @SneakyThrows
    private static Object newPlayerInfoData(@NotNull UUID id, @Nullable GameProfile profile, boolean listed, int latency,
                                            @Nullable Object gameMode, @Nullable Object displayName, boolean showHat, int listOrder, @Nullable Object chatSession) {
        if (BukkitReflection.is1_21_4Plus()) {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName, showHat, listOrder, chatSession);
        } else if (v1_21_2Plus) {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName,          listOrder, chatSession);
        } else {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName,                     chatSession);
        }
    }
}
