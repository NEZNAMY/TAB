package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * An error assistant to print internal errors into error file and warn user about misconfiguration
 */
public class ErrorManager {

	//date format used in error messages
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");

	//error logs
	private final File errorLog;
	private final File antiOverrideLog;
	private final File placeholderErrorLog;

	/**
	 * Constructs new instance
	 */
	public ErrorManager() {
		errorLog = new File(TAB.getInstance().getPlatform().getDataFolder(), "errors.log");
		antiOverrideLog = new File(TAB.getInstance().getPlatform().getDataFolder(), "anti-override.log");
		placeholderErrorLog = new File(TAB.getInstance().getPlatform().getDataFolder(), "placeholder-errors.log");
		if (getErrorLog().exists() && getErrorLog().length() > 10) {
			startupWarn("File &e" + getErrorLog().getPath() + "&c exists and is not empty. Take a look at the error messages and try to resolve them. After you do, delete the file.");
		}
	}

	/**
	 * Prints an error message into errors.txt file
	 * @param message - message to print
	 */
	public void printError(String message) {
		printError(message, null, false);
	}


	public void printError(String message, Throwable t) {
		printError(message, t, false);
	}

	/**
	 * Prints an error message and stack trace into errors.txt file
	 * @param message - message to print
	 * @param t - the throwable
	 * @param intoConsoleToo - if the message should be printed into console as well
	 */
	public void printError(String message, Throwable t, boolean intoConsoleToo) {
		printError(message, t, intoConsoleToo, getErrorLog());
	}

	/**
	 * Prints an error message and stack trace into errors.txt file
	 * @param message - message to print
	 * @param t - the throwable
	 * @param intoConsoleToo - if the message should be printed into console as well
	 * @param file - file to log error to
	 */
	public void printError(String message, Throwable t, boolean intoConsoleToo, File file) {
		Throwable error = t;
		if (error instanceof InvocationTargetException) {
			error = error.getCause();
		}
		try {
			if (!createFile(file)) return;
			if (file.length() > 1000000) return; //not going over 1 MB
			try (BufferedWriter buf = new BufferedWriter(new FileWriter(file, true))){
				if (message != null) {
					write(buf, "&c[TAB v" + TAB.PLUGIN_VERSION + "] ", EnumChatFormat.decolor(message), intoConsoleToo);
				}
				if (error != null) {
					write(buf, "&c", error.getClass().getName() + ": " + error.getMessage(), intoConsoleToo);
					for (StackTraceElement ste : error.getStackTrace()) {
						write(buf, "&c       at ", ste.toString(), intoConsoleToo);
					}
				}
			}
		} catch (IOException ex) {
			TAB.getInstance().getPlatform().sendConsoleMessage("&c[TAB] An error occurred when printing error message into file", true);
			TAB.getInstance().getPlatform().sendConsoleMessage(ex.getClass().getName() + ": " + ex.getMessage(), true);
			for (StackTraceElement e : ex.getStackTrace()) {
				TAB.getInstance().getPlatform().sendConsoleMessage("\t" + e.toString(), true);
			}
			TAB.getInstance().getPlatform().sendConsoleMessage("&c[TAB] Original error: " + message, true);
			if (error != null) {
				TAB.getInstance().getPlatform().sendConsoleMessage(error.getClass().getName() + ": " + error.getMessage(), true);
				for (StackTraceElement e : error.getStackTrace()) {
					TAB.getInstance().getPlatform().sendConsoleMessage("\t" + e.toString(), true);
				}
			}
		}
	}
	
	public void placeholderError(String message, Throwable t) {
		printError(message, t, false, placeholderErrorLog);
	}

	/**
	 * Creates the file if it does not exist. Returns true if file already existed or creation was successful
	 * or false if file does not exist and creation failed due to an exception which is then printed into console
	 * @param file - file to create
	 * @return true if file already exists / was successfully created, false if creation failed due to an IOException
	 */
	private boolean createFile(File file) {
		if (file.exists()) return true;
		try {
			return file.createNewFile();
		} catch (IOException e) {
			TAB.getInstance().getPlatform().sendConsoleMessage("&c[TAB] Failed to create file " + file.getPath() + ": " + e.getMessage(), true);
			return false;
		}
	}

