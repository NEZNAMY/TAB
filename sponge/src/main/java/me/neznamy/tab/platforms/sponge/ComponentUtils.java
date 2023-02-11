package me.neznamy.tab.platforms.sponge;

import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import net.minecraft.network.chat.Component;

public final class ComponentUtils {

    private static final Map<IChatBaseComponent, Component> textCache = new HashMap<>();

    public static Component fromComponent(final IChatBaseComponent component, final ProtocolVersion clientVersion) {
        if (component == null) return null;
        if (textCache.containsKey(component)) return textCache.get(component);

        final Component text = Component.Serializer.fromJson(component.toString(clientVersion));
        if (textCache.size() > 10000) textCache.clear();
        textCache.put(component, text);
        return text;
    }

    private ComponentUtils() {
    }
}
