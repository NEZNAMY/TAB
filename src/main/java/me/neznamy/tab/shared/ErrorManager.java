package me.neznamy.tab.shared;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

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
	public String suggestYamlFix(Exception e, List<String> fileLines) {
		try {
			int line1 = Integer.parseInt(e.getMessage().split(", line ")[1].split(",")[0]);
			if (e instanceof ScannerException) {
				String text = fileLines.get(line1-1).split("#")[0];
				if (e.getMessage().contains("\\t(TAB)")) {
					return "Replace \\t (TAB) with 4 spaces on line " + line1 + ".";
				}
				if (e.getMessage().contains("Do not use %")) {
					if (!text.contains("\"") && !text.contains("'")) {
						return "Wrap value in line " + line1 + " into quotes.";
					} else {
						return "One of your lines (from 1 to " + (line1-1) + ") is missing ending ' (or \").";
					}
				}
				if (e.getMessage().contains("expected alphabetic or numeric character")) {
					String quotes = brokenQuotes(fileLines, 1, line1-1);
					if (quotes != null) return quotes;
					return "Wrap value in line " + line1 + " into quotes.";
				}
				if (e.getMessage().contains("found unexpected end of stream")) {
					String quotes = brokenQuotes(fileLines, line1, line1);
					if (quotes != null) return quotes;
				}
				if (e.getMessage().contains("mapping values are not allowed here.")) {
					return "Remove the last : from line " + line1 + ".";
				}
				if (e.getMessage().contains("could not find expected ':'")) {
					while (text.startsWith(" ")) text = text.substring(1, text.length());
					if (text.startsWith("-") && !text.startsWith("- ")) {
						return "Add a space after the \"-\" at line " + line1 + ".";
					}
					if (text.contains(":") && !text.contains(": ")) {
						return "Add a space after the \":\" at line " + line1 + ".";
					}
					return "Remove line " + line1 + " or add a : followed by a value.";
				}
				if (e.getMessage().contains("found unknown escape character")) {
					return "Remove the \\ from line " + line1 + ".";
				}
			}
			String indent;
			if ((indent = checkForIndent(fileLines)) != null) {
				return indent;
			}
			if (e instanceof ParserException) {
				int line2 = Integer.parseInt(e.getMessage().split(", line ")[2].split(",")[0]);
				String text2 = fileLines.get(line2-1).split("#")[0];
				if (e.getMessage().contains("expected <block end>, but found '-'")) {
					if (fileLines.get(line2-2).endsWith(":")) {
						return "List starting at line " + line2 + " seems to be starting at line " + line1 + " already. Make sure indenting is correct.";
					} else {
						return "List starting at line " + line2 + " is missing a name.";
					}
				}
				String quotes = brokenQuotes(fileLines, line1, line2);
				if (quotes != null) return quotes;
				if (text2.contains(":") && !text2.contains(": ") && !text2.endsWith(":")) {
					return "Add a space after the \":\" at line " + line2 + ".";
				}
			}
			return null;
		} catch (Exception ex) {
			if (Configs.SECRET_debugMode) {
				Shared.errorManager.criticalError("Failed to check yaml file for assistance", ex);
			}
			return null;
		}
	}
	private String brokenQuotes(List<String> lines, int from, int to) {
		for (int line=from; line<=to; line++) {
			String text = lines.get(line-1);
			if (isComment(text)) continue;
			int simpleQstart = text.indexOf("'");
			int simpleQend = text.lastIndexOf("'");
			int doubleQstart = text.indexOf("\"");
			int doubleQend = text.lastIndexOf("\"");
			if (simpleQstart != -1 && doubleQstart == -1) {
				//text in simple quotes
				if (simpleQstart == simpleQend) {
					return "Add ' at the end of line " + line;
				}
			}
			if (doubleQstart != -1 && simpleQstart == -1) {
				//text in double quotes
				if (doubleQstart == doubleQend) {
					return "Add \" at the end of line " + line;
				}
			}
			if (simpleQstart != -1 && simpleQstart < doubleQstart) {
				//the text is supposed to be in simple quotes
				if (simpleQend < doubleQend && simpleQstart == simpleQend) {
					return "Add ' at the end of line " + line;
				}
			}
			if (doubleQstart != -1 && doubleQstart < simpleQstart) {
				//the text is supposed to be in double quotes
				if (doubleQend < simpleQend && doubleQstart == doubleQend) {
					return "Add \" at the end of line " + line;
				}
			}
			if (text.endsWith("''")) {
				return "Remove one ' from the end of line " + line;
			}
			if (text.endsWith("\"\"")) {
				return "Remove one \" from the end of line " + line;
			}
		}
		return null;
	}
	private String checkForIndent(List<String> lines) {
		int lineId = -1;
		for (String line : lines) {
			lineId++;
			if (line.isEmpty()) continue;
			if (line.startsWith("#")) continue;
			line = line.split("#")[0];
			if (line.length() == 0 || line.replace(" ", "").length() == 0 || isComment(line)) continue;
			int currentLineIndent = getIndentCount(line);
			String prevLine = lineId == 0 ? "" : lines.get(lineId-1);
			int remove = 1;
			while (isComment(prevLine)) {
				int id = lineId-(remove++);
				if (id == -1) {
					prevLine = "";
					break;
				}
				prevLine = lines.get(id);
			}
			prevLine = prevLine.split("#")[0];
			int prevLineIndent = getIndentCount(prevLine);
			if (prevLine.replace(" ", "").endsWith(":")) {
				//expecting 2 more spaces or same or 2k less (k = 1,2,..)
				if (currentLineIndent - prevLineIndent > 2) {
					return "Remove " + (currentLineIndent-prevLineIndent-2) + " space(s) from line " + (lineId+1);
				}
				if (currentLineIndent - prevLineIndent == 1) {
					return "Add 1 space to line " + (lineId+1);
				}
				if (prevLineIndent - currentLineIndent == 1) {
					if (line.replace(" ", "").startsWith("-")) {
						return "Add 1 or 3 spaces to line " +  (lineId+1);
					} else {
						return "Remove 1 space from line " + (lineId+1);
					}
				}
			} else {
				//expecting same indent count or 2k less (k = 1,2,..)
				if (currentLineIndent != prevLineIndent) {
					if (currentLineIndent > prevLineIndent) {
						return "Remove " + (currentLineIndent-prevLineIndent) + " space(s) from line " + (lineId+1);
					}
				}
			}
			if (currentLineIndent%2 == 1) {
				return "Add or remove one space at line " + (lineId+1);
			}
		}
		return null;
	}
	private int getIndentCount(String line) {
		if (isComment(line)) return 0;
		int i = -1;
		while (line.charAt(++i) == ' ');
		return i;
	}
	private boolean isComment(String line) {
		line = line.replace(" ", "");
		return line.startsWith("#") || line.length() == 0;
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