package me.neznamy.tab.api;

import java.io.File;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;

public interface ErrorManager {

	/**
	 * Prints an error message into errors.txt file
	 * @param message - message to print
	 */
	public void printError(String message);
	
	/**
	 * Prints an error message and stack trace into errors.log file
	 * @param message - message to print
	 * @param t - the throwable
	 */
	public void printError(String message, Throwable t);

	/**
	 * Prints an error message and stack trace into errors.log file and console
	 * @param message - message to print
	 * @param t - the throwable
	 */
	public void criticalError(String message, Throwable t);

	/**
	 * Sends a startup warn message into console
	 * @param message - message to send
	 */
	public void startupWarn(String message);
	
	/**
	 * Parses integer in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed integer
	 */
	public int parseInteger(String string, int defaultValue, String place);

	/**
	 * Parses float in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed float
	 */
	public float parseFloat(String string, float defaultValue, String place);

	/**
	 * Parses double in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed double
	 */
	public double parseDouble(String string, double defaultValue, String place);
	
	/**
	 * Parses bar color in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed bar color
	 */
	public BarColor parseColor(String string, BarColor defaultValue, String place);

	/**
	 * Parses bar style in given string, returns second argument if string is not valid
	 * @param string - string to parse
	 * @param defaultValue - value to return if string is not valid
	 * @param place - used in error message
	 * @return parsed bar style
	 */
	public BarStyle parseStyle(String string, BarStyle defaultValue, String place);

	/**
	 * Sends message into console once
	 * @param message - message to send
	 */
	public void oneTimeConsoleError(String message);

	public File getAntiOverrideLog();

	public File getErrorLog();

	/**
	 * Sends a startup warn about missing object parameter
	 * @param objectType - object type missing parameter
	 * @param objectName - name of object
	 * @param attribute - missing parameter
	 */
	public void missingAttribute(String objectType, Object objectName, String attribute);

	/**
	 * Prints an error message and stack trace into errors.txt file
	 * @param message - message to print
	 * @param t - the throwable
	 * @param intoConsoleToo - if the message should be printed into console as well
	 * @param file - file to log error to
	 */
	public void printError(String message, Throwable t, boolean intoConsoleToo, File file);
}