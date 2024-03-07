package me.neznamy.tab.shared.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data @NoArgsConstructor
public class ChatModifier {

    @Nullable private TextColor color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;
    @Nullable private String font;

    public ChatModifier(@NotNull ChatModifier modifier) {
        color = modifier.color;
        bold = modifier.bold;
        italic = modifier.italic;
        underlined = modifier.underlined;
        strikethrough = modifier.strikethrough;
        obfuscated = modifier.obfuscated;
        font = modifier.font;
    }

    /**
     * Returns a String consisting of magic codes (color symbol + character) of
     * each magic code used. If none are used, empty String is returned.
     *
     * @return  Magic codes of this modifier as String
     */
    @NotNull
    public String getMagicCodes() {
        StringBuilder builder = new StringBuilder();
        if (bold) builder.append(EnumChatFormat.BOLD);
        if (italic) builder.append(EnumChatFormat.ITALIC);
        if (underlined) builder.append(EnumChatFormat.UNDERLINE);
        if (strikethrough) builder.append(EnumChatFormat.STRIKETHROUGH);
        if (obfuscated) builder.append(EnumChatFormat.OBFUSCATED);
        return builder.toString();
    }

    /**
     * Returns {@code true} if this modifier has any magic codes, {@code false} if not.
     *
     * @return  {@code true} if this modifier has any magic codes, {@code false} if not
     */
    public boolean hasMagicCodes() {
        return bold || italic || underlined || strikethrough || obfuscated;
    }
}