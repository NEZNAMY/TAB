package me.neznamy.tab.shared.util.cache;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Component cache to save resources when converting the same
 * values over and over.
 *
 * @param   <K>
 *          Source component
 * @param   <V>
 *          Target component
 */
@RequiredArgsConstructor
public class ComponentCache<K, V> {

    private int accessCount;
    private final String name;
    private final int cacheSize;
    private final BiFunction<K, ProtocolVersion, V> function;
    private final Map<K, V> cacheModern = new HashMap<>();
    private final Map<K, V> cacheLegacy = new HashMap<>();

    /**
     * Gets value from cache. If not present, it is created using given function, inserted
     * into the cache and then returned.
     *
     * @param   key
     *          Source component
     * @param   clientVersion
     *          Client version to convert for
     * @return  Converted component
     */
    @NotNull
    public synchronized V get(@NotNull K key, @Nullable ProtocolVersion clientVersion) {
        accessCount++;
        Map<K, V> cache = clientVersion == null || clientVersion.supportsRGB() ? cacheModern : cacheLegacy;
        if (cache.size() > cacheSize) {
            float efficiency = (float) accessCount / (accessCount + cacheSize);
            TAB.getInstance().debug("Clearing " + name + " cache due to limit (efficiency " + efficiency*100 + "% with " + accessCount + " accesses)");
            accessCount = 0;
            cache.clear();
        }
        return cache.computeIfAbsent(key, k -> function.apply(k, clientVersion));
    }
}
