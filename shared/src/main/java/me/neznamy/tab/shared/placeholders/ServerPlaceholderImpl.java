package me.neznamy.tab.shared.placeholders;

import java.util.Set;
import java.util.function.Supplier;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;

/**
 * Implementation of ServerPlaceholder interface
 */
public class ServerPlaceholderImpl extends TabPlaceholder implements ServerPlaceholder {

    /** Placeholder function returning fresh output on request */
    private final Supplier<Object> supplier;

    /** Last known output of the placeholder */
    private String lastValue;

    /**
     * Constructs new instance with given parameters
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by 50 or equal to -1 for trigger placeholders
     * @param   supplier
     *          supplier returning fresh output on request
     */
    public ServerPlaceholderImpl(String identifier, int refresh, Supplier<Object> supplier) {
        super(identifier, refresh);
        if (identifier.startsWith("%rel_")) throw new IllegalArgumentException("\"rel_\" is reserved for relational placeholder identifiers");
        this.supplier = supplier;
        update();
    }

    /**
     * Updates placeholder, saves it and returns true if value changed, false if not
     *
     * @return  true if value changed, false if not
     */
    public boolean update() {
        String obj = getReplacements().findReplacement(String.valueOf(request()));
        String newValue = obj == null ? identifier : setPlaceholders(obj, null);

        //make invalid placeholders return identifier instead of nothing
        if (identifier.equals(newValue) && lastValue == null) {
            lastValue = identifier;
        }
        if (!"ERROR".equals(newValue) && !identifier.equals(newValue) && (lastValue == null || !lastValue.equals(newValue))) {
            lastValue = newValue;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                updateParents(player);
                if (TAB.getInstance().getPlaceholderManager().getTabExpansion() != null)
                    TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, newValue);
            }
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
    private void updateValue(Object value, boolean force) {
        String s = getReplacements().findReplacement(value == null ? lastValue == null ? identifier : lastValue : value.toString());
        if (s.equals(lastValue) && !force) return;
        lastValue = s;
        Set<TabFeature> usage = TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().get(identifier);
        if (usage == null) return;
        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            for (TabFeature f : usage) {
                long time = System.nanoTime();
                f.refresh(player, false);
                TAB.getInstance().getCPUManager().addTime(f.getFeatureName(), f.getRefreshDisplayName(), System.nanoTime()-time);
            }
            updateParents(player);
            if (TAB.getInstance().getPlaceholderManager().getTabExpansion() != null)
                TAB.getInstance().getPlaceholderManager().getTabExpansion().setPlaceholderValue(player, identifier, s);
        }
    }

    @Override
    public void updateValue(Object value) {
        updateValue(value, false);
    }

    @Override
    public void updateFromNested(TabPlayer player) {
        updateValue(request(), true);
    }

    @Override
    public String getLastValue(TabPlayer p) {
        return lastValue;
    }

    @Override
    public String getLastValue() {
        return lastValue;
    }

    @Override
    public Object request() {
        try {
            return supplier.get();
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().placeholderError("Server placeholder " + identifier + " generated an error", t);
            return "ERROR";
        }
    }
}