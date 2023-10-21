package me.neznamy.tab.shared.placeholders;

import java.util.Set;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
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
    @Getter private String lastValue;

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
        update0();
        if (lastValue == null) lastValue = identifier;
    }

    /**
     * Updates placeholder, saves it and returns true if value changed, false if not
     *
     * @return  true if value changed, false if not
     */
    public boolean update0() {
        String obj = getReplacements().findReplacement(String.valueOf(request()));
        String newValue = setPlaceholders(obj, null);

        //make invalid placeholders return identifier instead of nothing
        if (identifier.equals(newValue) && lastValue == null) {
            lastValue = identifier;
        }
        if (!"ERROR".equals(newValue) && !identifier.equals(newValue) && (lastValue == null || !lastValue.equals(newValue))) {
            lastValue = newValue;
            TAB.getInstance().getCPUManager().runMeasuredTask(TAB.getInstance().getPlaceholderManager().getFeatureName(),
                    TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, () -> {
                        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                            updateParents(player);
                            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, newValue);
                        }
                    });
            return true;
        }
        return false;
    }

    /**
     * Internal method with an additional parameter {@code force}, which, if set to true,
     * features using the placeholder will refresh despite placeholder seemingly not
     * changing output, which is caused by nested placeholder changing value.
     *
     * @param   value
     *          new placeholder output
     * @param   force
     *          whether refreshing should be forced or not
     */
    private void updateValue(@Nullable Object value, boolean force) {
        String s = getReplacements().findReplacement(value == null ? lastValue == null ? identifier : lastValue : value.toString());
        if (s.equals(lastValue) && !force) return;
        lastValue = s;
        Set<Refreshable> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
        if (usage == null) return;
        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            for (Refreshable f : usage) {
                long time = System.nanoTime();
                f.refresh(player, false);
                TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
            }
            updateParents(player);
            TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, s);
        }
    }

    @Override
    public void updateValue(@NonNull Object value) {
        updateValue(value, false);
    }

    @Override
    public void update() {
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        TAB.getInstance().getCPUManager().runMeasuredTask(pm.getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, () -> {
            if (update0()) {
                for (Refreshable r : pm.getPlaceholderUsage().get(identifier)) {
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        long startTime = System.nanoTime();
                        r.refresh(all, false);
                        TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
                    }
                 }
            }
        });
    }

    @Override
    public void updateFromNested(@NonNull TabPlayer player) {
        updateValue(request(), true);
    }

    @Override
    public @NotNull String getLastValue(@Nullable TabPlayer p) {
        return lastValue;
    }

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and {@link #ERROR_VALUE} is returned.
     *
     * @return  value placeholder returned or {@link #ERROR_VALUE} if it threw an error
     */
    public @Nullable Object request() {
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