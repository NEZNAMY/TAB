package me.neznamy.tab.platforms.bukkit.v1_16_R3;

import me.neznamy.chat.ChatModifier;
import me.neznamy.chat.component.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import net.minecraft.server.v1_16_R3.*;
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
    public Object newObjectComponent(@NotNull String atlas, @NotNull String sprite) {
        return new ChatComponentText(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatMutableComponent)nmsComponent).setChatModifier(
                net.minecraft.server.v1_16_R3.ChatModifier.a
                        .setColor(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
                        .setBold(modifier.getBold())
                        .setItalic(modifier.getItalic())
                        .setUnderline(modifier.getUnderlined())
                        .setStrikethrough(modifier.getStrikethrough())
                        .setRandom(modifier.getObfuscated())
        );
    }

    @Override
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((IChatMutableComponent)parent).addSibling((IChatBaseComponent) child);
    }
}
