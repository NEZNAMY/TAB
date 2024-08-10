package me.neznamy.tab.shared.config.files.config;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScoreboardConfiguration extends ConfigurationSection {

    private final String SECTION = "scoreboard";
    @NotNull public final String toggleCommand = getString(SECTION + ".toggle-command", "/sb");
    public final boolean rememberToggleChoice = getBoolean(SECTION + ".remember-toggle-choice", false);
    public final boolean hiddenByDefault = getBoolean(SECTION + ".hidden-by-default", false);
    public final boolean useNumbers = getBoolean(SECTION + ".use-numbers", true);
    public final int staticNumber = getInt(SECTION + ".static-number", 0);
    public final int joinDelay = getInt(SECTION + ".delay-on-join-milliseconds", 0);
    @NotNull public final Map<String, ScoreboardDefinition> scoreboards = new LinkedHashMap<>();

    public ScoreboardConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "toggle-command", "remember-toggle-choice", "hidden-by-default",
                "use-numbers", "static-number", "delay-on-join-milliseconds", "scoreboards"));

        for (Object scoreboard : getMap(SECTION + ".scoreboards", Collections.emptyMap()).keySet()) {
            checkForUnknownKey(new String[]{SECTION, "scoreboards", scoreboard.toString()}, Arrays.asList("display-condition", "title", "lines"));
            ScoreboardDefinition def = new ScoreboardDefinition(
                    getString(new String[]{SECTION, "scoreboards", scoreboard.toString(), "display-condition"}),
                    getString(new String[]{SECTION, "scoreboards", scoreboard.toString(), "title"}, "<Title is not defined>"),
                    getStringList(new String[]{SECTION, "scoreboards", scoreboard.toString(), "lines"}, Arrays.asList("scoreboard \"" +
                            scoreboard +"\" is missing \"lines\" property!", "did you forget to configure it or just your spacing is wrong?"))
            );
            scoreboards.put(scoreboard.toString(), def);
        }
        checkLineCounts();
        checkChain();
    }

    private void checkLineCounts() {
        for (Map.Entry<String, ScoreboardDefinition> entry : scoreboards.entrySet()) {
            int alwaysVisibleLines = 0;
            for (String line : entry.getValue().lines) {
                String withoutPlaceholders = line;
                for (String placeholder : PlaceholderManagerImpl.detectPlaceholders(line)) {
                    withoutPlaceholders = withoutPlaceholders.replace(placeholder, "");
                }
                if (!withoutPlaceholders.isEmpty()) alwaysVisibleLines++;
            }
            if (alwaysVisibleLines > 15) {
                startupWarn(String.format("Scoreboard \"%s\" has %d defined lines, at least %d of which are permanently visible. " +
                        "However, the client only displays up to 15 lines, with any lines below them not being displayed.",
                        entry.getKey(), entry.getValue().lines.size(), alwaysVisibleLines));
            }
        }
    }

    private void checkChain() {
        String noConditionScoreboard = null;
        for (Map.Entry<String, ScoreboardDefinition> entry : scoreboards.entrySet()) {
            if (entry.getValue().displayCondition == null) {
                noConditionScoreboard = entry.getKey();
            } else if (noConditionScoreboard != null) {
                startupWarn("Scoreboard \"" + noConditionScoreboard + "\" has no display condition set, however, there is" +
                        " another scoreboard in the chain (" + entry.getKey() + "). Scoreboards are checked from top to bottom" +
                        " until a scoreboard with meeting condition or no condition is found. Because of this, the scoreboard (" +
                        entry.getKey() + ") after the no-condition scoreboard (" + noConditionScoreboard + ") will never be displayed. " +
                        "Unless this is intentional to externally display the scoreboard (commands, API), this is a mistake.");
            }
        }
    }

    @RequiredArgsConstructor
    public static class ScoreboardDefinition {

        @Nullable public final String displayCondition;
        @NotNull public final String title;
        @NotNull public final List<String> lines;
    }
}
