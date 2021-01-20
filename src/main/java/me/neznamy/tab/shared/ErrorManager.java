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

/**
 * An error assistant to print internal errors into error file and warn user about misconfiguration
 */
public class ErrorManager {

	//date format used in error messages
	private final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");

	//one time messages already sent into console so they are not sent again
	private Set<String> oneTimeMessages = new HashSet<String>();

	//amount of logged startup warns
	private int startupWarns = 0;

	//error logs
	public File errorLog;
	public File papiErrorLog;
	public File antiOverrideLog;
	
	//plugin instance
	private TAB tab;
	
	public ErrorManager(TAB tab) {
		this.tab = tab;
		errorLog = new File(tab.getPlatform().getDataFolder(), "errors.log");
		papiErrorLog = new File(tab.getPlatform().getDataFolder(), "PlaceholderAPI.errors.log");
		antiOverrideLog = new File(tab.getPlatform().getDataFolder(), "anti-override.log");
		if (errorLog.exists() && errorLog.length() > 10) {
			startupWarn("File &e" + errorLog.getPath() + "&c exists and is not empty. Take a look at the error messages and try to resolve them. After you do, delete the file.");
		}
	}

	/**
	 * Prints an error message into errors.txt file
	 * @param message - message to print
	 */
	public void printError(String message) {
		printError(message, null, false);
	}

	/**
	 * Prints an error message into errors.txt file and returns first argument
	 * @param defaultValue - value to return
	 * @param message - message to print
	 * @return first argument
	 */
	public <T> T printError(T defaultValue, String message) {
		return printError(defaultValue, message, null);
	}

	/**
	 * Prints an error message and stack trace into errors.txt file and returns first argument
	 * @param defaultValue - value to return
	 * @param message - message to print
	 * @param t - the throwable
	 * @return first argument
	 */
	public <T> T printError(T defaultValue, String message, Throwable t) {
		printError(message, t, false);
		return defaultValue;
	}

	/**
	 * Prints an error message and stack trace into errors.txt file
	 * @param message - message to print
	 * @param t - the throwable
	 */
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
		printError(message, t, intoConsoleToo, errorLog);
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
			if (!file.exists()) file.createNewFile();
			if (file.length() > 1000000) return; //not going over 1 MB
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
			if (message != null) {
				write(buf, "&c[TAB v" + tab.getPluginVersion() + (tab.isPremium() ? " Premium": "") + "] " + message, intoConsoleToo);
			}
			if (error != null) {
				write(buf, "&c" + error.getClass().getName() + ": " + error.getMessage(), intoConsoleToo);
				for (StackTraceElement ste : error.getStackTrace()) {
					write(buf, "&c       at " + ste.toString(), intoConsoleToo);
				}
			}
			buf.close();
		} catch (Throwable ex) {
			tab.getPlatform().sendConsoleMessage("&c[TAB] An error occurred when printing error message into file", true);
			ex.printStackTrace();
			tab.getPlatform().sendConsoleMessage("&c[TAB] Original error: " + message, true);
			if (error != null) error.printStackTrace();
		}
	}

	/**
	 * Writes message into buffer and console if set
	 * @param buf - buffered write to write message to
	 * @param message - message to write
	 * @param forceConsole - send into console even without debug mode
	 * @throws IOException - if IO writer operation fails
	 */
	private void write(BufferedWriter buf, String message, boolean forceConsole) throws IOException {
		buf.write(getCurrentTime() + removeColors(message) + System.getProperty("line.separator"));
		if (tab.debugMode || forceConsole) tab.getPlatform().sendConsoleMessage(message, true);
	}
	
	private static String removeColors(String text) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c == '&' || c == '\u00a7') {
				i++;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Prints an error message and stack trace into errors.txt file and console
	 * @param message - message to print
	 * @param t - the throwable
	 */
	public void criticalError(String message, Throwable t) {
		printError(message, t, true);
	}

	/**
	 * Prints an error message and stack trace into errors.txt file and console
	 * @param message - message to print
	 * @param defaultValue - value to return
	 * @return first argument
	 */
	public <T> T oneTimeConsoleError(T defaultValue, String message) {
		oneTimeConsoleError(message);
		return defaultValue;
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
		} catch (Throwable e) {
			if (string.contains("%")) {
				return oneTimeConsoleError(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return oneTimeConsoleError(defaultValue, place + " only accepts numeric values! (Attempted to use \"" + string + "\")");
			}
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
		} catch (Throwable e) {
			if (string.contains("%")) {
				return oneTimeConsoleError(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return oneTimeConsoleError(defaultValue, place + " only accepts numeric values! (Attempted to use \"" + string + "\")");
			}
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
		try {
			return Double.parseDouble(string);
		} catch (Throwable e) {
			if (string.contains("%")) {
				return oneTimeConsoleError(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return oneTimeConsoleError(defaultValue, place + " only accepts numeric values! (Attempted to use \"" + string + "\")");
			}
		}
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
		} catch (Throwable e) {
			if (string.contains("%")) {
				return oneTimeConsoleError(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return oneTimeConsoleError(defaultValue, place + " only accepts one of the defined colors! (Attempted to use \"" + string + "\")");
			}
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
		} catch (Throwable e) {
			if (string.contains("%")) {
				return oneTimeConsoleError(defaultValue, "Value \"" + string + "\" used in " + place + " still has unparsed placeholders! Did you forget to download an expansion ?");
			} else {
				return oneTimeConsoleError(defaultValue, place + " only accepts one of the defined styles! (Attempted to use \"" + string + "\")");
			}
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
			startupWarn("Animation \"&e" + name + "&c\" has refresh interval of 0 milliseconds! Did you forget to configure it? &bUsing 1000.");
			return 1000;
		}
		if (interval < 0) {
			startupWarn("Animation \"&e" + name + "&c\" has refresh interval of "+interval+". Refresh cannot be negative! &bUsing 1000.");
			return 1000;
		}
		if (interval % 50 != 0) {
			int newInterval = interval - interval%50;
			if (newInterval == 0) newInterval = 50;
			startupWarn("Animation \"&e" + name + "&c\" has refresh interval of "+interval+" which is not divisible by 50! Using " + newInterval + ".");
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

	/**
	 * Sends a startup warn message into console
	 * @param message - message to send
	 */
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

	/**
	 * Evaluates inserted date format, returns default one and console error message if not valid
	 * @param value - date format to evaluate
	 * @param defaultValue - value to use if not valid
	 * @return evaluated date format
	 */
	public SimpleDateFormat createDateFormat(String value, String defaultValue) {
		try {
			return new SimpleDateFormat(value);
		} catch (Exception e) {
			startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
			return new SimpleDateFormat(defaultValue);
		}
	}
}