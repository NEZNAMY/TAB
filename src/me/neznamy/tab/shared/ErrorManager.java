package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

public class ErrorManager {

	private static final String newline = System.getProperty("line.separator");
	private SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss - ");
	private List<String> oneTimeMessages = new ArrayList<String>();
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
		try {
			if (!Configs.errorFile.exists()) Configs.errorFile.createNewFile();
			if (Configs.errorFile.length() < 1000000) { //not going over 1 MB
				BufferedWriter buf = new BufferedWriter(new FileWriter(Configs.errorFile, true));
				if (message != null) {
					buf.write(getCurrentTime() + "[TAB v" + Shared.pluginVersion + "] " + message + newline);
					if (Configs.SECRET_debugMode || intoConsoleToo) Shared.mainClass.sendConsoleMessage("&c[TAB] " + message);
				}
				if (t != null) {
					buf.write(getCurrentTime() + t.getClass().getName() +": " + t.getMessage() + newline);
					if (Configs.SECRET_debugMode || intoConsoleToo) Shared.mainClass.sendConsoleMessage("&c" + t.getClass().getName() +": " + t.getMessage());
					for (StackTraceElement ste : t.getStackTrace()) {
						buf.write(getCurrentTime() + "       at " + ste.toString() + newline);
						if (Configs.SECRET_debugMode || intoConsoleToo) Shared.mainClass.sendConsoleMessage("&c       at " + ste.toString());
					}
				}
				buf.close();
			}
		} catch (Throwable ex) {
			Shared.mainClass.sendConsoleMessage("&c[TAB] An error occurred when printing error message into file");
			ex.printStackTrace();
			Shared.mainClass.sendConsoleMessage("&c[TAB] Original error: " + message);
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
		try {
			return Integer.parseInt(string);
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
			interval = 1000;
		}
		if (interval < 0) {
			startupWarn("Animation \"&e" + name + "&c\" has refresh interval of "+interval+". Refresh cannot be negative! &bUsing 1000.");
			interval = 1000;
		}
		return interval;
	}
	public List<String> fixAnimationFrames(String name, List<String> list) {
		if (list == null) {
			startupWarn("Animation \"&e" + name + "&c\" does not have any texts! &bIgnoring.");
			list = Arrays.asList("<Invalid Animation>");
		}
		return list;
	}
	
	public int fixBossBarRefresh(String name, int refresh) {
		if (refresh == 0) {
			startupWarn("Bossbar \"&e" + name + "&c\" has refresh interval of 0 milliseconds! Did you forget to configure it? &bUsing 1000.");
			refresh = 1000;
		}
		if (refresh < 0) {
			startupWarn("Bossbar \"&e" + name + "&c\" has refresh interval of "+refresh+". Refresh cannot be negative! &bUsing 1000.");
			refresh = 1000;
		}
		return refresh;
	}
	
	public void startupWarn(String message) {
		Shared.mainClass.sendConsoleMessage("&c[TAB] " + message);
		startupWarns++;
	}
	public void printConsoleWarnCount() {
		if (startupWarns > 0) {
			if (startupWarns == 1) {
				Shared.mainClass.sendConsoleMessage("&e[TAB] There was 1 startup warning.");
			} else {
				Shared.mainClass.sendConsoleMessage("&e[TAB] There were " + startupWarns + " startup warnings.");
			}
		}
	}
}