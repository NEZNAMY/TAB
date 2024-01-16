package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
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
    private static final GameMode[] gameModes = {
            GameModes.SURVIVAL, GameModes.CREATIVE, GameModes.ADVENTURE, GameModes.SPECTATOR
    };

    /** Player this TabList belongs to */
    @NotNull
    private final SpongeTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, Text> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> {
            Text component = displayName == null ? null : Text.of(displayName.toLegacyText());
            e.setDisplayName(component);
            setExpectedDisplayName(entry, component);
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
        Text displayName = entry.getDisplayName() == null ? null : Text.of(entry.getDisplayName().toLegacyText());
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
        setExpectedDisplayName(entry.getUniqueId(), displayName);
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().getTabList().setHeaderAndFooter(
                Text.of(header.toLegacyText()),
                Text.of(footer.toLegacyText())
        );
    }

    @Override
    public void checkDisplayNames() {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            player.getPlayer().getTabList().getEntry(target.getUniqueId()).ifPresent(entry -> {
                Text expectedComponent = expectedDisplayNames.get(target);
                if (expectedComponent != null && entry.getDisplayName().orElse(null) != expectedComponent) {
                    displayNameWrong(target.getName(), player);
                    entry.setDisplayName(expectedComponent);
                }
            });
        }
    }

    private void setExpectedDisplayName(@NotNull UUID entry, @Nullable Text displayName) {
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(entry);
        if (player != null) expectedDisplayNames.put(player, displayName);
    }
}
