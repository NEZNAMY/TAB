package me.neznamy.tab.shared.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class representing a component color, which can be either RGB or legacy code.
 */
public class TabTextColor {

    /** Map of legacy colors by their character */
    public static final Map<Character, TabTextColor> LEGACY_COLORS = new LinkedHashMap<>();

    public static final TabTextColor BLACK = new TabTextColor(EnumChatFormat.BLACK, 0x000000);
    public static final TabTextColor DARK_BLUE = new TabTextColor(EnumChatFormat.DARK_BLUE, 0x0000AA);
    public static final TabTextColor DARK_GREEN = new TabTextColor(EnumChatFormat.DARK_GREEN, 0x00AA00);
    public static final TabTextColor DARK_AQUA = new TabTextColor(EnumChatFormat.DARK_AQUA, 0x00AAAA);
    public static final TabTextColor DARK_RED = new TabTextColor(EnumChatFormat.DARK_RED, 0xAA0000);
    public static final TabTextColor DARK_PURPLE = new TabTextColor(EnumChatFormat.DARK_PURPLE, 0xAA00AA);
    public static final TabTextColor GOLD = new TabTextColor(EnumChatFormat.GOLD, 0xFFAA00);
    public static final TabTextColor GRAY = new TabTextColor(EnumChatFormat.GRAY, 0xAAAAAA);
    public static final TabTextColor DARK_GRAY = new TabTextColor(EnumChatFormat.DARK_GRAY, 0x555555);
    public static final TabTextColor BLUE = new TabTextColor(EnumChatFormat.BLUE, 0x5555FF);
    public static final TabTextColor GREEN = new TabTextColor(EnumChatFormat.GREEN, 0x55FF55);
    public static final TabTextColor AQUA = new TabTextColor(EnumChatFormat.AQUA, 0x55FFFF);
    public static final TabTextColor RED = new TabTextColor(EnumChatFormat.RED, 0xFF5555);
    public static final TabTextColor LIGHT_PURPLE = new TabTextColor(EnumChatFormat.LIGHT_PURPLE, 0xFF55FF);
    public static final TabTextColor YELLOW = new TabTextColor(EnumChatFormat.YELLOW, 0xFFFF55);
    public static final TabTextColor WHITE = new TabTextColor(EnumChatFormat.WHITE, 0xFFFFFF);
    public static final TabTextColor OBFUSCATED = new TabTextColor(EnumChatFormat.OBFUSCATED, 0);
    public static final TabTextColor BOLD = new TabTextColor(EnumChatFormat.BOLD, 0);
    public static final TabTextColor STRIKETHROUGH = new TabTextColor(EnumChatFormat.STRIKETHROUGH, 0);
    public static final TabTextColor UNDERLINE = new TabTextColor(EnumChatFormat.UNDERLINE, 0);
    public static final TabTextColor ITALIC = new TabTextColor(EnumChatFormat.ITALIC, 0);
    public static final TabTextColor RESET = new TabTextColor(EnumChatFormat.RESET, 0);

    private static final TabTextColor[] legacyColorArray = LEGACY_COLORS.values().toArray(new TabTextColor[0]);

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
    public TabTextColor(@NotNull String hexCode) {
        this.hexCode = hexCode;
    }

    /**
     * Constructs new instance from provided legacy color
     *
     * @param   legacyColor
     *          legacy color to construct the instance from
     */
    private TabTextColor(@NotNull EnumChatFormat legacyColor, int rgb) {
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
    public TabTextColor(int rgb) {
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
    public TabTextColor(int red, int green, int blue) {
        rgb = (red << 16) + (green << 8) + blue;
    }

    /**
     * Loads the closest legacy color based currently provided values
     */
    private EnumChatFormat loadClosestColor() {
        double minMaxDist = 9999;
        double maxDist;
        EnumChatFormat closestColor = EnumChatFormat.WHITE;
        for (TabTextColor color : legacyColorArray) {
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
    public static TabTextColor getLegacyByChar(char c) {
        return LEGACY_COLORS.get(c);
    }
}