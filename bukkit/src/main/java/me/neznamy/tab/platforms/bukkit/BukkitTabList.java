package me.neznamy.tab.platforms.bukkit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.shared.tablist.BulkUpdateTabList;

import java.util.*;
import java.util.stream.Collectors;

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
public class BukkitTabList extends BulkUpdateTabList {

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        if (PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket != null) {
            //1.19.3+
            try {
                player.sendPacket(PacketPlayOutPlayerInfoStorage.newClientboundPlayerInfoRemovePacket.newInstance(new ArrayList<>(entries)));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        } else {
            //1.19.2-
            player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("REMOVE_PLAYER",
                    entries.stream().map(id ->
                            new TabListEntry.Builder(id).build()).collect(Collectors.toList()),
                    player.getVersion())
            );
        }
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_DISPLAY_NAME",
                entries.entrySet().stream().map(entry ->
                        new TabListEntry.Builder(entry.getKey()).displayName(entry.getValue()).build()).collect(Collectors.toList()),
                player.getVersion())
        );
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_LATENCY",
                entries.entrySet().stream().map(entry ->
                        new TabListEntry.Builder(entry.getKey()).latency(entry.getValue()).build()).collect(Collectors.toList()),
                player.getVersion())
        );
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("UPDATE_GAME_MODE",
                entries.entrySet().stream().map(entry ->
                        new TabListEntry.Builder(entry.getKey()).gameMode(entry.getValue()).build()).collect(Collectors.toList()),
                player.getVersion())
        );
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket("ADD_PLAYER",
                entries.stream().map(entry ->
                        new TabListEntry(entry.getUniqueId(), entry.getName(), entry.getSkin(), entry.isListed(),
                                entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getChatSession())).collect(Collectors.toList()),
                player.getVersion())
        );
    }
}
