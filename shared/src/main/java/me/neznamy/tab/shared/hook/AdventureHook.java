package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for Adventure component conversion.
 */
public class AdventureHook {

    /** Component cache for adventure components */
    private static final ComponentCache<IChatBaseComponent, Component> cache =
            new ComponentCache<>(1000, AdventureHook::toAdventureComponent0);

    /**
     * Converts component to adventure component
     *
     * @param   component
     *          Component to convert
     * @param   clientVersion
     *          Version to create component for
     * @return  Adventure component from this component.
     */
    @NotNull
    public static Component toAdventureComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion clientVersion) {
        return cache.get(component, clientVersion);
    }

    /**
     * Converts component to adventure component
     *
     * @param   component
     *          Component to convert
     * @param   clientVersion
     *          Version to create component for
     * @return  Adventure component from this component.
     */
    @NotNull
    private static Component toAdventureComponent0(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion clientVersion) {
        ChatModifier modifier = component.getModifier();
        TextColor color = null;
        if (modifier.getColor() != null) {
            if (clientVersion.supportsRGB()) {
                color = TextColor.color(modifier.getColor().getRgb());
            } else {
                color = TextColor.color(modifier.getColor().getLegacyColor().getHexCode());
            }
        }
        Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
        if (modifier.isBold()) decorations.add(TextDecoration.BOLD);
        if (modifier.isItalic()) decorations.add(TextDecoration.ITALIC);
        if (modifier.isObfuscated()) decorations.add(TextDecoration.OBFUSCATED);
        if (modifier.isStrikethrough()) decorations.add(TextDecoration.STRIKETHROUGH);
        if (modifier.isUnderlined()) decorations.add(TextDecoration.UNDERLINED);

        Component adventureComponent = Component.text(component.getText(), color, decorations);

        if (modifier.getClickEvent() != null) {
            adventureComponent = adventureComponent.clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.valueOf(modifier.getClickEvent().getAction().name()),
                    modifier.getClickEvent().getValue()
            ));
        }

        if (modifier.getFont() != null) {
            adventureComponent = adventureComponent.font(Key.key(modifier.getFont()));
        }
        if (!component.getExtra().isEmpty()) {
            adventureComponent = adventureComponent.children(component.getExtra().stream().map(
                    c -> toAdventureComponent0(c, clientVersion)).collect(Collectors.toList()));
        }
        return adventureComponent;
    }
}
