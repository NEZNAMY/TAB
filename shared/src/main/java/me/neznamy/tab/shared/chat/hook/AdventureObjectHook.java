package me.neznamy.tab.shared.chat.hook;

import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.object.ObjectInfo;
import me.neznamy.tab.shared.chat.component.object.TabAtlasSprite;
import me.neznamy.tab.shared.chat.component.object.TabObjectComponent;
import me.neznamy.tab.shared.chat.component.object.TabPlayerSprite;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A separate class for handling object components in adventure components.
 * It is required to be separate. Using static functions on interfaces causes
 * it to load on class initialization even if the code never runs, resulting in an error
 * when using an older version of the library before shadow color class was added.
 */
public class AdventureObjectHook {

    /**
     * Converts TAB object component to adventure component
     *
     * @param   component
     *          Component to convert
     * @return  Adventure component from TAB component.
     * @throws  UnsupportedOperationException
     *          If the object type is not supported
     */
    @NotNull
    public static Component convert(@NotNull TabObjectComponent component) {
        ObjectInfo info = component.getContents();
        if (info instanceof TabAtlasSprite) {
            return Component.object(ObjectContents.sprite(
                    Key.key(((TabAtlasSprite) info).getAtlas()),
                    Key.key(((TabAtlasSprite) info).getSprite())
            ));
        }
        if (info instanceof TabPlayerSprite) {
            TabPlayerSprite sprite = (TabPlayerSprite) info;
            List<PlayerHeadObjectContents.ProfileProperty> properties;
            if (sprite.getSkin() == null) {
                properties = Collections.emptyList();
            } else {
                properties = Collections.singletonList(PlayerHeadObjectContents.property(TabList.TEXTURES_PROPERTY, sprite.getSkin().getValue(), sprite.getSkin().getSignature()));
            }
            return Component.object(ObjectContents.playerHead()
                    .id(sprite.getId())
                    .name(sprite.getName())
                    .profileProperties(properties)
                    .hat(sprite.isShowHat())
                    .build()
            );
        }
        throw new UnsupportedOperationException("ObjectComponent with " + info.getClass().getName() + " is not supported");
    }

    /**
     * Converts adventure object component to TAB component
     *
     * @param   component
     *          Component to convert
     * @return  TAB component from adventure component.
     * @throws  UnsupportedOperationException
     *          If the object type is not supported
     */
    @NotNull
    public static TabComponent convert(@NotNull ObjectComponent component) {
        ObjectContents contents = component.contents();
        if (contents instanceof SpriteObjectContents) {
            return TabComponent.atlasSprite(((SpriteObjectContents) contents).atlas().asString(), ((SpriteObjectContents) contents).sprite().asString());
        } else if (contents instanceof PlayerHeadObjectContents) {
            PlayerHeadObjectContents head = (PlayerHeadObjectContents) contents;
            PlayerHeadObjectContents.ProfileProperty skin = head.profileProperties().stream().filter(p -> p.name().equals(TabList.TEXTURES_PROPERTY)).findFirst().orElse(null);
            return TabComponent.head(new TabPlayerSprite(
                    head.id(),
                    head.name(),
                    skin == null ? null : new TabList.Skin(skin.value(), skin.signature()),
                    head.hat()
            ));
        } else {
            throw new UnsupportedOperationException("ObjectComponent with " + contents.getClass().getName() + " is not supported");
        }
    }
}