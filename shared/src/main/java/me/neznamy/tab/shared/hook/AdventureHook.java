package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class for Adventure component conversion.
 */
public class AdventureHook {

    /** Component to string cache for better performance */
    private static final ComponentCache<Component, String> componentToString = new ComponentCache<>(1000,
            (component, version) -> GsonComponentSerializer.gson().serialize(component));

    /**
     * Converts component to adventure component
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          Whether client supports RGB or not
     * @return  Adventure component from this component.
     */
    @NotNull
    public static Component toAdventureComponent(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent) return Component.text(((SimpleComponent) component).getText());
        StructuredComponent iComponent = (StructuredComponent) component;
        ChatModifier modifier = iComponent.getModifier();

        Component adventureComponent = Component.text(
                iComponent.getText(),
                convertColor(modifier.getColor(), modern),
                getDecorations(modifier)
        );

        if (modifier.getFont() != null) {
            adventureComponent = adventureComponent.font(Key.key(modifier.getFont()));
        }
        if (!iComponent.getExtra().isEmpty()) {
            List<Component> list = new ArrayList<>();
            for (StructuredComponent extra : iComponent.getExtra()) {
                list.add(toAdventureComponent(extra, modern));
            }
            adventureComponent = adventureComponent.children(list);
        }
        return adventureComponent;
    }

    /**
     * Serializes component using Adventure API.
     *
     * @param   component
     *          Component to serialize
     * @return  Serialized component to json
     */
    @NotNull
    public static String serialize(@NotNull Component component) {
        return componentToString.get(component, null);
    }

    /**
     * Converts TAB color into adventure color.
     *
     * @param   color
     *          Color to convert
     * @param   rgbSupport
     *          Whether RGB is supported or not
     * @return  Converted color
     */
    @Nullable
    private static TextColor convertColor(@Nullable me.neznamy.tab.shared.chat.TextColor color, boolean rgbSupport) {
        if (color == null) return null;
        if (rgbSupport) {
            return TextColor.color(color.getRgb());
        } else {
            return TextColor.color(color.getLegacyColor().getRgb());
        }
    }

    /**
     * Gets decorations from modifier.
     *
     * @param   modifier
     *          Modifier to get decorations from
     * @return  Decorations from modifier
     */
    private static Set<TextDecoration> getDecorations(@NotNull ChatModifier modifier) {
        if (!modifier.hasMagicCodes()) return Collections.emptySet();
        Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
        if (modifier.isBold()) decorations.add(TextDecoration.BOLD);
        if (modifier.isItalic()) decorations.add(TextDecoration.ITALIC);
        if (modifier.isObfuscated()) decorations.add(TextDecoration.OBFUSCATED);
        if (modifier.isStrikethrough()) decorations.add(TextDecoration.STRIKETHROUGH);
        if (modifier.isUnderlined()) decorations.add(TextDecoration.UNDERLINED);
        return decorations;
    }
}
