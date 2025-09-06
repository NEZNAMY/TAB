package me.neznamy.tab.shared.chat.component;

import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation that only uses the "text" field with legacy colors in it, without using
 * any component style or extra.
 */
public class LegacyTextComponent extends TextComponent {

    /**
     * Constructs new instance with given text.
     *
     * @param   text
     *          Component text
     */
    protected LegacyTextComponent(@NotNull String text) {
        super(text);
    }

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @NotNull
    protected ChatModifier fetchLastStyle() {
        ChatModifier modifier = new ChatModifier();
        char[] chars = text.toCharArray();
        for (int index = chars.length - 2; index >= 0; index--) {
            if (chars[index] != '§') continue;
            TextColor color = TextColor.getLegacyByChar(chars[index + 1]);
            if (color != null) {
                if (color == TextColor.BOLD) {
                    modifier.setBold(true);
                } else if (color == TextColor.ITALIC) {
                    modifier.setItalic(true);
                } else if (color == TextColor.UNDERLINE) {
                    modifier.setUnderlined(true);
                } else if (color == TextColor.STRIKETHROUGH) {
                    modifier.setStrikethrough(true);
                } else if (color == TextColor.OBFUSCATED) {
                    modifier.setObfuscated(true);
                } else {
                    modifier.setColor(color);
                    break; // color found, do not continue anymore
                }
            }
        }
        return modifier;
    }
}
