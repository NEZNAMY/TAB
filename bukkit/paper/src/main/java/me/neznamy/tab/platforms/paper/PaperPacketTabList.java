package me.neznamy.tab.platforms.paper;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TabList implementation using direct mojang-mapped code for versions 1.21.4+.
 */
@SuppressWarnings("unused") // Used via reflection
public class PaperPacketTabList extends TabListBase<Component> {

    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateDisplayName = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateLatency = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateGameMode = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateListed = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateListOrder = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> updateHat = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT);
    private static final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> addPlayer = EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class);

    private static final Field entries;

    static {
        try {
            entries = ReflectionUtils.setAccessible(ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("entries"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public PaperPacketTabList(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable Component displayName) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateDisplayName, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, false, 0, null, displayName, false, 0, null
        )));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateLatency, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, false, latency, null, null, false, 0, null
        )));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateGameMode, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, false, 0, GameType.byId(gameMode), null, false, 0, null
        )));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateListed, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, listed, 0, null, null, false, 0, null
        )));
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateListOrder, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, false, 0, null, null, false, listOrder, null
        )));
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(updateHat, new ClientboundPlayerInfoUpdatePacket.Entry(
                entry, null, false, 0, null, null, showHat, 0, null
        )));
    }

    @Override
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency,
                         int gameMode, @Nullable Component displayName, int listOrder, boolean showHat) {
        sendPacket(new ClientboundPlayerInfoUpdatePacket(addPlayer, new ClientboundPlayerInfoUpdatePacket.Entry(
                id, createProfile(id, name, skin), listed, latency, GameType.byId(gameMode), displayName, showHat, listOrder, null
        )));
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        sendPacket(new ClientboundTabListPacket(header.convert(player.getVersion()), footer.convert(player.getVersion())));
    }

    @Override
    @SneakyThrows
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
                    Component expectedDisplayName = getExpectedDisplayNames().get(nmsData.profileId());
                    if (expectedDisplayName != null) {
                        displayName = expectedDisplayName;
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
            if (rewritePacket) entries.set(info, updatedList);
        }
    }

    @Override
    @Nullable
    public Skin getSkin() {
        Collection<Property> col = ((CraftPlayer)player.getPlayer()).getProfile().getProperties().get(TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        return new TabList.Skin(property.value(), property.signature());
    }

    @NotNull
    private GameProfile createProfile(@NotNull UUID id, @NotNull String name, @Nullable Skin skin) {
        GameProfile profile = new GameProfile(id, name);
        if (skin != null) {
            profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                    new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        }
        return profile;
    }

    private void sendPacket(@NotNull Packet<?> packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().connection.sendPacket(packet);
    }
}
