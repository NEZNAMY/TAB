package me.neznamy.tab.shared.hook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Class for hooking into MiniMessage to support object components in a way that is compatible with older versions of the library.
 */
public class MiniMessageObjectHook {

    /** Constant prefix shared by every Minecraft texture URL */
    private static final String TEXTURE_URL_PREFIX = "https://textures.minecraft.net/texture/";

    /**
     * Creates a tag resolver for the "head_texture" tag, which allows inserting player heads with custom textures using a texture hash.
     * The tag expects a single argument: the Minecraft texture hash (the part after "https://textures.minecraft.net/texture/").
     *
     * @return A TagResolver for the "head_texture" tag.
     */
    public static TagResolver headTextureTag() {
        return TagResolver.resolver("head_texture", (args, context) -> {
            String hash = args.popOr("Expected texture hash").lowerValue().trim();
            String textureUrl = TEXTURE_URL_PREFIX + hash;

            String json = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", textureUrl);
            String texture = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            PlayerHeadObjectContents contents = ObjectContents.playerHead()
                    .profileProperty(
                            PlayerHeadObjectContents.property("textures", texture)
                    )
                    .build();

            return Tag.selfClosingInserting(Component.object(contents));
        });
    }

}
