package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItemPacket;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfoPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList implementation for Velocity using its API where possible + packet interception.
 */
public class VelocityTabList extends TrackedTabList<VelocityTabPlayer> {

    private static final int ADD_PLAYER = 0;
    private static final int UPDATE_GAMEMODE = 1;
    private static final int UPDATE_LATENCY = 2;
    private static final int UPDATE_DISPLAY_NAME = 3;
    private static final int REMOVE_PLAYER = 4;
    
    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public VelocityTabList(@NotNull VelocityTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setDisplayName(renderDisplayName(displayName)));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(gameMode));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setListed(listed));
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setListOrder(listOrder));
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setShowHat(showHat));
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName(), entry.getSkin() == null ? Collections.emptyList() : Collections.singletonList(
                        new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature()))));
        TabListEntry e = TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(profile)
                .displayName(this.renderDisplayName(entry.getDisplayName()))
                .latency(entry.getLatency())
                .gameMode(entry.getGameMode())
                .listed(entry.isListed())
                .listOrder(entry.getListOrder())
                .showHat(entry.isShowHat())
                .build();

        // Remove entry because:
        // #1 - If player is 1.8 - 1.19.2, KeyedVelocityTabList#addEntry will throw IllegalArgumentException
        //      if the entry is already present (most likely due to an accident trying to add existing player in global playerlist)
        // #2 - If player is 1.20.2+, tablist is cleared by the client itself without requirement to remove
        //      manually by the proxy, however velocity's tablist entry tracker still thinks they are present
        //      and therefore will refuse to add them
        removeEntry(entry.getUniqueId());

        player.getPlayer().getTabList().addEntry(e);
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(), footer.toAdventure());
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().getTabList().containsEntry(entry);
    }

    @Override
    @Nullable
    public Skin getSkin() {
        List<GameProfile.Property> properties = player.getPlayer().getGameProfile().getProperties();
        if (properties.isEmpty()) return null; //Offline mode
        for (GameProfile.Property property : properties) {
            if (property.getName().equals(TEXTURES_PROPERTY)) {
                return new Skin(property.getValue(), property.getSignature());
            }
        }
        return null;
    }

    @Override
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        if (packet instanceof LegacyPlayerListItemPacket listItem) {
            for (LegacyPlayerListItemPacket.Item item : listItem.getItems()) {
                if (listItem.getAction() == UPDATE_DISPLAY_NAME || listItem.getAction() == ADD_PLAYER) {
                    TabComponent forcedDisplayName = getForcedDisplayNames().get(item.getUuid());
                    if (forcedDisplayName != null) item.setDisplayName(this.renderDisplayName(forcedDisplayName));
                }
                if (listItem.getAction() == UPDATE_GAMEMODE || listItem.getAction() == ADD_PLAYER) {
                    if (getBlockedSpectators().contains(item.getUuid()) && item.getGameMode() == 3) {
                        item.setGameMode(0);
                    }
                }
                if (listItem.getAction() == UPDATE_LATENCY || listItem.getAction() == ADD_PLAYER) {
                    if (getForcedLatency() != null) {
                        item.setLatency(getForcedLatency());
                    }
                }
                if (listItem.getAction() == ADD_PLAYER) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getUuid(), item.getName());
                }
            }
        } else if (packet instanceof UpsertPlayerInfoPacket update) {
            for (UpsertPlayerInfoPacket.Entry item : update.getEntries()) {
                if (update.getActions().contains(UpsertPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME)) {
                    TabComponent forcedDisplayName = getForcedDisplayNames().get(item.getProfileId());
                    if (forcedDisplayName != null) {
                        item.setDisplayName(new ComponentHolder(ProtocolVersion.getProtocolVersion(player.getVersionId()), this.renderDisplayName(forcedDisplayName)));
                    }
                }
                if (update.getActions().contains(UpsertPlayerInfoPacket.Action.UPDATE_GAME_MODE)) {
                    if (getBlockedSpectators().contains(item.getProfileId()) && item.getGameMode() == 3) {
                        item.setGameMode(0);
                    }
                }
                if (update.getActions().contains(UpsertPlayerInfoPacket.Action.UPDATE_LATENCY)) {
                    if (getForcedLatency() != null) {
                        item.setLatency(getForcedLatency());
                    }
                }
                if (update.getActions().contains(UpsertPlayerInfoPacket.Action.UPDATE_LISTED)) {
                    if (allPlayersHidden && item.getProfileId().getMostSignificantBits() != 0) { // Filter out layout entries
                        item.setListed(false);
                    }
                }
                if (update.getActions().contains(UpsertPlayerInfoPacket.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(player, item.getProfileId(), item.getProfile().getName());
                }
            }
        }
        return packet;
    }

    private @Nullable Component renderDisplayName(@Nullable TabComponent displayName) {
        if (displayName == null) {
            return null;
        }

        Component rawComponent = displayName.toAdventure();

        Locale playerLocale = this.player.getPlayer().getEffectiveLocale();
        if (playerLocale == null) {
            playerLocale = Locale.getDefault();
        }

        return GlobalTranslator.render(rawComponent, playerLocale);
    }

    @Override
    @NotNull
    public Collection<UUID> getEntries() {
        return player.getPlayer().getTabList().getEntries().stream().map(e -> e.getProfile().getId()).toList();
    }
}
