package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

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
    public static Class<?> ProfilePublicKey$a;

    //1.19.3+
    public static Class<?> ClientboundPlayerInfoRemovePacket;
    public static Class<?> RemoteChatSession$Data;
    public static Constructor<?> newClientboundPlayerInfoRemovePacket;
    public static Constructor<?> newRemoteChatSession$Data;

    public static void load(NMSStorage nms) throws ReflectiveOperationException {
        if (nms.getMinorVersion() < 8) return;
        if (nms.is1_19_3Plus()) {
            newClientboundPlayerInfoRemovePacket = ClientboundPlayerInfoRemovePacket.getConstructor(List.class);
            CONSTRUCTOR = CLASS.getConstructor(EnumSet.class, Collection.class);
            ACTION = nms.getFields(CLASS, EnumSet.class).get(0);
            newRemoteChatSession$Data = RemoteChatSession$Data.getConstructor(UUID.class, ProfilePublicKey$a);
         } else {
            CONSTRUCTOR = CLASS.getConstructor(EnumPlayerInfoActionClass, Array.newInstance(nms.EntityPlayer, 0).getClass());
            ACTION = nms.getFields(CLASS, EnumPlayerInfoActionClass).get(0);
        }
        PLAYERS = nms.getFields(CLASS, List.class).get(0);
        PlayerInfoDataStorage.load(nms);
    }

    public static Object createPacket(String action, Collection<TabListEntry> entries, ProtocolVersion clientVersion) {
        NMSStorage nms = NMSStorage.getInstance();
        if (nms.getMinorVersion() < 8) return null;
        try {
            Object packet;
            List<Object> players = new ArrayList<>();
            if (NMSStorage.getInstance().is1_19_3Plus()) {
                EnumSet<?> actions;
                if (action.equals("ADD_PLAYER")) {
                    actions = EnumSet.allOf(EnumPlayerInfoActionClass);
                } else {
                    actions = EnumSet.of(Enum.valueOf(EnumPlayerInfoActionClass, action));
                }
                packet = CONSTRUCTOR.newInstance(actions, Collections.emptyList());
                for (TabListEntry entry : entries) {
                    GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
                    if (entry.getSkin() != null) profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                            new Property(TabList.TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
                    players.add(PlayerInfoDataStorage.newPlayerInfoData.newInstance(
                            entry.getUniqueId(),
                            profile,
                            entry.isListed(),
                            entry.getLatency(),
                            int2GameMode(entry.getGameMode()),
                            nms.toNMSComponent(entry.getDisplayName(), clientVersion),
                            entry.getChatSession()
                    ));
                }
            } else {
                packet = CONSTRUCTOR.newInstance(Enum.valueOf(EnumPlayerInfoActionClass, action),
                        Array.newInstance(NMSStorage.getInstance().EntityPlayer, 0));
                for (TabListEntry entry : entries) {
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
                    parameters.add(nms.toNMSComponent(entry.getDisplayName(), clientVersion));
                    if (nms.getMinorVersion() >= 19) parameters.add(entry.getChatSession());
                    players.add(PlayerInfoDataStorage.newPlayerInfoData.newInstance(parameters.toArray()));
                }
            }
            PLAYERS.set(packet, players);
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object int2GameMode(int gameMode) {
        switch (gameMode) {
            case 1: return Enum.valueOf(EnumGamemodeClass, "CREATIVE");
            case 2: return Enum.valueOf(EnumGamemodeClass, "ADVENTURE");
            case 3: return Enum.valueOf(EnumGamemodeClass, "SPECTATOR");
            default: return Enum.valueOf(EnumGamemodeClass, "SURVIVAL");
        }
    }

    public static int gameMode2Int(Object gameMode) {
        switch (String.valueOf(gameMode)) {
            case "CREATIVE": return 1;
            case "ADVENTURE": return 2;
            case "SPECTATOR": return 3;
            default: return 0;
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
            if (nms.getMinorVersion() < 8) return;
            newPlayerInfoData = CLASS.getConstructors()[0];
            PlayerInfoData_getProfile = nms.getMethods(CLASS, GameProfile.class).get(0);
            PlayerInfoData_Latency = nms.getFields(CLASS, int.class).get(0);
            PlayerInfoData_GameMode = nms.getFields(CLASS, EnumGamemodeClass).get(0);
            PlayerInfoData_DisplayName = nms.getFields(CLASS, nms.IChatBaseComponent).get(0);
            if (nms.is1_19_3Plus()) {
                PlayerInfoData_Listed = nms.getFields(CLASS, boolean.class).get(0);
                PlayerInfoData_RemoteChatSession = nms.getFields(CLASS, RemoteChatSession$Data).get(0);
            }
        }
    }
}
