package me.neznamy.tab.platforms.sponge8;

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
 * TabList implementation for Sponge 8 and up
 */
public class SpongeTabList extends TrackedTabList<SpongeTabPlayer> {

    /** Gamemode array for fast access */
    private static final GameMode[] gameModes = {
            GameModes.SURVIVAL.get(), GameModes.CREATIVE.get(), GameModes.ADVENTURE.get(), GameModes.SPECTATOR.get()
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
        player.getPlayer().tabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setDisplayName(displayName == null ? null : displayName.toAdventure()));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setGameMode(gameModes[gameMode]));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // TODO
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // TODO
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // TODO
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile = profile.withProperty(ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        //TODO listed, listOrder, showHat
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(gameModes[entry.getGameMode()])
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventure())
                .build();
        player.getPlayer().tabList().addEntry(tabListEntry);
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(header.toAdventure(), footer.toAdventure());
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().tabList().entry(entry).isPresent();
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().tabList().entries()) {
            TabComponent expectedComponent = getExpectedDisplayNames().get(entry.profile().uniqueId());
            if (expectedComponent != null && entry.displayName().orElse(null) != expectedComponent.toAdventure()) {
                entry.setDisplayName(expectedComponent.toAdventure());
            }
        }
    }
}
