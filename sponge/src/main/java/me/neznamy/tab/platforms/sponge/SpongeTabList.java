package me.neznamy.tab.platforms.sponge;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.List;
import java.util.UUID;

/**
 * TabList implementation for Sponge.
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
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setListed(listed));
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
        //TODO listOrder, showHat
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(gameModes[entry.getGameMode()])
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventure())
                .listed(entry.isListed())
                .build();
        player.getPlayer().tabList().addEntry(tabListEntry);
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(header.toAdventure(), footer.toAdventure());
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().tabList().entry(entry).isPresent();
    }

    @Override
    @Nullable
    public Skin getSkin() {
        List<ProfileProperty> list = player.getPlayer().profile().properties();
        if (list.isEmpty()) return null; // Offline mode
        for (ProfileProperty property : list) {
            if (property.name().equals(TEXTURES_PROPERTY)) {
                return new Skin(property.value(), property.signature().orElse(null));
            }
        }
        return null;
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().tabList().entries()) {
            TabComponent expectedComponent = getForcedDisplayNames().get(entry.profile().uniqueId());
            if (expectedComponent != null && entry.displayName().orElse(null) != expectedComponent.toAdventure()) {
                entry.setDisplayName(expectedComponent.toAdventure());
            }
        }
    }

    @Override
    public void checkGameModes() {
        for (TabListEntry entry : player.getPlayer().tabList().entries()) {
            Integer forcedGameMode = getForcedGameModes().get(entry.profile().uniqueId());
            if (forcedGameMode != null && getGamemode(entry.gameMode()) != forcedGameMode) {
                entry.setGameMode(gameModes[forcedGameMode]);
            }
        }
    }

    private int getGamemode(@NonNull GameMode gameMode) {
        if (gameMode == GameModes.CREATIVE.get()) return 1;
        if (gameMode == GameModes.ADVENTURE.get()) return 2;
        if (gameMode == GameModes.SPECTATOR.get()) return 3;
        return 0;
    }

    @Override
    public void checkHeaderFooter() {
        if (header == null || footer == null) return;
        Component actualHeader = player.getPlayer().tabList().header().orElse(Component.empty());
        Component actualFooter = player.getPlayer().tabList().footer().orElse(Component.empty());
        if (actualHeader != header.toAdventure() || actualFooter != footer.toAdventure()) {
            player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventure(), footer.toAdventure());
        }
    }
}
