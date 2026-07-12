package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;


/**
 * Class loader hack to avoid class initializer error when using static methods in interfaces.
 * No, try/catch does not solve this.
 */
public class MiniMessageObjectHook {

    /**
     * Creates a tag resolver for the "head_texture" tag, which allows inserting player heads with custom textures using a texture hash.
     * The tag expects a single argument: the Minecraft texture hash (the part after "https://textures.minecraft.net/texture/").
     *
     * @return A TagResolver for the "head_texture" tag.
     */
    public static TagResolver headTextureTag() {
        return TagResolver.resolver("head_texture", (args, context) -> {
            String hash = args.popOr("Expected texture hash").lowerValue().trim();
            PlayerHeadObjectContents contents = ObjectContents.playerHead()
                    .profileProperty(
                            PlayerHeadObjectContents.property("textures", TabList.Skin.fromTextureHash(hash).getValue())
                    )
                    .build();

            return Tag.selfClosingInserting(Component.object(contents));
        });
    }

    /**
     * Creates a tag resolver for the "mineskin" tag, which uses mineskin UUID.
     *
     * @return A TagResolver for the "mineskin" tag.
     */
    public static TagResolver mineskinTag() {
        return TagResolver.resolver("mineskin", (args, context) -> {
            String uuid = args.popOr("Expected skin uuid").lowerValue().trim();
            TabList.Skin skin = TAB.getInstance().getConfiguration().getSkinManager().getSkin("mineskin:" + uuid);
            if (skin == null) {
                return Tag.selfClosingInserting(Component.text("<Invalid mineskin UUID>"));
            }
            PlayerHeadObjectContents contents = ObjectContents.playerHead()
                    .profileProperty(
                            PlayerHeadObjectContents.property("textures", skin.getValue(), skin.getSignature())
                    )
                    .build();

            return Tag.selfClosingInserting(Component.object(contents));
        });
    }
}
