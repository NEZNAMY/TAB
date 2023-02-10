package me.neznamy.tab.platforms.sponge;

import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class TextUtils {

    private static final Map<IChatBaseComponent, Text> textCache = new HashMap<>();

    public static Text fromComponent(final IChatBaseComponent component, final ProtocolVersion clientVersion) {
        if (component == null) return null;
        if (textCache.containsKey(component)) return textCache.get(component);

        final Text text = TextSerializers.JSON.deserialize(component.toString(clientVersion));
        if (textCache.size() > 10000) textCache.clear();
        textCache.put(component, text);
        return text;
    }

    private TextUtils() {
    }
}
