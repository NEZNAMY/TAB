package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.shared.TAB;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;

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
            TAB.getInstance().getPlatform().logWarn(new SimpleTextComponent(message));
        }
    }

    public void startupWarn(@NotNull File file, @NotNull String message) {
        warnCount++;
        TAB.getInstance().getPlatform().logWarn(new SimpleTextComponent("[" + file.getName() + "] " + message));
    }

    /**
     * Prints amount of warns logged into console previously, if more than 0.
     */
    public void printWarnCount() {
        if (warnCount == 0) return;
        TAB.getInstance().getPlatform().logWarn(new SimpleTextComponent("Found a total of " + warnCount + " issues."));
        // Reset after printing to prevent count going up on each reload
        warnCount = 0;
    }
}
