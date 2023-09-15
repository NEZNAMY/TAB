package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.ChatSession;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class VelocityTabList implements TabList {

    /** Player this TabList belongs to */
    @NotNull private final VelocityTabPlayer player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabListEntry, Component> expectedDisplayNames = new WeakHashMap<>();

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    /**
     * https://github.com/PaperMC/Velocity/blob/b0862d2d16c4ba7560d3f24c824d78793ac3d9e0/proxy/src/main/java/com/velocitypowered/proxy/tablist/VelocityTabListLegacy.java#L129-L133
     * You are supposed to be overriding
     * {@link com.velocitypowered.api.proxy.player.TabList#buildEntry(GameProfile, Component, int, int, ChatSession, boolean)},
     * not the outdated {@link com.velocitypowered.api.proxy.player.TabList#buildEntry(GameProfile, Component, int, int)},
     * because {@link Entry.Builder#build()} calls that method. Manually removing the
     * entry and adding it again to avoid this bug.
     */
    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        getEntry(entry).ifPresent(e -> {
            if (player.getVersion().getMinorVersion() >= 8) {
                Component component = displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion());
                e.setDisplayName(component);
                expectedDisplayNames.put(e, component);
            } else {
                String username = e.getProfile().getName();
                removeEntry(entry);
                addEntry(new Entry.Builder(entry).name(username).displayName(displayName).build());
            }
        });
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        getEntry(entry).ifPresent(e -> e.setLatency(latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        getEntry(entry).ifPresent(e -> e.setGameMode(gameMode));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        Component displayName = entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion());
        TabListEntry e = TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(new GameProfile(
                        entry.getUniqueId(),
                        entry.getName() == null ? "" : entry.getName(),
                        entry.getSkin() == null ? Collections.emptyList() : Collections.singletonList(
                                new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature())))
                ))
                .latency(entry.getLatency())
                .gameMode(entry.getGameMode())
                .displayName(displayName)
                .build();
        player.getPlayer().getTabList().addEntry(e);
        expectedDisplayNames.put(e, displayName);
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        );
    }

    /**
     * Returns TabList entry with specified UUID. If no such entry was found,
     * empty Optional is returned.
     *
     * @param   id
     *          UUID to get entry by
     * @return  TabList entry with specified UUID
     */
    @NotNull
    private Optional<TabListEntry> getEntry(@NotNull UUID id) {
        for (TabListEntry entry : getEntries()) {
            if (entry.getProfile().getId().equals(id)) return Optional.of(entry);
        }
        return Optional.empty();
    }

    @Override
    public void checkDisplayNames() {
        for (TabListEntry entry : getEntries()) {
            Component expectedComponent = expectedDisplayNames.get(entry);
            if (expectedComponent != null && entry.getDisplayNameComponent().orElse(null) != expectedComponent) {
                displayNameWrong(entry.getProfile().getName(), player);
                entry.setDisplayName(expectedComponent);
            }
        }
    }

    /**
     * Returns list of entries in player's TabList. This includes a try/catch
     * to avoid {@link ConcurrentModificationException} when it's modified by the
     * backend server while iterating.
     *
     * @return  A copy of TabList entries in player's TabList
     */
    private Collection<TabListEntry> getEntries() {
        try {
            return new ArrayList<>(player.getPlayer().getTabList().getEntries());
        } catch (ConcurrentModificationException velocity) {
            // TabList was modified by backend server during iteration
            return getEntries();
        }
    }
}
