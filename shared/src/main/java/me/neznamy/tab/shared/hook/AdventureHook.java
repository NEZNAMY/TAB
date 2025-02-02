package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.KeybindComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TextComponent;
import me.neznamy.tab.shared.chat.component.TranslatableComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
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
    private static final boolean SHADOW_COLOR_AVAILABLE = ReflectionUtils.methodExists(Component.class, "shadowColor");

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
        if (SHADOW_COLOR_AVAILABLE && component.getModifier().getShadowColor() != null) {
            style.shadowColor(ShadowColor.shadowColor(component.getModifier().getShadowColor()));
        }

        // Extra
        List<Component> list = new ArrayList<>();
        for (TabComponent extra : component.getExtra()) {
            list.add(convert(extra));
        }
        
        // Component type & return
        if (component instanceof TextComponent) {
            return Component.text(((TextComponent) component).getText(), style.build()).children(list);
        }
        if (component instanceof TranslatableComponent) {
            return Component.translatable(((TranslatableComponent) component).getKey(), style.build()).children(list);
        }
        if (component instanceof KeybindComponent) {
            return Component.keybind(((KeybindComponent) component).getKeybind(), style.build()).children(list);
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
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            tabComponent = new TextComponent(((net.kyori.adventure.text.TextComponent) component).content());
        } else if (component instanceof net.kyori.adventure.text.TranslatableComponent) {
            tabComponent = new TranslatableComponent(((net.kyori.adventure.text.TranslatableComponent) component).key());
        } else if (component instanceof net.kyori.adventure.text.KeybindComponent) {
            tabComponent = new KeybindComponent(((net.kyori.adventure.text.KeybindComponent) component).keybind());
        } else {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
        }

        // Component style
        Map<TextDecoration, TextDecoration.State> decorations = component.style().decorations();
        tabComponent.setModifier(new ChatModifier(
                component.color() == null ? null : new me.neznamy.tab.shared.chat.TextColor(component.color().value()),
                !SHADOW_COLOR_AVAILABLE || component.shadowColor() == null ? null : component.shadowColor().value(),
                getDecoration(decorations.get(TextDecoration.BOLD)),
                getDecoration(decorations.get(TextDecoration.ITALIC)),
                getDecoration(decorations.get(TextDecoration.UNDERLINED)),
                getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)),
                getDecoration(decorations.get(TextDecoration.OBFUSCATED)),
                component.font() == null ? null : component.font().asString()
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
