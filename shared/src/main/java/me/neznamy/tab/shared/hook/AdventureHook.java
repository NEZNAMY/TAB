package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.component.TabComponent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
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

    /**
     * Converts TAB component to adventure component
     *
     * @param   component
     *          Component to convert
     * @return  Adventure component from TAB component.
     */
    @NotNull
    public static Component convert(@NotNull TabComponent component) {
        // Component type
        Component adventureComponent;
        if (component instanceof me.neznamy.tab.shared.chat.component.TextComponent) {
            adventureComponent = Component.text(((me.neznamy.tab.shared.chat.component.TextComponent) component).getText());
        } else if (component instanceof me.neznamy.tab.shared.chat.component.TranslatableComponent) {
            adventureComponent = Component.translatable(((me.neznamy.tab.shared.chat.component.TranslatableComponent) component).getKey());
        } else if (component instanceof me.neznamy.tab.shared.chat.component.KeybindComponent) {
            adventureComponent = Component.keybind(((me.neznamy.tab.shared.chat.component.KeybindComponent) component).getKeybind());
        } else {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
        }

        // Component style
        adventureComponent = adventureComponent.style(Style.empty().toBuilder()
                .color(component.getModifier().getColor() == null ? null : TextColor.color(component.getModifier().getColor().getRgb()))
                .decoration(TextDecoration.BOLD, getDecoration(component.getModifier().getBold()))
                .decoration(TextDecoration.ITALIC, getDecoration(component.getModifier().getItalic()))
                .decoration(TextDecoration.UNDERLINED, getDecoration(component.getModifier().getUnderlined()))
                .decoration(TextDecoration.STRIKETHROUGH, getDecoration(component.getModifier().getStrikethrough()))
                .decoration(TextDecoration.OBFUSCATED, getDecoration(component.getModifier().getObfuscated()))
                .font(component.getModifier().getFont() == null ? null : Key.key(component.getModifier().getFont())).build());

        // Extra
        List<Component> list = new ArrayList<>();
        for (TabComponent extra : component.getExtra()) {
            list.add(convert(extra));
        }
        adventureComponent = adventureComponent.children(list);

        return adventureComponent;
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
            tabComponent = new me.neznamy.tab.shared.chat.component.TextComponent(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            tabComponent = new me.neznamy.tab.shared.chat.component.TranslatableComponent(((TranslatableComponent) component).key());
        } else if (component instanceof KeybindComponent) {
            tabComponent = new me.neznamy.tab.shared.chat.component.KeybindComponent(((KeybindComponent) component).keybind());
        } else {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
        }

        // Component style
        Map<TextDecoration, TextDecoration.State> decorations = component.style().decorations();
        TextColor color = component.color();
        tabComponent.getModifier().setColor(color == null ? null : new me.neznamy.tab.shared.chat.TextColor(color.red(), color.green(), color.blue()));
        tabComponent.getModifier().setBold(getDecoration(decorations.get(TextDecoration.BOLD)));
        tabComponent.getModifier().setItalic(getDecoration(decorations.get(TextDecoration.ITALIC)));
        tabComponent.getModifier().setUnderlined(getDecoration(decorations.get(TextDecoration.UNDERLINED)));
        tabComponent.getModifier().setStrikethrough(getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)));
        tabComponent.getModifier().setObfuscated(getDecoration(decorations.get(TextDecoration.OBFUSCATED)));
        Key font = component.style().font();
        tabComponent.getModifier().setFont(font == null ? null : font.asString());

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
