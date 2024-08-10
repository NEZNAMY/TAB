package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import org.jetbrains.annotations.NotNull;

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

    public void invalidSortingTypeElement(@NotNull String element, @NotNull Set<String> validTypes) {
        startupWarn("\"" + element + "\" is not a valid sorting type element. Valid options are: " + validTypes + ".");
    }

    public void invalidSortingPlaceholder(@NotNull String placeholder, @NotNull SortingType type) {
        startupWarn("\"" + placeholder + "\" is not a valid placeholder for " + type.getClass().getSimpleName() + " sorting type");
    }

    public void invalidConditionPattern(@NotNull String conditionName, @NotNull String line) {
        startupWarn("Line \"" + line + "\" in condition " + conditionName + " is not a valid condition pattern.");
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

    public void layoutBreaksYellowNumber() {
        startupWarn("Layout feature breaks playerlist-objective feature, because it replaces real player with fake slots " +
                "with different usernames for more reliable functionality. Disable playerlist-objective feature, as it will only look bad " +
                "and consume resources.");
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
    public void startupWarn(@NotNull String... messages) {
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
