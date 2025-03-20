package me.neznamy.chat.component;

import lombok.NonNull;
import me.neznamy.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation that only uses the "text" field with legacy colors in it, without using
 * any component style or extra.
 */
public class SimpleTextComponent extends TextComponent {

    /** Empty component to avoid recreating one over and over */
    public static final SimpleTextComponent EMPTY = new SimpleTextComponent("");

    /**
     * Creates a new instance with given text.
     * 
     * @param   text
     *          Component text, including color codes
     * @return  Component with given text
     */
    @NotNull
    public static SimpleTextComponent text(@NonNull String text) {
        if (text.isEmpty()) return EMPTY;
        return new SimpleTextComponent(text);
    }
    
    /**
     * Constructs new instance with given text.
     *
     * @param   text
     *          Component text
     */
    private SimpleTextComponent(@NotNull String text) {
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
            TextColor color = TextColor.getLegacyByChar(chars[index + 1]);
            if (color != null) return color;
        }
        return null;
    }
}
