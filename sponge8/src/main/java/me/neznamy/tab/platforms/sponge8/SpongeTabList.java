package me.neznamy.tab.platforms.sponge8;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
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
    private static final GameMode[] gameModes = {
            GameModes.SURVIVAL.get(), GameModes.CREATIVE.get(), GameModes.ADVENTURE.get(), GameModes.SPECTATOR.get()
    };

    /** Player this TabList belongs to */
    @NotNull
    private final SpongeTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, Component> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().tabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable TabComponent displayName) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> {
            Component component = displayName == null ? null : AdventureHook.toAdventureComponent(displayName, player.getVersion());
            e.setDisplayName(component);
            setExpectedDisplayName(entry, component);
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
        Component displayName = entry.getDisplayName() == null ? null : AdventureHook.toAdventureComponent(entry.getDisplayName(), player.getVersion());
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
        setExpectedDisplayName(entry.getUniqueId(), displayName);

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull TabComponent header, @NotNull TabComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(
                AdventureHook.toAdventureComponent(header, player.getVersion()),
                AdventureHook.toAdventureComponent(footer, player.getVersion())
        );
    }

    @Override
    public void checkDisplayNames() {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            player.getPlayer().tabList().entry(target.getUniqueId()).ifPresent(entry -> {
                Component expectedComponent = expectedDisplayNames.get(target);
                if (expectedComponent != null && entry.displayName().orElse(null) != expectedComponent) {
                    displayNameWrong(target.getName(), player);
                    entry.setDisplayName(expectedComponent);
                }
            });
        }
    }

    private void setExpectedDisplayName(@NotNull UUID entry, @Nullable Component displayName) {
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(entry);
        if (player != null) expectedDisplayNames.put(player, displayName);
    }
}
