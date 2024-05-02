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
    private boolean obfuscated;
    private boolean strikethrough;
    private boolean underlined;
    @Nullable private String font;

    public ChatModifier(@NotNull ChatModifier modifier) {
        color = modifier.color;
        bold = modifier.bold;
        italic = modifier.italic;
        obfuscated = modifier.obfuscated;
        strikethrough = modifier.strikethrough;
        underlined = modifier.underlined;
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
        if (obfuscated) builder.append(EnumChatFormat.OBFUSCATED);
        if (strikethrough) builder.append(EnumChatFormat.STRIKETHROUGH);
        if (underlined) builder.append(EnumChatFormat.UNDERLINE);
        return builder.toString();
    }

    /**
     * Returns bitmask of magic codes.
     *
     * @return  Bitmask of magic codes
     */
    public int getMagicCodeBitMask() {
        int mask = 0;
        if (bold)          mask += 1;
        if (italic)        mask += 2;
        if (obfuscated)    mask += 4;
        if (strikethrough) mask += 8;
        if (underlined)    mask += 16;
        return mask;
    }
}