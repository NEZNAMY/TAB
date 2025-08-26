package me.neznamy.tab.platforms.bukkit.v1_18_R1;

import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.object.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import net.minecraft.network.chat.*;
import net.minecraft.resources.MinecraftKey;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct NMS code.
 */
public class NMSComponentConverter extends ComponentConverter {

    @Override
    @NotNull
    public Object newTextComponent(@NotNull String text) {
        return new ChatComponentText(text);
    }

    @Override
    @NotNull
    public Object newTranslatableComponent(@NotNull String key) {
        return new ChatMessage(key);
    }

    @Override
    @NotNull
    public Object newKeybindComponent(@NotNull String keybind) {
        return new ChatComponentKeybind(keybind);
    }

    @Override
    @NotNull
    public Object newObjectAtlasSpriteComponent(@NotNull String atlas, @NotNull String sprite) {
        return new ChatComponentText(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatMutableComponent)nmsComponent).c(
                net.minecraft.network.chat.ChatModifier.a
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
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((IChatMutableComponent)parent).a((IChatBaseComponent) child);
    }
}
