package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * TabList implementation for Sponge 7 and lower
 */
@RequiredArgsConstructor
public class SpongeTabList implements TabList {

    /** Gamemode array for fast access */
    private static final GameMode[] gameModes = new GameMode[]{
            GameModes.SURVIVAL, GameModes.CREATIVE, GameModes.ADVENTURE, GameModes.SPECTATOR
    };

    /** Player this TabList belongs to */
    @NotNull
    private final SpongeTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabListEntry, Text> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> {
            Text component = displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion());
            e.setDisplayName(component);
            expectedDisplayNames.put(e, component);
        });
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(gameModes[gameMode]));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        Text displayName = entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion());
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile.getPropertyMap().put(TEXTURES_PROPERTY, ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().getTabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(gameModes[entry.getGameMode()])
                .displayName(displayName)
                .build();
        player.getPlayer().getTabList().addEntry(tabListEntry);
        expectedDisplayNames.put(tabListEntry, displayName);
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().getTabList().setHeaderAndFooter(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        );
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            Text expectedComponent = expectedDisplayNames.get(entry);
            if (expectedComponent != null && entry.getDisplayName().orElse(null) != expectedComponent) {
                displayNameWrong(entry.getProfile().getName().orElse(null), player);
                entry.setDisplayName(expectedComponent);
            }
        }
    }
}
