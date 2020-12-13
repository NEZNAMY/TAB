package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * An error assistant to print internal errors into error file and warn user about misconfiguration
 */
public class ErrorManager {

	//line separator constant
	private final String newline = System.getProperty("line.separator");

	//date format used in error messages
	private final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");

	//one time messages already sent into console so they are not sent again
	private Set<String> oneTimeMessages = new HashSet<String>();

	//amount of logged startup warns
	private int startupWarns = 0;

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
		printError(message, t, intoConsoleToo, Configs.errorFile);
	}
	
	/**
	 * Prints an error message and stack trace into errors.txt file
	 * @param message - message to print
	 * @param t - the throwable
	 * @param intoConsoleToo - if the message should be printed into console as well
	 * @param file - file to log error to
	 */
	public void printError(String message, Throwable t, boolean intoConsoleToo, File file) {
		try {
			if (!file.exists()) file.createNewFile();
			if (file.length() < 1000000) { //not going over 1 MB
				BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
				String currentTime = getCurrentTime();
				if (message != null) {
					buf.write(currentTime + "[TAB v" + Shared.pluginVersion + (Premium.is() ? " Premium": "") + "] " + IChatBaseComponent.fromColoredText(message).toRawText() + newline);
					if (Configs.SECRET_debugMode || intoConsoleToo) Shared.platform.sendConsoleMessage("&c[TAB] " + message, true);
				}
				if (t != null) {
					buf.write(currentTime + t.getClass().getName() +": " + t.getMessage() + newline);
					if (Configs.SECRET_debugMode || intoConsoleToo) Shared.platform.sendConsoleMessage("&c" + t.getClass().getName() +": " + t.getMessage(), true);
					for (StackTraceElement ste : t.getStackTrace()) {
						buf.write(currentTime + "       at " + ste.toString() + newline);
						if (Configs.SECRET_debugMode || intoConsoleToo) Shared.platform.sendConsoleMessage("&c       at " + ste.toString(), true);
					}
				}
				buf.close();
			}
		} catch (Throwable ex) {
			Shared.platform.sendConsoleMessage("&c[TAB] An error occurred when printing error message into file", true);
			ex.printStackTrace();
			Shared.platform.sendConsoleMessage("&c[TAB] Original error: " + message, true);
			if (t != null) t.printStackTrace();
		}
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
		Shared.platform.sendConsoleMessage("&c[TAB] " + message, true);
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
				Shared.platform.sendConsoleMessage("&e[TAB] There was 1 startup warning.", true);
			} else {
				Shared.platform.sendConsoleMessage("&e[TAB] There were " + startupWarns + " startup warnings.", true);
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