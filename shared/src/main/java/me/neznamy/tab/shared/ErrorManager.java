package me.neznamy.tab.shared;

import lombok.Getter;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    @Getter private final File errorLog;

    /** anti-override.log file when some plugin or server itself attempts to override the plugin */
    @Getter private final File antiOverrideLog;

    /** placeholder-errors.log file for errors thrown by placeholders */
    private final File placeholderErrorLog;

    /**
     * Constructs new instance.
     *
     * @param   dataFolder
     *          Data folder for error files
     */
    public ErrorManager(@NotNull File dataFolder) {
        errorLog = new File(dataFolder, "errors.log");
        antiOverrideLog = new File(dataFolder, "anti-override.log");
        placeholderErrorLog = new File(dataFolder, "placeholder-errors.log");
    }

    /**
     * Prints error message into errors.log file
     *
     * @param   message
     *          message to print
     */
    public void printError(@Nullable String message) {
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
    public void printError(@Nullable String message, @Nullable Throwable t) {
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
    public void printError(@Nullable String message, @Nullable Throwable t, boolean intoConsoleToo) {
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
    public void printError(@Nullable String message, @Nullable Throwable t, boolean intoConsoleToo, @NotNull File file) {
        List<String> lines = t == null ? Collections.emptyList() : throwableToList(t, false);
        printError(message, lines, intoConsoleToo, file);
    }

    /**
     * Converts throwable into a list of lines.
     *
     * @param   t
     *          Throwable to print
     * @param   nested
     *          Whether this throwable is nested or not
     * @return  List of lines from given throwable
     */
    private List<String> throwableToList(@NotNull Throwable t, boolean nested) {
        List<String> list = new ArrayList<>();
        String causedText = nested ? "Caused by: " : "";
        list.add(causedText + t.getClass().getName() + ": " + t.getMessage());
        for (StackTraceElement ste : t.getStackTrace()) {
            list.add("\tat " + ste.toString());
        }
        if (t.getCause() != null) {
            list.addAll(throwableToList(t.getCause(), true));
        }
        return list;
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
    private synchronized void printError(@Nullable String message, @NotNull List<String> error, boolean intoConsoleToo, @NotNull File file) {
        try {
            if (!file.exists()) Files.createFile(file.toPath());
            try (BufferedWriter buf = new BufferedWriter(new FileWriter(file, true))) {
                if (message != null) {
                    if (file.length() < TabConstants.MAX_LOG_SIZE)
                        buf.write(dateFormat.format(new Date()) + "[TAB v" + TabConstants.PLUGIN_VERSION + "] " + EnumChatFormat.decolor(message) + System.getProperty("line.separator"));
                    if (intoConsoleToo || TAB.getInstance().getConfiguration().isDebugMode())
                        TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent(message));
                }
                for (String line : error) {
                    if (file.length() < TabConstants.MAX_LOG_SIZE)
                        buf.write(dateFormat.format(new Date()) + line + System.getProperty("line.separator"));
                    if (intoConsoleToo || TAB.getInstance().getConfiguration().isDebugMode())
                        TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent(line));
                }
            }
        } catch (IOException ex) {
            TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("An error occurred when printing error message into file"));
            TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent(ex.getClass().getName() + ": " + ex.getMessage()));
            for (StackTraceElement e : ex.getStackTrace()) {
                TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("\t" + e.toString()));
            }
            TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent("Original error: " + message));
            for (String line : error) {
                TAB.getInstance().getPlatform().logWarn(new IChatBaseComponent(line));
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
    public void placeholderError(@Nullable String message, @Nullable Throwable t) {
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
    public void placeholderError(@Nullable String message, @NotNull List<String> t) {
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
    public void criticalError(@Nullable String message, @Nullable Throwable t) {
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
    public int parseInteger(@NotNull String string, int defaultValue) {
        try {
            return (int) Math.round(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}