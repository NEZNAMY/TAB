package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.header.HeaderFooter;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabList.Entry.Builder;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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

    // NMS Fields

    public static Class<?> PlayerInfoClass;
    private static Constructor<?> newPlayerInfo;
    public static Field ACTION;
    public static Field PLAYERS;
    private static Class<Enum> ActionClass;
    public static Class<?> ClientboundPlayerInfoRemovePacket;
    private static Class<?> RemoteChatSession$Data;
    private static Constructor<?> newClientboundPlayerInfoRemovePacket;
    private static Class<?> EntityPlayer;

    public static Constructor<?> newPlayerInfoData;
    public static Field PlayerInfoData_UUID;
    public static Field PlayerInfoData_Profile;
    public static Field PlayerInfoData_Latency;
    public static Field PlayerInfoData_GameMode;
    public static Field PlayerInfoData_DisplayName;
    public static Field PlayerInfoData_Listed;
    public static Field PlayerInfoData_RemoteChatSession;

    private static Object[] gameModes;

    @Nullable
    private static SkinData skinData;

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    public static void load() throws NoSuchMethodException, ClassNotFoundException {
        if (BukkitReflection.getMinorVersion() < 8) return; // Not supported (yet?)

        // Classes
        Class<?> playerInfoDataClass;
        Class<?> IChatBaseComponent;
        Class<Enum> EnumGamemodeClass;
        if (BukkitReflection.isMojangMapped()) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.Component");
            EntityPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.GameType");
            if (BukkitReflection.is1_19_3Plus()) {
                ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
                PlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
                ActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
                RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$Data");
            } else {
                PlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
                ActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
            }
        } else if (BukkitReflection.getMinorVersion() >= 17) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
            EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
            EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.EnumGamemode");
            if (BukkitReflection.is1_19_3Plus()) {
                ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
                PlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
                ActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$a");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$b");
                RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$a");
            } else {
                PlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
                ActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
            }
        } else {
            IChatBaseComponent = BukkitReflection.getLegacyClass("IChatBaseComponent");
            EntityPlayer = BukkitReflection.getLegacyClass("EntityPlayer");
            PlayerInfoClass = BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo");
            ActionClass = (Class<Enum>) BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            playerInfoDataClass = BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            EnumGamemodeClass = (Class<Enum>) BukkitReflection.getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
        }

        PLAYERS = ReflectionUtils.getOnlyField(PlayerInfoClass, List.class);
        newPlayerInfoData = playerInfoDataClass.getConstructors()[0]; // #1105, a specific 1.8.8 fork has 2 constructors
        PlayerInfoData_Profile = ReflectionUtils.getOnlyField(playerInfoDataClass, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getOnlyField(playerInfoDataClass, int.class);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(playerInfoDataClass, IChatBaseComponent);
        if (BukkitReflection.is1_19_3Plus()) {
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
        player.sendPacket(createPacket(Action.UPDATE_DISPLAY_NAME, new Builder(entry).displayName(displayName).build()));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.sendPacket(createPacket(Action.UPDATE_LATENCY, new Builder(entry).latency(latency).build()));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.sendPacket(createPacket(Action.UPDATE_GAME_MODE, new Builder(entry).gameMode(gameMode).build()));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
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

    private Object toComponent(IChatBaseComponent component) {
        return player.getPlatform().toComponent(component, player.getVersion());
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

    private static class SkinData {

        private final Method getProfile;

        @SneakyThrows
        public SkinData() {
            Class<?> EntityHuman = BukkitReflection.getClass("world.entity.player.Player", "world.entity.player.EntityHuman", "EntityHuman");

            // There is only supposed to be one, however there are exceptions:
            // #1 - CatServer adds another method
            // #2 - Random mods may perform deep hack into the server and add another one (see #1089)
            // Get first and hope for the best, alternatively players may not have correct skins in layout, but who cares
            getProfile = ReflectionUtils.getMethods(EntityHuman, GameProfile.class).get(0);
        }

        @Nullable
        @SneakyThrows
        public Skin getSkin(@NotNull BukkitTabPlayer player) {
            Collection<Property> col = ((GameProfile) getProfile.invoke(player.getHandle())).getProperties().get(TEXTURES_PROPERTY);
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
