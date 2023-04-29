package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.ChatSession;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class VelocityTabList implements TabList {

    /** Player this TabList belongs to */
    private final VelocityTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
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
    public void updateDisplayName(@NonNull UUID id, @Nullable IChatBaseComponent displayName) {
        if (player.getVersion().getMinorVersion() >= 8) {
            getEntry(id).setDisplayName(displayName == null ? null : displayName.toAdventureComponent());
        } else {
            String username = getEntry(id).getProfile().getName();
            removeEntry(id);
            addEntry(new Entry.Builder(id).name(username).displayName(displayName).build());
        }
    }

    @Override
    public void updateLatency(@NonNull UUID id, int latency) {
        getEntry(id).setLatency(latency);
    }

    @Override
    public void updateGameMode(@NonNull UUID id, int gameMode) {
        getEntry(id).setGameMode(gameMode);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        if (player.getPlayer().getTabList().containsEntry(entry.getUniqueId())) return;
        player.getPlayer().getTabList().addEntry(TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(new GameProfile(
                        entry.getUniqueId(),
                        entry.getName() == null ? "" : entry.getName(),
                        entry.getSkin() == null ? Collections.emptyList() : Collections.singletonList(
                                new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature())))
                ))
                .latency(entry.getLatency())
                .gameMode(entry.getGameMode())
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventureComponent())
                .build());
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(header.toAdventureComponent(), footer.toAdventureComponent());
    }

    /**
     * Returns TabList entry with specified UUID. If no such entry was found,
     * a new, dummy entry is returned to avoid NPE.
     *
     * @param   id
     *          UUID to get entry by
     * @return  TabList entry with specified UUID
     */
    private TabListEntry getEntry(UUID id) {
        for (TabListEntry entry : player.getPlayer().getTabList().getEntries()) {
            if (entry.getProfile().getId().equals(id)) return entry;
        }
        //return dummy entry to not cause NPE
        //possibly add logging into the future to see when this happens
        return TabListEntry.builder().tabList(player.getPlayer().getTabList())
                .profile(new GameProfile(id, "", Collections.emptyList())).build();
    }
}
