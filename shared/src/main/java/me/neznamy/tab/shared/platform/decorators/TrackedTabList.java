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

    /** Player this tablist belongs to */
    protected final P player;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<UUID, TabComponent> expectedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        if (player.getVersion().getMinorVersion() < 8) {
            return; // Display names are not supported on 1.7 and below
        }
        expectedDisplayNames.put(entry, displayName);
        updateDisplayName0(entry, displayName);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        expectedDisplayNames.put(entry.getUniqueId(), entry.getDisplayName());
        addEntry0(entry);
        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName0(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkDisplayNames() {
        // Empty by default, overridden Sponge and Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     */
    public void onPacketSend(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
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
