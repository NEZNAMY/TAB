package me.neznamy.tab.platforms.sponge8;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.UUID;

/**
 * TabList implementation for Sponge 8 and up
 */
@RequiredArgsConstructor
public class SpongeTabList implements TabList {

    /** Player this TabList belongs to */
    private final SpongeTabPlayer player;

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().tabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.getPlayer().tabList().entry(entry).ifPresent(
                e -> e.setDisplayName(displayName == null ? null : displayName.toAdventureComponent(player.getVersion())));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setGameMode(convertGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile = profile.withProperty(ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        player.getPlayer().tabList().addEntry(org.spongepowered.api.entity.living.player.tab.TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(convertGameMode(entry.getGameMode()))
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventureComponent(player.getVersion()))
                .build());
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(header.toAdventureComponent(player.getVersion()), footer.toAdventureComponent(player.getVersion()));
    }

    private GameMode convertGameMode(int mode) {
        switch (mode) {
            case 1: return GameModes.CREATIVE.get();
            case 2: return GameModes.ADVENTURE.get();
            case 3: return GameModes.SPECTATOR.get();
            default: return GameModes.SURVIVAL.get();
        }
    }
}
