package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * TabList implementation for Velocity using its API.
 */
public class VelocityTabList extends TrackedTabList<VelocityTabPlayer> {

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
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setDisplayName(displayName == null ? null : displayName.toAdventure()));
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
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventure())
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
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            TabComponent expectedComponent = getForcedDisplayNames().get(entry.getProfile().getId());
            if (expectedComponent != null && entry.getDisplayNameComponent().orElse(null) != expectedComponent.toAdventure()) {
                entry.setDisplayName(expectedComponent.toAdventure());
            }
        }
    }

    @Override
    public void checkGameModes() {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            Integer forcedGameMode = getForcedGameModes().get(entry.getProfile().getId());
            if (forcedGameMode != null && entry.getGameMode() != forcedGameMode) {
                entry.setGameMode(forcedGameMode);
            }
        }
    }

    @Override
    public void checkHeaderFooter() {
        if (true) return; // Disable this for now. Velocity "translates" the component, breaking identity reference.
        if (header == null || footer == null) return;
        Component actualHeader = player.getPlayer().getPlayerListHeader();
        Component actualFooter = player.getPlayer().getPlayerListFooter();
        if (actualHeader != header.toAdventure() || actualFooter != footer.toAdventure()) {
            player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(), footer.toAdventure());
        }
    }
}
