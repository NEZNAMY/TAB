package me.neznamy.tab.shared.placeholders.types;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of ServerPlaceholder interface
 */
public class ServerPlaceholderImpl extends TabPlaceholder implements ServerPlaceholder {

    /** Placeholder function returning fresh output on request */
    private final Supplier<Object> supplier;

    /** Last known output of the placeholder */
    @Getter
    @NotNull
    private String lastValue = identifier;

    /**
     * Constructs new instance with given parameters
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     * @param   supplier
     *          supplier returning fresh output on request
     */
    public ServerPlaceholderImpl(@NonNull String identifier, int refresh, @NonNull Supplier<Object> supplier) {
        super(identifier, refresh);
        if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
        this.supplier = supplier;
        hasValueChanged(request());
    }

    @Override
    public void update() {
        updateValue(request());
    }

    @Override
    public void updateValue(@Nullable Object value) {
        if (hasValueChanged(value)) {
            for (Refreshable r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(identifier)) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (!all.isLoaded()) return; // Updated on join
                    long startTime = System.nanoTime();
                    r.refresh(all, false);
                    TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
                }
            }
        }
    }

    public boolean hasValueChanged(@Nullable Object value) {
        String newValue = setPlaceholders(replacements.findReplacement(String.valueOf(value)), null);

        if (!ERROR_VALUE.equals(newValue) && !identifier.equals(newValue) && !lastValue.equals(newValue)) {
            lastValue = newValue;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                updateParents(player);
                TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, newValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public void updateFromNested(@NonNull TabPlayer unused) {
        hasValueChanged(request());
    }

    @Override
    @NotNull
    public String getLastValue(@Nullable TabPlayer p) {
        return lastValue;
    }

    @Override
    @NotNull
    public String getLastValueSafe(@NotNull TabPlayer player) {
        return identifier;
    }

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and {@link #ERROR_VALUE} is returned.
     *
     * @return  value placeholder returned or {@link #ERROR_VALUE} if it threw an error
     */
    @Nullable
    public Object request() {
        long time = System.currentTimeMillis();
        try {
            return supplier.get();
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().placeholderError("Server placeholder " + identifier + " generated an error", t);
            return ERROR_VALUE;
        } finally {
            long timeDiff = System.currentTimeMillis() - time;
            if (timeDiff > TabConstants.Placeholder.RETURN_TIME_WARN_THRESHOLD) {
                TAB.getInstance().debug("Placeholder " + identifier + " took " + timeDiff + "ms to return value");
            }
        }
    }
}