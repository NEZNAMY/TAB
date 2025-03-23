package me.neznamy.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class representing a component color, which can be either RGB or legacy code.
 */
public class TextColor {

    /** Map of legacy colors by their character */
    public static final Map<Character, TextColor> LEGACY_COLORS = new LinkedHashMap<>();

    public static final TextColor BLACK = new TextColor(EnumChatFormat.BLACK, 0x000000);
    public static final TextColor DARK_BLUE = new TextColor(EnumChatFormat.DARK_BLUE, 0x0000AA);
    public static final TextColor DARK_GREEN = new TextColor(EnumChatFormat.DARK_GREEN, 0x00AA00);
    public static final TextColor DARK_AQUA = new TextColor(EnumChatFormat.DARK_AQUA, 0x00AAAA);
    public static final TextColor DARK_RED = new TextColor(EnumChatFormat.DARK_RED, 0xAA0000);
    public static final TextColor DARK_PURPLE = new TextColor(EnumChatFormat.DARK_PURPLE, 0xAA00AA);
    public static final TextColor GOLD = new TextColor(EnumChatFormat.GOLD, 0xFFAA00);
    public static final TextColor GRAY = new TextColor(EnumChatFormat.GRAY, 0xAAAAAA);
    public static final TextColor DARK_GRAY = new TextColor(EnumChatFormat.DARK_GRAY, 0x555555);
    public static final TextColor BLUE = new TextColor(EnumChatFormat.BLUE, 0x5555FF);
    public static final TextColor GREEN = new TextColor(EnumChatFormat.GREEN, 0x55FF55);
    public static final TextColor AQUA = new TextColor(EnumChatFormat.AQUA, 0x55FFFF);
    public static final TextColor RED = new TextColor(EnumChatFormat.RED, 0xFF5555);
    public static final TextColor LIGHT_PURPLE = new TextColor(EnumChatFormat.LIGHT_PURPLE, 0xFF55FF);
    public static final TextColor YELLOW = new TextColor(EnumChatFormat.YELLOW, 0xFFFF55);
    public static final TextColor WHITE = new TextColor(EnumChatFormat.WHITE, 0xFFFFFF);
    public static final TextColor OBFUSCATED = new TextColor(EnumChatFormat.OBFUSCATED, 0);
    public static final TextColor BOLD = new TextColor(EnumChatFormat.BOLD, 0);
    public static final TextColor STRIKETHROUGH = new TextColor(EnumChatFormat.STRIKETHROUGH, 0);
    public static final TextColor UNDERLINE = new TextColor(EnumChatFormat.UNDERLINE, 0);
    public static final TextColor ITALIC = new TextColor(EnumChatFormat.ITALIC, 0);
    public static final TextColor RESET = new TextColor(EnumChatFormat.RESET, 0);

    private static final TextColor[] legacyColorArray = LEGACY_COLORS.values().toArray(new TextColor[0]);

    /**
     * RGB values as a single number of 3 8-bit numbers (0-255).
     * It is only initialized if colors are actually used to avoid
     * unnecessary memory allocations with string operations.
     */
    private int rgb = -1;

    /** Closest legacy color to this color object. */
    @Nullable
    private EnumChatFormat legacyColor;

    /** HEX code of this color as a 6-digit hexadecimal string without "#" prefix. */
    @Nullable
    private String hexCode;

    /**
     * Constructs new instance from provided 6-digit hex code string
     *
     * @param   hexCode
     *          a 6-digit combination of hex numbers as a string
     * @throws  IllegalArgumentException
     *          if hexCode is {@code null}
     */
    public TextColor(@NotNull String hexCode) {
        this.hexCode = hexCode;
    }

    /**
     * Constructs new instance from provided legacy color
     *
     * @param   legacyColor
     *          legacy color to construct the instance from
     */
    private TextColor(@NotNull EnumChatFormat legacyColor, int rgb) {
        this.rgb = rgb;
        this.legacyColor = legacyColor;
        hexCode = String.format("%06X", rgb);
        LEGACY_COLORS.put(legacyColor.getCharacter(), this);
    }

    /**
     * Constructs new instance with given RGB value.
     *
     * @param   rgb
     *          RGB value
     */
    public TextColor(int rgb) {
        this.rgb = rgb;
    }

    /**
     * Constructs new instance with red, green and blue values
     *
     * @param   red
     *          red value
     * @param   green
     *          green value
     * @param   blue
     *          blue value
     */
    public TextColor(int red, int green, int blue) {
        rgb = (red << 16) + (green << 8) + blue;
    }

    /**
     * Loads the closest legacy color based currently provided values
     */
    private EnumChatFormat loadClosestColor() {
        double minMaxDist = 9999;
        double maxDist;
        EnumChatFormat closestColor = EnumChatFormat.WHITE;
        for (TextColor color : legacyColorArray) {
            int rDiff = Math.abs(color.getRed() - getRed());
            int gDiff = Math.abs(color.getGreen() - getGreen());
            int bDiff = Math.abs(color.getBlue() - getBlue());
            maxDist = rDiff;
            if (gDiff > maxDist) maxDist = gDiff;
            if (bDiff > maxDist) maxDist = bDiff;
            if (maxDist < minMaxDist) {
                minMaxDist = maxDist;
                closestColor = color.legacyColor;
            }
        }
        return closestColor;
    }

    /**
     * Returns {@code red} value
     *
     * @return  red value
     */
    public int getRed() {
        return (getRgb() >> 16) & 0xFF;
    }

    /**
     * Returns {@code green} value
     *
     * @return  green value
     */
    public int getGreen() {
        return (getRgb() >> 8) & 0xFF;
    }

    /**
     * Returns {@code blue} value
     *
     * @return  blue value
     */
    public int getBlue() {
        return getRgb() & 0xFF;
    }

    /**
     * Returns {@code rgb} value
     *
     * @return  rgb value
     */
    public int getRgb() {
        if (rgb == -1) rgb = Integer.parseInt(getHexCode(), 16);
        return rgb;
    }

    /**
     * Returns the closest legacy color of this color object.
     * If the color was defined in constructor, it's returned.
     * Otherwise, the closest color is calculated the then returned.
     *
     * @return  closest legacy color
     */
    @NotNull
    public EnumChatFormat getLegacyColor() {
        if (legacyColor == null) legacyColor = loadClosestColor();
        return legacyColor;
    }

    /**
     * Returns the rgb combination as a 6-digit hex code string
     *
     * @return  the rgb combination as a 6-digit hex code string
     */
    @NotNull
    public String getHexCode() {
        if (hexCode == null) hexCode = String.format("%06X", rgb);
        return hexCode;
    }

    /**
     * Returns legacy colors based on provided character or {@code null} if character is not valid
     *
     * @param   c
     *          color code character (0-9, a-f, k-o, r)
     * @return  instance from the character or {@code null} if character is not valid
     */
    @Nullable
    public static TextColor getLegacyByChar(char c) {
        return LEGACY_COLORS.get(c);
    }
}