package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class for storing bossbar configuration settings.
 */
@Getter
@RequiredArgsConstructor
public class BossBarConfiguration {

    @NotNull private final String toggleCommand;
    private final boolean rememberToggleChoice;
    private final boolean hiddenByDefault;
    @NotNull private final Map<String, BossBarDefinition> bars;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static BossBarConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "toggle-command", "remember-toggle-choice", "hidden-by-default", "bars"));

        ConfigurationSection barsSection = section.getConfigurationSection("bars");
        Map<String, BossBarDefinition> bars = new LinkedHashMap<>();
        for (Object bar : barsSection.getKeys()) {
            String asString = bar.toString();
            bars.put(asString, BossBarDefinition.fromSection(asString, barsSection.getConfigurationSection(asString)));
        }
        return new BossBarConfiguration(
                section.getString("toggle-command", "/bossbar"),
                section.getBoolean("remember-toggle-choice", false),
                section.getBoolean("hidden-by-default", false),
                bars
        );
    }

    /**
     * Structure representing a defined bossbar.
     */
    @Getter
    @RequiredArgsConstructor
    public static class BossBarDefinition {

        @NotNull private final String style;
        @NotNull private final String color;
        @NotNull private final String progress;
        @NotNull private final String text;
        private final boolean announcementOnly;
        @Nullable private final String displayCondition;

        /**
         * Returns instance of this class created from given configuration section. If there are
         * issues in the configuration, console warns are printed.
         *
         * @param   name
         *          Name of the bossbar this section belongs to
         * @param   section
         *          Configuration section to load from
         * @return  Loaded instance from given configuration section
         */
        @NotNull
        public static BossBarDefinition fromSection(@NotNull String name, @NotNull ConfigurationSection section) {
            // Check keys
            section.checkForUnknownKey(Arrays.asList("style", "color", "progress", "text", "announcement-bar", "display-condition"));

            // Check style
            String style = section.getString("style", "PROGRESS");
            if (!style.contains("%")) {
                try {
                    BarStyle.valueOf(style.toUpperCase(Locale.US));
                } catch (IllegalArgumentException e) {
                    section.startupWarn("Bossbar \"" + name + " has style set to \"" + style + "\", which is not one of the supported styles " +
                            Arrays.toString(BarStyle.values()) + " or a placeholder evaluating to one.");
                }
            }

            // Check color
            String color = section.getString("color", "PURPLE");
            if (!color.contains("%")) {
                try {
                    BarColor.valueOf(color.toUpperCase(Locale.US));
                } catch (IllegalArgumentException e) {
                    section.startupWarn("Bossbar \"" + name + "\" has color set to \"" + color + "\", which is not one of the supported colors " +
                            Arrays.toString(BarColor.values()) + " or a placeholder evaluating to one.");
                }
            }

            // Check progress
            String progress = section.getObject("progress", "100").toString();
            if (!progress.contains("%")) {
                try {
                    Float.parseFloat(progress);
                } catch (IllegalArgumentException e) {
                    section.startupWarn("Bossbar \"" + name + " has progress set to \"" + progress +
                            "\", which is not a valid number between 0 and 100 or a placeholder evaluating to one.");
                }
            }

            return new BossBarDefinition(
                    style,
                    color,
                    progress,
                    section.getString("text", "\"text\" is not defined!"),
                    section.getBoolean("announcement-bar") == Boolean.TRUE,
                    section.getString("display-condition")
            );
        }
    }
}
