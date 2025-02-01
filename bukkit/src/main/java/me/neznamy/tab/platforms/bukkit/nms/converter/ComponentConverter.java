package me.neznamy.tab.platforms.bukkit.nms.converter;

import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Interface for converting TAB component into NMS components (1.7+).
 */
public abstract class ComponentConverter {

    /** Versions supported by paper module that uses direct mojang-mapped NMS for latest MC version */
    private static final EnumSet<ProtocolVersion> paperNativeVersions = EnumSet.of(
            ProtocolVersion.V1_20_5,
            ProtocolVersion.V1_20_6,
            ProtocolVersion.V1_21,
            ProtocolVersion.V1_21_1,
            ProtocolVersion.V1_21_2,
            ProtocolVersion.V1_21_3,
            ProtocolVersion.V1_21_4
    );

    /** Instance of this class */
    @Nullable
    public static ComponentConverter INSTANCE;

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    @NotNull
    public abstract Object convert(@NotNull TabComponent component);

    /**
     * Attempts to load component converter.
     *
     * @param   serverVersion
     *          Server version
     */
    public static void tryLoad(@NotNull ProtocolVersion serverVersion) {
        try {
            if (ReflectionUtils.classExists("org.bukkit.craftbukkit.CraftServer") && paperNativeVersions.contains(serverVersion)) {
                INSTANCE = (ComponentConverter) Class.forName("me.neznamy.tab.platforms.paper.PaperComponentConverter").getConstructor().newInstance();
            } else {
                INSTANCE = new ReflectionComponentConverter();
            }
        } catch (Exception e) {
            if (BukkitUtils.PRINT_EXCEPTIONS) {
                e.printStackTrace();
            }
        }
    }
}
