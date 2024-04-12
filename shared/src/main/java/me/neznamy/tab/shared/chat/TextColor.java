package me.neznamy.tab.shared.chat;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A class representing a component color, which can be either RGB or legacy code.
 */
public class TextColor {

    /** Instances from legacy colors to avoid new class initialization each time */
    private static final TextColor[] legacyColors = {
            new TextColor(EnumChatFormat.BLACK),
            new TextColor(EnumChatFormat.DARK_BLUE),
            new TextColor(EnumChatFormat.DARK_GREEN),
            new TextColor(EnumChatFormat.DARK_AQUA),
            new TextColor(EnumChatFormat.DARK_RED),
            new TextColor(EnumChatFormat.DARK_PURPLE),
            new TextColor(EnumChatFormat.GOLD),
            new TextColor(EnumChatFormat.GRAY),
            new TextColor(EnumChatFormat.DARK_GRAY),
            new TextColor(EnumChatFormat.BLUE),
            new TextColor(EnumChatFormat.GREEN),
            new TextColor(EnumChatFormat.AQUA),
            new TextColor(EnumChatFormat.RED),
            new TextColor(EnumChatFormat.LIGHT_PURPLE),
            new TextColor(EnumChatFormat.YELLOW),
            new TextColor(EnumChatFormat.WHITE)
    };

    /**
     * RGB values as a single number of 3 8-bit numbers (0-255).
     * It is only initialized if colors are actually used to avoid
     * unnecessary memory allocations with string operations.
     */
    private int rgb = -1;

    /** Closest legacy color to this color object. */
    private EnumChatFormat legacyColor;

    /** HEX code of this color as a 6-digit hexadecimal string without "#" prefix. */
    private String hexCode;

    /**
     * Boolean value whether the legacy color was forced with constructor or should be
     * automatically assigned as closest color.
     * This value is used in gradients when converting text for legacy players. */
    @Getter private boolean legacyColorForced;

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
     * Constructs new instance from provided 6-digit hex code and forced legacy color
     *
     * @param   hexCode
     *          6-digit combination of hex numbers as a string
     * @param   legacyColor
     *          color to use for legacy clients instead of using the closest legacy color
     * @throws  IllegalArgumentException
     *          if {@code hexCode} is {@code null} or {@code legacyColor} is {@code null}
     */
    public TextColor(@NotNull String hexCode, @NotNull EnumChatFormat legacyColor) {
        this.hexCode = hexCode;
        legacyColorForced = true;
        this.legacyColor = legacyColor;
    }

    /**
     * Constructs new instance from provided legacy color
     *
     * @param   legacyColor
     *          legacy color to construct the instance from
     */
    private TextColor(@NotNull EnumChatFormat legacyColor) {
        rgb = legacyColor.getRgb();
        this.legacyColor = legacyColor;
        hexCode = String.format("%06X", legacyColor.getRgb());
        legacyColorForced = true;
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
        for (EnumChatFormat color : EnumChatFormat.VALUES) {
            int rDiff = Math.abs(color.getRed() - getRed());
            int gDiff = Math.abs(color.getGreen() - getGreen());
            int bDiff = Math.abs(color.getBlue() - getBlue());
            maxDist = rDiff;
            if (gDiff > maxDist) maxDist = gDiff;
            if (bDiff > maxDist) maxDist = bDiff;
            if (maxDist < minMaxDist) {
                minMaxDist = maxDist;
                closestColor = color;
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
        if (rgb == -1) rgb = Integer.parseInt(hexCode, 16);
        return rgb;
    }

    /**
     * Returns the closest legacy color of this color object.
     * If the color was defined in constructor, it's returned.
     * Otherwise, the closest color is calculated the then returned.
     *
     * @return  closest legacy color
     */
    public @NotNull EnumChatFormat getLegacyColor() {
        if (legacyColor == null) legacyColor = loadClosestColor();
        return legacyColor;
    }

    /**
     * Returns the rgb combination as a 6-digit hex code string
     *
     * @return  the rgb combination as a 6-digit hex code string
     */
    public @NotNull String getHexCode() {
        if (hexCode == null) hexCode = String.format("%06X", rgb);
        return hexCode;
    }

    /**
     * Returns color from given legacy color
     *
     * @param   format
     *          Legacy color
     * @return  Instance from legacy color
     */
    public static TextColor legacy(@NotNull EnumChatFormat format) {
        return legacyColors[format.ordinal()];
    }
}