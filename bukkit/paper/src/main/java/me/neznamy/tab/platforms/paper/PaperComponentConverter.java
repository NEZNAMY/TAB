package me.neznamy.tab.platforms.paper;

import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Component converter for Paper 1.20.5+ using direct mojang-mapped code.
 */
public class PaperComponentConverter extends ComponentConverter {

    @Override
    @NotNull
    public Component convert(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent) return Component.literal(((SimpleComponent) component).getText());

        StructuredComponent component1 = (StructuredComponent) component;
        MutableComponent nmsComponent = Component.literal(component1.getText());
        nmsComponent.setStyle(createModifierModern(component1.getModifier(), modern));
        for (StructuredComponent extra : component1.getExtra()) {
            nmsComponent.append(convert(extra, modern));
        }
        return nmsComponent;
    }

    @NotNull
    private Style createModifierModern(@NotNull ChatModifier modifier, boolean modern) {
        TextColor color = null;
        if (modifier.getColor() != null) {
            if (modern) {
                color = TextColor.fromRgb(modifier.getColor().getRgb());
            } else {
                color = TextColor.fromRgb(modifier.getColor().getLegacyColor().getRgb());
            }
        }
        List<ChatFormatting> formats = new ArrayList<>();
        if (modifier.isBold()) formats.add(ChatFormatting.BOLD);
        if (modifier.isItalic()) formats.add(ChatFormatting.ITALIC);
        if (modifier.isUnderlined()) formats.add(ChatFormatting.UNDERLINE);
        if (modifier.isStrikethrough()) formats.add(ChatFormatting.STRIKETHROUGH);
        if (modifier.isObfuscated()) formats.add(ChatFormatting.OBFUSCATED);

        Style style = Style.EMPTY;
        if (color != null) style = style.withColor(color);
        if (!formats.isEmpty()) style = style.applyFormats(formats.toArray(new ChatFormatting[0]));
        if (modifier.getFont() != null) style = style.withFont(ResourceLocation.tryParse(modifier.getFont()));
        return style;
    }
}
