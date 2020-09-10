package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

/**
 * An error assistant to print internal errors into error file and warn user about misconfiguration
 */
public class ErrorManager {

	private final String newline = System.getProperty("line.separator");
	private final SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");
	private final List<String> oneTimeMessages = new ArrayList<String>();
	private int startupWarns = 0;

	public void printError(String message) {
		printError(message, null, false);
	}
	public <T> T printError(T defaultValue, String message) {
		return printError(defaultValue, message, null);
	}
	public <T> T printError(T defaultValue, String message, Throwable t) {
		printError(message, t, false);
		return defaultValue;
	}
	public void printError(String message, Throwable t) {
		printError(message, t, false);
	}
	public void printError(String message, Throwable t, boolean intoConsoleToo) {
		printError(message, t, intoConsoleToo, Configs.errorFile);
	}
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
	public void criticalError(String message, Throwable t) {
		printError(message, t, true);
	}
	public <T> T oneTimeConsoleError(T defaultValue, String message) {
		oneTimeConsoleError(message);
		return defaultValue;
	}
	public void oneTimeConsoleError(String message) {
		if (oneTimeMessages.contains(message)) return;
		oneTimeMessages.add(message);
		printError(message, null, true);
	}
	private String getCurrentTime() {
		return dateformat.format(new Date());
	}

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
		Shared.platform.sendConsoleMessage("&c[TAB] " + message, true);
		startupWarns++;
	}
	public void missingAttribute(String objectType, Object objectName, String attribute) {
		startupWarn(objectType + " \"&e" + objectName + "&c\" is missing \"&e" + attribute + "&c\" attribute!");
	}
	public void printConsoleWarnCount() {
		if (startupWarns > 0) {
			if (startupWarns == 1) {
				Shared.platform.sendConsoleMessage("&e[TAB] There was 1 startup warning.", true);
			} else {
				Shared.platform.sendConsoleMessage("&e[TAB] There were " + startupWarns + " startup warnings.", true);
			}
		}
	}
	
	public SimpleDateFormat createDateFormat(String value, String defaultValue) {
		try {
			return new SimpleDateFormat(value);
		} catch (Exception e) {
			startupWarn("Format \"" + value + "\" is not a valid date/time format. Did you try to use color codes?");
			return new SimpleDateFormat(defaultValue);
		}
	}
}