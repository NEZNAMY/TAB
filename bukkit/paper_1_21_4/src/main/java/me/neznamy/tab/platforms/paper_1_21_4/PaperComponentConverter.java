package me.neznamy.tab.platforms.paper_1_21_4;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.object.AtlasSprite;
import me.neznamy.tab.shared.chat.component.object.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.PlayerSprite;
import net.minecraft.network.chat.*;
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
    public Object newObjectComponent(@NotNull AtlasSprite sprite) {
        return Component.literal(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    @NotNull
    public Object newObjectComponent(@NotNull PlayerSprite sprite) {
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
        if (modifier.getShadowColor() != null) style = style.withShadowColor(modifier.getShadowColor()); // withShadowColor takes int instead of Integer, bug?
        ((MutableComponent)nmsComponent).setStyle(style);
    }

    @Override
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((MutableComponent)parent).append((Component) child);
    }
}
