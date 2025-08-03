package me.neznamy.tab.shared.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Class storing information about a world. Instances are re-used,
 * so identity comparison is available to see if two worlds are the same.
 */
@RequiredArgsConstructor
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
    public static World byName(@NonNull String name) {
        return worlds.computeIfAbsent(name, World::new);
    }
}
