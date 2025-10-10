package me.neznamy.tab.shared.chat.hook;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabKeybindComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.chat.component.TabTranslatableComponent;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for Adventure component conversion.
 */
public class AdventureHook {

    /** Flag for tracking presence of shadow color parameter in current included adventure library (added in 1.21.4) */
    private static final boolean SHADOW_COLOR_AVAILABLE;
    private static final boolean OBJECT_COMPONENTS_AVAILABLE = ReflectionUtils.classExists("net.kyori.adventure.text.ObjectComponent");

    static {
        boolean value;
        try {
            Component.class.getDeclaredMethod("shadowColor");
            value = true;
        } catch (Throwable t) {
            value = false;
        }
        SHADOW_COLOR_AVAILABLE = value;
    }

    /**
     * Converts TAB component to adventure component
     *
     * @param   component
     *          Component to convert
     * @return  Adventure component from TAB component.
     */
    @NotNull
    public static Component convert(@NotNull TabComponent component) {
        // Component style
        Style.Builder style = Style.style()
                .color(component.getModifier().getColor() == null ? null : TextColor.color(component.getModifier().getColor().getRgb()))
                .decoration(TextDecoration.BOLD, getDecoration(component.getModifier().getBold()))
                .decoration(TextDecoration.ITALIC, getDecoration(component.getModifier().getItalic()))
                .decoration(TextDecoration.UNDERLINED, getDecoration(component.getModifier().getUnderlined()))
                .decoration(TextDecoration.STRIKETHROUGH, getDecoration(component.getModifier().getStrikethrough()))
                .decoration(TextDecoration.OBFUSCATED, getDecoration(component.getModifier().getObfuscated()))
                .font(component.getModifier().getFont() == null ? null : Key.key(component.getModifier().getFont()));
        if (SHADOW_COLOR_AVAILABLE) {
            AdventureShadowHook.setShadowColor(style, component.getModifier().getShadowColor());
        }

        // Extra
        List<Component> list = new ArrayList<>();
        for (TabComponent extra : component.getExtra()) {
            list.add(convert(extra));
        }
        
        // Component type & return
        if (component instanceof TabTextComponent) {
            return Component.text(((TabTextComponent) component).getText(), style.build()).children(list);
        }
        if (component instanceof TabTranslatableComponent) {
            return Component.translatable(((TabTranslatableComponent) component).getKey(), style.build()).children(list);
        }
        if (component instanceof TabKeybindComponent) {
            return Component.keybind(((TabKeybindComponent) component).getKeybind(), style.build()).children(list);
        }
        if (component instanceof TabObjectComponent) {
            if (OBJECT_COMPONENTS_AVAILABLE) {
                return AdventureObjectHook.convert((TabObjectComponent) component);
            } else {
                return Component.text("<Object components are not supported in your version of Adventure library>");
            }
        }
        throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
    }

    @NotNull
    private static TextDecoration.State getDecoration(@Nullable Boolean state) {
        if (state == null) return TextDecoration.State.NOT_SET;
        return state ? TextDecoration.State.TRUE : TextDecoration.State.FALSE;
    }

    /**
     * Converts adventure component to TAB component
     *
     * @param   component
     *          Component to convert
     * @return  TAB component from adventure component.
     */
    @NotNull
    public static TabComponent convert(@NotNull Component component) {
        // Component type
        TabComponent tabComponent;
        if (component instanceof TextComponent) {
            tabComponent = new TabTextComponent(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            tabComponent = TabComponent.translatable(((TranslatableComponent) component).key());
        } else if (component instanceof KeybindComponent) {
            tabComponent = TabComponent.keybind(((KeybindComponent) component).keybind());
        } else if (component instanceof ObjectComponent) {
            tabComponent = AdventureObjectHook.convert((ObjectComponent) component);
        } else {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
        }

        // Component style
        Map<TextDecoration, TextDecoration.State> decorations = component.style().decorations();
        TextColor color = component.color();
        Key font = component.font();
        tabComponent.setModifier(new TabStyle(
                color == null ? null : new TabTextColor(color.value()),
                SHADOW_COLOR_AVAILABLE ? AdventureShadowHook.getShadowColor(component) : null,
                getDecoration(decorations.get(TextDecoration.BOLD)),
                getDecoration(decorations.get(TextDecoration.ITALIC)),
                getDecoration(decorations.get(TextDecoration.UNDERLINED)),
                getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)),
                getDecoration(decorations.get(TextDecoration.OBFUSCATED)),
                font == null ? null : font.asString()
        ));

        // Extra
        for (Component extra : component.children()) {
            tabComponent.addExtra(convert(extra));
        }

        // Save original component to prevent potential data loss and avoid redundant conversion
        tabComponent.setAdventureComponent(component);

        return tabComponent;
    }

    @Nullable
    private static Boolean getDecoration(@Nullable TextDecoration.State state) {
        if (state == null || state == TextDecoration.State.NOT_SET) return null;
        return state == TextDecoration.State.TRUE;
    }
}
