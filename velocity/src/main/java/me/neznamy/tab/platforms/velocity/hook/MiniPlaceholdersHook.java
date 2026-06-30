package me.neznamy.tab.platforms.velocity.hook;

import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hook for parsing placeholders using MiniPlaceholders.
 */
public class MiniPlaceholdersHook {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_HEX_SERIALIZER =
            LegacyComponentSerializer.builder().character('§').hexColors().build();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^<>]+)>");

    /** MiniMessage tags that must not be treated as MiniPlaceholders placeholders */
    private static final Set<String> MINIMESSAGE_TAGS = Set.of(
            "bold", "b", "italic", "i", "em", "underlined", "underline", "u",
            "strikethrough", "st", "obfuscated", "obf", "reset",
            "color", "c", "colour", "gradient", "rainbow", "transition",
            "hover", "click", "newline", "br", "key", "lang", "insert",
            "margin", "shift", "font", "shadow", "head_texture", "mineskin",
            "pride", "selector", "score", "sprite", "nbt"
    );

    /**
     * Checks if the given identifier should be resolved using MiniPlaceholders.
     * MiniPlaceholders placeholders use {@code <syntax>}, while {@code %placeholder%} syntax
     * is reserved for PlaceholderAPI (via TAB Bridge on Velocity).
     *
     * @param   identifier
     *          placeholder identifier
     * @return  {@code true} if the identifier is a MiniPlaceholders placeholder, {@code false} otherwise
     */
    public static boolean isMiniPlaceholdersIdentifier(@NotNull String identifier) {
        return identifier.startsWith("<") && identifier.endsWith(">");
    }

    /**
     * Detects MiniPlaceholders placeholders in text using {@code <syntax>}.
     *
     * @param   text
     *          text to detect placeholders in
     * @return  list of detected placeholder identifiers
     */
    @NotNull
    public static List<String> detectPlaceholders(@NotNull String text) {
        if (!text.contains("<")) return Collections.emptyList();
        if (text.startsWith("<") && text.endsWith(">") && text.indexOf('<', 1) == -1) {
            if (!isMiniMessageTag(text.substring(1, text.length() - 1))) {
                return Collections.singletonList(text);
            }
            return Collections.emptyList();
        }
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String placeholder = matcher.group();
            if (!isMiniMessageTag(matcher.group(1)) && !placeholders.contains(placeholder)) {
                placeholders.add(placeholder);
            }
        }
        return placeholders;
    }

    private static boolean isMiniMessageTag(@NotNull String inner) {
        if (inner.isEmpty()) return true;
        if (inner.charAt(0) == '/') return true;
        if (inner.charAt(0) == '#') return true;
        String tagName = inner.contains(":") ? inner.substring(0, inner.indexOf(':')) : inner;
        return MINIMESSAGE_TAGS.contains(tagName.toLowerCase(Locale.US));
    }

    /**
     * Parses a global placeholder.
     *
     * @param   identifier
     *          placeholder identifier
     * @return  parsed placeholder value
     */
    @NotNull
    public static String parseGlobal(@NotNull String identifier) {
        Component component = MINI_MESSAGE.deserialize(
                identifier,
                MiniPlaceholders.globalPlaceholders()
        );
        return toLegacyString(component);
    }

    /**
     * Parses a player placeholder.
     *
     * @param   identifier
     *          placeholder identifier
     * @param   player
     *          player to parse placeholder for
     * @return  parsed placeholder value
     */
    @NotNull
    public static String parsePlayer(@NotNull String identifier, @NotNull Player player) {
        Component component = MINI_MESSAGE.deserialize(
                identifier,
                player,
                MiniPlaceholders.globalPlaceholders(),
                MiniPlaceholders.audiencePlaceholders()
        );
        return toLegacyString(component);
    }

    /**
     * Parses a relational placeholder.
     *
     * @param   identifier
     *          placeholder identifier
     * @param   viewer
     *          viewer player
     * @param   target
     *          target player
     * @return  parsed placeholder value
     */
    @NotNull
    public static String parseRelational(@NotNull String identifier, @NotNull Player viewer, @NotNull Player target) {
        Component component = MINI_MESSAGE.deserialize(
                identifier,
                new RelationalAudience<>(viewer, target),
                MiniPlaceholders.globalPlaceholders(),
                MiniPlaceholders.audiencePlaceholders(),
                MiniPlaceholders.relationalPlaceholders()
        );
        return toLegacyString(component);
    }

    /**
     * Parses placeholders in the given text for the specified player.
     *
     * @param   text
     *          text to parse
     * @param   player
     *          player to parse text for
     * @return  parsed text
     */
    @NotNull
    public static String parseText(@NotNull String text, @Nullable Player player) {
        if (player == null) {
            return parseGlobal(text);
        }
        Component component = MINI_MESSAGE.deserialize(
                text,
                player,
                MiniPlaceholders.globalPlaceholders(),
                MiniPlaceholders.audiencePlaceholders()
        );
        return toLegacyString(component);
    }

    /**
     * Converts an Adventure component into a string with hex colors preserved using
     * the {@code §x§R§R§G§G§B§B} format, compatible with TAB's existing color pipeline.
     *
     * @param   component
     *          component to convert
     * @return  legacy string with hex colors preserved
     */
    @NotNull
    public static String toLegacyString(@NotNull Component component) {
        return LEGACY_HEX_SERIALIZER.serialize(component);
    }
}
