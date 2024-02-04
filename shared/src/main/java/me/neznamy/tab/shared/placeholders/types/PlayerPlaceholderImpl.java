package me.neznamy.tab.shared.placeholders.types;

import lombok.NonNull;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Implementation of the PlayerPlaceholder interface
 */
public class PlayerPlaceholderImpl extends TabPlaceholder implements PlayerPlaceholder {

    /** Placeholder function returning fresh output on request */
    @NonNull private final Function<me.neznamy.tab.api.TabPlayer, Object> function;

    /** Last known values for each online player after applying replacements and nested placeholders */
    private final Map<TabPlayer, String> lastValues = Collections.synchronizedMap(new WeakHashMap<>());

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
    public PlayerPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull Function<me.neznamy.tab.api.TabPlayer, Object> function) {
        super(identifier, refresh);
        if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
        this.function = function;
    }

    @Override
    public void update(@NonNull me.neznamy.tab.api.TabPlayer player) {
        updateValue(player, request((TabPlayer) player));
    }

    @Override
    public void updateValue(@NonNull me.neznamy.tab.api.TabPlayer player, @Nullable Object value) {
        if (hasValueChanged((TabPlayer) player, value)) {
            if (!player.isLoaded()) return; // Updated on join
            for (Refreshable r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(identifier)) {
                long startTime = System.nanoTime();
                r.refresh((TabPlayer) player, false);
                TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
            }
        }
    }

    public boolean hasValueChanged(@NotNull TabPlayer p, @Nullable Object value) {
        if (value == null) return false; //bridge placeholders, they are updated using updateValue method
        String newValue = replacements.findReplacement(setPlaceholders(String.valueOf(value), p));

        //make invalid placeholders return identifier instead of nothing
        if (identifier.equals(newValue) && !lastValues.containsKey(p)) {
            lastValues.put(p, identifier);
        }
        if (!lastValues.containsKey(p) || (!ERROR_VALUE.equals(newValue) && !identifier.equals(newValue) && !newValue.equals(lastValues.getOrDefault(p, null)))) {
            lastValues.put(p, ERROR_VALUE.equals(newValue) ? identifier : newValue);
            updateParents(p);
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(p, identifier, newValue);
            return true;
        }
        return false;
    }

    @Override
    public void updateFromNested(@NonNull TabPlayer player) {
        hasValueChanged(player, request(player));
    }

    @NotNull
    public String getLastValue(@Nullable TabPlayer p) {
        if (p == null) return identifier;
        if (!lastValues.containsKey(p)) {
            lastValues.put(p, replacements.findReplacement(identifier));
            update(p);
        }
        return lastValues.get(p);
    }

    @Override
    @NotNull
    public String getLastValueSafe(@NotNull TabPlayer player) {
        return lastValues.getOrDefault(player, identifier);
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
    public Object request(@NonNull TabPlayer p) {
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