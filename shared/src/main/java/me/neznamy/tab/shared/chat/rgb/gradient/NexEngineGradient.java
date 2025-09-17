package me.neznamy.tab.shared.chat.rgb.gradient;

import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexEngineGradient implements GradientPattern {

    //pattern for <gradient:#RRGGBB></gradient:#RRGGBB>
    private final Pattern pattern = Pattern.compile("<gradient:#([A-Fa-f0-9]{6})>(.*?)</gradient:#([A-Fa-f0-9]{6})>");

    @Override
    @NotNull
    public String applyPattern(@NotNull String text, @NotNull TriFunction<TabTextColor, String, TabTextColor, String> gradientFunction) {
        if (!text.contains("<grad")) return text;
        String replaced = text;
        Matcher matcher = pattern.matcher(replaced);
        while (matcher.find()) {
            String format = matcher.group();
            TabTextColor start = new TabTextColor(matcher.group(1));
            String content = matcher.group(2);
            TabTextColor end = new TabTextColor(matcher.group(3));
            replaced = replaced.replace(format, gradientFunction.apply(start, content, end));
        }
        return replaced;
    }
}
