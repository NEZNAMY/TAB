package me.neznamy.tab.shared.util;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Component cache to save resources when converting the same
 * values over and over.
 *
 * @param   <K>
 *          Source component
 * @param   <V>
 *          Target component
 */
@AllArgsConstructor
public class ComponentCache<K, V> {

    private final int cacheSize;
    private final BiFunctionWithException<K, ProtocolVersion, V> function;
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
    @SneakyThrows
    public @NotNull V get(@NotNull K key, @Nullable ProtocolVersion clientVersion) {
        Map<K, V> cache = clientVersion == null || clientVersion.supportsRGB() ? cacheModern : cacheLegacy;
        if (cache.containsKey(key)) return cache.get(key);
        V value = function.apply(key, clientVersion);
        if (cache.size() > cacheSize) cache.clear();
        cache.put(key, value);
        return value;
    }
}
