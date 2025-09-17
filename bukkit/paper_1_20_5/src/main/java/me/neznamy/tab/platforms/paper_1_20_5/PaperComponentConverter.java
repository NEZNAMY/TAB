package me.neznamy.tab.platforms.paper_1_20_5;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct mojang-mapped code.
 */
public class PaperComponentConverter extends ComponentConverter<Component> {

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return Component.literal(text);
    }

    @Override
    @NotNull
    public Component newTranslatableComponent(@NotNull String key) {
        return Component.translatable(key);
    }

    @Override
    @NotNull
    public Component newKeybindComponent(@NotNull String keybind) {
        return Component.keybind(keybind);
    }

    @Override
    @NotNull
    public Component newObjectComponent(@NotNull TabAtlasSprite sprite) {
        return Component.literal(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    @NotNull
    public Component newObjectComponent(@NotNull TabPlayerSprite sprite) {
        return Component.literal(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Component nmsComponent, @NotNull TabStyle modifier) {
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
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        ((MutableComponent)parent).append(child);
    }
}
