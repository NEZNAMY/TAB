package me.neznamy.tab.shared.platform;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Netty injector for tablist entry tracking.
 */
@RequiredArgsConstructor
public abstract class TabListEntryTracker extends ChannelDuplexHandler {

    /** Players in the tablist */
    protected final Set<UUID> tablistEntries = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void write(@NotNull ChannelHandlerContext context, @Nullable Object packet, @NotNull ChannelPromise channelPromise) throws Exception{
        if (packet == null) return;
        onPacketSend(packet);
        super.write(context, packet, channelPromise);
    }

    /**
     * Checks whether the given player is currently in the tablist.
     *
     * @param   uuid
     *          UUID of player to check
     * @return  {@code true} if player is in tablist, {@code false} if not
     */
    public boolean containsEntry(@NotNull UUID uuid) {
        return tablistEntries.contains(uuid);
    }

    /**
     * Returns collection of all tablist entries.
     *
     * @return  Collection of all tablist entries
     */
    @NotNull
    public Collection<UUID> getEntries() {
        return Collections.unmodifiableSet(tablistEntries);
    }

    /**
     * Processes outgoing packet, updating tablist entries accordingly.
     *
     * @param   packet
     *          Packet to process
     */
    public abstract void onPacketSend(@NotNull Object packet);
}