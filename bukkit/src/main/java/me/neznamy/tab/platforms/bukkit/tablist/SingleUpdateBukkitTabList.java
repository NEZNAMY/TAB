package me.neznamy.tab.platforms.bukkit.tablist;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.shared.tablist.SingleUpdateTabList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

/**
 * TabList for 1.7 and lower, which only offers one entry change at a time.
 */
@RequiredArgsConstructor
public class SingleUpdateBukkitTabList extends SingleUpdateTabList {

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
        try {
            if (PacketPlayOutPlayerInfoStorage.newClientboundPlayerInfoRemovePacket != null) {
                //1.19.3+
                player.sendPacket(PacketPlayOutPlayerInfoStorage.newClientboundPlayerInfoRemovePacket.newInstance(Collections.singletonList(entry)));
            } else {
                //1.19.2-
                player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("REMOVE_PLAYER",
                        Collections.singletonList(new TabListEntry.Builder(entry).build()),
                        player.getVersion())
                );
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_DISPLAY_NAME",
                Collections.singletonList(new TabListEntry.Builder(entry).displayName(displayName).build()),
                player.getVersion())
        );
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_LATENCY",
                Collections.singletonList(new TabListEntry.Builder(entry).latency(latency).build()),
                player.getVersion())
        );
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_GAME_MODE",
                Collections.singletonList(new TabListEntry.Builder(entry).gameMode(gameMode).build()),
                player.getVersion())
        );
    }

    @Override
    public void addEntry(TabListEntry entry) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("ADD_PLAYER",
                Collections.singletonList(new TabListEntry(entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(),
                        entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getChatSession())),
                player.getVersion())
        );
    }
}
