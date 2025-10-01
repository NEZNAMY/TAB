package me.neznamy.tab.shared.placeholders.conditions;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to manage conditions.
 */
public class ConditionManager {

    /** All conditions defined in configuration including anonymous conditions */
    @NotNull
    private final Map<String, Condition> registeredConditions = new HashMap<>();

    /**
     * Constructs new instance and registers built-in conditions.
     */
    public ConditionManager() {
        registerCondition(TrueCondition.INSTANCE);
        registerCondition(FalseCondition.INSTANCE);
    }

    /**
     * Registers a condition to the manager.
     *
     * @param   condition
     *          Condition to register
     */
    public void registerCondition(@NonNull Condition condition) {
        registeredConditions.put(condition.getName(), condition);
    }

    /**
     * Returns condition from given string. If the string is name of a condition,
     * that condition is returned. If it's a condition pattern, it is compiled and
     * returned. If the string is {@code null}, {@code null} is returned.
     *
     * @param   string
     *          condition name or pattern
     * @return  condition from string
     */
    @Contract("null -> null")
    public Condition getByNameOrExpression(@Nullable String string) {
        if (string == null || string.isEmpty()) return null;
        String anonVersion = "AnonymousCondition[" + string + "]";
        if (registeredConditions.containsKey(string)) {
            return registeredConditions.get(string);
        } else if (registeredConditions.containsKey(anonVersion)) {
            return registeredConditions.get(anonVersion);
        } else {
            Condition c = new Condition(string);
            c.finishSetup();
            TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder(
                    TabConstants.Placeholder.condition(c.getName()),
                    c.getRefresh(),
                    p -> c.getText((TabPlayer) p)
            );
            registerCondition(c);
            return c;
        }
    }

    /**
     * Marks all placeholders used in the condition as used and registers them.
     * Using a separate method to avoid premature registration of nested conditional placeholders
     * before they are registered properly.
     */
    public void finishSetups() {
        for (Condition c : registeredConditions.values()) {
            c.finishSetup();
        }
    }
}
