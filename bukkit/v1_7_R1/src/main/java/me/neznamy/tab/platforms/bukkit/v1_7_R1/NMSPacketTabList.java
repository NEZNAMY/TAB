package me.neznamy.tab.platforms.bukkit.v1_7_R1;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.minecraft.server.v1_7_R1.Packet;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList implementation using direct NMS code.
 */
public class NMSPacketTabList extends TrackedTabList<BukkitTabPlayer> {

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public NMSPacketTabList(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        // TODO
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        // TODO
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        // TODO
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        // Added in 1.8
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Added in 1.21.2
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Added in 1.21.4
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        // TODO
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        // Added in 1.8
    }

    @Override
    @Nullable
    public Skin getSkin() {
        return null;
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Override
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        // Pipeline injection is not available (netty is relocated)
        return packet;
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    @SuppressWarnings("unused")
    private void sendPacket(@NotNull Packet packet) {
        ((CraftPlayer)player.getPlayer()).getHandle().playerConnection.sendPacket(packet);
    }
}
