package me.neznamy.tab.shared.config.files.config;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConditionsSection extends ConfigurationSection {

    private final String SECTION = "conditions";
    @NotNull public final Map<String, ConditionDefinition> conditions = new HashMap<>();

    public ConditionsSection(@NotNull ConfigurationFile config) {
        super(config);
        for (Object conditionName : getMap(SECTION, Collections.emptyMap()).keySet()) {
            checkForUnknownKey(SECTION + "." + conditionName, Arrays.asList("conditions", "type", "true", "false"));
            List<String> list = getStringList(SECTION + "." + conditionName + ".conditions");
            if (list == null) {
                startupWarn("Condition \"" + conditionName + "\" is missing \"conditions\" section.");
                continue;
            }
            String type = getString(SECTION + "." + conditionName + ".type");
            String yes = getString(SECTION + "." + conditionName + ".true", "true");
            String no = getString(SECTION + "." + conditionName + ".false", "false");
            if (list.size() >= 2 && type == null) {
                startupWarn(String.format("Condition \"%s\" has multiple conditions defined, but is missing \"type\" attribute. Using AND.", conditionName));
            }
            conditions.put(conditionName.toString(), new ConditionDefinition(list, !"OR".equals(type), yes, no));
        }
    }

    @RequiredArgsConstructor
    public static class ConditionDefinition {

        @NotNull public final List<String> conditions;
        public final boolean type;
        @Nullable public final String yes;
        @Nullable public final String no;
    }
}
