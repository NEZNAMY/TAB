package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
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
    private static Class<?> HeaderFooterClass;
    private static Constructor<?> newHeaderFooter;
    private static Field HEADER;
    private static Field FOOTER;

    public static Class<?> PacketPlayOutPlayerInfoClass;
    private static Constructor<?> newPacketPlayOutPlayerInfo;
    public static Field ACTION;
    public static Field PLAYERS;
    private static Class<Enum> EnumPlayerInfoActionClass;
    private static Class<Enum> EnumGamemodeClass;
    public static Class<?> ClientboundPlayerInfoRemovePacket;
    private static Class<?> RemoteChatSession$Data;
    private static Constructor<?> newClientboundPlayerInfoRemovePacket;
    private static Class<?> EntityPlayer;

    public static Constructor<?> newPlayerInfoData;
    public static Method PlayerInfoData_getProfile;
    public static Field PlayerInfoData_Latency;
    public static Field PlayerInfoData_GameMode;
    public static Field PlayerInfoData_DisplayName;
    public static Field PlayerInfoData_Listed;
    public static Field PlayerInfoData_RemoteChatSession;

    @Getter
    private static boolean available;

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    public static void load() throws NoSuchMethodException, ClassNotFoundException {
        if (BukkitReflection.getMinorVersion() < 8) return; // Not supported (yet?)

        // Classes
        Class<?> playerInfoDataClass;
        Class<?> IChatBaseComponent;
        if (BukkitReflection.isMojangMapped()) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.Component");
            EntityPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
            EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.GameType");
            HeaderFooterClass = Class.forName("net.minecraft.network.protocol.game.ClientboundTabListPacket");
            if (BukkitReflection.is1_19_3Plus()) {
                ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
                PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
                EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
                RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$Data");
            } else {
                PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
                EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
            }
        } else if (BukkitReflection.getMinorVersion() >= 17) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
            EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
            EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.EnumGamemode");
            HeaderFooterClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter");
            if (BukkitReflection.is1_19_3Plus()) {
                ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
                PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
                EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$a");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$b");
                RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$a");
            } else {
                PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
                EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                playerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
            }
        } else {
            IChatBaseComponent = BukkitReflection.getLegacyClass("IChatBaseComponent");
            EntityPlayer = BukkitReflection.getLegacyClass("EntityPlayer");
            HeaderFooterClass = BukkitReflection.getLegacyClass("PacketPlayOutPlayerListHeaderFooter");
            PacketPlayOutPlayerInfoClass = BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo");
            EnumPlayerInfoActionClass = (Class<Enum>) BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            playerInfoDataClass = BukkitReflection.getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            EnumGamemodeClass = (Class<Enum>) BukkitReflection.getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
        }

        // Header & Footer
        if (BukkitReflection.getMinorVersion() >= 17) {
            newHeaderFooter = HeaderFooterClass.getConstructor(
                    IChatBaseComponent, IChatBaseComponent);
        } else {
            newHeaderFooter = HeaderFooterClass.getConstructor();
            HEADER = ReflectionUtils.getFields(HeaderFooterClass, IChatBaseComponent).get(0);
            FOOTER = ReflectionUtils.getFields(HeaderFooterClass, IChatBaseComponent).get(1);
        }

        // Info packet
        if (BukkitReflection.is1_19_3Plus()) {
            newClientboundPlayerInfoRemovePacket = ClientboundPlayerInfoRemovePacket.getConstructor(List.class);
            newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfoClass.getConstructor(EnumSet.class, Collection.class);
            ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfoClass, EnumSet.class);
        } else {
            newPacketPlayOutPlayerInfo = PacketPlayOutPlayerInfoClass.getConstructor(EnumPlayerInfoActionClass,
                    Array.newInstance(EntityPlayer, 0).getClass());
            ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfoClass, EnumPlayerInfoActionClass);
        }
        PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfoClass, List.class);

        // Info data
        newPlayerInfoData = ReflectionUtils.getOnlyConstructor(playerInfoDataClass);
        PlayerInfoData_getProfile = ReflectionUtils.getOnlyMethod(playerInfoDataClass, GameProfile.class);
        PlayerInfoData_Latency = ReflectionUtils.getOnlyField(playerInfoDataClass, int.class);
        PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(playerInfoDataClass, EnumGamemodeClass);
        PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(playerInfoDataClass, IChatBaseComponent);
        if (BukkitReflection.is1_19_3Plus()) {
            PlayerInfoData_Listed = ReflectionUtils.getOnlyField(playerInfoDataClass, boolean.class);
            PlayerInfoData_RemoteChatSession = ReflectionUtils.getOnlyField(playerInfoDataClass, RemoteChatSession$Data);
        }

        available = true;
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NotNull UUID entry) {
        if (!available) return;
        if (ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            player.sendPacket(newClientboundPlayerInfoRemovePacket.newInstance(Collections.singletonList(entry)));
        } else {
            //1.19.2-
            player.sendPacket(createPacket(Action.REMOVE_PLAYER, new Entry.Builder(entry).build(), player.getVersion()));
        }
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        if (!available) return;
        player.sendPacket(createPacket(Action.UPDATE_DISPLAY_NAME,
                new Entry.Builder(entry).displayName(displayName).build(), player.getVersion())
        );
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        if (!available) return;
        player.sendPacket(createPacket(Action.UPDATE_LATENCY,
                new Entry.Builder(entry).latency(latency).build(), player.getVersion())
        );
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        if (!available) return;
        player.sendPacket(createPacket(Action.UPDATE_GAME_MODE,
                new Entry.Builder(entry).gameMode(gameMode).build(), player.getVersion())
        );
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        if (!available) return;
        player.sendPacket(createPacket(Action.ADD_PLAYER, entry, player.getVersion()));
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        if (HeaderFooterClass == null) return;
        if (BukkitReflection.getMinorVersion() >= 17) {
            player.sendPacket(newHeaderFooter.newInstance(toComponent(header), toComponent(footer)));
        } else {
            Object packet = newHeaderFooter.newInstance();
            HEADER.set(packet, toComponent(header));
            FOOTER.set(packet, toComponent(footer));
            player.sendPacket(packet);
        }
    }

    @SneakyThrows
    @NotNull
    private Object createPacket(@NotNull TabList.Action action, @NotNull TabList.Entry entry, @NotNull ProtocolVersion clientVersion) {
        Object packet;
        List<Object> players = new ArrayList<>();
        if (BukkitReflection.is1_19_3Plus()) {
            EnumSet<?> actions;
            if (action == TabList.Action.ADD_PLAYER) {
                actions = EnumSet.allOf(EnumPlayerInfoActionClass);
            } else {
                actions = EnumSet.of(Enum.valueOf(EnumPlayerInfoActionClass, action.name()));
            }
            packet = newPacketPlayOutPlayerInfo.newInstance(actions, Collections.emptyList());
            GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
            players.add(newPlayerInfoData.newInstance(
                    entry.getUniqueId(),
                    profile,
                    true,
                    entry.getLatency(),
                    int2GameMode(entry.getGameMode()),
                    entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()),
                    null
            ));
        } else {
            packet = newPacketPlayOutPlayerInfo.newInstance(Enum.valueOf(EnumPlayerInfoActionClass, action.name()),
                    Array.newInstance(EntityPlayer, 0));
            GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
            List<Object> parameters = new ArrayList<>();
            if (newPlayerInfoData.getParameterTypes()[0] == PacketPlayOutPlayerInfoClass) {
                parameters.add(packet);
            }
            parameters.add(profile);
            parameters.add(entry.getLatency());
            parameters.add(int2GameMode(entry.getGameMode()));
            parameters.add(entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()));
            if (BukkitReflection.getMinorVersion() >= 19) parameters.add(null);
            players.add(newPlayerInfoData.newInstance(parameters.toArray()));
        }
        PLAYERS.set(packet, players);
        return packet;
    }

    @NotNull
    private Object int2GameMode(int gameMode) {
        switch (gameMode) {
            case 1: return Enum.valueOf(EnumGamemodeClass, "CREATIVE");
            case 2: return Enum.valueOf(EnumGamemodeClass, "ADVENTURE");
            case 3: return Enum.valueOf(EnumGamemodeClass, "SPECTATOR");
            default: return Enum.valueOf(EnumGamemodeClass, "SURVIVAL");
        }
    }

    private Object toComponent(IChatBaseComponent component) {
        return NMSStorage.getInstance().toNMSComponent(component, player.getVersion());
    }
}
