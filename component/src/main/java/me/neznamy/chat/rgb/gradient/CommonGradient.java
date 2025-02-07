package me.neznamy.chat.rgb.gradient;

import lombok.AllArgsConstructor;
import me.neznamy.chat.TextColor;
import me.neznamy.chat.util.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common class for all gradient patterns.
 */
@AllArgsConstructor
public class CommonGradient implements GradientPattern {

    @NotNull private final Pattern pattern;
    @NotNull private final String containCheck;
    private final int startColorStart;
    private final int messageStart;
    private final int endColorStartSub;

    @Override
    @NotNull
    public String applyPattern(@NotNull String text, @NotNull TriFunction<TextColor, String, TextColor, String> gradientFunction) {
        if (!text.contains(containCheck)) return text;
        String replaced = text;
        Matcher m = pattern.matcher(replaced);
        while (m.find()) {
            String format = m.group();
            TextColor start = new TextColor(format.substring(startColorStart, startColorStart+6));
            String message = format.substring(messageStart, format.length()-10);
            TextColor end = new TextColor(format.substring(format.length()-endColorStartSub, format.length()-endColorStartSub+6));
            replaced = replaced.replace(format, gradientFunction.apply(start, message, end));
        }
        return replaced;
    }
}
