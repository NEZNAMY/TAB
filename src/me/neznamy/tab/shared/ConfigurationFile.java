package me.neznamy.tab.shared;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.shared.placeholders.Placeholders;

@SuppressWarnings("unchecked")
public class ConfigurationFile{
	
	public static final File dataFolder = new File("plugins" + File.separatorChar + "TAB");
	
	private File file;
	private Yaml yaml;
	private HashMap<String, List<String>> comments;
	private Map<String, Object> values;
	
	public ConfigurationFile(String source, String destination, HashMap<String, List<String>> comments) throws Exception{
		FileInputStream input = null;
		try {
			this.comments = comments;
			dataFolder.mkdirs();
			file = new File(dataFolder, destination);
			if (!file.exists()) Files.copy(getClass().getClassLoader().getResourceAsStream("resources/" + source), file.toPath());
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			input = new FileInputStream(file);
			values = yaml.load(new InputStreamReader(input, Charset.forName("UTF-8")));
			if (values == null) values = new HashMap<String, Object>();
			input.close();
			if (Shared.mainClass.convertConfig(values)) save();
			if (!hasComments()) fixComments();
			detectPlaceholders(values);
		} catch (Exception e) {
			input.close();
			Shared.startupWarn("File " + destination + " has broken formatting.");
			Shared.print('6', "Error message from yaml parser: " + e.getMessage());
			String fix = suggestFix(e);
			if (fix != null) {
				Shared.print('d', "Suggestion: " + fix);
			}
			throw e;
		}
	}
	public ConfigurationFile(String sourceAndDestination, HashMap<String, List<String>> comments) throws Exception{
		this(sourceAndDestination, sourceAndDestination, comments);
	}
	private void detectPlaceholders(Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				for (String placeholder : Placeholders.detectAll((String) value)) {
					if (!Placeholders.usedPlaceholders.contains(placeholder)) Placeholders.usedPlaceholders.add(placeholder);
				}
			}
			if (value instanceof Map) detectPlaceholders((Map<String, Object>) value);
			if (value instanceof List) {
				for (Object line : (List<Object>)value) {
					for (String placeholder : Placeholders.detectAll(line+"")) {
						if (!Placeholders.usedPlaceholders.contains(placeholder)) Placeholders.usedPlaceholders.add(placeholder);
					}
				}
			}
		}
	}
	private String suggestFix(Exception e) {
		try {
			List<String> lines = readFile(file);
			int line1 = Integer.parseInt(e.getMessage().split(", line ")[1].split(",")[0]);
			if (e instanceof ScannerException) {
				if (e.getMessage().contains("\\t(TAB)")) {
					return "Replace \\t (TAB) with 4 spaces on line " + line1 + ".";
				}
				if (e.getMessage().contains("Do not use %")) {
					String text = lines.get(line1-1);
					if (!text.contains("\"") && !text.contains("'")) {
						return "Wrap value in line " + line1 + " into quotes.";
					} else {
						return "One of your lines (from 1 to " + (line1-1) + ") is missing ending ' (or \").";
					}
				}
				if (e.getMessage().contains("expected alphabetic or numeric character")) {
					String quotes = brokenQuotes(lines, 1, line1-1);
					if (quotes != null) return quotes;
					return "Wrap value in line " + line1 + " into quotes.";
				}
				if (e.getMessage().contains("found unexpected end of stream")) {
					String quotes = brokenQuotes(lines, line1, line1);
					if (quotes != null) return quotes;
				}
				if (e.getMessage().contains("mapping values are not allowed here.")) {
					return "Remove the last : from line " + line1;
				}
				if (e.getMessage().contains("could not find expected ':'")) {
					return "Remove line " + line1 + " or add a : followed by a value.";
				}
				if (e.getMessage().contains("found unknown escape character")) {
					return "Remove the \\ from line " + line1;
				}
			}
			if (e instanceof ParserException) {
				int line2 = Integer.parseInt(e.getMessage().split(", line ")[2].split(",")[0]);
				if (e.getMessage().contains("expected <block end>, but found '<block sequence start>'")) {
					if (isIndentWrong(lines.get(line2-1))) {
						if (lines.get(line2-2).endsWith(":")) {
							return "Add one space at the beginning of line " + line2 + ".";
						} else {
							return "Remove one space from the beginning of line " + line2 + ".";
						}
					}
				}
				if (e.getMessage().contains("expected <block end>, but found '-'")) {
					if (lines.get(line2-2).endsWith(":")) {
						return "List starting at line " + line2 + " seems to be starting at line " + line1 + " already. Make sure indenting is correct.";
					} else {
						return "List starting at line " + line2 + " is missing a name.";
					}
				}
				String quotes = brokenQuotes(lines, line1, line2);
				if (quotes != null) return quotes;
			}
			return null;
		} catch (Exception ex) {
			//just making sure
			return null;
		}
	}
	private String brokenQuotes(List<String> lines, int from, int to) {
		for (int line=from; line<=to; line++) {
			String text = lines.get(line-1);
			if (text.indexOf("\"") != -1 && text.indexOf("\"") == text.lastIndexOf("\"") && !text.endsWith("\"")) {
				return "Add \" at the end of line " + line;
			}
			if (text.indexOf("'") != -1 && text.indexOf("'") == text.lastIndexOf("'") && !text.endsWith("'")) {
				return "Add ' at the end of line " + line;
			}
		}
		return null;
	}
	private boolean isIndentWrong(String line) {
		int i = -1;
		while (line.charAt(++i) == ' ');
		return i%2==1;
	}
	public Map<String, Object> getValues(){
		return values;
	}
	public Object get(String path) {
		return get(path, null);
	}
	public Object get(String path, Object defaultValue) {
		try {
			Object value = values;
			for (String tab : path.split("\\.")) {
				value = getIgnoreCase((Map<String, Object>) value, tab);
			}
			if (value == null && defaultValue != null) {
				set(path, defaultValue);
				return defaultValue;
			}
			return value;
		} catch (Throwable e) {
			if (defaultValue != null) set(path, defaultValue);
			return defaultValue;
		}
	}
	private Object getIgnoreCase(Map<String, Object> map, String key) {
		for (String mapkey : map.keySet()) {
			if (mapkey.equalsIgnoreCase(key)) return map.get(mapkey);
		}
		return null;
	}
	public String getString(String path) {
		return getString(path, null);
	}
	public String getString(String path, String defaultValue) {
		Object value = get(path, defaultValue);
		if (value == null) return null;
		return value+"";
	}
	public List<Object> getList(String path) {
		return getList(path, null);
	}
	public List<Object> getList(String path, List<String> defaultValue) {
		return (List<Object>) get(path, defaultValue);
	}
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}
	public List<String> getStringList(String path, List<String> defaultValue) {
		return (List<String>) get(path, defaultValue);
	}
	public int getInt(String path, Object defaultValue) {
		Object result = get(path, defaultValue);
		if (result == null) return 0;
		return Integer.parseInt(result+"");
	}
	public int getInt(String path) {
		return getInt(path, null);
	}
	public boolean getBoolean(String path, Object defaultValue) {
		return Boolean.parseBoolean(get(path, defaultValue)+"");
	}
	public boolean getBoolean(String path) {
		return getBoolean(path, null);
	}
	public double getDouble(String path, double defaultValue) {
		return Double.parseDouble(get(path, defaultValue)+"");
	}
	public void set(String path, Object value) {
		set(values, path, value);
		save();
	}
	private Map<String, Object> set(Map<String, Object> map, String path, Object value) {
		if (path.contains(".")) {
			String keyWord = path.split("\\.")[0];
			Object submap = map.get(keyWord);
			if (submap == null || !(submap instanceof Map)) {
				submap = new HashMap<String, Object>();
			}
			map.put(keyWord, set((Map<String, Object>) submap, path.substring(keyWord.length()+1, path.length()), value));
		} else {
			if (value == null) {
				map.remove(path);
			} else {
				map.put(path, value);
			}
		}
		return map;
	}
	public Map<String, Object> getConfigurationSection(String path) {
		if (path == null || path.length() == 0) return values;
		return ((Map<String, Object>)get(path, null));
	}
	public void save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			yaml.dump(values, writer);
			writer.close();
			fixComments();
		} catch (Throwable e) {
			Shared.errorManager.criticalError("Failed to save yaml file " + file.getPath(), e);
		}
	}
	public boolean hasComments() {
		for (String line : readFile(file)) {
			if (line.startsWith("#")) return true;
		}
		return false;
	}
	public void fixComments() {
		try {
			List<String> fileContent = readFile(file);
			List<String> commented = new ArrayList<String>();
			for (String line : fileContent) {
				for (String commentedSetting : comments.keySet()) {
					if (line.startsWith(commentedSetting)) {
						commented.addAll(comments.get(commentedSetting));
					}
				}
				commented.add(line);
			}
			file.delete();
			file.createNewFile();
			BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
			for (String line : commented) {
				buf.write(line + System.getProperty("line.separator"));
			}
			buf.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to modify file " + file, ex);
		}
	}
	private List<String> readFile(File file) {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				list.add(line);
			}
			br.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to read file " + file, ex);
		}
		return list;
	}
}