package me.neznamy.tab.shared.placeholders.conditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.placeholders.conditions.expression.ConditionalExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class storing configured conditions.
 */
@Getter
@RequiredArgsConstructor
public class ConditionsSection {

    @NotNull private final Map<String, ConditionDefinition> conditions;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static ConditionsSection fromSection(@NotNull ConfigurationSection section) {
        Map<String, ConditionDefinition> conditions = new HashMap<>();
        for (Object condition : section.getKeys()) {
            conditions.put(condition.toString(), ConditionDefinition.fromSection(section.getConfigurationSection(condition.toString()), condition.toString()));
        }
        return new ConditionsSection(conditions);
    }

    /**
     * Structure with defined condition settings.
     */
    @Getter
    @RequiredArgsConstructor
    public static class ConditionDefinition {

        @NotNull private final String name;
        @NotNull private final List<ConditionalExpression> conditions;
        private final boolean type;
        @NotNull private final String yes;
        @NotNull private final String no;

        /**
         * Returns instance of this class created from given configuration section. If there are
         * issues in the configuration, console warns are printed.
         *
         * @param   section
         *          Configuration section to load from
         * @param   name
         *          Name of the condition
         * @return  Loaded instance from given configuration section
         */
        @NotNull
        public static ConditionDefinition fromSection(@NotNull ConfigurationSection section, @NotNull String name) {
            // Check keys
            section.checkForUnknownKey(Arrays.asList("conditions", "type", "true", "false"));

            List<String> list = section.getStringList("conditions");
            if (list == null) {
                section.startupWarn("Condition \"" + name + "\" is missing \"conditions\" section.");
                list = Collections.emptyList();
            }
            String type = section.getString("type");
            Object yes = section.getObject("true");
            if (yes == null) yes = "true";
            Object no = section.getObject("false");
            if (no == null) no = "false";
            if (list.size() >= 2 && type == null) {
                section.startupWarn(String.format("Condition \"%s\" has multiple conditions defined, but is missing \"type\" attribute. Using AND.", name));
            }
            List<ConditionalExpression> expressions = list.stream().map(expressionString -> {
                ConditionalExpression expression = ConditionalExpression.compile(expressionString.trim());
                if (expression == null) {
                    TAB.getInstance().getConfigHelper().startup().startupWarn("Line \"" + expressionString + "\" is not a valid conditional expression.");
                }
                return expression;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return new ConditionDefinition(name, expressions, !"OR".equals(type), yes.toString(), no.toString());
        }
    }
}
