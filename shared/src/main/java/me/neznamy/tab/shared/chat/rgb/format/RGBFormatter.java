package me.neznamy.tab.shared.chat.rgb.format;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for different RGB patterns
 */
public interface RGBFormatter {

    /**
     * Reformats RGB codes in provided text into #RRGGBB format
     *
     * @param   text
     *          text to format
     * @return  reformatted text
     */
    @NotNull String reformat(@NotNull String text);
}