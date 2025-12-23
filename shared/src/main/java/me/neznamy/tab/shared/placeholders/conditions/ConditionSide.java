package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing one side of a condition. Offers useful functions for parsing
 * and saving constants for better performance.
 */
@Getter
public class ConditionSide {

    /** Text on the side of condition */
    @NotNull
    private final String value;

    /** If side is a static number, value is stored here */
    @Nullable
    private Double staticNumber;

    /** Placeholders used in the side */
    @NotNull
    private final ConditionPlaceholder[] placeholders;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   value
     *          Raw configured value of this side
     */
    public ConditionSide(@NonNull String value) {
        this.value = value;
        try {
            staticNumber = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Value is not a static number
        }
        placeholders = PlaceholderManagerImpl.detectPlaceholders(value).stream().map(ConditionPlaceholder::new).toArray(ConditionPlaceholder[]::new);
    }

    /**
     * Replaces placeholders in the condition side value and return result.
     *
     * @param   viewer
     *          Viewer of the placeholder (relational conditions only)
     * @param   target
     *          Target of the placeholder
     * @return  replaced string
     */
    @NotNull
    public String parse(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        String result = value;
        for (ConditionPlaceholder placeholder : placeholders) {
            String parsed = placeholder.parse(viewer, target);
            if (result.equals(placeholder.getPlaceholderDefinition())) {
                result = parsed;
            } else {
                result = result.replace(placeholder.getPlaceholderDefinition(), parsed);
            }
        }
        return EnumChatFormat.color(result);
    }

    /**
     * Returns a number parsed from this condition side. If it's not a number, 0 is returned;
     * and a console warn is printed.
     *
     * @param   viewer
     *          Viewer of the placeholder (relational conditions only)
     * @param   target
     *          Target of the placeholder
     * @return  Parsed side
     */
    public double parseAsNumber(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        if (staticNumber != null) return staticNumber;
        String parsedValue = parse(viewer, target);
        if (parsedValue.contains(",")) parsedValue = parsedValue.replace(",", "");
        try {
            return Double.parseDouble(parsedValue);
        } catch (NumberFormatException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForCondition(value, parsedValue, target);
            return 0;
        }
    }

    /**
     * Returns {@code true} if this side contains relational placeholders,
     * {@code false} if it only contains regular placeholders or constants.
     *
     * @return  {@code true} if relational content is present, {@code false} if not
     */
    public boolean hasRelationalContent() {
        for (ConditionPlaceholder placeholder : placeholders) {
            if (placeholder.isRelational()) return true;
        }
        return false;
    }
}
