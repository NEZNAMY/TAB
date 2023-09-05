package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureHook {

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
        ChatModifier modifier = component.getModifier();
        net.kyori.adventure.text.format.TextColor color = null;
        if (modifier.getColor() != null) {
            if (clientVersion.getMinorVersion() >= 16) {
                color = net.kyori.adventure.text.format.TextColor.color(modifier.getColor().getRgb());
            } else {
                color = net.kyori.adventure.text.format.TextColor.color(modifier.getColor().getLegacyColor().getHexCode());
            }
        }
        Set<TextDecoration> decorations = new HashSet<>();
        if (modifier.isBold()) decorations.add(TextDecoration.BOLD);
        if (modifier.isItalic()) decorations.add(TextDecoration.ITALIC);
        if (modifier.isObfuscated()) decorations.add(TextDecoration.OBFUSCATED);
        if (modifier.isStrikethrough()) decorations.add(TextDecoration.STRIKETHROUGH);
        if (modifier.isUnderlined()) decorations.add(TextDecoration.UNDERLINED);
        Component advComponent = Component.text(component.getText(), color, decorations);
        if (modifier.getFont() != null) advComponent = advComponent.font(Key.key(modifier.getFont()));
        return advComponent.children(component.getExtra().stream().map(c -> toAdventureComponent(c, clientVersion)).collect(Collectors.toList()));
    }
}
