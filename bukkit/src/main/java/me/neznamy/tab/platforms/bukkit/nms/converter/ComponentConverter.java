package me.neznamy.tab.platforms.bukkit.nms.converter;

import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Interface for converting TAB components into NMS components (1.7+).
 */
public abstract class ComponentConverter {

    /** Versions supported by paper module that uses direct mojang-mapped NMS */
    private static final EnumSet<ProtocolVersion> paperNativeVersions = EnumSet.of(
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
    public Object convert(@NotNull TabComponent component) {
        // Component type
        Object nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = newTextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = newTranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = newKeybindComponent(((KeybindComponent)component).getKeybind());
        } else {
            throw new IllegalArgumentException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        applyStyle(nmsComponent, component.getModifier());

        // Extra
        for (TabComponent extra : component.getExtra()) {
            addSibling(nmsComponent, convert(extra));
        }

        return nmsComponent;
    }

    /**
     * Creates a new text component with given text.
     *
     * @param   text
     *          Text to display
     * @return  Text component with given text
     */
    @NotNull
    public abstract Object newTextComponent(@NotNull String text);

    /**
     * Creates a new translatable component with the given key.
     *
     * @param   key
     *          Key to translate
     * @return  Translatable component with the given key
     */
    @NotNull
    public abstract Object newTranslatableComponent(@NotNull String key);

    /**
     * Creates a new keybind component with given keybind.
     *
     * @param   keybind
     *          Keybind to show
     * @return  Keybind component with given keybind
     */
    @NotNull
    public abstract Object newKeybindComponent(@NotNull String keybind);

    /**
     * Converts given chat modifier to minecraft style and applies it to the component.
     *
     * @param   nmsComponent
     *          Component to apply style to
     * @param   style
     *          Style to convert and apply
     */
    public abstract void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier style);

    /**
     * Appends child to the given parent component.
     *
     * @param   parent
     *          Parent to append the child to
     * @param   child
     *          Child component to append
     */
    public abstract void addSibling(@NotNull Object parent, @NotNull Object child);

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
            } else if (BukkitReflection.getMinorVersion() >= 19) {
                // 1.19+
                INSTANCE = new ModernComponentConverter();
            } else if (BukkitReflection.getMinorVersion() >= 16) {
                // 1.16 - 1.18.2
                INSTANCE = new ModerateComponentConverter();
            } else {
                // 1.7 - 1.15.2
                INSTANCE = new LegacyComponentConverter();
            }
        } catch (Exception e) {
            if (BukkitUtils.PRINT_EXCEPTIONS) {
                e.printStackTrace();
            }
        }
    }
}
