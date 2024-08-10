package me.neznamy.tab.shared.config.files.config;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PerWorldPlayerListConfiguration extends ConfigurationSection {

    private final String SECTION = "per-world-playerlist";
    public final boolean allowBypassPermission = getBoolean(SECTION + ".allow-bypass-permission", false);
    @NotNull public final List<String> ignoredWorlds = getStringList(SECTION + ".ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
    @NotNull public final Map<String, List<String>> sharedWorlds = new HashMap<>();

    public PerWorldPlayerListConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "allow-bypass-permission", "ignore-effect-in-worlds", "shared-playerlist-world-groups"));
        for (Object worldGroup : getMap(SECTION + ".shared-playerlist-world-groups", Collections.emptyMap()).keySet()) {
            sharedWorlds.put(worldGroup.toString(), getStringList(SECTION + ".shared-playerlist-world-groups." + worldGroup, Collections.emptyList()));
        }
    }
}
