package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.util.ReflectionUtils;

import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutPlayerInfoStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field ACTION;
    public static Field PLAYERS;
    public static Class<Enum> EnumPlayerInfoActionClass;
    public static Class<Enum> EnumGamemodeClass;

    //1.19.3+
    public static Class<?> ClientboundPlayerInfoRemovePacket;
    public static Class<?> RemoteChatSession$Data;
    public static Constructor<?> newClientboundPlayerInfoRemovePacket;

    @SneakyThrows
    public static void load(NMSStorage nms) {
        if (nms.is1_19_3Plus()) {
            newClientboundPlayerInfoRemovePacket = ClientboundPlayerInfoRemovePacket.getConstructor(List.class);
            CONSTRUCTOR = CLASS.getConstructor(EnumSet.class, Collection.class);
            ACTION = ReflectionUtils.getFields(CLASS, EnumSet.class).get(0);
         } else {
            CONSTRUCTOR = CLASS.getConstructor(EnumPlayerInfoActionClass, Array.newInstance(nms.EntityPlayer, 0).getClass());
            ACTION = ReflectionUtils.getFields(CLASS, EnumPlayerInfoActionClass).get(0);
        }
        PLAYERS = ReflectionUtils.getFields(CLASS, List.class).get(0);
        PlayerInfoDataStorage.load(nms);
    }

    @SneakyThrows
    public static Object createPacket(TabList.Action action, TabList.Entry entry, ProtocolVersion clientVersion) {
        NMSStorage nms = NMSStorage.getInstance();
        if (nms.getMinorVersion() < 8) return null;
        Object packet;
        List<Object> players = new ArrayList<>();
        if (NMSStorage.getInstance().is1_19_3Plus()) {
            EnumSet<?> actions;
            if (action == TabList.Action.ADD_PLAYER) {
                actions = EnumSet.allOf(EnumPlayerInfoActionClass);
            } else {
                actions = EnumSet.of(Enum.valueOf(EnumPlayerInfoActionClass, action.name()));
            }
            packet = CONSTRUCTOR.newInstance(actions, Collections.emptyList());
            GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
            players.add(PlayerInfoDataStorage.newPlayerInfoData.newInstance(
                    entry.getUniqueId(),
                    profile,
                    true,
                    entry.getLatency(),
                    int2GameMode(entry.getGameMode()),
                    entry.getDisplayName() == null ? null : nms.toNMSComponent(entry.getDisplayName(), clientVersion),
                    null
            ));
        } else {
            packet = CONSTRUCTOR.newInstance(Enum.valueOf(EnumPlayerInfoActionClass, action.name()),
                    Array.newInstance(NMSStorage.getInstance().EntityPlayer, 0));
            GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
            List<Object> parameters = new ArrayList<>();
            if (PlayerInfoDataStorage.newPlayerInfoData.getParameterTypes()[0] == CLASS) {
                parameters.add(packet);
            }
            parameters.add(profile);
            parameters.add(entry.getLatency());
            parameters.add(int2GameMode(entry.getGameMode()));
            parameters.add(entry.getDisplayName() == null ? null : nms.toNMSComponent(entry.getDisplayName(), clientVersion));
            if (nms.getMinorVersion() >= 19) parameters.add(null);
            players.add(PlayerInfoDataStorage.newPlayerInfoData.newInstance(parameters.toArray()));
        }
        PLAYERS.set(packet, players);
        return packet;
    }

    public static Object int2GameMode(int gameMode) {
        switch (gameMode) {
            case 1: return Enum.valueOf(EnumGamemodeClass, "CREATIVE");
            case 2: return Enum.valueOf(EnumGamemodeClass, "ADVENTURE");
            case 3: return Enum.valueOf(EnumGamemodeClass, "SPECTATOR");
            default: return Enum.valueOf(EnumGamemodeClass, "SURVIVAL");
        }
    }

    public static class PlayerInfoDataStorage {

        public static Class<?> CLASS;
        public static Constructor<?> newPlayerInfoData;
        public static Method PlayerInfoData_getProfile;
        public static Field PlayerInfoData_Latency;
        public static Field PlayerInfoData_GameMode;
        public static Field PlayerInfoData_DisplayName;

        //1.19.3+
        public static Field PlayerInfoData_Listed;
        public static Field PlayerInfoData_RemoteChatSession;

        public static void load(NMSStorage nms) {
            newPlayerInfoData = CLASS.getConstructors()[0];
            PlayerInfoData_getProfile = ReflectionUtils.getMethods(CLASS, GameProfile.class).get(0);
            PlayerInfoData_Latency = ReflectionUtils.getFields(CLASS, int.class).get(0);
            PlayerInfoData_GameMode = ReflectionUtils.getFields(CLASS, EnumGamemodeClass).get(0);
            PlayerInfoData_DisplayName = ReflectionUtils.getFields(CLASS, nms.IChatBaseComponent).get(0);
            if (nms.is1_19_3Plus()) {
                PlayerInfoData_Listed = ReflectionUtils.getFields(CLASS, boolean.class).get(0);
                PlayerInfoData_RemoteChatSession = ReflectionUtils.getFields(CLASS, RemoteChatSession$Data).get(0);
            }
        }
    }
}
