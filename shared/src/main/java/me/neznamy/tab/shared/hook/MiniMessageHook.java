package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.hook.AdventureHook;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Class for hooking into MiniMessage to support its syntax.
 */
public class MiniMessageHook {

    /** Minimessage deserializer with disabled component post-processing */
    @Nullable
    private static final MiniMessage mm = createMiniMessage();

    /** Pattern for validating minecraft texture URLs */
    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("^https?://textures\\.minecraft\\.net/texture/[0-9a-f]+$");

    @Nullable
    private static MiniMessage createMiniMessage() {
        try {
            return MiniMessage.builder()
                    .editTags(builder -> builder.resolver(headTextureTag()))
                    .build();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static TagResolver headTextureTag() {
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

    /**
     * Returns {@code true} if MiniMessage is available on the server, {@code false} if not.
     *
     * @return  {@code true} if MiniMessage is available on the server, {@code false} if not
     */
    public static boolean isAvailable() {
        return mm != null && TAB.getInstance().getConfiguration().getConfig().getComponents().isMinimessageSupport();
    }

    /**
     * Attempts to parse the text into an adventure component using MiniMessage syntax. If MiniMessage is
     * not available or the text failed to parse for any reason, {@code null} is returned.
     *
     * @param   text
     *          Text to attempt to parse
     * @return  Parsed component or {@code null} if unable to parse
     */
    @Nullable
    public static TabComponent parseText(@NotNull String text) {
        if (mm == null) return null;
        try {
            return AdventureHook.convert(mm.deserialize(text));
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to convert \"" + text + "\" into a MiniMessage component", t);
            return null;
        }
    }
}
