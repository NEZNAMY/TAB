package me.neznamy.tab.shared.config.files.config;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BossBarConfiguration extends ConfigurationSection {

    private final String SECTION = "bossbar";
    public final boolean enabled = getBoolean(SECTION + ".enabled", false);
    @NotNull public final String toggleCommand = getString(SECTION + ".toggle-command", "/bossbar");
    public final boolean rememberToggleChoice = getBoolean(SECTION + ".remember-toggle-choice", false);
    public final boolean hiddenByDefault = getBoolean(SECTION + ".hidden-by-default", false);
    @NotNull public final Map<String, BossBarDefinition> bars = new LinkedHashMap<>();

    public BossBarConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "toggle-command", "remember-toggle-choice", "hidden-by-default", "bars"));
        for (Object bossbar : getMap(SECTION + ".bars", Collections.emptyMap()).keySet()) {
            checkForUnknownKey(SECTION + ".bars." + bossbar, Arrays.asList("style", "color", "progress", "text", "announcement-bar", "display-condition"));
            BossBarDefinition def = new BossBarDefinition(
                    getString(SECTION + ".bars." + bossbar + ".style", "PROGRESS"),
                    getString(SECTION + ".bars." + bossbar + ".color", "PURPLE"),
                    getObject(SECTION + ".bars." + bossbar + ".progress", "100").toString(),
                    getString(SECTION + ".bars." + bossbar + ".text", "\"text\" is not defined!"),
                    getBoolean(SECTION + ".bars." + bossbar + ".announcement-bar") == Boolean.TRUE,
                    getString(SECTION + ".bars." + bossbar + ".display-condition")
            );
            bars.put(bossbar.toString(), def);
            printStartupWarns(bossbar.toString(), def);
        }
    }

    private void printStartupWarns(@NotNull String name, @NotNull BossBarDefinition bossbar) {
        if (!bossbar.color.contains("%")) {
            try {
                BarColor.valueOf(bossbar.color.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                startupWarn("Bossbar \"" + name + " has color set to \"" + bossbar.color + "\", which is not one of the supported colors " +
                        Arrays.toString(BarColor.values()) + " or a placeholder evaluating to one.");
            }
        }
        if (!bossbar.style.contains("%")) {
            try {
                BarStyle.valueOf(bossbar.style.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                startupWarn("Bossbar \"" + name + " has style set to \"" + bossbar.style + "\", which is not one of the supported styles " +
                        Arrays.toString(BarStyle.values()) + " or a placeholder evaluating to one.");
            }
        }
        if (!bossbar.progress.contains("%")) {
            try {
                Float.parseFloat(bossbar.progress);
            } catch (IllegalArgumentException e) {
                startupWarn("Bossbar \"" + name + " has progress set to \"" + bossbar.progress +
                        "\", which is not a valid number between 0 and 100 or a placeholder evaluating to one.");
            }
        }
    }

    @RequiredArgsConstructor
    public static class BossBarDefinition {

        @NotNull public final String style;
        @NotNull public final String color;
        @NotNull public final String progress;
        @NotNull public final String text;
        public final boolean announcementOnly;
        @Nullable public final String displayCondition;
    }
}
