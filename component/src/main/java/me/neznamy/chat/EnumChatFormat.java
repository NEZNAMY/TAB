package me.neznamy.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An enum containing all possible legacy color codes and magic codes. Also contains handy color-related methods.
 */
@Getter
@AllArgsConstructor
public enum EnumChatFormat {

    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    OBFUSCATED('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');

    /** Character representing the color or magic code */
    private final char character;

    /**
     * Color translation method taken from bukkit, which converts '&amp;' symbol into
     * the actual color character if followed by a valid color character.
     *
     * @param   textToTranslate
     *          text to replace color symbol in
     * @return  colorized string from provided text
     */
    public static @NotNull String color(@NotNull String textToTranslate) {
        if (!textToTranslate.contains("&")) return textToTranslate;
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx#".indexOf(b[(i + 1)]) > -1)) {
                b[i] = '§';
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }
        }
        return new String(b);
    }
}