package me.neznamy.tab.platforms.bukkit.v1_9_R2;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.object.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import net.minecraft.server.v1_9_R2.*;
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
        return new ChatComponentText("<Keybind components were added in 1.12>");
    }

    @Override
    @NotNull
    public Object newObjectAtlasSpriteComponent(@NotNull String atlas, @NotNull String sprite) {
        return new ChatComponentText(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatBaseComponent)nmsComponent).setChatModifier(
                new net.minecraft.server.v1_9_R2.ChatModifier()
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
