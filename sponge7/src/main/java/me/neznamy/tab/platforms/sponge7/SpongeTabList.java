package me.neznamy.tab.platforms.sponge7;

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

@RequiredArgsConstructor
public class SpongeTabList implements TabList {

    /** Player this TabList belongs to */
    private final SpongeTabPlayer player;

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(
                e -> e.setDisplayName(displayName == null ? null : Sponge7TAB.getTextCache().get(displayName, player.getVersion())));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(convertGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile.getPropertyMap().put(TEXTURES_PROPERTY, ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        player.getPlayer().getTabList().addEntry(org.spongepowered.api.entity.living.player.tab.TabListEntry.builder()
                .list(player.getPlayer().getTabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(convertGameMode(entry.getGameMode()))
                .displayName(entry.getDisplayName() == null ? null : Sponge7TAB.getTextCache().get(entry.getDisplayName(), player.getVersion()))
                .build());
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().getTabList().setHeaderAndFooter(
                Sponge7TAB.getTextCache().get(header, player.getVersion()),
                Sponge7TAB.getTextCache().get(footer, player.getVersion())
        );
    }

    private GameMode convertGameMode(int mode) {
        switch (mode) {
            case 0: return GameModes.SURVIVAL;
            case 1: return GameModes.CREATIVE;
            case 2: return GameModes.ADVENTURE;
            case 3: return GameModes.SPECTATOR;
            default: return GameModes.NOT_SET;
        }
    }
}
