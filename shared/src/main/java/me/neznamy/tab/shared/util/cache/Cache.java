package me.neznamy.tab.shared.util.cache;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Cache to save resources when converting the same values over and over.
 *
 * @param   <K>
 *          Source to convert from
 * @param   <V>
 *          Target to convert to
 */
@RequiredArgsConstructor
public class Cache<K, V> {

    private int accessCount;
    private final String name;
    private final int cacheSize;
    private final Function<K, V> function;
    private final Map<K, V> cache = new HashMap<>();

    /**
     * Gets value from cache. If not present, it is created using given function, inserted
     * into the cache and then returned.
     *
     * @param   key
     *          Source to convert
     * @return  Converted value
     */
    @NotNull
    public synchronized V get(@NotNull K key) {
        accessCount++;
        if (cache.size() > cacheSize) {
            float efficiency = (float) (accessCount-cacheSize) / accessCount;
            TAB.getInstance().debug("Clearing " + name + " cache due to limit (efficiency " + efficiency*100 + "% with " + accessCount + " accesses)");
            accessCount = 0;
            cache.clear();
        }
        return cache.computeIfAbsent(key, function);
    }
}
