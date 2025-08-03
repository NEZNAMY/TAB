package me.neznamy.tab.shared.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing information about a world. Instances are re-used,
 * so identity comparison is available to see if two worlds are the same.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class World {

    /** Map of all worlds, indexed by their name */
    private static final Map<String, World> worlds = new HashMap<>();

    /** Name of the world */
    @NonNull
    private final String name;

    /**
     * Returns world with given name, creating it if it does not exist.
     *
     * @param   name
     *          Name of the world
     * @return  World instance with given name
     */
    @Contract("!null -> !null")
    public static World byName(@Nullable String name) {
        if (name == null) return null;
        return worlds.computeIfAbsent(name, World::new);
    }
}
