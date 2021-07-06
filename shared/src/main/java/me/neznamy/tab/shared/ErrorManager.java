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
import java.util.Locale;
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
	private Set<String> oneTimeMessages = new HashSet<>();

	//amount of logged startup warns
	private int startupWarns = 0;

	//error logs
	private File errorLog;
	private File papiErrorLog;
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
		papiErrorLog = new File(tab.getPlatform().getDataFolder(), "PlaceholderAPI.errors.log");
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
					write(buf, "&c[TAB v" + tab.getPluginVersion() + (tab.isPremium() ? " Premium": "") + "] ", message.replace('\u00a7', '&'), intoConsoleToo);
				}
				if (error != null) {
					write(buf, "&c", error.getClass().getName() + ": " + error.getMessage(), intoConsoleToo);
					for (StackTraceElement ste : error.getStackTrace()) {
						write(buf, "&c       at ", ste.toString(), intoConsoleToo);
					}
				}
			}
		} catch (Exception ex) {
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
		buf.write(getCurrentTime() + removeColors(prefix) + message + System.getProperty("line.separator"));
		if (tab.isDebugMode() || forceConsole) tab.getPlatform().sendConsoleMessage(prefix.replace('&', '\u00a7') + message, false);
	}
	
	/**
	 * Removes all color codes from defined text
	 * @param text - text to remove colors from
	 * @return text without colors
	 */
	private String removeColors(String text) {
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
		} catch (NumberFormatException e) {
			return oneTimeConsoleError(defaultValue, formatNumberError(place, string));
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
			return oneTimeConsoleError(defaultValue, formatNumberError(place, string));
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
			return oneTimeConsoleError(defaultValue, formatNumberError(place, string));
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
		} catch (Exception e) {
			return oneTimeConsoleError(defaultValue, String.format("%s only accepts one of the defined colors! (Attempted to use \"%s\")", place, string));
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
		} catch (Exception e) {
			return oneTimeConsoleError(defaultValue, String.format("%s only accepts one of the defined styles! (Attempted to use \"%s\")", place, string));
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
			startupWarn("Animation \"&e" + name + "&c\" has refresh interval of "+interval+" which is not divisible by 50! &bUsing " + newInterval + ".");
			return newInterval;
		}
		return interval;
	}
	
	/**
	 * Makes interval divisible by 50 and sends error message if it was not already or was 0 or less
	 * @param identifier - placeholder identifier
	 * @param interval - configured interval
	 * @return fixed interval
	 */
	public int fixPlaceholderInterval(String identifier, int interval) {
		if (interval == 0) {
			startupWarn("Placeholder \"&e" + identifier + "&c\" has refresh interval of 0 milliseconds! Is that misconfiguration? &bUsing 100.");
			return 100;
		}
		if (interval < 0) {
			startupWarn("Placeholder \"&e" + identifier + "&c\" has refresh interval of "+interval+". Refresh cannot be negative! &bUsing 100.");
			return 100;
		}
		if (interval % 50 != 0) {
			int newInterval = interval - interval%50;
			if (newInterval == 0) newInterval = 50;
			startupWarn("Placeholder \"&e" + identifier + "&c\" has refresh interval of "+interval+" which is not divisible by 50! &bUsing " + newInterval + ".");
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
			return new SimpleDateFormat(value, Locale.ENGLISH);
		} catch (Exception e) {
			startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
			return new SimpleDateFormat(defaultValue);
		}
	}

	public File getAntiOverrideLog() {
		return antiOverrideLog;
	}

	public File getErrorLog() {
		return errorLog;
	}

	public File getPapiErrorLog() {
		return papiErrorLog;
	}
}