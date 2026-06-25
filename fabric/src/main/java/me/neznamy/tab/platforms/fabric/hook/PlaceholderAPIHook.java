package me.neznamy.tab.platforms.fabric.hook;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Class for managing placeholder-api hook.
 */
public class PlaceholderAPIHook {

    /** Flag tracking whether placeholder-api is installed or not */
    private static final boolean available = FabricLoader.getInstance().isModLoaded("placeholder-api");

    /**
     * Parses placeholders in the given text for the specified player.
     *
     * @param   text
     *          Text to parse
     * @param   player
     *          Player to parse text for
     * @return  Parsed text
     */
    @NotNull
    public static String parsePlaceholders(@NotNull String text, @NotNull ServerPlayer player) {
        if (!available) return "<placeholder-api is not installed>";
        return toTabString(Placeholders.SERVER_PLACEHOLDER_PARSER.parseComponent(
                TextNode.convert(Component.literal(text)),
                PlaceholderContext.of(player).asParserContext()
        ));
    }

    /**
     * Converts component to string format supported by TAB for parsing into a component.
     *
     * @param   component
     *          Component to convert
     * @return  String representation of the component
     */
    @NotNull
    private static String toTabString(@NotNull Component component) {
        StringBuilder sb = new StringBuilder();
        if (component.getStyle().getColor() != null) {
            sb.append(String.format("#%06X", component.getStyle().getColor().getValue()));
        }
        if (component.getStyle().isBold()) sb.append("§l");
        if (component.getStyle().isItalic()) sb.append("§o");
        if (component.getStyle().isUnderlined()) sb.append("§n");
        if (component.getStyle().isStrikethrough()) sb.append("§m");
        if (component.getStyle().isObfuscated()) sb.append("§k");
        if (component.getContents() instanceof PlainTextContents text) {
            sb.append(text.text());
        } else {
            sb.append(component.getContents()); // Fallback for non-text contents
        }
        for (Component extra : component.getSiblings()) {
            sb.append(toTabString(extra));
        }
        return sb.toString();
    }
}