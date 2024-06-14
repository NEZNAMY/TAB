package me.neznamy.tab.platforms.bukkit.nms.converter;

import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for converting TAB component into NMS components (1.7+).
 */
public abstract class ComponentConverter {

    /** Instance of this class */
    @Nullable
    public static ComponentConverter INSTANCE;

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          Whether client supports RGB or not
     * @return  Converted component
     */
    @NotNull
    public abstract Object convert(@NotNull TabComponent component, boolean modern);
}
