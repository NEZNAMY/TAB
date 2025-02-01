package me.neznamy.tab.shared.chat.component;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation that only uses the "text" field with legacy colors in it, without using
 * any component style or extra.
 */
public class SimpleTextComponent extends TextComponent {

    /**
     * Constructs new instance with given text.
     *
     * @param   text
     *          Component text
     */
    public SimpleTextComponent(@NotNull String text) {
        super(text);
    }

    @Override
    @NotNull
    public String toLegacyText() {
        return text;
    }

    @Override
    @Nullable
    protected TextColor fetchLastColor() {
        char[] chars = text.toCharArray();
        for (int index = chars.length - 2; index >= 0; index--) {
            if (chars[index] != '§') continue;
            EnumChatFormat color = EnumChatFormat.getByChar(chars[index + 1]);
            if (color != null) return TextColor.legacy(color);
        }
        return null;
    }
}
