package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;

import java.util.ArrayList;
import java.util.List;

/**
 * General collection of variables and functions shared between all placeholder types
 */
public abstract class TabPlaceholder implements Placeholder {

    /** Refresh interval of the placeholder */
    @Getter private final int refresh;

    /** Placeholder's identifier including % */
    @Getter protected final String identifier;

    /** Configured placeholder output replacements */
    @Getter protected final PlaceholderReplacementPattern replacements;

    /** Boolean tracking whether this placeholder is actually used or not */
    @Getter private boolean used;

    /**
     * Runnable to run when this placeholder becomes used and this is a trigger placeholder.
     * This is typically registering an event listener so placeholders don't listen to
     * events if they are not used at all. May be null if nothing should run.
     */
    private Runnable onActivation;

    /**
     * Runnable to run when this is a trigger placeholder and the plugin shuts down,
     * which may just be a /tab reload. This is typically unregistering and event
     * listener to avoid resource leak on reload. May be null if nothing should run.
     */
    private Runnable onDisable;

    /**
     * List of placeholders using this placeholder as a nested placeholder,
     * mutual tracking allows faster parent placeholder changes when a nested
     * placeholder changed value.
     */
    protected final List<String> parents = new ArrayList<>();

    /**
     * Constructs new instance with given parameters and loads placeholder output replacements
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link me.neznamy.tab.api.TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     */
    protected TabPlaceholder(String identifier, int refresh) {
        if (refresh % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0 && refresh != -1)
            throw new IllegalArgumentException("Refresh interval must be divisible by " + TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        if (!identifier.startsWith("%") || !identifier.endsWith("%"))
            throw new IllegalArgumentException("Identifier must start and end with % (attempted to use \"" + identifier + "\")");
        this.identifier = identifier;
        this.refresh = refresh;
        replacements = new PlaceholderReplacementPattern(identifier, TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements." + identifier));
        for (String nested : getNestedPlaceholders("")) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
        }
        for (String nested : replacements.getNestedPlaceholders()) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
        }
    }

    /**
     * Replaces this placeholder in given string and returns output. If the entered string
     * is equal to the placeholder identifier or does not contain the identifier at all,
     * value is returned directly without calling {@code String#replace} for better performance.
     *
     * @param   string
     *          string to replace this placeholder in
     * @param   player
     *          player to set placeholder for
     * @return  string with this placeholder replaced
     */
    public String set(String string, TabPlayer player) {
        return replace(string, identifier, setPlaceholders(getLastValue(player), player));
    }

    /**
     * Returns all nested placeholders in provided output. If no placeholders are detected,
     * returns empty list.
     *
     * @param   output
     *          output to check
     * @return  List of nested placeholders in provided output
     */
    public List<String> getNestedPlaceholders(String output) {
        return TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(output);
    }

    /**
     * An alternative for {@code String#replace} function with better performance.
     * If the input string does not contain string to replace, it is returned immediately.
     * If the input string is equal to text to replace, output is returned directly.
     *
     * @param   string
     *          String to replace text in
     * @param   original
     *          Text to replace
     * @param   replacement
     *          Replacement text
     * @return  Replaced text
     */
    private String replace(String string, String original, String replacement) {
        if (!string.contains(original)) return string;
        if (string.equals(original)) return replacement;
        return string.replace(original, replacement);
    }

    /**
     * Applies all nested placeholders in output
     *
     * @param   text
     *          replaced placeholder
     * @param   p
     *          player to replace for
     * @return  text with replaced placeholders in output
     */
    protected String setPlaceholders(String text, TabPlayer p) {
        if (identifier.equals(text)) return text;
        String replaced = text;
        for (String s : getNestedPlaceholders(text)) {
            if (s.equals(identifier) || (identifier.startsWith("%sync:") && ("%" + identifier.substring(6)).equals(s)) || s.startsWith("%rel_")) continue;
            replaced = TAB.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced, p);
        }
        return replaced;
    }

    /**
     * Marks this placeholder as used, which sets {@link #used} to true and if
     * {@link #onActivation} is not null, runs it.
     */
    public void markAsUsed() {
        if (used) return;
        used = true;
        if (onActivation != null) onActivation.run();
    }

    /**
     * Internal method used to mark placeholders as parents who use this placeholder
     * inside their outputs for faster updates.
     *
     * @param   parent
     *          parent placeholder using this placeholder in output
     */
    public void addParent(String parent) {
        if (!parents.contains(parent)) parents.add(parent);
    }

    /**
     * Updates all placeholders that use this placeholder
     * as a nested placeholder
     *
     * @param   player
     *          Player to update placeholders for.
     */
    public void updateParents(TabPlayer player) {
        if (parents.isEmpty()) return;
        parents.stream().map(identifier -> TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier)).forEach(placeholder -> placeholder.updateFromNested(player));
    }

    /**
     * Updates the placeholder with force mark for requested player
     *
     * @param   player
     *          player to update placeholder for
     */
    public abstract void updateFromNested(TabPlayer player);

    /**
     * Returns last known value of defined player
     *
     * @param   player
     *          player to get value of
     * @return  last known value for specified player
     */
    public abstract String getLastValue(TabPlayer player);

    @Override
    public void unload() {
        if (onDisable != null && used) onDisable.run();
    }

    @Override
    public void enableTriggerMode(Runnable onActivation, Runnable onDisable) {
        this.onActivation = onActivation;
        this.onDisable = onDisable;
        if (used && onActivation != null) onActivation.run();
    }
}