package me.neznamy.tab.shared.chat.rgb.gradient;

import me.neznamy.tab.shared.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexEngineGradient implements GradientPattern {

    //pattern for <gradient:#RRGGBB></gradient:#RRGGBB>
    private final Pattern pattern = Pattern.compile("<gradient:#([A-Fa-f0-9]{6})>(.*?)</gradient:#([A-Fa-f0-9]{6})>");

    @Override
    public String applyPattern(@NotNull String text, boolean ignorePlaceholders) {
        if (!text.contains("<grad")) return text;
        String replaced = text;
        Matcher matcher = pattern.matcher(replaced);
        while (matcher.find()) {
            String format = matcher.group();
            if ((ignorePlaceholders && format.contains("%"))) continue;
            TextColor start = new TextColor(matcher.group(1));
            String content = matcher.group(2);
            TextColor end = new TextColor(matcher.group(3));
            String applied = asGradient(start, content, end);
            replaced = replaced.replace(format, applied);
        }
        return replaced;
    }
}
