package me.neznamy.tab.platforms.bukkit.v1_13_R1;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import net.minecraft.server.v1_13_R1.*;
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
        nmsComponent.setChatModifier(new ChatModifier()
                .setColor(modifier.getColor() == null ? null : EnumChatFormat.valueOf(modifier.getColor().getLegacyColor().name()))
                .setBold(modifier.getBold())
                .setItalic(modifier.getItalic())
                .setUnderline(modifier.getUnderlined())
                .setStrikethrough(modifier.getStrikethrough())
                .setRandom(modifier.getObfuscated())
        );
    }

    @Override
    public void addSibling(@NotNull IChatBaseComponent parent, @NotNull IChatBaseComponent child) {
        parent.addSibling(child);
    }
}
