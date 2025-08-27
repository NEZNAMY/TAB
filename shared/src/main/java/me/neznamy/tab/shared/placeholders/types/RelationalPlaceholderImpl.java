package me.neznamy.tab.shared.placeholders.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

import lombok.NonNull;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of RelationalPlaceholder interface
 */
public class RelationalPlaceholderImpl extends TabPlaceholder implements RelationalPlaceholder {

    /** Placeholder function returning fresh output on request */
    @NonNull private final BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function;

    /**
     * Constructs new instance with given parameters
     *
     * @param   identifier
     *          placeholder identifier, must start with {@code %rel_} and end with {@code %}
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     * @param   function
     *          refresh function which returns new up-to-date output on request
     */
    public RelationalPlaceholderImpl(@NonNull String identifier, int refresh,
                                     @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function) {
        super(identifier, refresh);
        if (!identifier.startsWith("%rel_")) throw new IllegalArgumentException("Relational placeholder identifiers must start with \"rel_\"");
        this.function = function;
    }

    @Override
    public void update(@NonNull me.neznamy.tab.api.TabPlayer viewer, @NonNull me.neznamy.tab.api.TabPlayer target) {
        updateValue(viewer, target, request((TabPlayer) viewer, (TabPlayer) target));
    }

    @Override
    public void updateValue(@NonNull me.neznamy.tab.api.TabPlayer viewer, @NonNull me.neznamy.tab.api.TabPlayer target, @Nullable String value) {
        if (hasValueChanged((TabPlayer) viewer, (TabPlayer) target, value)) {
            for (RefreshableFeature r : TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(identifier)) {
                TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> r.refresh((TabPlayer) target, true),
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
     * Updates placeholder value and returns {@code true} if value changed, {@code false} if not.
     *
     * @param   viewer
     *          Player viewing the placeholder
     * @param   target
     *          Player the placeholder is displayed on
     * @param   value
     *          New value
     * @return  {@code true} if value changed, {@code false} if not
     */
    public boolean hasValueChanged(@NonNull TabPlayer viewer, @NonNull TabPlayer target, @Nullable String value) {
        if (value == null) return false; //bridge placeholders, they are updated using updateValue method
        String newValue = replacements.findReplacement(value);
        Map<TabPlayer, String> viewerMap = viewer.lastRelationalValues.computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>()));
        if (!viewerMap.getOrDefault(target, identifier).equals(newValue)) {
            viewerMap.put(target, newValue);
            updateParents(viewer);
            updateParents(target);
            return true;
        }
        return false;
    }

    @Override
    public void updateFromNested(@NonNull TabPlayer viewer) {
        Set<RefreshableFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage(identifier);
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            String value = request(viewer, target);
            String s = replacements.findReplacement(String.valueOf(value));
            viewer.lastRelationalValues.computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>())).put(target, s);
            if (!target.isLoaded()) return; // Updated on join
            for (RefreshableFeature f : usage) {
                TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> f.refresh(target, true),
                        f.getFeatureName(), f.getRefreshDisplayName());
                if (f instanceof CustomThreaded) {
                    ((CustomThreaded) f).getCustomThread().execute(task);
                } else {
                    task.run();
                }
            }
            updateParents(target);
        }
        if (!viewer.isLoaded()) return; // Updated on join
        for (RefreshableFeature f : usage) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> f.refresh(viewer, true),
                    f.getFeatureName(), f.getRefreshDisplayName());
            if (f instanceof CustomThreaded) {
                ((CustomThreaded) f).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        updateParents(viewer);
    }

    /**
     * Returns last known value for given players. First player is viewer,
     * second player is target.
     *
     * @param   viewer
     *          viewer of the placeholder
     * @param   target
     *          target who is the text displayed on
     * @return  last known value for entered player duo
     */
    public String getLastValue(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        return setPlaceholders(
                EnumChatFormat.color(
                        viewer.lastRelationalValues.computeIfAbsent(this, v -> Collections.synchronizedMap(new WeakHashMap<>()))
                                .computeIfAbsent(target, t -> retrieveValue(viewer, target))
                ),
                target
        );
    }

    @NotNull
    private String retrieveValue(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        String output = request(viewer, target);
        if (output == null) output = identifier;
        return replacements.findReplacement(output);
    }

    @Override
    public @NotNull String getLastValue(@Nullable TabPlayer p) {
        return identifier;
    }

    @Override
    public @NotNull String getLastValueSafe(@NotNull TabPlayer player) {
        return identifier;
    }

    /**
     * Calls the placeholder request function and returns the output.
     * If the placeholder threw an exception, it is logged in {@code placeholder-errors.log}
     * file and {@link #ERROR_VALUE} is returned.
     *
     * @param   viewer
     *          player looking at output of the placeholder
     * @param   target
     *          player the placeholder is displayed on
     * @return  value placeholder returned or {@link #ERROR_VALUE} if it threw an error
     */
    public @Nullable String request(@NonNull TabPlayer viewer, @NonNull TabPlayer target) {
        long time = System.currentTimeMillis();
        try {
            return function.apply(viewer, target);
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().placeholderError("Relational placeholder " + identifier + " generated an error when setting for players " + viewer.getName() + " and " + target.getName(), t);
            return ERROR_VALUE;
        } finally {
            long timeDiff = System.currentTimeMillis() - time;
            if (timeDiff > TabConstants.Placeholder.RETURN_TIME_WARN_THRESHOLD) {
                TAB.getInstance().debug("Placeholder " + identifier + " took " + timeDiff + "ms to return value for " + viewer.getName() + " and " + target.getName());
            }
        }
    }
}