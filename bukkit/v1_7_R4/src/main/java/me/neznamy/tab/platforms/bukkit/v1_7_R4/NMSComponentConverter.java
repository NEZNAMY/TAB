package me.neznamy.tab.platforms.bukkit.v1_7_R4;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.component.object.AtlasSprite;
import me.neznamy.tab.shared.chat.component.object.ObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.PlayerSprite;
import net.minecraft.server.v1_7_R4.*;
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
    public Object newObjectComponent(@NotNull AtlasSprite sprite) {
        return new ChatComponentText(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    @NotNull
    public Object newObjectComponent(@NotNull PlayerSprite sprite) {
        return new ChatComponentText(ObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier modifier) {
        ((IChatBaseComponent)nmsComponent).setChatModifier(
                new net.minecraft.server.v1_7_R4.ChatModifier()
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
