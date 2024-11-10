package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * TabList implementation for Velocity using its API.
 */
public class VelocityTabList extends TrackedTabList<VelocityTabPlayer, Component> {

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
    public void updateDisplayName(@NonNull UUID entry, @Nullable Component displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setDisplayName(displayName));
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
        // TODO once velocity adds it
    }

    @Override
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency,
                         int gameMode, @Nullable Component displayName, int listOrder, boolean showHat) {
        GameProfile profile = new GameProfile(id, name, skin == null ? Collections.emptyList() : Collections.singletonList(
                        new GameProfile.Property(TEXTURES_PROPERTY, skin.getValue(), Objects.requireNonNull(skin.getSignature()))));
        TabListEntry e = TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(profile)
                .displayName(displayName)
                .latency(latency)
                .gameMode(gameMode)
                .listed(listed)
                .listOrder(listOrder)
                .build();
        // TODO showHat once velocity adds it

        // Remove entry because:
        // #1 - If player is 1.8 - 1.19.2, KeyedVelocityTabList#addEntry will throw IllegalArgumentException
        //      if the entry is already present (most likely due to an accident trying to add existing player in global playerlist)
        // #2 - If player is 1.20.2+, tablist is cleared by the client itself without requirement to remove
        //      manually by the proxy, however velocity's tablist entry tracker still thinks they are present
        //      and therefore will refuse to add them
        removeEntry(id);

        player.getPlayer().getTabList().addEntry(e);
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(player.getVersion()), footer.toAdventure(player.getVersion()));
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().getTabList().containsEntry(entry);
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            Component expectedComponent = getExpectedDisplayNames().get(entry.getProfile().getId());
            if (expectedComponent != null && entry.getDisplayNameComponent().orElse(null) != expectedComponent) {
                entry.setDisplayName(expectedComponent);
            }
        }
    }
}
