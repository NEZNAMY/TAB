package me.neznamy.tab.platforms.krypton;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.shared.tablist.BulkUpdateTabList;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.auth.GameProfile;
import org.kryptonmc.api.world.GameMode;
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoRemove;
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KryptonTabList extends BulkUpdateTabList {

    private final KryptonTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        player.sendPacket(new PacketOutPlayerInfoRemove(new ArrayList<>(entries)));
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        player.sendPacket(new PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME),
                entries.entrySet().stream().map(entry -> new PacketOutPlayerInfoUpdate.Entry(
                        entry.getKey(),
                        GameProfile.of("", entry.getKey()),
                        false,
                        0,
                        GameMode.SURVIVAL,
                        entry.getValue() == null ? null : GsonComponentSerializer.gson().deserialize(entry.getValue().toString(player.getVersion())),
                        null
                )).collect(Collectors.toList())
        ));
    }

    @Override
    public void updateLatencies(@NotNull Map<UUID, Integer> entries) {
        player.sendPacket(new PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_LATENCY),
                entries.entrySet().stream().map(entry -> new PacketOutPlayerInfoUpdate.Entry(
                        entry.getKey(),
                        GameProfile.of("", entry.getKey()),
                        false,
                        entry.getValue(),
                        GameMode.SURVIVAL,
                        null,
                        null
                )).collect(Collectors.toList())
        ));
    }

    @Override
    public void updateGameModes(@NotNull Map<UUID, Integer> entries) {
        player.sendPacket(new PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_GAME_MODE),
                entries.entrySet().stream().map(entry -> new PacketOutPlayerInfoUpdate.Entry(
                        entry.getKey(),
                        GameProfile.of("", entry.getKey()),
                        false,
                        0,
                        GameMode.values()[entry.getValue()],
                        null,
                        null
                )).collect(Collectors.toList())
        ));
    }

    @Override
    public void addEntries(@NotNull Collection<TabListEntry> entries) {
        player.sendPacket(new PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.ADD_PLAYER),
                entries.stream().map(entry -> new PacketOutPlayerInfoUpdate.Entry(
                        entry.getUniqueId(),
                        GameProfile.of(entry.getName() == null ? "" : entry.getName(), entry.getUniqueId()),
                        entry.isListed(),
                        entry.getLatency(),
                        GameMode.values()[entry.getGameMode()],
                        entry.getDisplayName() == null ? null : GsonComponentSerializer.gson().deserialize(entry.getDisplayName().toString(player.getVersion())),
                        null //TODO chat session?
                )).collect(Collectors.toList())
        ));
    }
}