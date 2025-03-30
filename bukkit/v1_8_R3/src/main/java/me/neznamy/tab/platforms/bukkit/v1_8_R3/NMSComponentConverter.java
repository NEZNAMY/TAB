package me.neznamy.tab.platforms.bukkit.v1_8_R3;

import me.neznamy.chat.ChatModifier;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
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
        throw new UnsupportedOperationException("Keybind components were added in 1.12");
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatBaseComponent)nmsComponent).setChatModifier(
                new net.minecraft.server.v1_8_R3.ChatModifier()
                        .setColor(modifier.getColor() == null ? null : EnumChatFormat.valueOf(modifier.getColor().getLegacyColor().name()))
                        .setBold(modifier.getBold())
                        .setItalic(modifier.getItalic())
                        .setUnderline(modifier.getUnderlined())
                        .setStrikethrough(modifier.getStrikethrough())
                        .setRandom(modifier.getObfuscated())
        );
    }

    @Override
    public void addSibling(@NotNull Object parent, @NotNull Object child) {
        ((IChatBaseComponent)parent).addSibling((IChatBaseComponent) child);
    }
}