	/**
	 * Writes message into buffer and console if set
	 * @param buf - buffered write to write message to
	 * @param message - message to write
	 * @param forceConsole - send into console even without debug mode
	 * @throws IOException - if IO writer operation fails
	 */
	private void write(BufferedWriter buf, String prefix, String message, boolean forceConsole) throws IOException {
		buf.write(dateFormat.format(new Date()) + IChatBaseComponent.fromColoredText(prefix).toRawText() + message + System.getProperty("line.separator"));
		if (TAB.getInstance().isDebugMode() || forceConsole) TAB.getInstance().getPlatform().sendConsoleMessage(EnumChatFormat.color(prefix) + message, false);
	}

	public void criticalError(String message, Throwable t) {
		printError(message, t, true);
	}

	/**
	 * Parses integer in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @return parsed integer
	 */
	public int parseInteger(String string, int defaultValue) {
		try {
			return (int) Math.round(Double.parseDouble(string));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}


	/**
	 * Parses float in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @return parsed float
	 */
	public float parseFloat(String string, float defaultValue) {
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Parses double in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @return parsed double
	 */
	public double parseDouble(String string, double defaultValue) {
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Parses bar color in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @return parsed bar color
	 */
	public BarColor parseColor(String string, BarColor defaultValue) {
		try {
			return BarColor.valueOf(string);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	/**
	 * Parses bar style in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @return parsed bar style
	 */
	public BarStyle parseStyle(String string, BarStyle defaultValue) {
		try {
			return BarStyle.valueOf(string);
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	/**
	 * Makes interval divisible by 50 and sends error message if it was not already or was 0 or less
	 * @param name - name of animation used in error message
	 * @param interval - configured interval
	 * @return fixed interval
	 */
	public int fixAnimationInterval(String name, int interval) {
		if (interval == 0) {
			startupWarn(String.format("Animation \"&e%s&c\" has refresh interval of 0 milliseconds! Did you forget to configure it? &bUsing 1000.", name));
			return 1000;
		}
		if (interval < 0) {
			startupWarn(String.format("Animation \"&e%s&c\" has refresh interval of %s. Refresh cannot be negative! &bUsing 1000.", name, interval));
			return 1000;
		}
		if (interval % 50 != 0) {
			int newInterval = interval - interval%50;
			if (newInterval == 0) newInterval = 50;
			startupWarn(String.format("Animation \"&e%s&c\" has refresh interval of %s which is not divisible by 50! &bUsing %s.", name, interval, newInterval));
			return newInterval;
		}
		return interval;
	}

	/**
	 * Returns the list if not null, empty list and error message if null
	 * @param name - name of animation used in error message
	 * @param list - list of animation frames
	 * @return the list if not null, empty list otherwise
	 */
	public List<String> fixAnimationFrames(String name, List<String> list) {
		if (list == null) {
			startupWarn("Animation \"&e" + name + "&c\" does not have any texts! &bIgnoring.");
			return Collections.singletonList("<Invalid Animation>");
		}
		return list;
	}

	public void startupWarn(String message) {
		TAB.getInstance().getPlatform().sendConsoleMessage("&c[TAB] " + message, true);
	}

	/**
	 * Sends a startup warn about missing object parameter
	 * @param objectType - object type missing parameter
	 * @param objectName - name of object
	 * @param attribute - missing parameter
	 */
	public void missingAttribute(String objectType, Object objectName, String attribute) {
		startupWarn(objectType + " \"&e" + objectName + "&c\" is missing \"&e" + attribute + "&c\" attribute!");
	}

	public File getAntiOverrideLog() {
		return antiOverrideLog;
	}

	public File getErrorLog() {
		return errorLog;
	}
}