package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.UUID;

/**
 * TabList implementation for Sponge 7 and lower
 */
public class SpongeTabList extends TrackedTabList<SpongeTabPlayer> {

    /** Gamemode array for fast access */
    private static final GameMode[] gameModes = {
            GameModes.SURVIVAL, GameModes.CREATIVE, GameModes.ADVENTURE, GameModes.SPECTATOR
    };

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public SpongeTabList(@NotNull SpongeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setDisplayName(displayName == null ? null : displayName.convert()));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(gameModes[gameMode]));
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
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile.getPropertyMap().put(TEXTURES_PROPERTY, ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().getTabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(gameModes[entry.getGameMode()])
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().convert())
                .build();
        player.getPlayer().getTabList().addEntry(tabListEntry);
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().getTabList().setHeaderAndFooter(header.convert(), footer.convert());
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().getTabList().getEntry(entry).isPresent();
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            TabComponent expectedComponent = getExpectedDisplayNames().get(entry.getProfile().getUniqueId());
            if (expectedComponent != null && entry.getDisplayName().orElse(null) != expectedComponent.convert()) {
                entry.setDisplayName(expectedComponent.convert());
            }
        }
    }
}
