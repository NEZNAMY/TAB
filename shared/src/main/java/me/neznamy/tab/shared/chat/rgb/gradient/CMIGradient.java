package me.neznamy.tab.shared.chat.rgb.gradient;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gradient formatter using CMI's syntax.
 */
public class CMIGradient extends CommonGradient {

    //pattern for {#RRGGBB<>}
    private final Pattern shortcutPattern = Pattern.compile("\\{#[0-9a-fA-F]{6}<>}");

    /**
     * Constructs new instance.
     */
    public CMIGradient() {
        super(Pattern.compile("\\{#[0-9a-fA-F]{6}>}[^{]*\\{#[0-9a-fA-F]{6}<}"), "{#", 2, 10, 8);
    }
    
    @Override
    public String applyPattern(@NotNull String text) {
        String replaced = text;
        if (replaced.contains("<>}")) {
            Matcher m = shortcutPattern.matcher(replaced);
            while (m.find()) {
                String format = m.group();
                String code = format.substring(2, 8);
                replaced = replaced.replace(format, "{#" + code + "<}{#" + code + ">}");
            }
        }
        return super.applyPattern(replaced);
    }
}