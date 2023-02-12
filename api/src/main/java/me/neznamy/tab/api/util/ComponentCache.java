package me.neznamy.tab.api.util;

import lombok.AllArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ComponentCache<K, V> {

    private final int cacheSize;
    private final BiFunctionWithException<K, ProtocolVersion, V> function;
    private final Map<K, V> cacheModern = new HashMap<>();
    private final Map<K, V> cacheLegacy = new HashMap<>();

    public V get(K key, ProtocolVersion clientVersion) {
        try {
            if (key == null) return null;
            Map<K, V> cache = clientVersion == null || clientVersion.getMinorVersion() >= 16 ? cacheModern : cacheLegacy;
            if (cache.containsKey(key)) return cache.get(key);
            V value = function.apply(key, clientVersion);
            if (cache.size() > cacheSize) cache.clear();
            cache.put(key, value);
            return value;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
