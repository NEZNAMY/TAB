package me.neznamy.tab.platforms.sponge8;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * TabList implementation for Sponge 8 and up
 */
@RequiredArgsConstructor
public class SpongeTabList implements TabList {

    /** Gamemode array for fast access */
    private static final GameMode[] gameModes = new GameMode[]{
            GameModes.SURVIVAL.get(), GameModes.CREATIVE.get(), GameModes.ADVENTURE.get(), GameModes.SPECTATOR.get()
    };

    /** Player this TabList belongs to */
    @NotNull
    private final SpongeTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabListEntry, Component> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().tabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> {
            Component component = displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion());
            e.setDisplayName(component);
            expectedDisplayNames.put(e, component);
        });
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setGameMode(gameModes[gameMode]));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        Component displayName = entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion());
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile = profile.withProperty(ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(gameModes[entry.getGameMode()])
                .displayName(displayName)
                .build();
        player.getPlayer().tabList().addEntry(tabListEntry);
        expectedDisplayNames.put(tabListEntry, displayName);
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        );
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : player.getPlayer().tabList().entries()) {
            Component expectedComponent = expectedDisplayNames.get(entry);
            if (expectedComponent != null && entry.displayName().orElse(null) != expectedComponent) {
                displayNameWrong(entry.profile().name().orElse(null), player);
                entry.setDisplayName(expectedComponent);
            }
        }
    }
}
