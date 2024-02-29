package me.neznamy.tab.platforms.bukkit.nms;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Class for converting TAB component into NMS components (1.7+).
 */
public class ComponentConverter {

    /** Component deserialize method */
    private final Method ChatSerializer_DESERIALIZE;

    /** Component cache for better performance */
    private final ComponentCache<TabComponent, Object> componentCache;

    /**
     * Constructs new instance and loads required fields.
     *
     * @throws  ReflectiveOperationException
     *          If a compatibility issue was found
     */
    public ComponentConverter() throws ReflectiveOperationException {
        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
        List<Method> methods = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class);
        if (methods.isEmpty()) throw new NoSuchMethodException("Json deserialize method not found");
        ChatSerializer_DESERIALIZE = methods.get(0);
        componentCache =  new ComponentCache<>(1000, (component, clientVersion) ->
                ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));
    }

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Client version to convert component for
     * @return  Converted component
     */
    public Object convert(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        return componentCache.get(component, version);
    }
}
