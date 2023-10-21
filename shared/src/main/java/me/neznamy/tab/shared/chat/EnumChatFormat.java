package me.neznamy.tab.shared.chat;

import lombok.Getter;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An enum containing all possible legacy color codes and magic codes. Also contains handy color-related methods.
 */
public enum EnumChatFormat {

    BLACK('0', 0x000000),
    DARK_BLUE('1', 0x0000AA),
    DARK_GREEN('2', 0x00AA00),
    DARK_AQUA('3', 0x00AAAA),
    DARK_RED('4', 0xAA0000),
    DARK_PURPLE('5', 0xAA00AA),
    GOLD('6', 0xFFAA00),
    GRAY('7', 0xAAAAAA),
    DARK_GRAY('8', 0x555555),
    BLUE('9', 0x5555FF),
    GREEN('a', 0x55FF55),
    AQUA('b', 0x55FFFF),
    RED('c', 0xFF5555),
    LIGHT_PURPLE('d', 0xFF55FF),
    YELLOW('e', 0xFFFF55),
    WHITE('f', 0xFFFFFF),
    OBFUSCATED('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');

    /** Creating a constant to avoid memory allocations on each request */
    public static final EnumChatFormat[] VALUES = values();

    /** The symbol minecraft uses to colorize text */
    public static final char COLOR_CHAR = 0x00a7;

    /** The color symbol in form of a string */
    public static final String COLOR_STRING = String.valueOf(COLOR_CHAR);

    /** Character representing the color or magic code */
    @Getter private final char character;

    /** Red value of this constant, 0 for magic codes */
    @Getter private final short red;

    /** Green value of this constant, 0 for magic codes */
    @Getter private final short green;

    /** Blue value of this constant, 0 for magic codes */
    @Getter private final short blue;

    /** Color as a hex code, 0 for magic codes */
    @Getter private final int hexCode;

    /** Color symbol followed by constant's character */
    @Getter private final String format;

    /**
     * Constructs new color instance with given character and hex code
     *
     * @param   character
     *          character which the color goes by
     * @param   hexCode
     *          hex code of the color
     */
    EnumChatFormat(char character, int hexCode) {
        this.character = character;
        this.hexCode = hexCode;
        format = String.valueOf(COLOR_CHAR) + character;
        red = (short) ((hexCode >> 16) & 0xFF);
        green = (short) ((hexCode >> 8) & 0xFF);
        blue = (short) (hexCode & 0xFF);
    }

    /**
     * Constructs new magic code instance with given character
     *
     * @param   character
     *          character representing the magic code
     */
    EnumChatFormat(char character) {
        this.character = character;
        format = String.valueOf(COLOR_CHAR) + character;
        red = 0;
        green = 0;
        blue = 0;
        hexCode = 0;
    }

    /**
     * Returns enum value based on provided character or null if character is not valid
     *
     * @param   c
     *          color code character (0-9, a-f, k-o, r)
     * @return  instance from the character or null if character is not valid
     */
    public static @Nullable EnumChatFormat getByChar(char c) {
        for (EnumChatFormat format : VALUES) {
            if (format.character == c) return format;
        }
        return null;
    }

    /**
     * Returns enum value of last colors used in given string.
     * If it's null, empty or does not contain color codes, WHITE is returned.
     *
     * @param   string
     *          string to check last colors of
     * @return  last used color code in given string or WHITE if nothing is found
     */
    public static @NotNull EnumChatFormat lastColorsOf(@NotNull String string) {
        if (string.length() == 0) return EnumChatFormat.WHITE;
        String legacyText = RGBUtils.getInstance().convertRGBtoLegacy(string);
        String last = getLastColors(legacyText);
        if (last.length() > 0) {
            char c = last.toCharArray()[1];
            for (EnumChatFormat e : VALUES) {
                if (e.character == c) return e;
            }
        }
        return EnumChatFormat.WHITE;
    }

    /**
     * Returns enum value with exact red, green and blue values or null if no match was found
     *
     * @param   red
     *          exact red value
     * @param   green
     *          exact green value
     * @param   blue
     *          exact blue value
     * @return  enum value or null if no such combination exists
     */
    public static @Nullable EnumChatFormat fromRGBExact(int red, int green, int blue) {
        for (EnumChatFormat format : VALUES) {
            if (format.red == red && format.green == green && format.blue == blue) return format;
        }
        return null;
    }

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
            if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[(i + 1)]) > -1)) {
                b[i] = COLOR_CHAR;
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }
        }
        return new String(b);
    }

    /**
     * Turns back the color symbol into '&amp;' symbol in provided text.
     *
     * @param   text
     *          text to revert colors in
     * @return  reverted text
     */
    public static @NotNull String decolor(@NotNull String text) {
        if (!text.contains(COLOR_STRING)) return text;
        return text.replace(COLOR_CHAR, '&');
    }

    /**
     * Code taken from bukkit, which returns last color codes used in provided text.
     *
     * @param   input
     *          text to get last colors from
     * @return  last colors used in provided text or empty string if nothing was found
     */
    public static @NotNull String getLastColors(@NotNull String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if ((section == COLOR_CHAR || section == '&') && (index < length - 1)) {
                char c = input.charAt(index + 1);
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
                    result.insert(0, COLOR_CHAR);
                    result.insert(1, c);
                    if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }
}