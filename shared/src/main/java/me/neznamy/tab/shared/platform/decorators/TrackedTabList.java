package me.neznamy.tab.shared.platform.decorators;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Decorated class for TabList that tracks entries and their expected values.
 *
 * @param   <P>
 *          Platform's player class
 */
@RequiredArgsConstructor
@Getter
public abstract class TrackedTabList<P extends TabPlayer> implements TabList {

    /** Forced latency for all entries*/
    @Getter
    @Setter
    private static Integer forcedLatency;

    /** Player this tablist belongs to */
    protected final P player;

    /** Forced display names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<UUID, TabComponent> forcedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    /** Players to change to survival gamemode instead of spectator */
    private final Set<UUID> blockedSpectators = Collections.synchronizedSet(new HashSet<>());

    /** Players added into tablist (detected in pipeline) */
    private final Set<UUID> addedEntries = Collections.synchronizedSet(new HashSet<>());

    /** Players removed from tablist (detected in pipeline) */
    private final Set<UUID> removedEntries = Collections.synchronizedSet(new HashSet<>());

    /** Header sent by the plugin */
    @Nullable
    protected TabComponent header;

    /** Footer sent by the plugin */
    @Nullable
    protected TabComponent footer;

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        forcedDisplayNames.put(entry, displayName);
        if (player.getVersion().getMinorVersion() < 8) {
            return; // Display names are not supported on 1.7 and below
        }
        updateDisplayName0(entry, displayName);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        forcedDisplayNames.put(entry.getUniqueId(), entry.getDisplayName());
        addEntry0(entry);
        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName0(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    @Override
    public void updateDisplayName(@NonNull TabPlayer target, @Nullable TabComponent displayName) {
        forcedDisplayNames.put(target.getTablistId(), displayName);
        if (target.getVersion().getMinorVersion() < 8) {
            return; // Display names are not supported on 1.7 and below
        }
        Boolean contains = containsEntry(target.getTablistId());
        if (contains == Boolean.FALSE) return; // Player is definitely not in tablist, drop packet
        if (contains == Boolean.TRUE) {
            // Player is definitely in tablist, update directly
            updateDisplayName0(target.getTablistId(), displayName);
            return;
        }
        // Unknown result, try other means
        if (!player.canSee(target)) {
            // Target is vanished and this player should not see them. Drop packet.
            return;
        }
        updateDisplayName0(target.getTablistId(), displayName);
    }

    @Override
    public void updateLatency(@NonNull TabPlayer target, int latency) {
        Boolean contains = containsEntry(target.getTablistId());
        if (contains == Boolean.FALSE) return; // Player is definitely not in tablist, drop packet
        if (contains == Boolean.TRUE) {
            // Player is definitely in tablist, update directly
            updateLatency(target.getTablistId(), latency);
            return;
        }
        // Unknown result, try other means
        if (!player.canSee(target)) {
            // Target is vanished and this player should not see them. Drop packet.
            return;
        }
        updateLatency(target.getTablistId(), latency);
    }

    @Override
    public void updateGameMode(@NonNull TabPlayer target, int gameMode) {
        Boolean contains = containsEntry(target.getTablistId());
        if (contains == Boolean.FALSE) return; // Player is definitely not in tablist, drop packet
        if (contains == Boolean.TRUE) {
            // Player is definitely in tablist, update directly
            updateGameMode(target.getTablistId(), gameMode);
            return;
        }
        // Unknown result, try other means
        if (!player.canSee(target)) {
            // Target is vanished and this player should not see them. Drop packet.
            return;
        }
        updateGameMode(target.getTablistId(), gameMode);
    }

    @Override
    public void setPlayerListHeaderFooter(@Nullable TabComponent header, @Nullable TabComponent footer) {
        this.header = header;
        this.footer = footer;
        setPlayerListHeaderFooter0(
                header == null ? TabComponent.empty() : header,
                footer == null ? TabComponent.empty() : footer
        );
    }

    /**
     * Resends header and footer to the player. Called on server switch.
     */
    public void resendHeaderFooter() {
        if (header != null && footer != null) {
            setPlayerListHeaderFooter0(header, footer);
        }
    }

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkDisplayNames() {
        // Empty by default, overridden by Velocity
    }

    /**
     * Checks if all entries have game modes as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkGameModes() {
        // Empty by default, overridden by Velocity
    }

    /**
     * Checks if header and footer are as set by the plugin and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkHeaderFooter() {
        // Empty by default, overridden by Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     * @return  Packet to forward
     */
    @NotNull
    public Object onPacketSend(@NonNull Object packet) {
        return packet;
    }

    @Override
    public void blockSpectator(@NonNull TabPlayer player) {
        blockedSpectators.add(player.getTablistId());
        updateGameMode(player, 0);
    }

    @Override
    public void unblockSpectator(@NonNull TabPlayer player) {
        blockedSpectators.remove(player.getTablistId());
        updateGameMode(player, player.getGamemode());
    }

    /**
     * Returns {@code Boolean.TRUE} if tablist definitely contains specified entry, {@code Boolean.FALSE} if
     * definitely not. If not sure due to lack of API and tracked data, returns {@code null}.
     *
     * @param   entry
     *          UUID of entry to check
     * @return  {@code Boolean.TRUE} if tablist contains specified entry, {@code Boolean.FALSE} if not and {@code null} if unknown
     */
    @Nullable
    public Boolean containsEntry(@NonNull UUID entry) {
        // This is the default implementation.
        // Platforms with tablist entry tracker (proxies) will override it for non-null results.

        if (addedEntries.contains(entry)) return Boolean.TRUE;
        if (removedEntries.contains(entry)) return Boolean.FALSE;
        return null; // No packets received for this entry yet, not sure
    }

    /**
     * Processes entry addition on platforms without full TabList API, called when packet is
     * received in the pipeline.
     *
     * @param   entry
     *          Added entry
     */
    protected void onEntryAdd(@NotNull UUID entry) {
        addedEntries.add(entry);
        removedEntries.remove(entry);
    }

    /**
     * Processes entry removal on platforms without full TabList API, called when packet is
     * received in the pipeline.
     *
     * @param   entry
     *          Removed entry
     */
    protected void onEntryRemove(@NotNull UUID entry) {
        removedEntries.add(entry);
        addedEntries.remove(entry);
    }

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    public abstract void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName);

    /**
     * Adds specified entry to tablist.
     *
     * @param   entry
     *          Entry to add
     */
    public abstract void addEntry0(@NonNull Entry entry);

    /**
     * Sends header and footer to player.
     *
     * @param   header
     *          Header to send
     * @param   footer
     *          Footer to send
     */
    public abstract void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer);
}
