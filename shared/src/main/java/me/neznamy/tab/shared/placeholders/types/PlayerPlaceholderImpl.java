package me.neznamy.tab.shared.placeholders.types;

import lombok.NonNull;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation of the PlayerPlaceholder interface
 */
public class PlayerPlaceholderImpl extends TabPlaceholder implements PlayerPlaceholder {

    /** Placeholder function returning fresh output on request */
    @NonNull private final Function<me.neznamy.tab.api.TabPlayer, String> function;

    /**
     * Constructs new instance with given parameters
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     * @param   function
     *          refresh function which returns new up-to-date output on request
     */
    public PlayerPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull Function<me.neznamy.tab.api.TabPlayer, String> function) {
        super(identifier, refresh);
        if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
        this.function = function;
    }

    @Override
    public void update(@NonNull me.neznamy.tab.api.TabPlayer player) {
        updateValue(player, request((TabPlayer) player));
    }

    @Override
    public void updateValue(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable String value) {
        if (hasValueChanged((TabPlayer) player, value, true)) {
            if (!player.isLoaded()) return; // Updated on join
            for (RefreshableFeature r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(identifier)) {
                TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> r.refresh((TabPlayer) player, false),
                        r.getFeatureName(), r.getRefreshDisplayName());
                if (r instanceof CustomThreaded) {
                    ((CustomThreaded) r).getCustomThread().execute(task);
                } else {
                    task.run();
                }
            }
        }
    }

    /**
     * Bulk-updates all listed placeholders for a player. The advantage of using this method is that
     * {@link RefreshableFeature#refresh(TabPlayer, boolean)} is only called a single time instead of
     * for every changed placeholder, causing inefficiency.
     *
     * @param   player
     *          Player to update placeholders for
     * @param   values
     *          Map of placeholders and their new values
     */
    public static void bulkUpdateValues(@NotNull TabPlayer player, @NotNull Map<PlayerPlaceholderImpl, String> values) {
        Set<RefreshableFeature> features = new HashSet<>();
        for (Map.Entry<PlayerPlaceholderImpl, String> entry : values.entrySet()) {
            if (entry.getKey().hasValueChanged(player, entry.getValue(), true)) {
                features.addAll(TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(entry.getKey().identifier));
            }
        }
        if (!player.isLoaded()) return;
        for (RefreshableFeature r : features) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> r.refresh(player, false),
                    r.getFeatureName(), r.getRefreshDisplayName());
            if (r instanceof CustomThreaded) {
                ((CustomThreaded) r).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Updates placeholder value and returns {@code true} if value changed, {@code false} if not.
     *
     * @param   p
     *          Player to update value for
     * @param   value
     *          New value
     * @param   updateParents
     *          Whether parents should be updated or not
     * @return  {@code true} if value changed, {@code false} if not
     */
    public boolean hasValueChanged(@NotNull TabPlayer p, @Nullable String value, boolean updateParents) {
        if (value == null) return false; //bridge placeholders, they are updated using updateValue method
        if (ERROR_VALUE.equals(value)) return false;
        String newValue = replacements.findReplacement(setPlaceholders(value, p));
        String lastValue = p.lastPlaceholderValues.put(this, newValue);
        if (lastValue == null || (!identifier.equals(newValue) && !newValue.equals(lastValue))) {
            if (updateParents) updateParents(p);
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(p, identifier, newValue);
            return true;
        }
        return false;
    }

    @Override
    public void updateFromNested(@NonNull TabPlayer player) {
        hasValueChanged(player, request(player), true);
    }

    @NotNull
    public synchronized String getLastValue(@Nullable TabPlayer p) {
        if (p == null) return identifier;
        String value = p.lastPlaceholderValues.get(this);
        if (value != null) return value;

        // Value not present, initialize
        p.lastPlaceholderValues.put(this, replacements.findReplacement(identifier));
        hasValueChanged(p, request(p), false);
        return p.lastPlaceholderValues.get(this);
    }

    @Override
    @NotNull
    public String getLastValueSafe(@NotNull TabPlayer player) {
        return player.lastPlaceholderValues.getOrDefault(this, identifier);
    }

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and {@link #ERROR_VALUE} is returned.
     *
     * @param   p
     *          player to get placeholder value for
     * @return  value placeholder returned or {@link #ERROR_VALUE} if it threw an error
     */
    public String request(@NonNull TabPlayer p) {
        long time = System.currentTimeMillis();
        try {
            return function.apply(p);
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().placeholderError("Player placeholder " + identifier + " generated an error when setting for player " + p.getName(), t);
            return ERROR_VALUE;
        } finally {
            long timeDiff = System.currentTimeMillis() - time;
            if (timeDiff > TabConstants.Placeholder.RETURN_TIME_WARN_THRESHOLD) {
                TAB.getInstance().debug("Placeholder " + identifier + " took " + timeDiff + "ms to return value for player " + p.getName());
            }
        }
    }
}