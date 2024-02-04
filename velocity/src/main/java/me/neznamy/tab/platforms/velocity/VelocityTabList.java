package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.ChatSession;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList implementation for Velocity using its API.
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class VelocityTabList implements TabList {

    /** Player this TabList belongs to */
    @NotNull private final VelocityTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, Component> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    /**
     * <a href="https://github.com/PaperMC/Velocity/blob/b0862d2d16c4ba7560d3f24c824d78793ac3d9e0/proxy/src/main/java/com/velocitypowered/proxy/tablist/VelocityTabListLegacy.java#L129-L133">VelocityTabListLegacy</a>
     * You are supposed to be overriding
     * {@link com.velocitypowered.api.proxy.player.TabList#buildEntry(GameProfile, Component, int, int, ChatSession, boolean)},
     * not the outdated {@link com.velocitypowered.api.proxy.player.TabList#buildEntry(GameProfile, Component, int, int)},
     * because {@link TabListEntry.Builder#build()} calls that method. Manually removing the
     * entry and adding it again to avoid this bug.
     */
    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable TabComponent displayName) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> {
            if (player.getVersion().getMinorVersion() >= 8) {
                Component component = displayName == null ? null : AdventureHook.toAdventureComponent(displayName, player.getVersion());
                e.setDisplayName(component);
                setExpectedDisplayName(entry, component);
            } else {
                String username = e.getProfile().getName();
                removeEntry(entry);
                addEntry(new Entry(entry, username, null, 0, 0, displayName));
            }
        });
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.getPlayer().getTabList().getEntry(entry).ifPresent(e -> e.setGameMode(gameMode));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        Component displayName = entry.getDisplayName() == null ? null : AdventureHook.toAdventureComponent(entry.getDisplayName(), player.getVersion());
        TabListEntry e = TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(new GameProfile(
                        entry.getUniqueId(),
                        entry.getName(),
                        entry.getSkin() == null ? Collections.emptyList() : Collections.singletonList(
                                new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature())))
                ))
                .latency(entry.getLatency())
                .gameMode(entry.getGameMode())
                .displayName(displayName)
                .build();
        setExpectedDisplayName(entry.getUniqueId(), displayName);

        // Remove entry because:
        // #1 - If player is 1.8 - 1.19.2, KeyedVelocityTabList#addEntry will throw IllegalArgumentException
        //      if the entry is already present (most likely due to an accident trying to add existing player in global playerlist)
        // #2 - If player is 1.20.2+, tablist is cleared by the client itself without requirement to remove
        //      manually by the proxy, however velocity's tablist entry tracker still thinks they are present
        //      and therefore will refuse to add them
        removeEntry(entry.getUniqueId());

        player.getPlayer().getTabList().addEntry(e);

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull TabComponent header, @NotNull TabComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(
                AdventureHook.toAdventureComponent(header, player.getVersion()),
                AdventureHook.toAdventureComponent(footer, player.getVersion())
        );
    }

    @Override
    public void checkDisplayNames() {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            player.getPlayer().getTabList().getEntry(target.getUniqueId()).ifPresent(entry -> {
                Component expectedComponent = expectedDisplayNames.get(target);
                if (expectedComponent != null && entry.getDisplayNameComponent().orElse(null) != expectedComponent) {
                    displayNameWrong(entry.getProfile().getName(), player);
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
