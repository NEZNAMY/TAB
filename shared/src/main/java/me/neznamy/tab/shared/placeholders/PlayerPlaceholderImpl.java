package me.neznamy.tab.shared.placeholders;

import java.util.*;
import java.util.function.Function;

import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.TAB;

/**
 * Implementation of the PlayerPlaceholder interface
 */
public class PlayerPlaceholderImpl extends TabPlaceholder implements PlayerPlaceholder {

    /**
     * Internal constant used to detect if placeholder threw an error.
     * If so, placeholder's last known value is displayed.
     */
    private final String ERROR_VALUE = "ERROR";

    /** Placeholder function returning fresh output on request */
    private final Function<TabPlayer, Object> function;

    /** Last known values for each online player after applying replacements and nested placeholders */
    private final WeakHashMap<TabPlayer, String> lastValues = new WeakHashMap<>();

    /**
     * Constructs new instance with given parameters
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link me.neznamy.tab.api.TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     * @param   function
     *          refresh function which returns new up-to-date output on request
     */
    public PlayerPlaceholderImpl(String identifier, int refresh, Function<TabPlayer, Object> function) {
        super(identifier, refresh);
        if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
        this.function = function;
    }

    /**
     * Gets new value of the placeholder, saves it to map and returns true if value changed, false if not
     *
     * @param   p
     *          player to update placeholder for
     * @return  {@code true} if value changed since last time, {@code false} if not
     */
    public boolean update(TabPlayer p) {
        Object output = request(p);
        if (output == null) return false; //bridge placeholders, they are updated using updateValue method
        String obj = getReplacements().findReplacement(String.valueOf(output));
        String newValue = obj == null ? identifier : setPlaceholders(obj, p);

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

    /**
     * Internal method with an additional parameter {@code force}, which, if set to true,
     * features using the placeholder will refresh despite placeholder seemingly not
     * changing output, which is caused by nested placeholder changing value.
     *
     * @param   player
     *          player to update value for
     * @param   value
     *          new placeholder output
     * @param   force
     *          whether refreshing should be forced or not
     */
    private void updateValue(TabPlayer player, Object value, boolean force) {
        String s = getReplacements().findReplacement(value == null ? lastValues.getOrDefault(player, identifier) :
                setPlaceholders(value.toString(), player));
        if (s.equals(lastValues.getOrDefault(player, identifier)) && !force) return;
        lastValues.put(player, s);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, s);
        if (!player.isLoaded()) {
            if (player.isOnline()) {
                TAB.getInstance().getCPUManager().runTask(() -> updateValue(player, value, force));
            }
            return;
        }
        Set<Refreshable> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
        if (usage == null) return;
        for (Refreshable f : usage) {
            long time = System.nanoTime();
            f.refresh(player, false);
            TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
        }
        updateParents(player);
    }

    @Override
    public void updateValue(TabPlayer player, Object value) {
        updateValue(player, value, false);
    }

    @Override
    public void updateFromNested(TabPlayer player) {
        updateValue(player, request(player), true);
    }

    @Override
    public String getLastValue(TabPlayer p) {
        if (p == null) return identifier;
        if (!lastValues.containsKey(p)) {
            lastValues.put(p, getReplacements().findReplacement(identifier));
            update(p);
        }
        return lastValues.get(p);
    }

    @Override
    public Object request(TabPlayer p) {
        try {
            return function.apply(p);
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().placeholderError("Player placeholder " + identifier + " generated an error when setting for player " + p.getName(), t);
            return ERROR_VALUE;
        }
    }
}