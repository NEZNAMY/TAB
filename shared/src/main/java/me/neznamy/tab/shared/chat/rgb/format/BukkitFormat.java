package me.neznamy.tab.shared.chat.rgb.format;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatter for &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class BukkitFormat implements RGBFormatter {

    private final Pattern pattern = Pattern.compile("[§&]x[§&\\p{XDigit}]{12}");
    
    @Override
    public @NotNull String reformat(@NotNull String text) {
        if (!text.contains("&x") && !text.contains("§x")) return text;
        String replaced = text;
        Matcher m = pattern.matcher(replaced);
        while (m.find()) {
            String hexCode = m.group();
            String fixed = new String(new char[] {'#', hexCode.charAt(3), hexCode.charAt(5), hexCode.charAt(7), hexCode.charAt(9), hexCode.charAt(11), hexCode.charAt(13)});
            replaced = replaced.replace(hexCode, fixed);
        }
        return replaced;
    }
}