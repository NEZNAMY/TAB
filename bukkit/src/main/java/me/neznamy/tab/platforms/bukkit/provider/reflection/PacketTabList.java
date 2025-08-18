package me.neznamy.tab.platforms.bukkit.provider.reflection;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * TabList handler for 1.19.3+ servers using packets.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketTabList extends TrackedTabList<BukkitTabPlayer> {

    private static final Method getProfile = ReflectionUtils.getMethods(BukkitReflection.getClass(
            "world.entity.player.Player", "world.entity.player.EntityHuman"), GameProfile.class).get(0);

    private static Class<?> PlayerInfoClass;
    private static Constructor<?> newPlayerInfo;
    private static Field ACTION;
    private static Field PLAYERS;

    private static Constructor<?> newHeaderFooter;

    private static Constructor<?> newPlayerInfoData;
    private static Field PlayerInfoData_Profile;
    private static Field PlayerInfoData_Latency;
    private static Field PlayerInfoData_DisplayName;
    private static Field PlayerInfoData_UUID;
    private static Field PlayerInfoData_GameMode;
    private static Field PlayerInfoData_Listed;
    private static Field PlayerInfoData_ShowHat;
    private static Field PlayerInfoData_ListOrder;
    private static Field PlayerInfoData_RemoteChatSession;

    private static Object[] gameModes;

    private static PacketSender packetSender;

    private static boolean v1_21_2Plus;
    private static boolean v1_21_4Plus;

    private static Enum actionAddPlayer;
    private static Enum actionUpdateDisplayName;
    private static Enum actionUpdateLatency;
    private static Enum actionUpdateGameMode;

    private static Constructor<?> newRemovePacket;

    private static EnumSet<?> ADD_PLAYER;
    private static EnumSet<?> UPDATE_GAME_MODE;
    private static EnumSet<?> UPDATE_DISPLAY_NAME;
    private static EnumSet<?> UPDATE_LATENCY;
    private static EnumSet<?> UPDATE_LISTED;
    private static EnumSet<?> UPDATE_LIST_ORDER;
    private static EnumSet<?> UPDATE_HAT;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public PacketTabList(@NonNull BukkitTabPlayer player) {
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
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
        Class<?> RemoteChatSession$Data = BukkitReflection.getClass("network.chat.RemoteChatSession$Data", "network.chat.RemoteChatSession$a");
        Class<Enum> EnumGamemodeClass = (Class<Enum>) BukkitReflection.getClass("world.level.GameType", "world.level.EnumGamemode");
        Class<Enum> actionClass = (Class<Enum>) BukkitReflection.getClass(
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

        PLAYERS = ReflectionUtils.getOnlyField(PlayerInfoClass, List.class);
        PlayerInfoData_Profile = ReflectionUtils.getOnlyField(playerInfoDataClass, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getFields(playerInfoDataClass, int.class).get(0);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(playerInfoDataClass, IChatBaseComponent);
        gameModes = new Object[] {
                Enum.valueOf(EnumGamemodeClass, "SURVIVAL"),
                Enum.valueOf(EnumGamemodeClass, "CREATIVE"),
                Enum.valueOf(EnumGamemodeClass, "ADVENTURE"),
                Enum.valueOf(EnumGamemodeClass, "SPECTATOR")
        };
        newHeaderFooter = BukkitReflection.getClass(
                "network.protocol.game.ClientboundTabListPacket",
                "network.protocol.game.PacketPlayOutPlayerListHeaderFooter"
        ).getConstructor(IChatBaseComponent, IChatBaseComponent);
        packetSender = new PacketSender();

        PlayerInfoData_Listed = ReflectionUtils.getFields(playerInfoDataClass, boolean.class).get(0);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
        PlayerInfoData_UUID = ReflectionUtils.getOnlyField(playerInfoDataClass, UUID.class);
        newRemovePacket = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoRemovePacket").getConstructor(List.class);

        actionAddPlayer = Enum.valueOf(actionClass, Action.ADD_PLAYER.name());
        actionUpdateDisplayName = Enum.valueOf(actionClass, Action.UPDATE_DISPLAY_NAME.name());
        actionUpdateLatency = Enum.valueOf(actionClass, Action.UPDATE_LATENCY.name());
        actionUpdateGameMode = Enum.valueOf(actionClass, Action.UPDATE_GAME_MODE.name());

        ADD_PLAYER = EnumSet.allOf(actionClass);
        UPDATE_GAME_MODE = EnumSet.of(actionUpdateGameMode);
        UPDATE_DISPLAY_NAME = EnumSet.of(actionUpdateDisplayName);
        UPDATE_LATENCY = EnumSet.of(actionUpdateLatency);
        UPDATE_LISTED = EnumSet.of(Enum.valueOf(actionClass, Action.UPDATE_LISTED.name()));

        try {
            UPDATE_LIST_ORDER = EnumSet.of(Enum.valueOf(actionClass, Action.UPDATE_LIST_ORDER.name()));
            PlayerInfoData_ListOrder = ReflectionUtils.getFields(playerInfoDataClass, int.class).get(1);
            v1_21_2Plus = true;
            try {
                // 1.21.4+
                UPDATE_HAT = EnumSet.of(Enum.valueOf(actionClass, Action.UPDATE_HAT.name()));
                PlayerInfoData_ShowHat = ReflectionUtils.getFields(playerInfoDataClass, boolean.class).get(1);
                newPlayerInfoData = playerInfoDataClass.getConstructor(UUID.class, GameProfile.class, boolean.class, int.class,
                        EnumGamemodeClass, IChatBaseComponent, boolean.class, int.class, RemoteChatSession$Data);
                v1_21_4Plus = true;
            } catch (Exception e) {
                // 1.21.2 - 1.21.3
                newPlayerInfoData = playerInfoDataClass.getConstructor(UUID.class, GameProfile.class, boolean.class, int.class,
                        EnumGamemodeClass, IChatBaseComponent, int.class, RemoteChatSession$Data);
            }
        } catch (Exception e) {
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
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        packetSender.sendPacket(player,
                createPacket(UPDATE_DISPLAY_NAME, entry, "", null, false, 0, 0, displayName, 0, false));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        packetSender.sendPacket(player,
                createPacket(UPDATE_LATENCY, entry, "", null, false, latency, 0, null, 0, false));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        packetSender.sendPacket(player,
                createPacket(UPDATE_GAME_MODE, entry, "", null, false, 0, gameMode, null, 0, false));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        packetSender.sendPacket(player,
                createPacket(UPDATE_LISTED, entry, "", null, listed, 0, 0, null, 0, false));
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        if (v1_21_2Plus) {
            packetSender.sendPacket(player,
                    createPacket(UPDATE_LIST_ORDER, entry, "", null, false, 0, 0, null, listOrder, false));
        }
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        if (v1_21_4Plus) {
            packetSender.sendPacket(player,
                    createPacket(UPDATE_HAT, entry, "", null, false, 0, 0, null, 0, showHat));
        }
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        packetSender.sendPacket(player,
                createPacket(ADD_PLAYER, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(),
                        entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getListOrder(), entry.isShowHat()));
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        packetSender.sendPacket(player, newHeaderFooter.newInstance(header.convert(), footer.convert()));
    }

    @SneakyThrows
    @NotNull
    private Object createPacket(@NonNull EnumSet<?> actions, @NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                               boolean listed, int latency, int gameMode, @Nullable TabComponent displayName, int listOrder, boolean showHat) {
        Object packet = newPlayerInfo.newInstance(actions, Collections.emptyList());
        PLAYERS.set(packet, Collections.singletonList(newPlayerInfoData(
                id,
                actions.contains(actionAddPlayer) ? createProfile(id, name, skin) : null,
                listed,
                latency,
                gameModes[gameMode],
                displayName == null ? null : displayName.convert(),
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
            int gameMode = ((Enum<?>)PlayerInfoData_GameMode.get(nmsData)).ordinal();
            int listOrder = v1_21_2Plus ? PlayerInfoData_ListOrder.getInt(nmsData) : 0;
            boolean showHat = v1_21_4Plus && PlayerInfoData_ShowHat.getBoolean(nmsData);
            if (actions.contains(actionUpdateDisplayName)) {
                TabComponent expectedName = getForcedDisplayNames().get(id);
                if (expectedName != null && expectedName.convert() != displayName) {
                    displayName = expectedName.convert();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(actionUpdateGameMode)) {
                Integer forcedGameMode = getForcedGameModes().get(id);
                if (forcedGameMode != null && forcedGameMode != gameMode) {
                    gameMode = forcedGameMode;
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
                    gameModes[gameMode],
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
        if (v1_21_4Plus) {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName, showHat, listOrder, chatSession);
        } else if (v1_21_2Plus) {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName,          listOrder, chatSession);
        } else {
            return newPlayerInfoData.newInstance(id, profile, listed, latency, gameMode, displayName,                     chatSession);
        }
    }

    /**
     * Creates GameProfile from given parameters.
     *
     * @param   id
     *          Profile ID
     * @param   name
     *          Profile name
     * @param   skin
     *          Player skin
     * @return  GameProfile from given parameters
     */
    @NotNull
    public GameProfile createProfile(@NonNull UUID id, @NonNull String name, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(id, name);
        if (skin != null) {
            profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return profile;
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Nullable
    @Override
    @SneakyThrows
    public Skin getSkin() {
        Collection<Property> col = ((GameProfile) getProfile.invoke(player.getHandle())).getProperties().get(TabList.TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        if (BukkitReflection.is1_20_2Plus()) {
            return new TabList.Skin(
                    (String) property.getClass().getMethod("value").invoke(property),
                    (String) property.getClass().getMethod("signature").invoke(property)
            );
        } else {
            return new TabList.Skin(property.getValue(), property.getSignature());
        }
    }
}
