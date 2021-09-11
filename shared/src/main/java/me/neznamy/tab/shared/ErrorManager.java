package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * An error assistant to print internal errors into error file and warn user about misconfiguration
 */
public class ErrorManager {

	//date format used in error messages
	private final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");

	//one time messages already sent into console so they are not sent again
	private Set<String> oneTimeMessages = new HashSet<>();

	//amount of logged startup warns
	private int startupWarns = 0;

	//error logs
	private File errorLog;
	private File antiOverrideLog;

	//plugin instance
	private TAB tab;

	/**
	 * Constructs new instance
	 * @param tab - tab instance
	 */
	public ErrorManager(TAB tab) {
		this.tab = tab;
		errorLog = new File(tab.getPlatform().getDataFolder(), "errors.log");
		antiOverrideLog = new File(tab.getPlatform().getDataFolder(), "anti-override.log");
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
			tab.getPlatform().sendConsoleMessage("&c[TAB] An error occurred when printing error message into file", true);
			tab.getPlatform().sendConsoleMessage(ex.getClass().getName() + ": " + ex.getMessage(), true);
			for (StackTraceElement e : ex.getStackTrace()) {
				tab.getPlatform().sendConsoleMessage("\t" + e.toString(), true);
			}
			tab.getPlatform().sendConsoleMessage("&c[TAB] Original error: " + message, true);
			if (error != null) {
				tab.getPlatform().sendConsoleMessage(error.getClass().getName() + ": " + error.getMessage(), true);
				for (StackTraceElement e : error.getStackTrace()) {
					tab.getPlatform().sendConsoleMessage("\t" + e.toString(), true);
				}
			}
		}
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
			tab.getPlatform().sendConsoleMessage("&c[TAB] Failed to create file " + file.getPath() + ": " + e.getMessage(), true);
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
		buf.write(getCurrentTime() + IChatBaseComponent.fromColoredText(prefix).toRawText() + message + System.getProperty("line.separator"));
		if (tab.isDebugMode() || forceConsole) tab.getPlatform().sendConsoleMessage(EnumChatFormat.color(prefix) + message, false);
	}

	public void criticalError(String message, Throwable t) {
		printError(message, t, true);
	}

	/**
	 * Sends message into console once
	 * @param message - message to send
	 */
	public void oneTimeConsoleError(String message) {
		if (oneTimeMessages.contains(message)) return;
		oneTimeMessages.add(message);
		printError(message, null, true);
	}

	/**
	 * Returns current formatted time
	 * @return current formatted time
	 */
	private String getCurrentTime() {
		return dateformat.format(new Date());
	}

	/**
	 * Parses integer in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed integer
	 */
	public int parseInteger(String string, int defaultValue, String place) {
		if (string == null || string.length() == 0) return 0; //preventing error message on bungee with papi placeholders due to them not being initialized yet
		try {
			return (int) Math.round(Double.parseDouble(string));
		} catch (NumberFormatException e) {
			oneTimeConsoleError(formatNumberError(place, string));
			return defaultValue;
		}
	}


	/**
	 * Parses float in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed float
	 */
	public float parseFloat(String string, float defaultValue, String place) {
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			oneTimeConsoleError(formatNumberError(place, string));
			return defaultValue;
		}
	}

	/**
	 * Parses double in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed double
	 */
	public double parseDouble(String string, double defaultValue, String place) {
		if (string == null || string.length() == 0) return 0; //preventing error message on bungee with papi placeholders due to them not being initialized yet
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			oneTimeConsoleError(formatNumberError(place, string));
			return defaultValue;
		}
	}

	private String formatNumberError(String place, String value) {
		return String.format("%s only accepts numeric values! (Attempted to use \"%s\")", place, value);
	}

	/**
	 * Parses bar color in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed bar color
	 */
	public BarColor parseColor(String string, BarColor defaultValue, String place) {
		try {
			return BarColor.valueOf(string);
		} catch (IllegalArgumentException e) {
			oneTimeConsoleError(String.format("%s only accepts one of the defined colors! (Attempted to use \"%s\")", place, string));
			return defaultValue;
		}
	}

	/**
	 * Parses bar style in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed bar style
	 */
	public BarStyle parseStyle(String string, BarStyle defaultValue, String place) {
		try {
			return BarStyle.valueOf(string);
		} catch (IllegalArgumentException e) {
			oneTimeConsoleError(String.format("%s only accepts one of the defined styles! (Attempted to use \"%s\")", place, string));
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
			return Arrays.asList("<Invalid Animation>");
		}
		return list;
	}

	public void startupWarn(String message) {
		if (oneTimeMessages.contains(message)) return;
		oneTimeMessages.add(message);
		tab.getPlatform().sendConsoleMessage("&c[TAB] " + message, true);
		startupWarns++;
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

	/**
	 * Prints amount of startup warns into console if more than 0
	 */
	public void printConsoleWarnCount() {
		if (startupWarns > 0) {
			if (startupWarns == 1) {
				tab.getPlatform().sendConsoleMessage("&e[TAB] There was 1 startup warning.", true);
			} else {
				tab.getPlatform().sendConsoleMessage("&e[TAB] There were " + startupWarns + " startup warnings.", true);
			}
		}
	}

	public File getAntiOverrideLog() {
		return antiOverrideLog;
	}

	public File getErrorLog() {
		return errorLog;
	}
}