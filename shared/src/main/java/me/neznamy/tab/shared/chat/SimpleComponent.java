package me.neznamy.tab.shared.chat;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ComponentCache;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

/**
 * Simple component with only text using legacy colors and nothing else.
 */
@RequiredArgsConstructor
public class SimpleComponent extends TabComponent {

    /** Component cache for fast access */
    private static final ComponentCache<SimpleComponent, String> serializeCache =
            new ComponentCache<>(1000, (component, clientVersion) -> component.toString());

    @NotNull
    private final String text;

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @NotNull
    public String toString(@NotNull ProtocolVersion clientVersion) {
        if (text.isEmpty()) return "{\"text\":\"\"}";
        return serializeCache.get(this, clientVersion);
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public String toString() {
        // Use json lib to escape special characters
        JSONObject json = new JSONObject();
        json.put("text", text);
        return json.toString();
    }
}
