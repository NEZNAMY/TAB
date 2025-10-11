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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

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
     * Checks if header and footer are as set by the plugin and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkHeaderFooter() {
        // Empty by default, overridden by Sponge and Velocity
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
