package me.neznamy.tab.shared.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An enum containing all possible legacy color codes and magic codes. Also contains handy color-related methods.
 */
@Getter
@AllArgsConstructor
public enum EnumChatFormat {

    BLACK('0', true),
    DARK_BLUE('1', true),
    DARK_GREEN('2', true),
    DARK_AQUA('3', true),
    DARK_RED('4', true),
    DARK_PURPLE('5', true),
    GOLD('6', true),
    GRAY('7', true),
    DARK_GRAY('8', true),
    BLUE('9', true),
    GREEN('a', true),
    AQUA('b', true),
    RED('c', true),
    LIGHT_PURPLE('d', true),
    YELLOW('e', true),
    WHITE('f', true),
    OBFUSCATED('k', false),
    BOLD('l', false),
    STRIKETHROUGH('m', false),
    UNDERLINE('n', false),
    ITALIC('o', false),
    RESET('r', false);

    /** Character representing the color or magic code */
    private final char character;

    /** Flag tracking whether this is a color code or not */
    private final boolean color;

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