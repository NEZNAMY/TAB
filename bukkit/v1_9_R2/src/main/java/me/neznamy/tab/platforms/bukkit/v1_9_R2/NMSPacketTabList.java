package me.neznamy.tab.platforms.bukkit.v1_9_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_9_R2.WorldSettings.EnumGamemode;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * TabList implementation using direct NMS code.
 */
public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {

    // PlayerInfoData subclass is broken, using reflection to get it
    private static final Class<?> PlayerInfoData = Arrays.stream(PacketPlayOutPlayerInfo.class.getDeclaredClasses()).filter(c -> !c.isEnum() && c.getConstructors().length > 0).findFirst().get();
    private static final Constructor<?> newPlayerInfoData = ReflectionUtils.getOnlyConstructor(PlayerInfoData);

    private static final Field ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, EnumPlayerInfoAction.class);
    private static final Field PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, List.class);

    private static final Field PlayerInfoData_Profile = ReflectionUtils.getOnlyField(PlayerInfoData, GameProfile.class);
    private static final Field PlayerInfoData_Latency = ReflectionUtils.getFields(PlayerInfoData, int.class).get(0);
    private static final Field PlayerInfoData_DisplayName = ReflectionUtils.getOnlyField(PlayerInfoData, IChatBaseComponent.class);
    private static final Field PlayerInfoData_GameMode = ReflectionUtils.getOnlyField(PlayerInfoData, EnumGamemode.class);

    private static final Field HEADER = ReflectionUtils.getFields(PacketPlayOutPlayerListHeaderFooter.class, IChatBaseComponent.class).get(0);
    private static final Field FOOTER = ReflectionUtils.getFields(PacketPlayOutPlayerListHeaderFooter.class, IChatBaseComponent.class).get(1);

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public NMSPacketTabList(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    @SneakyThrows
    public void removeEntry(@NonNull UUID entry) {
        sendPacket(EnumPlayerInfoAction.REMOVE_PLAYER, entry, "", null, 0, 0, null);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        sendPacket(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, entry, "", null, 0, 0, displayName);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendPacket(EnumPlayerInfoAction.UPDATE_LATENCY, entry, "", null, latency, 0, null);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendPacket(EnumPlayerInfoAction.UPDATE_GAME_MODE, entry, "", null, 0, gameMode, null);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Added in 1.21.2
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Added in 1.21.4
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        sendPacket(EnumPlayerInfoAction.ADD_PLAYER, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.getLatency(),
                entry.getGameMode(), entry.getDisplayName());
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        sendPacket(newHeaderFooter(header, footer));
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Override
    @Nullable
    public Skin getSkin() {
        Collection<Property> properties = ((CraftPlayer)player.getPlayer()).getProfile().getProperties().get(TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        Property property = properties.iterator().next();
        return new Skin(property.getValue(), property.getSignature());
    }

    @NonNull
    @SneakyThrows
    private PacketPlayOutPlayerListHeaderFooter newHeaderFooter(@NotNull TabComponent header, @NonNull TabComponent footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(header.convert());
        FOOTER.set(packet, footer.convert());
        return packet;
    }

    @Override
    @SneakyThrows
    @NotNull
    @SuppressWarnings("unchecked")
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
            PacketPlayOutPlayerListHeaderFooter tablist = (PacketPlayOutPlayerListHeaderFooter) packet;
            if (header == null || footer == null) return packet;
            IChatBaseComponent header = (IChatBaseComponent) HEADER.get(tablist);
            IChatBaseComponent footer = (IChatBaseComponent) FOOTER.get(tablist);
            if (header != this.header.convert() || footer != this.footer.convert()) {
                return newHeaderFooter(this.header, this.footer);
            }
        }
        if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
        PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
        EnumPlayerInfoAction action = (EnumPlayerInfoAction) ACTION.get(info);
        List<Object> updatedList = new ArrayList<>();
        boolean rewritePacket = false;
        for (Object nmsData : (List<Object>) PLAYERS.get(info)) {
            boolean rewriteEntry = false;
            GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
            UUID id = profile.getId();
            IChatBaseComponent displayName = (IChatBaseComponent) PlayerInfoData_DisplayName.get(nmsData);
            int latency = PlayerInfoData_Latency.getInt(nmsData);
            int gameMode = ((EnumGamemode)PlayerInfoData_GameMode.get(nmsData)).getId();
            if (action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME || action == EnumPlayerInfoAction.ADD_PLAYER) {
                TabComponent forcedDisplayName = getForcedDisplayNames().get(id);
                if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                    displayName = forcedDisplayName.convert();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == EnumPlayerInfoAction.UPDATE_GAME_MODE || action == EnumPlayerInfoAction.ADD_PLAYER) {
                Integer forcedGameMode = getForcedGameModes().get(id);
                if (forcedGameMode != null && forcedGameMode != gameMode) {
                    gameMode = forcedGameMode;
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == EnumPlayerInfoAction.UPDATE_LATENCY || action == EnumPlayerInfoAction.ADD_PLAYER) {
                if (getForcedLatency() != null) {
                    latency = getForcedLatency();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == EnumPlayerInfoAction.ADD_PLAYER) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
            updatedList.add(rewriteEntry ? newPlayerInfoData(
                    (PacketPlayOutPlayerInfo) packet,
                    profile,
                    latency,
                    EnumGamemode.getById(gameMode),
                    displayName
            ) : nmsData);
        }
        if (rewritePacket) {
            PacketPlayOutPlayerInfo newPacket = new PacketPlayOutPlayerInfo(action, Collections.emptyList());
            PLAYERS.set(newPacket, updatedList);
            return newPacket;
        }
        return packet;
    }

    @SneakyThrows
    private void sendPacket(@NonNull EnumPlayerInfoAction action, @NonNull UUID id, @NonNull String name,
                            @Nullable Skin skin, int latency, int gameMode, @Nullable TabComponent displayName) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(action);
        PLAYERS.set(packet, Collections.singletonList(newPlayerInfoData(
                packet,
                createProfile(id, name, skin),
                latency,
                EnumGamemode.getById(gameMode),
                displayName == null ? null : displayName.convert())
        ));
        sendPacket(packet);
    }

    @NotNull
    @SneakyThrows
    private Object newPlayerInfoData(@NonNull PacketPlayOutPlayerInfo packet, @NonNull GameProfile profile,
                                     int latency, @NonNull EnumGamemode gameMode, @Nullable IChatBaseComponent displayName) {
        return newPlayerInfoData.newInstance(packet, profile, latency, gameMode, displayName);
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
    private GameProfile createProfile(@NonNull UUID id, @NonNull String name, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(id, name);
        if (skin != null) {
            profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return profile;
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }
}
