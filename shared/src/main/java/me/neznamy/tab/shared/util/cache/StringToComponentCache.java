package me.neznamy.tab.shared.util.cache;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.MiniMessageHook;

/**
 * Cache for String -> TabComponent conversion.
 */
public class StringToComponentCache extends Cache<String, TabComponent> {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   name
     *          Cache name
     * @param   cacheSize
     *          Size limit of the cache
     */
    public StringToComponentCache(String name, int cacheSize) {
        super(name, cacheSize, text -> {
            TabComponent component = MiniMessageHook.parseText(text);
            if (component != null) return component;
            return text.contains("#") || text.contains("&x") || text.contains(EnumChatFormat.COLOR_CHAR + "x") || text.contains("<") ?
                    TabComponent.fromColoredText(text) : //contains RGB colors or font
                    new SimpleComponent(text); //no RGB
        });
    }
}
