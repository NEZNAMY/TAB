package me.neznamy.tab.platforms.neoforge;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList implementation for Fabric using packets.
 */
public class NeoForgeTabList extends TrackedTabList<NeoForgeTabPlayer> {

    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> addPlayer = EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateDisplayName = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateLatency = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateGameMode = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateListed = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateListOrder = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateHat = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT);
    
    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public NeoForgeTabList(@NotNull NeoForgeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        sendPacket(updateDisplayName, entry, "", null, false, 0, 0, displayName, 0, false);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendPacket(updateLatency, entry, "", null, false, latency, 0, null, 0, false);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendPacket(updateGameMode, entry, "", null, false, 0, gameMode, null, 0, false);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        sendPacket(updateListed, entry, "", null, listed, 0, 0, null, 0, false);
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        sendPacket(updateListOrder, entry, "", null, false, 0, 0, null, listOrder, false);
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        sendPacket(updateHat, entry, "", null, false, 0, 0, null, 0, showHat);
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        sendPacket(addPlayer, entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(), entry.getLatency(),
                entry.getGameMode(), entry.getDisplayName(), entry.getListOrder(), entry.isShowHat());
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        sendPacket(new ClientboundTabListPacket(header.convert(), footer.convert()));
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Override
    @Nullable
    public Skin getSkin() {
        Collection<Property> properties = player.getPlayer().getGameProfile().getProperties().get(TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        Property property = properties.iterator().next();
        return new Skin(property.value(), property.signature());
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacket info) {
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = info.actions();
            List<ClientboundPlayerInfoUpdatePacket.Entry> updatedList = new ArrayList<>();
            boolean rewritePacket = false;
            for (ClientboundPlayerInfoUpdatePacket.Entry nmsData : info.entries()) {
                boolean rewriteEntry = false;
                Component displayName = nmsData.displayName();
                int latency = nmsData.latency();
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
                    TabComponent expectedDisplayName = getExpectedDisplayNames().get(nmsData.profileId());
                    if (expectedDisplayName != null && expectedDisplayName.convert() != displayName) {
                        displayName = expectedDisplayName.convert();
                        rewriteEntry = rewritePacket = true;
                    }
                }
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)) {
                    int newLatency = TAB.getInstance().getFeatureManager().onLatencyChange(player, nmsData.profileId(), latency);
                    if (newLatency != latency) {
                        latency = newLatency;
                        rewriteEntry = rewritePacket = true;
                    }
                }
                if (actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, nmsData.profileId(), nmsData.profile().getName());
                }
                updatedList.add(rewriteEntry ? new ClientboundPlayerInfoUpdatePacket.Entry(
                        nmsData.profileId(), nmsData.profile(), nmsData.listed(), latency, nmsData.gameMode(), displayName,
                        nmsData.showHat(), nmsData.listOrder(), nmsData.chatSession()
                ) : nmsData);
            }
            if (rewritePacket) info.entries = updatedList;
        }
    }

    private void sendPacket(@NonNull EnumSet<ClientboundPlayerInfoUpdatePacket.Action> action, @NonNull UUID id, @NonNull String name, @Nullable Skin skin,
                            boolean listed, int latency, int gameMode, @Nullable TabComponent displayName, int listOrder, boolean showHat) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(action, Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.Entry(
                id,
                action.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? createProfile(id, name, skin) : null,
                listed,
                latency,
                GameType.byId(gameMode),
                displayName == null ? null : displayName.convert(),
                showHat,
                listOrder,
                null
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
        player.getPlayer().connection.send(packet);
    }
}
