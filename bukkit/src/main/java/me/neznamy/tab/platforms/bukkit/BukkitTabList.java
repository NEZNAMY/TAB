package me.neznamy.tab.platforms.bukkit;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerListHeaderFooterStorage;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList which support modifying many entries at once
 * for significantly better performance. For 1.7 players,
 * ViaVersion properly splits the packet into multiple, so
 * we don't need to worry about that here.
 * <p>
 * This class does not support server versions of 1.7 and
 * below, because of the massive differences in tab list
 * and packet fields.
 */
@RequiredArgsConstructor
public class BukkitTabList implements TabList {

    /** Player this TabList belongs to */
    private final BukkitTabPlayer player;

    @Override
    public void removeEntry(@NotNull UUID entry) {
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
                    Action.REMOVE_PLAYER, new Entry.Builder(entry).build(), player.getVersion())
            );
        }
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket(Action.UPDATE_DISPLAY_NAME,
                new Entry.Builder(entry).displayName(displayName).build(), player.getVersion())
        );
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket(Action.UPDATE_LATENCY,
                new Entry.Builder(entry).latency(latency).build(), player.getVersion())
        );
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket(Action.UPDATE_GAME_MODE,
                new Entry.Builder(entry).gameMode(gameMode).build(), player.getVersion())
        );
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        player.sendPacket(PacketPlayOutPlayerInfoStorage.createPacket(Action.ADD_PLAYER, entry, player.getVersion()));
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        // Method was added to Bukkit API in 1.13.1, however despite that it's just a String one
        // Using it would cause high CPU usage and massive memory allocations on RGB & animations
        // Send packet instead for performance & older server version support

        /*if (TAB.getInstance().getServerVersion().getNetworkId() >= ProtocolVersion.V1_13_1.getNetworkId()) {
            String bukkitHeader = RGBUtils.getInstance().convertToBukkitFormat(header.toFlatText(),
                    getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
            String bukkitFooter = RGBUtils.getInstance().convertToBukkitFormat(footer.toFlatText(),
                    getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
            getPlayer().setPlayerListHeaderFooter(bukkitHeader, bukkitFooter);
            return;
        }*/

        try {
            player.sendPacket(PacketPlayOutPlayerListHeaderFooterStorage.build(header, footer, player.getVersion()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
