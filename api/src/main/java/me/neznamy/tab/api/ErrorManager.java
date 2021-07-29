package me.neznamy.tab.api;

public interface ErrorManager {

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
}