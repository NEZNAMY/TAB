package me.neznamy.tab.platforms.bukkit.nms.component;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Component converter using JSON serialization and deserialization.
 */
public class JsonDeserializer {

    /** Component deserialize method */
    private final Method ChatSerializer_DESERIALIZE;

    /**
     * Constructs new instance and loads deserialization method.
     *
     * @throws  ReflectiveOperationException
     *          If operation fails
     */
    public JsonDeserializer() throws ReflectiveOperationException {
        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
        List<Method> methods = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class);
        if (methods.isEmpty()) throw new NoSuchMethodException("Json deserialize method not found");
        ChatSerializer_DESERIALIZE = methods.get(0);
    }

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Client version
     * @return  Converted component
     */
    @SneakyThrows
    public Object convert(@NotNull TabComponent component, @NotNull ProtocolVersion version) {
        return ChatSerializer_DESERIALIZE.invoke(null, component.toString(version));
    }
}
