package me.neznamy.tab.shared.util.cache;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Cache for String -> TabComponent conversion.
 */
@RequiredArgsConstructor
public class StringToComponentCache {

    private int accessCount;
    private final String name;
    private final int cacheSize;
    private final Function<String, TabComponent> function = (text) -> {
        return text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.COLOR_CHAR + "x") || text.contains("<") ?
                TabComponent.fromColoredText(text) : //contains RGB colors or font
                new SimpleComponent(text); //no RGB
    };
    private final Map<String, TabComponent> cache = new HashMap<>();

    /**
     * Gets value from cache. If not present, it is created using given function, inserted
     * into the cache and then returned.
     *
     * @param   key
     *          Source string
     * @return  Converted component
     */
    @NotNull
    public synchronized TabComponent get(@NotNull String key) {
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
