package me.neznamy.tab.platforms.velocity.hook;

import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hook for parsing placeholders using MiniPlaceholders.
 */
public class MiniPlaceholdersHook {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Converts TAB's {@code %placeholder%} identifier into MiniMessage format.
     *
     * @param   identifier
     *          placeholder identifier
     * @return  MiniMessage representation of the placeholder
     */
    @NotNull
    public static String toMiniMessage(@NotNull String identifier) {
        if (identifier.startsWith("%sync:") && identifier.endsWith("%")) {
            return "<" + identifier.substring(6, identifier.length() - 1) + ">";
        }
        if (identifier.startsWith("%tab_replace_") && identifier.endsWith("%")) {
            return "<tab_replace:" + identifier.substring(14, identifier.length() - 1) + ">";
        }
        if (identifier.startsWith("%tab_placeholder_") && identifier.endsWith("%")) {
            return "<tab_placeholder:" + identifier.substring(17, identifier.length() - 1) + ">";
        }
        if (identifier.startsWith("%") && identifier.endsWith("%")) {
            return "<" + identifier.substring(1, identifier.length() - 1) + ">";
        }
        return identifier;
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
                toMiniMessage(identifier),
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
                toMiniMessage(identifier),
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
                toMiniMessage(identifier),
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
     * Converts an Adventure component into legacy text supported by TAB.
     *
     * @param   component
     *          component to convert
     * @return  legacy text representation of the component
     */
    @NotNull
    public static String toLegacyString(@NotNull Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }
}
