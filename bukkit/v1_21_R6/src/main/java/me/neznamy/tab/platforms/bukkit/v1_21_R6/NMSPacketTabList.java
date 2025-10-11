package me.neznamy.tab.platforms.bukkit.v1_21_R6;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.a;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TabList implementation using direct NMS code.
 */
public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {

    private static final Field PLAYERS = ReflectionUtils.getOnlyField(ClientboundPlayerInfoUpdatePacket.class, List.class);

    private static final ClientboundPlayerInfoUpdatePacket.a ADD_PLAYER = a.valueOf("ADD_PLAYER");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_GAME_MODE = a.valueOf("UPDATE_GAME_MODE");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_LATENCY = a.valueOf("UPDATE_LATENCY");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_DISPLAY_NAME = a.valueOf("UPDATE_DISPLAY_NAME");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_LISTED = a.valueOf("UPDATE_LISTED");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_LIST_ORDER = a.valueOf("UPDATE_LIST_ORDER");
    private static final ClientboundPlayerInfoUpdatePacket.a UPDATE_HAT = a.valueOf("UPDATE_HAT");

    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> ADD_PLAYER_SET = EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.a.class);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_GAME_MODE_SET = EnumSet.of(UPDATE_GAME_MODE);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_DISPLAY_NAME_SET = EnumSet.of(UPDATE_DISPLAY_NAME);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_LATENCY_SET = EnumSet.of(UPDATE_LATENCY);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_LISTED_SET = EnumSet.of(UPDATE_LISTED);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_LIST_ORDER_SET = EnumSet.of(UPDATE_LIST_ORDER);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.a> UPDATE_HAT_SET = EnumSet.of(UPDATE_HAT);

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
        sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        sendPacket(UPDATE_DISPLAY_NAME_SET, entry, "", null, false, 0, 0, displayName, 0, false);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendPacket(UPDATE_LATENCY_SET, entry, "", null, false, latency, 0, null, 0, false);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendPacket(UPDATE_GAME_MODE_SET, entry, "", null, false, 0, gameMode, null, 0, false);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        sendPacket(UPDATE_LISTED_SET, entry, "", null, listed, 0, 0, null, 0, false);
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        sendPacket(UPDATE_LIST_ORDER_SET, entry, "", null, false, 0, 0, null, listOrder, false);
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        sendPacket(UPDATE_HAT_SET, entry, "", null, false, 0, 0, null, 0, showHat);
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        sendPacket(ADD_PLAYER_SET, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(), entry.getLatency(),
                entry.getGameMode(), entry.getDisplayName(), entry.getListOrder(), entry.isShowHat());
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
        Collection<Property> properties = ((CraftPlayer)player.getPlayer()).getProfile().properties().get(TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        Property property = properties.iterator().next();
        return new Skin(property.value(), property.signature());
    }

    @SneakyThrows
    @Override
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerListHeaderFooter tablist) {
            if (header == null || footer == null) return packet;
            if (tablist.b != header.convert() || tablist.c != footer.convert()) {
                return new PacketPlayOutPlayerListHeaderFooter(header.convert(), footer.convert());
            }
        }
        if (!(packet instanceof ClientboundPlayerInfoUpdatePacket info)) return packet;
        EnumSet<ClientboundPlayerInfoUpdatePacket.a> actions = info.b();
        List<ClientboundPlayerInfoUpdatePacket.b> updatedList = new ArrayList<>();
        boolean rewritePacket = false;
        for (ClientboundPlayerInfoUpdatePacket.b nmsData : info.e()) {
            UUID profileId = nmsData.a();
            boolean rewriteEntry = false;
            IChatBaseComponent displayName = nmsData.f();
            int latency = nmsData.d();
            int gameMode = nmsData.e().a();
            if (actions.contains(UPDATE_DISPLAY_NAME)) {
                TabComponent forcedDisplayName = getForcedDisplayNames().get(profileId);
                if (forcedDisplayName != null && forcedDisplayName.convert() != displayName) {
                    displayName = forcedDisplayName.convert();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(UPDATE_GAME_MODE)) {
                Integer forcedGameMode = getForcedGameModes().get(profileId);
                if (forcedGameMode != null && forcedGameMode != gameMode) {
                    gameMode = forcedGameMode;
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(UPDATE_LATENCY)) {
                if (getForcedLatency() != null) {
                    latency = getForcedLatency();
                    rewriteEntry = rewritePacket = true;
                }
            }
            if (actions.contains(ADD_PLAYER)) {
                TAB.getInstance().getFeatureManager().onEntryAdd(player, profileId, nmsData.b().name());
            }
            updatedList.add(rewriteEntry ? new ClientboundPlayerInfoUpdatePacket.b(
                    profileId, nmsData.b(), nmsData.c(), latency, EnumGamemode.a(gameMode), displayName,
                    nmsData.g(), nmsData.h(), nmsData.i()
            ) : nmsData);
        }
        if (rewritePacket) {
            ClientboundPlayerInfoUpdatePacket newPacket = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
            PLAYERS.set(newPacket, updatedList);
            return newPacket;
        }
        return packet;
    }

    @SneakyThrows
    private void sendPacket(@NonNull EnumSet<ClientboundPlayerInfoUpdatePacket.a> action, @NonNull UUID id, @NonNull String name,
                            @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable TabComponent displayName, int listOrder,
                            boolean showHat) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(action, Collections.emptyList());
        PLAYERS.set(packet, Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.b(
                id,
                createProfile(id, name, skin),
                listed,
                latency,
                EnumGamemode.a(gameMode),
                displayName == null ? null : displayName.convert(),
                showHat,
                listOrder,
                null
        )));
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
        ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
        if (skin != null) {
            builder.put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return new GameProfile(id, name, new PropertyMap(builder.build()));
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().g.b(packet);
    }
}
