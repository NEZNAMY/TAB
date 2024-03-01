package me.neznamy.tab.platforms.bukkit.nms.component;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

/**
 * Component converter using JSON serialization and deserialization.
 */
public class JsonDeserializer {

    /** Component deserialize method */
    private final FunctionWithException<String, Object> deserialize = getDeserializeFunction();

    /**
     * Constructs new instance and loads deserialization method.
     *
     * @throws  ReflectiveOperationException
     *          If operation fails
     */
    public JsonDeserializer() throws ReflectiveOperationException {}

    private FunctionWithException<String, Object> getDeserializeFunction() throws ReflectiveOperationException {
        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
        try {
            // 1.20.5+
            Class<?> HolderLookup$Provider = BukkitReflection.getClass("core.HolderLookup$Provider", "core.HolderLookup$b");
            Method fromJson = first(ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class, HolderLookup$Provider));
            Object emptyProvider = ReflectionUtils.getOnlyMethod(HolderLookup$Provider, HolderLookup$Provider, Stream.class).invoke(null, Stream.empty());
            return string -> fromJson.invoke(null, string, emptyProvider);
        } catch (ReflectiveOperationException e) {
            // 1.20.4-
            Method fromJson = first(ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class));
            return string -> fromJson.invoke(null, string);
        }
    }

    @NotNull
    private Method first(@NotNull List<Method> methods) throws NoSuchMethodException {
        if (methods.isEmpty()) throw new NoSuchMethodException("Json deserialize method not found");
        return methods.get(0);
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
        return deserialize.apply(component.toString(version));
    }
}
