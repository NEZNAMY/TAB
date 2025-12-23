package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a placeholder used in condition, along with information
 * whether it should be parsed as viewer or target.
 */
public class ConditionPlaceholder {

    /** Pattern to detect if placeholder should be parsed for viewer */
    @NotNull
    private static final Pattern VIEWER_PATTERN = Pattern.compile("%viewer:([^%]*)%");

    /** Pattern to detect if placeholder should be parsed for target */
    @NotNull
    private static final Pattern TARGET_PATTERN = Pattern.compile("%target:([^%]*)%");

    /** Definition of the placeholder, including viewer: or target: prefix */
    @NotNull
    @Getter
    private final String placeholderDefinition;

    /** The actual placeholder that is being parsed */
    @NotNull
    @Getter
    private final String realPlaceholder;

    /** Flag tracking whether the placeholder should be parsed as viewer or not */
    private final boolean parseAsViewer;

    /** Flag tracking whether the placeholder is relational */
    @Getter
    private final boolean isRelational;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   placeholderDefinition
     *          Raw configured placeholder that may contain viewer/target specifier
     */
    public ConditionPlaceholder(@NotNull String placeholderDefinition) {
        this.placeholderDefinition = placeholderDefinition;
        // Parse for viewer
        Matcher m = VIEWER_PATTERN.matcher(placeholderDefinition);
        if (m.find()) {
            realPlaceholder = "%" + m.group(1) + "%";
            parseAsViewer = true;
            isRelational = true;
            return;
        }
        // Parse for target
        m = TARGET_PATTERN.matcher(placeholderDefinition);
        if (m.find()) {
            realPlaceholder = "%" + m.group(1) + "%";
            parseAsViewer = false;
            isRelational = false;
            return;
        }
        // No viewer/target specified, parse as target (or a relational placeholder)
        realPlaceholder = placeholderDefinition;
        parseAsViewer = false;
        isRelational = placeholderDefinition.startsWith("%rel_");
    }

    /**
     * Parses the placeholder for the correct player and returns the result.
     *
     * @param   viewer
     *          Player specified as viewer
     * @param   target
     *          Player specified as target
     * @return  Parsed placeholder value
     */
    @NotNull
    public String parse(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        TabPlaceholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(realPlaceholder);
        if (placeholder instanceof RelationalPlaceholderImpl) {
            return ((RelationalPlaceholderImpl) placeholder).getLastValue(viewer, target);
        } else {
            return placeholder.set(realPlaceholder, parseAsViewer ? viewer : target);
        }
    }
}
