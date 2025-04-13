package me.neznamy.tab.platforms.bukkit.v1_17_R1;

import me.neznamy.chat.ChatModifier;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
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
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatMutableComponent)nmsComponent).setChatModifier(
                net.minecraft.network.chat.ChatModifier.a
                        .setColor(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
                        .setBold(modifier.getBold())
                        .setItalic(modifier.getItalic())
                        .setUnderline(modifier.getUnderlined())
                        .setStrikethrough(modifier.getStrikethrough())
                        .setRandom(modifier.getObfuscated())
                        .a(modifier.getFont() == null ? null : MinecraftKey.a(modifier.getFont()))
        );
    }

    @Override
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((IChatMutableComponent)parent).addSibling((IChatBaseComponent) child);
    }
}
