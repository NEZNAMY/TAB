package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
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

import java.util.UUID;

/**
 * TabList implementation for Sponge 8 and up
 */
public class SpongeTabList extends TabList<SpongeTabPlayer, Component> {

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
    public void updateDisplayName0(@NonNull UUID entry, @Nullable Component displayName) {
        player.getPlayer().tabList().entry(entry).ifPresent(e -> e.setDisplayName(displayName));
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
    public void addEntry0(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, int latency, int gameMode, @Nullable Component displayName) {
        GameProfile profile = GameProfile.of(id, name);
        if (skin != null) profile = profile.withProperty(ProfileProperty.of(
                TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
        TabListEntry tabListEntry = TabListEntry.builder()
                .list(player.getPlayer().tabList())
                .profile(profile)
                .latency(latency)
                .gameMode(gameModes[gameMode])
                .displayName(displayName)
                .build();
        player.getPlayer().tabList().addEntry(tabListEntry);
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.getPlayer().tabList().setHeaderAndFooter(toComponent(header), toComponent(footer));
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getPlayer().tabList().entry(entry).isPresent();
    }

    @Override
    public void checkDisplayNames() {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            player.getPlayer().tabList().entry(target.getUniqueId()).ifPresent(entry -> {
                Component expectedComponent = getExpectedDisplayName(target);
                if (expectedComponent != null && entry.displayName().orElse(null) != expectedComponent) {
                    displayNameWrong(target.getName(), player);
                    entry.setDisplayName(expectedComponent);
                }
            });
        }
    }

    @Override
    public Component toComponent(@NonNull TabComponent component) {
        return AdventureHook.toAdventureComponent(component, player.getVersion());
    }
}
