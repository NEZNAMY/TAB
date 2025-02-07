package me.neznamy.chat.rgb.format;

import lombok.RequiredArgsConstructor;
import me.neznamy.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common class for various RGB formatters.
 */
@RequiredArgsConstructor
public class CommonFormatter implements RGBFormatter {

    /** Pattern for finding the RGB code */
    private final Pattern pattern;

    /** String to check if the text contains to possibly skip pattern matching for better performance */
    private final String stringCheck;
    
    @Override
    @NotNull
    public String reformat(@NotNull String text, @NotNull Function<TextColor, String> rgbFunction) {
        if (!text.contains(stringCheck)) return text;
        String replaced = text;
        Matcher m = pattern.matcher(replaced);
        while (m.find()) {
            String group = m.group();
            String hexCode = group.substring(2, 8);
            replaced = replaced.replace(group, rgbFunction.apply(new TextColor(hexCode)));
        }
        return replaced;
    }
}