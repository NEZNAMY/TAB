package me.neznamy.tab.platforms.paper_1_21_2;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.object.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct mojang-mapped code.
 */
public class PaperComponentConverter extends ComponentConverter {

    @Override
    @NotNull
    public Object newTextComponent(@NotNull String text) {
        return Component.literal(text);
    }

    @Override
    @NotNull
    public Object newTranslatableComponent(@NotNull String key) {
        return Component.translatable(key);
    }

    @Override
    @NotNull
    public Object newKeybindComponent(@NotNull String keybind) {
        return Component.keybind(keybind);
    }

    @Override
    @NotNull
    public Object newObjectAtlasSpriteComponent(@NotNull String atlas, @NotNull String sprite) {
        return Component.literal(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        Style style = Style.EMPTY
                .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
                .withBold(modifier.getBold())
                .withItalic(modifier.getItalic())
                .withUnderlined(modifier.getUnderlined())
                .withStrikethrough(modifier.getStrikethrough())
                .withObfuscated(modifier.getObfuscated())
                .withFont(modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont()));
        ((MutableComponent)nmsComponent).setStyle(style);
    }

    @Override
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((MutableComponent)parent).append((Component) child);
    }
}
