package me.neznamy.tab.platforms.bukkit.v1_8_R3;

import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.server.v1_8_R3.*;
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
    public Object newObjectComponent(@NotNull TabAtlasSprite sprite) {
        return new ChatComponentText(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    @NotNull
    public Object newObjectComponent(@NotNull TabPlayerSprite sprite) {
        return new ChatComponentText(TabObjectComponent.ERROR_MESSAGE);
    }

    @Override
    public void applyStyle(@NotNull Object nmsComponent, @NotNull TabStyle modifier) {
        ((IChatBaseComponent)nmsComponent).setChatModifier(new ChatModifier()
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
