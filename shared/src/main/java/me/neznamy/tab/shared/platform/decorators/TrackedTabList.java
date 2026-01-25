package me.neznamy.tab.shared.platform.decorators;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
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

    /** Header sent by the plugin */
    @Nullable
    protected TabComponent header;

    /** Footer sent by the plugin */
    @Nullable
    protected TabComponent footer;

    /** Flag tracking whether all real players should be hidden or not */
    protected boolean allPlayersHidden;

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        forcedDisplayNames.put(entry, displayName);
        if (player.getVersion().getNetworkId() < ProtocolVersion.V1_8.getNetworkId()) {
            return; // Display names are not supported on 1.7 and below
        }
        updateDisplayName0(entry, displayName);
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        forcedDisplayNames.put(entry.getUniqueId(), entry.getDisplayName());
        addEntry0(entry);
        if (player.getVersion() == ProtocolVersion.V1_8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName0(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    @Override
    public void updateDisplayName(@NonNull TabPlayer target, @Nullable TabComponent displayName) {
        forcedDisplayNames.put(target.getTablistId(), displayName);
        if (target.getVersion().getNetworkId() < ProtocolVersion.V1_8.getNetworkId()) {
            return; // Display names are not supported on 1.7 and below
        }
        if (containsEntry(target.getTablistId())) {
            updateDisplayName0(target.getTablistId(), displayName);
        } else {
            // Entry is not in tablist. This could be on join. Delay and try again.
            TAB.getInstance().getCpu().getTablistEntryCheckThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                // If entry was added in the meantime and display name did not
                if (containsEntry(target.getTablistId()) && Objects.equals(forcedDisplayNames.get(target.getTablistId()), displayName)) {
                    updateDisplayName0(target.getTablistId(), displayName);
                }
            }, TabConstants.Feature.PLAYER_LIST, "Delayed format update"), 500);
        }
    }

    @Override
    public void updateLatency(@NonNull TabPlayer target, int latency) {
        if (containsEntry(target.getTablistId())) {
            updateLatency(target.getTablistId(), latency);
        } else {
            // Entry is not in tablist. This could be on join. Delay and try again.
            TAB.getInstance().getCpu().getTablistEntryCheckThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                // If entry was added in the meantime
                if (containsEntry(target.getTablistId())) {
                    updateLatency(target.getTablistId(), latency);
                }
            }, TabConstants.Feature.PING_SPOOF, "Delayed ping update"), 500);
        }
    }

    @Override
    public void updateGameMode(@NonNull TabPlayer target, int gameMode) {
        if (containsEntry(target.getTablistId())) {
            updateGameMode(target.getTablistId(), gameMode);
        } else {
            // Entry is not in tablist. This could be on join. Delay and try again.
            TAB.getInstance().getCpu().getTablistEntryCheckThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                // If entry was added in the meantime
                if (containsEntry(target.getTablistId())) {
                    updateGameMode(target.getTablistId(), gameMode);
                }
            }, TabConstants.Feature.SPECTATOR_FIX, "Delayed gamemode update"), 500);
        }
    }

    @Override
    public void updateListed(@NonNull TabPlayer target, boolean listed) {
        if (containsEntry(target.getTablistId())) {
            updateListed(target.getTablistId(), listed);
        } else {
            // Entry is not in tablist. This could be on join. Delay and try again.
            TAB.getInstance().getCpu().getTablistEntryCheckThread().executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                // If entry was added in the meantime
                if (containsEntry(target.getTablistId())) {
                    updateListed(target.getTablistId(), listed);
                }
            }, TabConstants.Feature.LAYOUT, "Delayed listed update"), 500);
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

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return player.getTabListEntryTracker() == null || player.getTabListEntryTracker().containsEntry(entry);
    }

    @Override
    public void hideAllPlayers() {
        allPlayersHidden = true;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateListed(all, false);
        }
    }

    @Override
    public void showAllPlayers() {
        allPlayersHidden = false;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            updateListed(all, true);
        }
    }

    @Override
    @NotNull
    public Collection<UUID> getEntries() {
        if (player.getTabListEntryTracker() == null) return Collections.emptyList(); // This method is overridden in these cases anyway
        return player.getTabListEntryTracker().getEntries();
    }

    @Override
    @NotNull
    public Object dump() {
        Map<String, Object> data = new LinkedHashMap<>();

        // Expected display names
        Map<String, Object> forcedDisplayNames = new LinkedHashMap<>();
        for (Map.Entry<UUID, TabComponent> entry : this.forcedDisplayNames.entrySet()) {
            forcedDisplayNames.put(entry.getKey().toString(), entry.getValue() == null ? null : entry.getValue().toLegacyText());
        }
        data.put("expected display names (using legacy text)", forcedDisplayNames);

        // All entries
        List<String> entries = new ArrayList<>();
        for (UUID entry : getEntries()) {
            StringBuilder string = new StringBuilder(entry.toString());
            TabPlayer byInternalId = TAB.getInstance().getPlayer(entry);
            List<String> matches = new ArrayList<>();
            if (byInternalId != null) {
                matches.add("internal UUID of " + byInternalId.getName());
            }
            TabPlayer byTablistId = TAB.getInstance().getPlayerByTabListUUID(entry);
            if (byTablistId != null) {
                matches.add("tablist UUID of " + byTablistId.getName());
            }
            if (!matches.isEmpty()) {
                string.append(" (").append(String.join(", ", matches)).append(")");
            }

            entries.add(string.toString());
        }
        data.put("entries", entries);

        return data;
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     * @return  Packet to forward
     */
    @NotNull
    public abstract Object onPacketSend(@NonNull Object packet);

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
