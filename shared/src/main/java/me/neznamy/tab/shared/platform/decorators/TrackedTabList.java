package me.neznamy.tab.shared.platform.decorators;

import lombok.*;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
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

    /** Forced game modes by spectator fix, saving to restore them on packet sends */
    private final Map<UUID, Integer> forcedGameModes = Collections.synchronizedMap(new WeakHashMap<>());

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
    public void updateDisplayName(@NonNull TabPlayer player, @Nullable TabComponent displayName) {
        forcedDisplayNames.put(player.getTablistId(), displayName);
        if (player.getVersion().getMinorVersion() < 8) {
            return; // Display names are not supported on 1.7 and below
        }
        if (containsEntry(player.getTablistId()) && this.player.canSee(player)) {
            updateDisplayName0(player.getTablistId(), displayName);
        }
    }

    @Override
    public void updateLatency(@NonNull TabPlayer player, int latency) {
        if (containsEntry(player.getTablistId()) && this.player.canSee(player)) {
            updateLatency(player.getTablistId(), latency);
        }
    }

    @Override
    public void updateGameMode(@NonNull TabPlayer player, int gameMode) {
        forcedGameModes.put(player.getTablistId(), gameMode);
        if (containsEntry(player.getTablistId()) && this.player.canSee(player)) {
            updateGameMode(player.getTablistId(), gameMode);
        }
    }

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkDisplayNames() {
        // Empty by default, overridden by Sponge and Velocity
    }

    /**
     * Checks if all entries have game modes as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkGameModes() {
        // Empty by default, overridden by Sponge and Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     */
    public void onPacketSend(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord, Fabric, Forge and NeoForge
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
}
