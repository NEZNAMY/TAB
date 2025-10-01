package me.neznamy.tab.platforms.bukkit.v1_21_R6;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct NMS code.
 */
public class NMSComponentConverter extends ComponentConverter<IChatBaseComponent> {

    @Override
    @NotNull
    public IChatBaseComponent newTextComponent(@NotNull String text) {
        return IChatBaseComponent.b(text);
    }

    @Override
    @NotNull
    public IChatBaseComponent newTranslatableComponent(@NotNull String key) {
        return IChatBaseComponent.c(key);
    }

    @Override
    @NotNull
    public IChatBaseComponent newKeybindComponent(@NotNull String keybind) {
        return IChatBaseComponent.d(keybind);
    }

    @Override
    @NotNull
    public IChatBaseComponent newObjectComponent(@NotNull TabAtlasSprite sprite) {
        return IChatBaseComponent.a(new AtlasSprite(MinecraftKey.a(sprite.getAtlas()), MinecraftKey.a(sprite.getSprite())));
    }

    @Override
    @NotNull
    public IChatBaseComponent newObjectComponent(@NotNull TabPlayerSprite sprite) {
        ResolvableProfile profile;
        if (sprite.getId() != null) {
            profile = ResolvableProfile.a(sprite.getId());
        } else if (sprite.getName() != null) {
            profile = ResolvableProfile.a(sprite.getName());
        } else if (sprite.getSkin() != null) {
            ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
            builder.put(TabList.TEXTURES_PROPERTY, new Property(TabList.TEXTURES_PROPERTY, sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
            profile = ResolvableProfile.a(new GameProfile(NIL_UUID, "", new PropertyMap(builder.build())));
        } else {
            throw new IllegalStateException("Player head component does not have id, name or skin set");
        }
        return IChatBaseComponent.a(new PlayerSprite(profile, sprite.isShowHat()));
    }

    @Override
    public void applyStyle(@NotNull IChatBaseComponent nmsComponent, @NotNull TabStyle modifier) {
        ChatModifier style = ChatModifier.a
                .a(modifier.getColor() == null ? null : ChatHexColor.a(modifier.getColor().getRgb()))
                .a(modifier.getBold())
                .b(modifier.getItalic())
                .c(modifier.getUnderlined())
                .d(modifier.getStrikethrough())
                .e(modifier.getObfuscated())
                .a(modifier.getFont() == null ? null : new FontDescription.c(MinecraftKey.a(modifier.getFont())));
        if (modifier.getShadowColor() != null) style = style.b(modifier.getShadowColor());
        ((IChatMutableComponent)nmsComponent).c(style);
    }

    @Override
    public void addSibling(@NotNull IChatBaseComponent parent, @NotNull IChatBaseComponent child) {
        ((IChatMutableComponent)parent).b(child);
    }
}
