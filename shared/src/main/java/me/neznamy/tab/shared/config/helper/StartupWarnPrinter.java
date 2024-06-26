package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.layout.GroupPattern;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Class for printing startup warns when features are
 * clearly not configured properly when loading them.
 */
public class StartupWarnPrinter {

    /** Amount of logged warns on startup */
    private int warnCount;

    /**
     * Checks if configured refresh intervals are non-negative, non-zero and
     * divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}. If not,
     * value is fixed in the map and console warn is sent.
     *
     * @param   refreshIntervals
     *          Configured refresh intervals
     */
    public void fixRefreshIntervals(@NotNull Map<String, Integer> refreshIntervals) {
        int defaultRefresh = refreshIntervals.getOrDefault("default-refresh-interval", 500);
        LinkedHashMap<String, Integer> valuesToFix = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : refreshIntervals.entrySet()) {
            if (entry.getValue() == null) {
                startupWarn("Refresh interval of " + entry.getKey() +
                        " is set to null. Define a valid value or remove it if you don't want to override default value.");
                valuesToFix.put(entry.getKey(), TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
                continue;
            }
            if (!(entry.getValue() instanceof Integer)) {
                startupWarn("Refresh interval configured for \"" + entry.getKey() +
                        "\" is not a valid number.");
                valuesToFix.put(entry.getKey(), 500);
                continue;
            }
            int interval = (int) entry.getValue();
            if (!entry.getKey().equals("default-refresh-interval") && interval == defaultRefresh) {
                TAB.getInstance().getConfigHelper().hint().redundantRefreshInterval(entry.getKey());
            }
            if (interval == -1) continue;
            if (interval <= 0) {
                startupWarn("Invalid refresh interval configured for " + entry.getKey() +
                        " (" + interval + "). Value cannot be zero or negative (except -1).");
            } else if (interval % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
                startupWarn("Invalid refresh interval configured for " + entry.getKey() +
                        " (" + interval + "). Value must be divisible by " + TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL + ".");
            } else continue;
            valuesToFix.put(entry.getKey(), TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        }
        refreshIntervals.putAll(valuesToFix);
    }

    /**
     * Makes interval divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     * and sends error message if it was not already or was 0 or less
     *
     * @param   name
     *          name of animation used in error message
     * @param   interval
     *          configured change interval
     * @return  fixed change interval
     */
    public int fixAnimationInterval(@NotNull String name, int interval) {
        if (interval == 0) {
            startupWarn(String.format("Animation \"%s\" has refresh interval of 0 milliseconds! Did you forget to configure it? Using 1000.", name));
            return 1000;
        }
        if (interval < 0) {
            startupWarn(String.format("Animation \"%s\" has refresh interval of %s. Refresh cannot be negative! Using 1000.", name, interval));
            return 1000;
        }
        if (interval % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
            int min = TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL;
            int newInterval = Math.round((float) interval / min) * min; // rounding
            if (newInterval == 0) newInterval = min;
            startupWarn(String.format("Animation \"%s\" has refresh interval of %s, which is not divisible by %s! Using %s.",
                    name, interval, min, newInterval));
            return newInterval;
        }
        return interval;
    }

    /**
     *
     * Returns the list if not null, empty list and error message if null
     *
     * @param   name
     *          name of animation used in error message
     * @param   list
     *          list of configured animation frames
     * @return  the list if it's valid, singleton list with {@code "<Invalid Animation>"} otherwise
     */
    public List<String> fixAnimationFrames(@NotNull String name, @Nullable List<String> list) {
        if (list == null) {
            startupWarn("Animation \"" + name + "\" does not have any texts defined!");
            return Collections.singletonList("<Animation does not have any texts>");
        }
        return list;
    }

    /**
     * Checks if belowname text contains suspicious placeholders, which
     * will not work as users may expect, since the text must be
     * the same for all players.
     *
     * @param   text
     *          Configured belowname text
     */
    public void checkBelowNameText(@NotNull String text) {
        if (!text.contains("%")) return;
        if (text.contains("%animation") || text.contains("%condition")) return;
        startupWarn("Belowname text is set to " + text + ", however, the feature cannot display different text on different players " +
                "due to a minecraft limitation. Placeholders will be parsed for viewing player.");
    }

    /**
     * Sends a console warn that entered skin definition does not match
     * any of the supported patterns.
     *
     * @param   definition
     *          Configured skin definition
     */
    public void invalidLayoutSkinDefinition(@NotNull String definition) {
        startupWarn("Invalid skin definition: \"" + definition + "\". Supported patterns are:",
                "#1 - \"player:<name>\" for skin of player with specified name",
                "#2 - \"mineskin:<id>\" for UUID of chosen skin from mineskin.org",
                "#3 - \"texture:<texture>\" for raw texture string");
    }

    /**
     * Sends a console warn about a fixed line in layout being invalid.
     *
     * @param   layout
     *          Layout name where fixed slot is defined
     * @param   line
     *          Line definition from configuration
     */
    public void invalidFixedSlotDefinition(@NotNull String layout, @NotNull String line) {
        startupWarn("Layout " + layout + " has invalid fixed slot defined as \"" + line + "\". Supported values are " +
                "\"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, TEXT is displayed text and SKIN is skin used for the slot");
    }

    /**
     * Sends a console warn that specified layout direction is not a valid
     * enum value.
     *
     * @param   direction
     *          Configured direction
     */
    public void invalidLayoutDirection(@NotNull String direction) {
        startupWarn("\"" + direction + "\" is not a valid type of layout direction. Valid options are: " + Arrays.deepToString(LayoutManagerImpl.Direction.values()) + ". Using COLUMNS");
    }

    public void invalidSortingTypeElement(@NotNull String element, @NotNull Set<String> validTypes) {
        startupWarn("\"" + element + "\" is not a valid sorting type element. Valid options are: " + validTypes + ".");
    }

    public void invalidSortingPlaceholder(@NotNull String placeholder, @NotNull SortingType type) {
        startupWarn("\"" + placeholder + "\" is not a valid placeholder for " + type.getClass().getSimpleName() + " sorting type");
    }

    public void conditionHasNoConditions(@NotNull String conditionName) {
        startupWarn("Condition \"" + conditionName + "\" is missing \"conditions\" section.");
    }

    public void conditionMissingType(@NotNull String conditionName) {
        startupWarn(String.format("Condition \"%s\" has multiple conditions defined, but is missing \"type\" attribute. Using AND.",
                conditionName));
    }

    public void invalidConditionPattern(@NotNull String conditionName, @NotNull String line) {
        startupWarn("Line \"" + line + "\" in condition " + conditionName + " is not a valid condition pattern.");
    }

    public void invisibleAndUnlimitedNameTagsAreMutuallyExclusive() {
        startupWarn("Unlimited name tag mode is enabled as well as invisible name tags. These 2 options are mutually exclusive.",
                "If you want name tags to be invisible, you don't need unlimited name tag mode at all.",
                "If you want enhanced name tags without limits, making them invisible would defeat the purpose.");
    }

    public void invalidDateFormat(@NotNull String format) {
        startupWarn("Format \"" + format + "\" is not a valid date/time format. Did you try to use color codes?");
    }

    public void bothGlobalPlayerListAndLayoutEnabled() {
        startupWarn("Both global playerlist and layout features are enabled, but layout makes global playerlist redundant.",
                "Layout automatically works with all connected players on the proxy and replaces real player entries with" +
                        " fake players, making global playerlist completely useless.",
                "Disable global playerlist for the same result, but with better performance.");
    }

    public void bothPerWorldPlayerListAndLayoutEnabled() {
        startupWarn("Both per world playerlist and layout features are enabled, but layout makes per world playerlist redundant.",
                "Layout automatically works with all connected players and replaces real player entries with" +
                        " fake players, making per world playerlist completely useless as real players are pushed out of the playerlist.",
                "Disable per world playerlist for the same result, but with better performance.");
    }

    public void checkLayoutMap(@NotNull String layoutName, @NotNull Map<String, Object> map) {
        List<String> expectedKeys = Arrays.asList("condition", "fixed-slots", "groups");
        for (String mapKey : map.keySet()) {
            if (!expectedKeys.contains(mapKey)) {
                startupWarn("Unknown property \"" + mapKey + "\" in layout \"" + layoutName + "\". Valid properties: " + expectedKeys);
            }
        }
    }

    public void checkLayoutGroupMap(@NotNull String layoutName, @NotNull String groupName, @NotNull Map<String, Object> map) {
        List<String> expectedKeys = Arrays.asList("condition", "slots");
        for (String mapKey : map.keySet()) {
            if (!expectedKeys.contains(mapKey)) {
                startupWarn("Unknown property \"" + mapKey + "\" in layout \"" + layoutName + "\"'s group \"" + groupName + "\". Valid properties: " + expectedKeys);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T> T fromMapOrElse(@NotNull Map<String, Object> map, @NotNull String key, @NotNull T defaultValue, @NotNull String warnMessage) {
        if (map.containsKey(key)) {
            return (T) map.get(key);
        } else {
            startupWarn(warnMessage);
            return defaultValue;
        }
    }

    /**
     * Checks bossbar section configuration for: <p>
     * - Unknown keys, to let people know if they made a typo <p>
     * - Missing required properties (text, color, style, progress) and adding them with some default values <p>
     * - Evaluating static values of color, style and progress if they can represent the required data type
     *
     * @param   bossbarSection
     *          Map section of a bossbar in config
     * @param   name
     *          Name of the bossbar defined in config
     */
    public void checkBossBarProperties(Map<String, Object> bossbarSection, String name) {
        // Unknown properties
        List<String> validProperties = Arrays.asList("style", "color", "progress", "text", "announcement-bar", "display-condition");
        for (String mapKey : bossbarSection.keySet()) {
            if (!validProperties.contains(mapKey)) {
                startupWarn("Unknown property \"" + mapKey + "\" in bossbar \"" + name + "\". Valid properties: " + validProperties);
            }
        }
        // Text
        if (!bossbarSection.containsKey("text")) {
            startupWarn("Bossbar \"" + name + "\" is missing \"text\" property.");
            bossbarSection.put("text", "Text is not defined!");
        }
        // Color
        if (bossbarSection.containsKey("color")) {
            String color = bossbarSection.get("color").toString();
            if (!color.contains("%")) {
                try {
                    BarColor.valueOf(color.toUpperCase(Locale.US));
                } catch (IllegalArgumentException e) {
                    startupWarn("Bossbar \"" + name + " has color set to \"" + color + "\", which is not one of the supported colors " +
                            Arrays.toString(BarColor.values()) + " or a placeholder evaluating to one.");
                    bossbarSection.put("color", "PURPLE");
                }
            }
        } else {
            startupWarn("Bossbar \"" + name + "\" is missing \"color\" property.");
            bossbarSection.put("color", "PURPLE");
        }
        // Style
        if (bossbarSection.containsKey("style")) {
            String style = bossbarSection.get("style").toString();
            if (!style.contains("%")) {
                try {
                    BarStyle.valueOf(style.toUpperCase(Locale.US));
                } catch (IllegalArgumentException e) {
                    startupWarn("Bossbar \"" + name + " has style set to \"" + style + "\", which is not one of the supported styles " +
                            Arrays.toString(BarStyle.values()) + " or a placeholder evaluating to one.");
                    bossbarSection.put("style", "PROGRESS");
                }
            }
        } else {
            startupWarn("Bossbar \"" + name + "\" is missing \"style\" property.");
            bossbarSection.put("style", "PROGRESS");
        }
        // Progress
        if (bossbarSection.containsKey("progress")) {
            String progress = bossbarSection.get("progress").toString();
            if (!progress.contains("%")) {
                try {
                    Float.parseFloat(progress);
                } catch (IllegalArgumentException e) {
                    startupWarn("Bossbar \"" + name + " has progress set to \"" + progress + "\", which is not a valid number between 0 and 100 or a placeholder evaluating to one.");
                    bossbarSection.put("progress", "100");
                }
            }
        } else {
            startupWarn("Bossbar \"" + name + "\" is missing \"progress\" property.");
            bossbarSection.put("progress", "100");
        }
    }

    public void teamAntiOverrideDisabled() {
        startupWarn("anti-override for scoreboard-teams is disabled in config. This is usually a mistake. If you notice the" +
                " feature randomly breaking, enable it back.");
    }

    public void tablistAntiOverrideDisabled() {
        startupWarn("anti-override for tablist-name-formatting is disabled in config. This is usually a mistake. If you notice the" +
                " feature randomly breaking, enable it back.");
    }

    public void nonLastNoConditionScoreboard(@NotNull String noConditionScoreboard, @NotNull String nextScoreboard) {
        startupWarn("Scoreboard \"" + noConditionScoreboard + "\" has no display condition set, however, there is" +
                " another scoreboard in the chain (" + nextScoreboard + "). Scoreboards are checked from top to bottom" +
                " until a scoreboard with meeting condition or no condition is found. Because of this, the scoreboard (" +
                nextScoreboard + ") after the no-condition scoreboard (" + noConditionScoreboard + ") will never be displayed. " +
                "Unless this is intentional to externally display the scoreboard (commands, API), this is a mistake.");
    }

    public void layoutBreaksYellowNumber() {
        startupWarn("Layout feature breaks playerlist-objective feature, because it replaces real player with fake slots " +
                "with different usernames for more reliable functionality. Disable playerlist-objective feature, as it will only look bad " +
                "and consume resources.");
    }

    public void invalidScoreboardSection(@NotNull String name) {
        startupWarn("Invalid scoreboard section \"" + name + "\" with no value.");
    }

    public void checkLayoutGroups(@NotNull String layoutName, @NotNull List<GroupPattern> groups) {
        // Checking for duplicated slots
        Map<Integer, String> takenSlots = new HashMap<>();
        for (GroupPattern pattern : groups) {
            for (int slot : pattern.getSlots()) {
                if (takenSlots.containsKey(slot)) {
                    startupWarn("Layout \"" + layoutName + "\"'s player group \"" + pattern.getName() +
                            "\" defines slot " + slot + ", but this slot is already taken by group \"" +
                            takenSlots.get(slot) + "\", which will take priority.");
                } else {
                    takenSlots.put(slot, pattern.getName());
                }
            }
        }

        // Checking for unreachable groups
        String noConditionGroup = null;
        for (GroupPattern pattern : groups) {
            if (noConditionGroup != null) {
                startupWarn("Layout \"" + layoutName + "\"'s player group \"" + pattern.getName() +
                        "\" is unreachable, because it is defined after group \"" + noConditionGroup +
                        "\", which has no condition requirement.");
            } else if (pattern.getCondition() == null) {
                noConditionGroup = pattern.getName();
            }
        }
    }

    public void checkErrorLog() {
        File errorLog = TAB.getInstance().getErrorManager().getErrorLog();
        if (errorLog.length() > TabConstants.MAX_LOG_SIZE) {
            startupWarn("File " + errorLog.getPath() + " has reached its size limit (1MB). No new errors will be logged. " +
                    "Take a look at the existing reported errors, as they may have caused the plugin to not work properly " +
                    "in the past and if not fixed, will most likely cause problems in the future as well.");
        }
    }

    public void incompleteSortingLine(@NotNull String configuredLine) {
        startupWarn("Sorting line \"" + configuredLine + "\" is incomplete.");
    }

    /**
     * Sends a startup warn message into console
     *
     * @param   messages
     *          messages to print into console
     */
    private void startupWarn(@NotNull String... messages) {
        warnCount++;
        for (String message : messages) {
            TAB.getInstance().getPlatform().logWarn(TabComponent.fromColoredText(message));
        }
    }

    /**
     * Prints amount of warns logged into console previously, if more than 0.
     */
    public void printWarnCount() {
        if (warnCount == 0) return;
        TAB.getInstance().getPlatform().logWarn(TabComponent.fromColoredText("Found a total of " + warnCount + " issues."));
        // Reset after printing to prevent count going up on each reload
        warnCount = 0;
    }
}
