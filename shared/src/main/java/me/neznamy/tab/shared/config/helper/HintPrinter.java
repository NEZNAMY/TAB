package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Class for printing hints for a cleaner configuration
 * with the same effect, typically due to redundancy.
 */
@SuppressWarnings("unchecked")
public class HintPrinter {

    /**
     * Checks header/footer config section if any of per-world or per-server
     * values are identical to default value and prints a hint if it is.
     *
     * @param   configSection
     *          Header/footer config section
     */
    public void checkHeaderFooterForRedundancy(@NotNull Map<String, Object> configSection) {
        String defaultHeader = String.valueOf(configSection.get("header"));
        String defaultFooter = String.valueOf(configSection.get("footer"));
        if (configSection.get("per-world") instanceof Map) {
            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) configSection.get("per-world");
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                String world = entry.getKey();
                if (String.valueOf(entry.getValue().getOrDefault("header", "-")).equals(defaultHeader)) {
                    hint("Per-world header for world \"" + world + "\" is identical to default header. " +
                            "This is redundant and can be removed for cleaner config.");
                }
                if (String.valueOf(entry.getValue().getOrDefault("footer", "-")).equals(defaultFooter)) {
                    hint("Per-world footer for world \"" + world + "\" is identical to default footer. " +
                            "This is redundant and can be removed for cleaner config.");
                }
            }
        }
        if (configSection.get("per-server") instanceof Map) {
            Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) configSection.get("per-server");
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                String server = entry.getKey();
                if (String.valueOf(entry.getValue().getOrDefault("header", "-")).equals(defaultHeader)) {
                    hint("Per-server header for server \"" + server + "\" is identical to default header. " +
                            "This is redundant and can be removed for cleaner config.");
                }
                if (String.valueOf(entry.getValue().getOrDefault("footer", "-")).equals(defaultFooter)) {
                    hint("Per-server footer for server \"" + server + "\" is identical to default footer. " +
                            "This is redundant and can be removed for cleaner config.");
                }
            }
        }
    }

    /**
     * Prints a hint saying layout already includes prevent-spectator-effect feature.
     */
    public void layoutIncludesPreventSpectatorEffect() {
        hint("Layout feature automatically includes prevent-spectator-effect, therefore the feature can be disabled " +
                "for better performance, as it is not needed at all (assuming it is configured to always display some layout).");
    }

    /**
     * Prints a hint that a placeholder was configured for refresh interval, but the interval is
     * identical to default interval, therefore there is no need to define it.
     *
     * @param   placeholder
     *          Defined placeholder with same refresh as default
     */
    public void redundantRefreshInterval(@NotNull String placeholder) {
        hint("Refresh interval of " + placeholder + " is same as default interval, therefore there is no need to override it.");
    }

    /**
     * Prints a hint when placeholder replacement is configured to show the placeholder itself
     * in "else" value, which is already the default behavior.
     *
     * @param   replacementMap
     *          Placeholder output replacement map from config
     */
    public void checkForRedundantElseReplacement(@NotNull Map<Object, Object> replacementMap) {
        for (Map.Entry<Object, Object> entry : replacementMap.entrySet()) {
            String placeholder = String.valueOf(entry.getKey());
            if (!(entry.getValue() instanceof Map)) continue;
            for (Map.Entry<?, ?> pattern : ((Map<?, ?>) entry.getValue()).entrySet()) {
                if (pattern.getKey().equals("else") && pattern.getValue().equals(placeholder)) {
                    hint(String.format("Placeholder %s has configured \"else -> %s\" replacement pattern, but this is already the default behavior " +
                            "and therefore this pattern can be removed.", placeholder, placeholder));
                }
            }
        }
    }

    /**
     * Logs the message with "Hint" prefix.
     *
     * @param   message
     *          Message to log
     */
    private void hint(@NotNull String message) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GOLD + "[Hint] " + message));
    }
}
