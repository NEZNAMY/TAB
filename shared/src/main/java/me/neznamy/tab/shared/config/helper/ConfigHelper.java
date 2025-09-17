package me.neznamy.tab.shared.config.helper;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.TAB;

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
    public void hint(@NonNull File file, @NonNull String message) {
        TAB.getInstance().getPlatform().logInfo(new TabTextComponent("[Hint] [" + file.getName() + "] " + message, TabTextColor.GOLD));
    }

    /**
     * Prints a configuration hint into console, typically when a redundancy is found.
     *
     * @param   file
     *          File where the redundancy was found
     * @param   message
     *          Hint message to print
     */
    public void hint(@NonNull String file, @NonNull String message) {
        TAB.getInstance().getPlatform().logInfo(new TabTextComponent("[" + file + "] [Hint] " + message, TabTextColor.GOLD));
    }
}
