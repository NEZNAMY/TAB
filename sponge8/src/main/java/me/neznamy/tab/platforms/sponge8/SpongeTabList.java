package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.shared.tablist.SingleUpdateTabList;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.util.UUID;

@RequiredArgsConstructor
public class SpongeTabList extends SingleUpdateTabList {

    /** Player this TabList belongs to */
    private final SpongeTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.getPlayer().tabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NonNull UUID id, IChatBaseComponent displayName) {
        player.getPlayer().tabList().entry(id).ifPresent(
                entry -> entry.setDisplayName(Sponge8TAB.getAdventureCache().get(displayName, player.getVersion())));
    }

    @Override
    public void updateLatency(@NonNull UUID id, int latency) {
        player.getPlayer().tabList().entry(id).ifPresent(entry -> entry.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID id, int gameMode) {
        player.getPlayer().tabList().entry(id).ifPresent(entry -> entry.setGameMode(convertGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NonNull TabListEntry entry) {
        GameProfile profile = GameProfile.of(entry.getUniqueId(), entry.getName());
        if (entry.getSkin() != null) profile.withProperty(ProfileProperty.of(
                TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature()));
        player.getPlayer().tabList().addEntry(org.spongepowered.api.entity.living.player.tab.TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(entry.getLatency())
                .gameMode(convertGameMode(entry.getGameMode()))
                .displayName(Sponge8TAB.getAdventureCache().get(entry.getDisplayName(), player.getVersion()))
                .build());
    }

    private GameMode convertGameMode(int mode) {
        switch (mode) {
            case 0: return GameModes.SURVIVAL.get();
            case 1: return GameModes.CREATIVE.get();
            case 2: return GameModes.ADVENTURE.get();
            case 3: return GameModes.SPECTATOR.get();
            default: return GameModes.NOT_SET.get();
        }
    }
}
