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

    /** Pattern for validating Minecraft texture URLs */
    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("^https?://textures\\.minecraft\\.net/texture/[0-9a-f]+$");

    /**
     * Creates a tag resolver for the "head_texture" tag, which allows inserting player heads with custom textures using a texture URL.
     * The tag expects a single argument: the URL of the Minecraft texture. It validates the URL format and constructs the appropriate player head object if valid.
     * If the URL is invalid, it throws an exception with a descriptive error message.
     * @return A TagResolver for the "head_texture" tag.
     */
    public static TagResolver headTextureTag() {
        return TagResolver.resolver("head_texture", (args, context) -> {
            String textureUrl = args.popOr("Expected texture url").lowerValue().trim();
            if (!TEXTURE_URL_PATTERN.matcher(textureUrl).matches()) {
                throw context.newException("Expected a valid minecraft texture URL", args);
            }

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
