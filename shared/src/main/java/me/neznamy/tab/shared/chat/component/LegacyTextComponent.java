package me.neznamy.tab.shared.chat.component;

import me.neznamy.tab.shared.chat.TabStyle;
import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation that only uses the "text" field with legacy colors in it, without using
 * any component style or extra.
 */
public class LegacyTextComponent extends TabTextComponent {

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
    protected TabStyle fetchLastStyle() {
        TabStyle modifier = new TabStyle();
        char[] chars = text.toCharArray();
        for (int index = chars.length - 2; index >= 0; index--) {
            if (chars[index] != '§') continue;
            TabTextColor color = TabTextColor.getLegacyByChar(chars[index + 1]);
            if (color != null) {
                if (color == TabTextColor.BOLD) {
                    modifier.setBold(true);
                } else if (color == TabTextColor.ITALIC) {
                    modifier.setItalic(true);
                } else if (color == TabTextColor.UNDERLINE) {
                    modifier.setUnderlined(true);
                } else if (color == TabTextColor.STRIKETHROUGH) {
                    modifier.setStrikethrough(true);
                } else if (color == TabTextColor.OBFUSCATED) {
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
