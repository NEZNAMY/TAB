package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class representing per world playerlist configuration.
 */
@Getter
@RequiredArgsConstructor
public class PerWorldPlayerListConfiguration {

    private final boolean allowBypassPermission;
    @NotNull private final List<String> ignoredWorlds;
    @NotNull private final Map<String, List<String>> sharedWorlds;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static PerWorldPlayerListConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "allow-bypass-permission", "ignore-effect-in-worlds", "shared-playerlist-world-groups"));

        ConfigurationSection sharedWorldsSection = section.getConfigurationSection("shared-playerlist-world-groups");
        Map<String, List<String>> sharedWorlds = new HashMap<>();
        Map<String, String> takenWorlds = new HashMap<>();
        for (Object worldGroup : sharedWorldsSection.getKeys()) {
            String group = worldGroup.toString();
            List<String> worlds = sharedWorldsSection.getStringList(group, Collections.emptyList());
            sharedWorlds.put(group, worlds);
            for (String server : worlds) {
                if (takenWorlds.containsKey(server)) {
                    section.startupWarn(String.format("World \"%s\" is defined in per world playerlist groups \"%s\" and \"%s\", but it can only be a part of one group.",
                            server, takenWorlds.get(server), group));
                    continue;
                }
                takenWorlds.put(server, group);
            }
        }

        return new PerWorldPlayerListConfiguration(
                section.getBoolean("allow-bypass-permission", false),
                section.getStringList("ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build")),
                sharedWorlds
        );
    }
}
