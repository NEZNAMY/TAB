package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Class for detecting misconfiguration in config files and fix mistakes
 * to avoid headaches when making a configuration mistake.
 */
public class ConfigHelper {

    /** Printer for startup warns */
    private final StartupWarnPrinter startupWarnPrinter = new StartupWarnPrinter();

    /** Printer for runtime errors */
    private final RuntimeErrorPrinter runtimeErrorPrinter = new RuntimeErrorPrinter();

    /**
     * Returns startup warn printer.
     *
     * @return  startup warn printer
     */
    public StartupWarnPrinter startup() {
        return startupWarnPrinter;
    }

    /**
     * Returns runtime error printer.
     *
     * @return  runtime error printer
     */
    public RuntimeErrorPrinter runtime() {
        return runtimeErrorPrinter;
    }

    /**
     * Prints a configuration hint into console, typically when a redundancy is found.
     *
     * @param   file
     *          File where the redundancy was found
     * @param   message
     *          Hint message to print
     */
    public void hint(@NotNull File file, @NotNull String message) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GOLD + "[Hint] [" + file.getName() + "] " + message));
    }

    /**
     * Prints a configuration hint into console, typically when a redundancy is found.
     *
     * @param   file
     *          File where the redundancy was found
     * @param   message
     *          Hint message to print
     */
    public void hint(@NotNull String file, @NotNull String message) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GOLD + "[" + file + "] [Hint] " + message));
    }
}
