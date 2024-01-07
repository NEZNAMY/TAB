package me.neznamy.tab.platforms.bukkit.tablist;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TabList handler for 1.19.3+ servers using packets.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketTabList1193 extends TabListBase {

    private static Class<?> PlayerInfoClass;
    private static Constructor<?> newPlayerInfo;
    private static Field ACTION;
    private static Field PLAYERS;
    private static Class<Enum> ActionClass;
    private static Constructor<?> newRemovePacket;

    private static Constructor<?> newPlayerInfoData;
    private static Field PlayerInfoData_UUID;
    private static Field PlayerInfoData_Profile;
    private static Field PlayerInfoData_Latency;
    private static Field PlayerInfoData_GameMode;
    private static Field PlayerInfoData_DisplayName;
    private static Field PlayerInfoData_Listed;
    private static Field PlayerInfoData_RemoteChatSession;

    private static Object[] gameModes;

    private static Method ChatSerializer_DESERIALIZE;
    private static final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    private static PacketSender packetSender;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this tablist will belong to.
     */
    public PacketTabList1193(@NotNull BukkitTabPlayer player) {
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
        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer");
        ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
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

        PLAYERS = ReflectionUtils.getOnlyField(PlayerInfoClass, List.class);
        newPlayerInfoData = playerInfoDataClass.getConstructors()[0];
        PlayerInfoData_Profile = ReflectionUtils.getOnlyField(playerInfoDataClass, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getOnlyField(playerInfoDataClass, int.class);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(playerInfoDataClass, IChatBaseComponent);
        Class<?> RemoteChatSession$Data = BukkitReflection.getClass("network.chat.RemoteChatSession$Data", "network.chat.RemoteChatSession$a");
        newRemovePacket = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoRemovePacket").getConstructor(List.class);
        newPlayerInfo = PlayerInfoClass.getConstructor(EnumSet.class, Collection.class);
        ACTION = ReflectionUtils.getOnlyField(PlayerInfoClass, EnumSet.class);
        PlayerInfoData_Listed = ReflectionUtils.getOnlyField(playerInfoDataClass, boolean.class);
        PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
        PlayerInfoData_UUID = ReflectionUtils.getOnlyField(playerInfoDataClass, UUID.class);
        gameModes = new Object[] {
                Enum.valueOf(EnumGamemodeClass, "SURVIVAL"),
                Enum.valueOf(EnumGamemodeClass, "CREATIVE"),
                Enum.valueOf(EnumGamemodeClass, "ADVENTURE"),
                Enum.valueOf(EnumGamemodeClass, "SPECTATOR")
        };
        packetSender = new PacketSender();
        try {
            skinData = new SkinData();
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "getting player's game profile", null,
                    "Player skins not working in layout feature");
        }
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NotNull UUID entry) {
        packetSender.sendPacket(player.getPlayer(), newRemovePacket.newInstance(Collections.singletonList(entry)));
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        packetSender.sendPacket(player.getPlayer(), createPacket(Action.UPDATE_DISPLAY_NAME, Entry.displayName(entry, displayName)));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        packetSender.sendPacket(player.getPlayer(), createPacket(Action.UPDATE_LATENCY, Entry.latency(entry, latency)));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        packetSender.sendPacket(player.getPlayer(), createPacket(Action.UPDATE_GAME_MODE, Entry.gameMode(entry, gameMode)));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        packetSender.sendPacket(player.getPlayer(), createPacket(Action.ADD_PLAYER, entry));
    }

    @SneakyThrows
    @NotNull
    private Object createPacket(@NotNull Action action, @NotNull Entry entry) {
        List<Object> players = new ArrayList<>();
        EnumSet<?> actions;
        if (action == Action.ADD_PLAYER) {
            actions = EnumSet.allOf(ActionClass);
        } else {
            actions = EnumSet.of(Enum.valueOf(ActionClass, action.name()));
        }
        Object packet = newPlayerInfo.newInstance(actions, Collections.emptyList());
        players.add(newPlayerInfoData.newInstance(
                entry.getUniqueId(),
                createProfile(entry.getUniqueId(), entry.getName(), entry.getSkin()),
                true,
                entry.getLatency(),
                gameModes[entry.getGameMode()],
                entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()),
                null
        ));
        PLAYERS.set(packet, players);
        return packet;
    }

    /**
     * Converts TAB component into NMS component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    public Object toComponent(@NotNull IChatBaseComponent component) {
        return componentCache.get(component, player.getVersion());
    }

    @NotNull
    private GameProfile createProfile(@NotNull UUID id, @Nullable String name, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(id, name == null ? "" : name);
        if (skin != null) {
            profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return profile;
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NotNull Object packet) {
        if (!(PlayerInfoClass.isInstance(packet))) return;
        List<String> actions = ((EnumSet<?>)ACTION.get(packet)).stream().map(Enum::name).collect(Collectors.toList());
        List<Object> updatedList = new ArrayList<>();
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            UUID id;
            id = (UUID) PlayerInfoData_UUID.get(nmsData);
            Object displayName = null;
            int latency = 0;
            if (actions.contains(Action.UPDATE_DISPLAY_NAME.name())) {
                displayName = PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(player, id);
                if (newDisplayName != null) displayName = toComponent(newDisplayName);
            }
            if (actions.contains(Action.UPDATE_LATENCY.name())) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, PlayerInfoData_Latency.getInt(nmsData));
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
