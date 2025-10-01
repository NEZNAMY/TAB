package me.neznamy.tab.platforms.paper_1_21_9;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

/**
 * Component converter using direct mojang-mapped code.
 */
public class PaperComponentConverter extends ComponentConverter<Component> {

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return Component.literal(text);
    }

    @Override
    @NotNull
    public Component newTranslatableComponent(@NotNull String key) {
        return Component.translatable(key);
    }

    @Override
    @NotNull
    public Component newKeybindComponent(@NotNull String keybind) {
        return Component.keybind(keybind);
    }

    @Override
    @NotNull
    public Component newObjectComponent(@NotNull TabAtlasSprite sprite) {
        return Component.object(new AtlasSprite(ResourceLocation.parse(sprite.getAtlas()), ResourceLocation.parse(sprite.getSprite())));
    }

    @Override
    @NotNull
    public Component newObjectComponent(@NotNull TabPlayerSprite sprite) {
        ResolvableProfile profile;
        if (sprite.getId() != null) {
            profile = ResolvableProfile.createUnresolved(sprite.getId());
        } else if (sprite.getName() != null) {
            profile = ResolvableProfile.createUnresolved(sprite.getName());
        } else if (sprite.getSkin() != null) {
            ImmutableMultimap.Builder<String, Property> builder = ImmutableMultimap.builder();
            builder.put(TabList.TEXTURES_PROPERTY, new Property(TabList.TEXTURES_PROPERTY, sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
            profile = ResolvableProfile.createResolved(new GameProfile(NIL_UUID, "", new PropertyMap(builder.build())));
        } else {
            throw new IllegalStateException("Player head component does not have id, name or skin set");
        }
        return Component.object(new PlayerSprite(profile, sprite.isShowHat()));
    }

    @Override
    public void applyStyle(@NotNull Component nmsComponent, @NotNull TabStyle modifier) {
        Style style = Style.EMPTY
                .withColor(modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()))
                .withBold(modifier.getBold())
                .withItalic(modifier.getItalic())
                .withUnderlined(modifier.getUnderlined())
                .withStrikethrough(modifier.getStrikethrough())
                .withObfuscated(modifier.getObfuscated())
                .withFont(modifier.getFont() == null ? null : new FontDescription.Resource(ResourceLocation.parse(modifier.getFont())));
        if (modifier.getShadowColor() != null) style = style.withShadowColor(modifier.getShadowColor()); // withShadowColor takes int instead of Integer, bug?
        ((MutableComponent)nmsComponent).setStyle(style);
    }

    @Override
    public void addSibling(@NotNull Component parent, @NotNull Component child) {
        ((MutableComponent)parent).append(child);
    }
}
