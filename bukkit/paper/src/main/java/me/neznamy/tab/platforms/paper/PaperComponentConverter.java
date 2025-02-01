package me.neznamy.tab.platforms.paper;

import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.KeybindComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TextComponent;
import me.neznamy.tab.shared.chat.component.TranslatableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct mojang-mapped code for versions 1.20.5+.
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
        nmsComponent.setStyle(Style.EMPTY
                .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
                .withBold(modifier.getBold())
                .withItalic(modifier.getItalic())
                .withUnderlined(modifier.getUnderlined())
                .withStrikethrough(modifier.getStrikethrough())
                .withObfuscated(modifier.getObfuscated())
                .withFont(modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont())));

        // Extra
        for (TabComponent extra : component.getExtra()) {
            nmsComponent.append(convert(extra));
        }
        return nmsComponent;
    }
}
