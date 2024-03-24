package me.neznamy.tab.shared.config.helper;

/**
 * Class for detecting misconfiguration in config files and fix mistakes
 * to avoid headaches when making a configuration mistake.
 */
public class ConfigHelper {

    /** Printer for startup warns */
    private final StartupWarnPrinter startupWarnPrinter = new StartupWarnPrinter();

    /** Hint detector */
    private final HintPrinter hintPrinter = new HintPrinter();

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
     * Returns hint printer.
     *
     * @return  hint printer
     */
    public HintPrinter hint() {
        return hintPrinter;
    }

    /**
     * Returns runtime error printer.
     *
     * @return  runtime error printer
     */
    public RuntimeErrorPrinter runtime() {
        return runtimeErrorPrinter;
    }
}
