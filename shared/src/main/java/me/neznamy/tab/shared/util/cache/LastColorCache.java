package me.neznamy.tab.shared.util.cache;

import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.NotNull;

/**
 * An override for StringToComponentCache that appends dummy text to preserve the last color code
 * due to MiniMessage component compacting removing trailing color codes, breaking last color
 * detection for team color to use based on last prefix color.
 */
public class LastColorCache extends StringToComponentCache {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   name
     *          Cache name
     * @param   cacheSize
     *          Size limit of the cache
     */
    public LastColorCache(String name, int cacheSize) {
        super(name, cacheSize);
    }

    @NotNull
    @Override
    public TabComponent convert(@NotNull String text) {
        return super.convert(text + "extra text"); // Append dummy text to preserve last color due to MM compacting
    }
}
