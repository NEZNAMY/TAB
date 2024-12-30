package me.neznamy.tab.platforms.paper;

import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.shared.chat.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component converter using direct mojang-mapped code for versions 1.20.5+.
 */
@SuppressWarnings("unused") // Used via reflection
public class PaperComponentConverter extends ComponentConverter {

    @Override
    @NotNull
    public Component convert(@NotNull TabComponent component, boolean modern) {
        switch (component) {
            case SimpleComponent simpleComponent -> {
                return Component.literal(simpleComponent.getText());
            }
            case StructuredComponent component1 -> {
                MutableComponent nmsComponent = Component.literal(component1.getText());
                ChatModifier modifier = component1.getModifier();
                TextColor color = null;
                if (modifier.getColor() != null) {
                    if (modern) {
                        color = TextColor.fromRgb(modifier.getColor().getRgb());
                    } else {
                        color = TextColor.fromRgb(modifier.getColor().getLegacyColor().getRgb());
                    }
                }
                nmsComponent.setStyle(newStyle(color, modifier.isBold(), modifier.isItalic(), modifier.isUnderlined(),
                        modifier.isStrikethrough(), modifier.isObfuscated(), modifier.getFont()));
                for (StructuredComponent extra : component1.getExtra()) {
                    nmsComponent.append(convert(extra, modern));
                }
                return nmsComponent;
            }
            case AdventureComponent component1 -> {
                return fromAdventure(component1.getComponent());
            }
            default -> throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }
    }

    @NotNull
    private Component fromAdventure(@NotNull net.kyori.adventure.text.Component component) {
        MutableComponent nmsComponent = switch (component) {
            case TextComponent text -> Component.literal(text.content());
            case TranslatableComponent translate -> Component.translatable(translate.key());
            case KeybindComponent keyBind -> Component.keybind(keyBind.keybind());
            default -> throw new IllegalStateException("Cannot convert " + component.getClass().getName());
        };

        net.kyori.adventure.text.format.TextColor color = component.color();
        Key font = component.style().font();
        nmsComponent.setStyle(newStyle(
                color == null ? null : TextColor.fromRgb(color.value()),
                component.style().hasDecoration(TextDecoration.BOLD),
                component.style().hasDecoration(TextDecoration.ITALIC),
                component.style().hasDecoration(TextDecoration.UNDERLINED),
                component.style().hasDecoration(TextDecoration.STRIKETHROUGH),
                component.style().hasDecoration(TextDecoration.OBFUSCATED),
                font == null ? null : font.asString()
        ));
        for (net.kyori.adventure.text.Component extra : component.children()) {
            nmsComponent.append(fromAdventure(extra));
        }
        return nmsComponent;
    }

    @NotNull
    private Style newStyle(@Nullable TextColor color, boolean bold, boolean italic, boolean underlined,
                           boolean strikethrough, boolean obfuscated, @Nullable String font) {
        return Style.EMPTY
                .withColor(color)
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
                .withFont(font == null ? null : ResourceLocation.tryParse(font));
    }
}
