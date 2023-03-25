package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.player.ChatSession;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.tablist.SingleUpdateTabList;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class VelocityTabList extends SingleUpdateTabList {

    /** Player this TabList belongs to */
    private final VelocityTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.getPlayer().getTabList().removeEntry(entry);
    }

    @Override
    public void updateDisplayName(@NonNull UUID id, IChatBaseComponent displayName) {
        if (player.getVersion().getMinorVersion() >= 8) {
            getEntry(id).setDisplayName(VelocityTAB.getComponentCache().get(displayName, player.getVersion()));
        } else {
            /**
             * https://github.com/PaperMC/Velocity/blob/b0862d2d16c4ba7560d3f24c824d78793ac3d9e0/proxy/src/main/java/com/velocitypowered/proxy/tablist/VelocityTabListLegacy.java#L129-L133
             * You are supposed to be overriding
             * {@link TabList#buildEntry(GameProfile, Component, int, int, ChatSession, boolean)},
             * not the outdated {@link TabList#buildEntry(GameProfile, Component, int, int)},
             * because {@link TabListEntry.Builder#build()} calls that method.
             */
            String username = getEntry(id).getProfile().getName();
            removeEntry(id);
            addEntry(new me.neznamy.tab.api.tablist.TabListEntry.Builder(id).name(username).displayName(displayName).build());
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
    public void addEntry(me.neznamy.tab.api.tablist.@NonNull TabListEntry entry) {
        if (player.getPlayer().getTabList().containsEntry(entry.getUniqueId())) return;
        player.getPlayer().getTabList().addEntry(TabListEntry.builder()
                .tabList(player.getPlayer().getTabList())
                .profile(new GameProfile(
                        entry.getUniqueId(),
                        entry.getName() == null ? "" : entry.getName(),
                        entry.getSkin() == null ? Collections.emptyList() : Collections.singletonList(
                                new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), Objects.requireNonNull(entry.getSkin().getSignature())))
                ))
                .listed(entry.isListed())
                .latency(entry.getLatency())
                .gameMode(entry.getGameMode())
                .displayName(VelocityTAB.getComponentCache().get(entry.getDisplayName(), player.getVersion()))
                //.chatSession(new RemoteChatSession(entry.getChatSessionId(), entry.getProfilePublicKey())) // RemoteChatSession is in proxy module
                .build());
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
