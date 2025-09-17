package me.neznamy.tab.shared.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing a component style.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabStyle {

    @Nullable private TabTextColor color;
    @Nullable private Integer shadowColor; // ARGB
    @Nullable private Boolean bold;
    @Nullable private Boolean italic;
    @Nullable private Boolean underlined;
    @Nullable private Boolean strikethrough;
    @Nullable private Boolean obfuscated;
    @Nullable private String font;

    /**
     * Constructs a copy of the provided modifier.
     *
     * @param   modifier
     *          Modifier to copy
     */
    public TabStyle(@NotNull TabStyle modifier) {
        color = modifier.color;
        shadowColor = modifier.shadowColor;
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
        if (Boolean.TRUE.equals(bold)) builder.append("§l");
        if (Boolean.TRUE.equals(italic)) builder.append("§o");
        if (Boolean.TRUE.equals(obfuscated)) builder.append("§k");
        if (Boolean.TRUE.equals(strikethrough)) builder.append("§m");
        if (Boolean.TRUE.equals(underlined)) builder.append("§n");
        return builder.toString();
    }

    /**
     * Converts this style to EnumChatFormat for determining team color.
     * Magic codes are preferred, since that is how they are defined in configuration as well.
     *
     * @return  EnumChatFormat to show to represent this style
     */
    @NotNull
    public EnumChatFormat toEnumChatFormat() {
        if (Boolean.TRUE == bold) return EnumChatFormat.BOLD;
        if (Boolean.TRUE == italic) return EnumChatFormat.ITALIC;
        if (Boolean.TRUE == underlined) return EnumChatFormat.UNDERLINE;
        if (Boolean.TRUE == strikethrough) return EnumChatFormat.STRIKETHROUGH;
        if (Boolean.TRUE == obfuscated) return EnumChatFormat.OBFUSCATED;
        if (color != null) return color.getLegacyColor();
        return EnumChatFormat.RESET;
    }
}