package me.neznamy.tab.shared.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Class storing information about a world. Instances are re-used,
 * so identity comparison is available to see if two worlds are the same.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class World {

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
        return TAB.getInstance().getDataManager().getWorlds().computeIfAbsent(name, World::new);
    }
}
