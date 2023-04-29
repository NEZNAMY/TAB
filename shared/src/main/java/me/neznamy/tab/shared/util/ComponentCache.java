package me.neznamy.tab.shared.util;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ComponentCache<K, V> {

    private final int cacheSize;
    private final BiFunctionWithException<K, ProtocolVersion, V> function;
    private final Map<K, V> cacheModern = new HashMap<>();
    private final Map<K, V> cacheLegacy = new HashMap<>();

    public @NotNull V get(@NonNull K key, @Nullable ProtocolVersion clientVersion) {
        try {
            Map<K, V> cache = clientVersion == null || clientVersion.getMinorVersion() >= 16 ? cacheModern : cacheLegacy;
            if (cache.containsKey(key)) return cache.get(key);
            V value = function.apply(key, clientVersion);
            if (cache.size() > cacheSize) cache.clear();
            cache.put(key, value);
            return value;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
