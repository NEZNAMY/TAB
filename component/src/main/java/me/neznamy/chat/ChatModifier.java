package me.neznamy.chat;

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
public class ChatModifier {

    @Nullable private TextColor color;
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
    public ChatModifier(@NotNull ChatModifier modifier) {
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
}