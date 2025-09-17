package me.neznamy.tab.platforms.bukkit.v1_18_R2;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.network.chat.*;
import net.minecraft.resources.MinecraftKey;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct NMS code.
 */
public class NMSComponentConverter extends ComponentConverter<IChatBaseComponent> {

    @Override
    @NotNull
    public IChatBaseComponent newTextComponent(@NotNull String text) {
        return new ChatComponentText(text);
    }

    @Override
    @NotNull
    public IChatBaseComponent newTranslatableComponent(@NotNull String key) {
        return new ChatMessage(key);
    }

    @Override
    @NotNull
    public IChatBaseComponent newKeybindComponent(@NotNull String keybind) {
        return new ChatComponentKeybind(keybind);
    }

    @Override
    @NotNull
    public IChatBaseComponent newObjectComponent(@NotNull TabAtlasSprite sprite) {
        return new ChatComponentText(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    @NotNull
    public IChatBaseComponent newObjectComponent(@NotNull TabPlayerSprite sprite) {
        return new ChatComponentText(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull IChatBaseComponent nmsComponent, @NotNull TabStyle modifier) {
        ((IChatMutableComponent)nmsComponent).c(ChatModifier.a
                .a(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
                .a(modifier.getBold())
                .b(modifier.getItalic())
                .c(modifier.getUnderlined())
                .d(modifier.getStrikethrough())
                .e(modifier.getObfuscated())
                .a(modifier.getFont() == null ? null : MinecraftKey.a(modifier.getFont()))
        );
    }

    @Override
    public void addSibling(@NotNull IChatBaseComponent parent, @NotNull IChatBaseComponent child) {
        ((IChatMutableComponent)parent).a(child);
    }
}
