package me.neznamy.tab.platforms.bukkit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList which support modifying many entries at once
 * for significantly better performance. For 1.7 players,
 * ViaVersion properly splits the packet into multiple, so
 * we don't need to worry about that here.
 * <p>
 * This class does not support server versions of 1.7 and
 * below, because of the massive differences in tablist
 * and packet fields.
 */
@RequiredArgsConstructor
public class BukkitTabList implements TabList {

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
        if (PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            try {
                player.sendPacket(PacketPlayOutPlayerInfoStorage.newClientboundPlayerInfoRemovePacket.newInstance(Collections.singletonList(entry)));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        } else {
            //1.19.2-
            player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket(
                    "REMOVE_PLAYER", new Entry.Builder(entry).build(), player.getVersion())
            );
        }
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_DISPLAY_NAME",
                new Entry.Builder(entry).displayName(displayName).build(), player.getVersion())
        );
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_LATENCY",
                new Entry.Builder(entry).latency(latency).build(), player.getVersion())
        );
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_GAME_MODE",
                new Entry.Builder(entry).gameMode(gameMode).build(), player.getVersion())
        );
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("ADD_PLAYER", entry, player.getVersion()));
    }
}
