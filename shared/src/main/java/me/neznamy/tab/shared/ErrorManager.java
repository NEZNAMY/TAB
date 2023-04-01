package me.neznamy.tab.shared;

import lombok.Getter;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An error assistant to print internal errors into error file
 * and warn user about misconfiguration
 */

public class ErrorManager {

    /** Date format used in error messages */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");

    /** errors.log file for internal plugin errors */
    private final File errorLog;

    /** anti-override.log file when some plugin or server itself attempts to override the plugin */
    @Getter private final File antiOverrideLog;

    /** placeholder-errors.log file for errors thrown by placeholders */
    private final File placeholderErrorLog;

    public ErrorManager(TAB tab) {
        errorLog = new File(tab.getDataFolder(), "errors.log");
        antiOverrideLog = new File(tab.getDataFolder(), "anti-override.log");
        placeholderErrorLog = new File(tab.getDataFolder(), "placeholder-errors.log");
    }

    /**
     * Prints error message into errors.log file
     *
     * @param   message
     *          message to print
     */
    public void printError(String message) {
        printError(message, null, false);
    }

    /**
     * Prints error message and stack trace into errors.log file
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown error
     */
    public void printError(String message, Throwable t) {
        printError(message, t, false);
    }

    /**
     * Prints error message and stack trace into errors.log file
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown error
     * @param   intoConsoleToo
     *          if the message should be printed into console as well or not
     */
    public void printError(String message, Throwable t, boolean intoConsoleToo) {
        printError(message, t, intoConsoleToo, errorLog);
    }

    /**
     * Prints error message and stack trace into specified file
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown error
     * @param   intoConsoleToo
     *          if the message should be printed into console as well or not
     * @param   file
     *          file to print error to
     */
    public void printError(String message, Throwable t, boolean intoConsoleToo, File file) {
        Throwable error = t;
        if (error instanceof InvocationTargetException) {
            error = error.getCause();
        }
        List<String> lines = new ArrayList<>();
        if (error != null) {
            lines.add(error.getClass().getName() + ": " + error.getMessage());
            for (StackTraceElement ste : error.getStackTrace()) {
                lines.add("\tat " + ste.toString());
            }
        }
        printError(message, lines, intoConsoleToo, file);
    }

    /**
     * Prints error message and stack trace into specified file
     *
     * @param   message
     *          message to print
     * @param   error
     *          thrown error
     * @param   intoConsoleToo
     *          if the message should be printed into console as well or not
     * @param   file
     *          file to print error to
     */
    private synchronized void printError(String message, List<String> error, boolean intoConsoleToo, File file) {
        try {
            if (!file.exists()) Files.createFile(file.toPath());
            try (BufferedWriter buf = new BufferedWriter(new FileWriter(file, true))) {
                if (message != null) {
                    if (file.length() < 1000000)
                        buf.write(dateFormat.format(new Date()) + IChatBaseComponent.fromColoredText("&c[TAB v" + TabConstants.PLUGIN_VERSION + "] ").toRawText() + EnumChatFormat.decolor(message) + System.getProperty("line.separator"));
                    if (intoConsoleToo || TAB.getInstance().getConfiguration().isDebugMode())
                        TAB.getInstance().sendConsoleMessage(EnumChatFormat.color("&c[TAB v" + TabConstants.PLUGIN_VERSION + "] ") + EnumChatFormat.decolor(message), false);
                }
                for (String line : error) {
                    if (file.length() < 1000000)
                        buf.write(dateFormat.format(new Date()) + IChatBaseComponent.fromColoredText("&c").toRawText() + line + System.getProperty("line.separator"));
                    if (intoConsoleToo || TAB.getInstance().getConfiguration().isDebugMode())
                        TAB.getInstance().sendConsoleMessage(EnumChatFormat.color("&c") + line, false);
                }
            }
        } catch (IOException ex) {
            TAB.getInstance().sendConsoleMessage("&cAn error occurred when printing error message into file", true);
            TAB.getInstance().sendConsoleMessage(ex.getClass().getName() + ": " + ex.getMessage(), true);
            for (StackTraceElement e : ex.getStackTrace()) {
                TAB.getInstance().sendConsoleMessage("\t" + e.toString(), true);
            }
            TAB.getInstance().sendConsoleMessage("&cOriginal error: " + message, true);
            for (String line : error) {
                TAB.getInstance().sendConsoleMessage(line, true);
            }
        }
    }

    /**
     * Prints error message thrown by placeholder and stack trace into placeholder-errors.log file
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown error
     */
    public void placeholderError(String message, Throwable t) {
        printError(message, t, false, placeholderErrorLog);
    }

    /**
     * Prints error message thrown by placeholder and stack trace into placeholder-errors.log file
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown stack trace elements
     */
    public void placeholderError(String message, List<String> t) {
        printError(message, t, false, placeholderErrorLog);
    }

    /**
     * Prints error message and stack trace into errors.log file as well as the console
     *
     * @param   message
     *          message to print
     * @param   t
     *          thrown error
     */
    public void criticalError(String message, Throwable t) {
        printError(message, t, true);
    }

    /**
     * Parses integer in given string and returns it.
     * Returns second argument if string is not valid.
     *
     * @param   string
     *          string to parse
     * @param   defaultValue
     *          value to return if string is not valid
     * @return  parsed integer or {@code defaultValue} if input is invalid
     */
    public int parseInteger(String string, int defaultValue) {
        try {
            return (int) Math.round(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses float in given string and returns it.
     * Returns second argument if string is not valid.
     *
     * @param   string
     *          string to parse
     * @param   defaultValue
     *          value to return if string is not valid
     * @return  parsed float or {@code defaultValue} if input is invalid
     */
    public float parseFloat(String string, float defaultValue) {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses double in given string and returns it.
     * Returns second argument if string is not valid.
     *
     * @param   string
     *          string to parse
     * @param   defaultValue
     *          value to return if string is not valid
     * @return  parsed float or {@code defaultValue} if input is invalid
     */
    public double parseDouble(String string, double defaultValue) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}