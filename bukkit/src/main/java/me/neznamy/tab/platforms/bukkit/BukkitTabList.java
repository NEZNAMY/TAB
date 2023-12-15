package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabList.Entry.Builder;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TabList which support modifying many entries at once
 * for significantly better performance. For 1.7 players,
 * ViaVersion properly splits the packet into multiple, so
 * we don't need to worry about that here.
 * <p>
 * This class does not support server versions of 1.7 and
 * below, because of the massive differences in tab list
 * and packet fields.
 */
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitTabList implements TabList {

    private static Class<?> PlayerInfoClass;
    private static Constructor<?> newPlayerInfo;
    private static Field ACTION;
    private static Field PLAYERS;
    private static Class<Enum> ActionClass;
    private static Class<?> ClientboundPlayerInfoRemovePacket;
    private static Constructor<?> newClientboundPlayerInfoRemovePacket;
    private static Class<?> EntityPlayer;

    private static Constructor<?> newPlayerInfoData;
    private static Field PlayerInfoData_UUID;
    private static Field PlayerInfoData_Profile;
    private static Field PlayerInfoData_Latency;
    private static Field PlayerInfoData_GameMode;
    private static Field PlayerInfoData_DisplayName;
    private static Field PlayerInfoData_Listed;
    private static Field PlayerInfoData_RemoteChatSession;

    private static Object[] gameModes;

    @Nullable
    private static SkinData skinData;

    private static Method ChatSerializer_DESERIALIZE;
    private static final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    public static void load() throws NoSuchMethodException, ClassNotFoundException {
        if (BukkitReflection.getMinorVersion() < 8) return; // Not supported

        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
        ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
        Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        Class<Enum> EnumGamemodeClass = (Class<Enum>) BukkitReflection.getClass("world.level.GameType",
                "world.level.EnumGamemode", "EnumGamemode", "WorldSettings$EnumGamemode");
        EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
        ActionClass = (Class<Enum>) BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action", // Mojang 1.19.3+
                "network.protocol.game.ClientboundPlayerInfoPacket$Action", // Mojang 1.17 - 1.19.2
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$a", // Bukkit 1.19.3+
                "network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction", // Bukkit 1.17 - 1.19.2
                "PacketPlayOutPlayerInfo$EnumPlayerInfoAction", // Bukkit 1.8.1 - 1.16.5
                "EnumPlayerInfoAction" // Bukkit 1.8.0
        );
        PlayerInfoClass = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoUpdatePacket",
                "network.protocol.game.ClientboundPlayerInfoPacket",
                "network.protocol.game.PacketPlayOutPlayerInfo", "PacketPlayOutPlayerInfo");
        Class<?> playerInfoDataClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry", // Mojang 1.19.3
                "network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate", // Mojang 1.17 - 1.19.2
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$b", // Bukkit 1.19.3+
                "network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData", // Bukkit 1.17 - 1.19.2
                "PacketPlayOutPlayerInfo$PlayerInfoData", // Bukkit 1.8.1 - 1.16.5
                "PlayerInfoData" // Bukkit 1.8.0
        );

        PLAYERS = ReflectionUtils.getOnlyField(PlayerInfoClass, List.class);
        newPlayerInfoData = playerInfoDataClass.getConstructors()[0]; // #1105, a specific 1.8.8 fork has 2 constructors
        PlayerInfoData_Profile = ReflectionUtils.getOnlyField(playerInfoDataClass, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getOnlyField(playerInfoDataClass, int.class);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(playerInfoDataClass, IChatBaseComponent);
        if (BukkitReflection.is1_19_3Plus()) {
            ClientboundPlayerInfoRemovePacket = BukkitReflection.getClass("network.protocol.game.ClientboundPlayerInfoRemovePacket");
            Class<?> RemoteChatSession$Data = BukkitReflection.getClass("network.chat.RemoteChatSession$Data", "network.chat.RemoteChatSession$a");
            newClientboundPlayerInfoRemovePacket = ClientboundPlayerInfoRemovePacket.getConstructor(List.class);
            newPlayerInfo = PlayerInfoClass.getConstructor(EnumSet.class, Collection.class);
            ACTION = ReflectionUtils.getOnlyField(PlayerInfoClass, EnumSet.class);
            PlayerInfoData_Listed = ReflectionUtils.getOnlyField(playerInfoDataClass, boolean.class);
            PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
            PlayerInfoData_UUID = ReflectionUtils.getOnlyField(playerInfoDataClass, UUID.class);
        } else {
            newPlayerInfo = PlayerInfoClass.getConstructor(ActionClass, Array.newInstance(EntityPlayer, 0).getClass());
            ACTION = ReflectionUtils.getOnlyField(PlayerInfoClass, ActionClass);
        }
        gameModes = new Object[] {
                Enum.valueOf(EnumGamemodeClass, "SURVIVAL"),
                Enum.valueOf(EnumGamemodeClass, "CREATIVE"),
                Enum.valueOf(EnumGamemodeClass, "ADVENTURE"),
                Enum.valueOf(EnumGamemodeClass, "SPECTATOR")
        };

        try {
            skinData = new SkinData();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "getting player's game profile due to a compatibility error. This will " +
                    "result in player skins not properly working in layout feature. " +
                    "Please update the plugin a to version with native support for your server version for optimal experience.");
        }
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NotNull UUID entry) {
        if (PlayerInfoClass == null) return;
        if (ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            player.sendPacket(newClientboundPlayerInfoRemovePacket.newInstance(Collections.singletonList(entry)));
        } else {
            //1.19.2-
            player.sendPacket(createPacket(Action.REMOVE_PLAYER, new Builder(entry).build()));
        }
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        if (PlayerInfoClass == null) return;
        player.sendPacket(createPacket(Action.UPDATE_DISPLAY_NAME, new Builder(entry).displayName(displayName).build()));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        if (PlayerInfoClass == null) return;
        player.sendPacket(createPacket(Action.UPDATE_LATENCY, new Builder(entry).latency(latency).build()));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        if (PlayerInfoClass == null) return;
        player.sendPacket(createPacket(Action.UPDATE_GAME_MODE, new Builder(entry).gameMode(gameMode).build()));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        if (PlayerInfoClass == null) return;
        player.sendPacket(createPacket(Action.ADD_PLAYER, entry));
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        if (HeaderFooter.getInstance() != null) HeaderFooter.getInstance().set(player, header, footer);
    }

    @SneakyThrows
    @NotNull
    private Object createPacket(@NotNull Action action, @NotNull Entry entry) {
        Object packet;
        List<Object> players = new ArrayList<>();
        if (BukkitReflection.is1_19_3Plus()) {
            EnumSet<?> actions;
            if (action == Action.ADD_PLAYER) {
                actions = EnumSet.allOf(ActionClass);
            } else {
                actions = EnumSet.of(Enum.valueOf(ActionClass, action.name()));
            }
            packet = newPlayerInfo.newInstance(actions, Collections.emptyList());
            players.add(newPlayerInfoData.newInstance(
                    entry.getUniqueId(),
                    createProfile(entry.getUniqueId(), entry.getName(), entry.getSkin()),
                    true,
                    entry.getLatency(),
                    gameModes[entry.getGameMode()],
                    entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()),
                    null
            ));
        } else {
            packet = newPlayerInfo.newInstance(Enum.valueOf(ActionClass, action.name()), Array.newInstance(EntityPlayer, 0));
            List<Object> parameters = new ArrayList<>();
            if (newPlayerInfoData.getParameterTypes()[0] == PlayerInfoClass) {
                parameters.add(packet);
            }
            parameters.add(createProfile(entry.getUniqueId(), entry.getName(), entry.getSkin()));
            parameters.add(entry.getLatency());
            parameters.add(gameModes[entry.getGameMode()]);
            parameters.add(entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()));
            if (BukkitReflection.getMinorVersion() >= 19) parameters.add(null);
            players.add(newPlayerInfoData.newInstance(parameters.toArray()));
        }
        PLAYERS.set(packet, players);
        return packet;
    }

    public Object toComponent(IChatBaseComponent component) {
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

    @Nullable
    public Skin getSkin() {
        if (skinData == null) return null;
        return skinData.getSkin(player);
    }

    @Override
    public void onPacketSend(@NotNull Object packet) {
        if (PlayerInfoClass == null || !PlayerInfoClass.isInstance(packet)) return;
        if (BukkitReflection.is1_19_3Plus()) {
            onPlayerInfo1_19_3(packet);
        } else {
            onPlayerInfo1_19_2(packet);
        }
    }

    @SneakyThrows
    private void onPlayerInfo1_19_3(@NotNull Object packet) {
        List<String> actions = ((EnumSet<?>)ACTION.get(packet)).stream().map(Enum::name).collect(Collectors.toList());
        List<Object> updatedList = new ArrayList<>();
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            UUID id;
            id = (UUID) PlayerInfoData_UUID.get(nmsData);
            Object displayName = null;
            int latency = 0;
            if (actions.contains(TabList.Action.UPDATE_DISPLAY_NAME.name())) {
                displayName = PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(player, id);
                if (newDisplayName != null) displayName = toComponent(newDisplayName);
            }
            if (actions.contains(TabList.Action.UPDATE_LATENCY.name())) {
                latency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, PlayerInfoData_Latency.getInt(nmsData));
            }
            if (actions.contains(TabList.Action.ADD_PLAYER.name())) {
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

    @SneakyThrows
    private void onPlayerInfo1_19_2(@NotNull Object packet) {
        String action = ACTION.get(packet).toString();
        for (Object nmsData : (List<?>) PLAYERS.get(packet)) {
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            UUID id = profile.getId();
            if (action.equals(TabList.Action.UPDATE_DISPLAY_NAME.name()) || action.equals(TabList.Action.ADD_PLAYER.name())) {
                Object displayName = PlayerInfoData_DisplayName.get(nmsData);
                IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(player, id);
                if (newDisplayName != null) displayName = toComponent(newDisplayName);
                PlayerInfoData_DisplayName.set(nmsData, displayName);
            }
            if (action.equals(TabList.Action.UPDATE_LATENCY.name()) || action.equals(TabList.Action.ADD_PLAYER.name())) {
                int latency = TAB.getInstance().getFeatureManager().onLatencyChange(player, id, PlayerInfoData_Latency.getInt(nmsData));
                PlayerInfoData_Latency.set(nmsData, latency);
            }
            if (action.equals(TabList.Action.ADD_PLAYER.name())) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
        }
    }

    private static class SkinData {

        private final Method getHandle;
        private final Method getProfile;

        @SneakyThrows
        public SkinData() {
            Class<?> EntityHuman = BukkitReflection.getClass("world.entity.player.Player", "world.entity.player.EntityHuman", "EntityHuman");
            getHandle = BukkitReflection.getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
            // There is only supposed to be one, however there are exceptions:
            // #1 - CatServer adds another method
            // #2 - Random mods may perform deep hack into the server and add another one (see #1089)
            // Get first and hope for the best, alternatively players may not have correct skins in layout, but who cares
            getProfile = ReflectionUtils.getMethods(EntityHuman, GameProfile.class).get(0);
        }

        @Nullable
        @SneakyThrows
        public Skin getSkin(@NotNull BukkitTabPlayer player) {
            Collection<Property> col = ((GameProfile) getProfile.invoke(getHandle.invoke(player.getPlayer()))).getProperties().get(TEXTURES_PROPERTY);
            if (col.isEmpty()) return null; //offline mode
            Property property = col.iterator().next();
            if (BukkitReflection.is1_20_2Plus()) {
                return new Skin(
                        (String) property.getClass().getMethod("value").invoke(property),
                        (String) property.getClass().getMethod("signature").invoke(property)
                );
            } else {
                return new Skin(property.getValue(), property.getSignature());
            }
        }
    }
}
