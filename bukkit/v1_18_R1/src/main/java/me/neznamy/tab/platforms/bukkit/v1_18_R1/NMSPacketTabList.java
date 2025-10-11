package me.neznamy.tab.platforms.bukkit.v1_18_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TabList implementation using direct NMS code.
 */
public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {

    private static final Field PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, List.class);

    private static final EnumPlayerInfoAction ADD_PLAYER = EnumPlayerInfoAction.a;
    private static final EnumPlayerInfoAction UPDATE_GAME_MODE = EnumPlayerInfoAction.b;
    private static final EnumPlayerInfoAction UPDATE_LATENCY = EnumPlayerInfoAction.c;
    private static final EnumPlayerInfoAction UPDATE_DISPLAY_NAME = EnumPlayerInfoAction.d;

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
        sendPacket(EnumPlayerInfoAction.e, entry, "", null, 0, 0, null);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        sendPacket(EnumPlayerInfoAction.d, entry, "", null, 0, 0, displayName);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendPacket(EnumPlayerInfoAction.c, entry, "", null, latency, 0, null);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendPacket(EnumPlayerInfoAction.b, entry, "", null, 0, gameMode, null);
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
        sendPacket(EnumPlayerInfoAction.a, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.getLatency(),
                entry.getGameMode(), entry.getDisplayName());
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        sendPacket(new PacketPlayOutPlayerListHeaderFooter(header.convert(), footer.convert()));
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

    @Override
    @SneakyThrows
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
            PacketPlayOutPlayerListHeaderFooter tablist = (PacketPlayOutPlayerListHeaderFooter) packet;
            if (header == null || footer == null) return packet;
            if (tablist.a != header.convert() || tablist.b != footer.convert()) {
                return new PacketPlayOutPlayerListHeaderFooter(header.convert(), footer.convert());
            }
        }
        if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
        PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
        EnumPlayerInfoAction action = info.c();
        List<PlayerInfoData> updatedList = new ArrayList<>();
        boolean rewritePacket = false;
        for (PlayerInfoData nmsData : info.b()) {
            boolean rewriteEntry = false;
            GameProfile profile = nmsData.a();
            UUID id = profile.getId();
            IChatBaseComponent displayName = nmsData.d();
            int latency = nmsData.b();
            int gameMode = nmsData.c().a();
            if (action == UPDATE_DISPLAY_NAME || action == ADD_PLAYER) {
                TabComponent forcedDisplayName = getForcedDisplayNames().get(id);
                if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                    displayName = forcedDisplayName.convert();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == UPDATE_GAME_MODE || action == ADD_PLAYER) {
                Integer forcedGameMode = getForcedGameModes().get(id);
                if (forcedGameMode != null && forcedGameMode != gameMode) {
                    gameMode = forcedGameMode;
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == UPDATE_LATENCY || action == ADD_PLAYER) {
                if (getForcedLatency() != null) {
                    latency = getForcedLatency();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (action == ADD_PLAYER) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, id, profile.getName());
            }
            updatedList.add(rewriteEntry ? new PlayerInfoData(
                    profile,
                    latency,
                    EnumGamemode.a(gameMode),
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
        PLAYERS.set(packet, Collections.singletonList(new PlayerInfoData(
                createProfile(id, name, skin),
                latency,
                EnumGamemode.a(gameMode),
                displayName == null ? null : displayName.convert())
        ));
        sendPacket(packet);
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
        ((CraftPlayer)player.getPlayer()).getHandle().b.a(packet);
    }
}
