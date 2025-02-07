package me.neznamy.tab.platforms.paper;

import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct mojang-mapped code for versions 1.21.4+.
 */
@SuppressWarnings("unused") // Used via reflection
public class PaperComponentConverter extends ComponentConverter {

    @Override
    @NotNull
    public Component convert(@NotNull TabComponent component) {
        // Component type
        MutableComponent nmsComponent = switch (component) {
            case TextComponent text -> Component.literal(text.getText());
            case TranslatableComponent translatable -> Component.translatable(translatable.getKey());
            case KeybindComponent keybind -> Component.keybind(keybind.getKeybind());
            default -> throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        };

        // Component style
        ChatModifier modifier = component.getModifier();
        Style style = Style.EMPTY
                .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
                .withBold(modifier.getBold())
                .withItalic(modifier.getItalic())
                .withUnderlined(modifier.getUnderlined())
                .withStrikethrough(modifier.getStrikethrough())
                .withObfuscated(modifier.getObfuscated())
                .withFont(modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont()));
        if (modifier.getShadowColor() != null) style = style.withShadowColor(modifier.getShadowColor()); // withShadowColor takes int instead of Integer, bug?
        nmsComponent.setStyle(style);

        // Extra
        for (TabComponent extra : component.getExtra()) {
            nmsComponent.append(convert(extra));
        }
        return nmsComponent;
    }
}
