package me.neznamy.tab.shared.chat.rgb.format;

import me.neznamy.tab.shared.chat.TabTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatter for &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class BukkitFormat implements RGBFormatter {

    private final Pattern pattern = Pattern.compile("§x[§\\p{XDigit}]{12}");
    
    @Override
    @NotNull
    public String reformat(@NotNull String text, @NotNull Function<TabTextColor, String> rgbFunction) {
        if (!text.contains("§x")) return text;
        String replaced = text;
        Matcher m = pattern.matcher(replaced);
        while (m.find()) {
            String group = m.group();
            String hexCode = new String(new char[] {group.charAt(3), group.charAt(5), group.charAt(7), group.charAt(9), group.charAt(11), group.charAt(13)});
            replaced = replaced.replace(group, rgbFunction.apply(new TabTextColor(hexCode)));
        }
        return replaced;
    }
}