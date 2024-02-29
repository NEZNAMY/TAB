package me.neznamy.tab.platforms.bukkit.nms.component;

import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.BiFunctionWithException;
import me.neznamy.tab.shared.util.ComponentCache;
import org.jetbrains.annotations.NotNull;

/**
 * Class for converting TAB component into NMS components (1.7+).
 */
public class ComponentConverter {

    /** Component cache for better performance */
    private final ComponentCache<TabComponent, Object> componentCache;

    /**
     * Constructs new instance and loads required fields.
     *
     * @throws  ReflectiveOperationException
     *          If a compatibility issue was found
     */
    public ComponentConverter() throws ReflectiveOperationException {
        BiFunctionWithException<TabComponent, ProtocolVersion, Object> function;
        try {
            DirectConverter directConverter = new DirectConverter();
            function = directConverter::convert;
        } catch (ReflectiveOperationException e) {
            if (BukkitUtils.PRINT_EXCEPTIONS) e.printStackTrace();
            JsonDeserializer jsonDeserializer = new JsonDeserializer();
            function = jsonDeserializer::convert;
        }
        componentCache = new ComponentCache<>(1000, function);
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
