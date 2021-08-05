package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;

/**
 * A string with placeholders
 */
public class PropertyImpl implements Property {

	//features using this property to track used placeholders and receive refresh()
	private List<TabFeature> listeners = new ArrayList<>();
	
	//owner of the property
	private TabPlayer owner;
	
	//raw value
	private String rawValue;
	
	//raw value using %s ready for string formatter
	private String rawFormattedValue;
	
	//value assigned via API
	private String temporaryValue;
	
	//last known output after placeholder replacement
	private String lastReplacedValue;
	
	//source of property's raw value
	private String source;

	//used placeholders in current raw value
	private String[] placeholders;
	
	//used relational placeholders in current raw value
	private String[] relPlaceholders;

	public PropertyImpl(TabFeature listener, TabPlayer owner, String rawValue) {
		this(listener, owner, rawValue, null);
	}
	
	public PropertyImpl(TabFeature listener, TabPlayer owner, String rawValue, String source) {
		if (listener != null) {
			listeners.add(listener);
		}
		this.owner = owner;
		this.source = source;
		this.rawValue = RGBUtils.getInstance().applyFormats((rawValue == null ? "" : rawValue), true);
		analyze(this.rawValue);
		update();
	}

	/**
	 * Finds all placeholders used in the value
	 * @param value - raw value to be checked
	 */
	private void analyze(String value) {
		List<String> placeholders0 = new ArrayList<>();
		List<String> relPlaceholders0 = new ArrayList<>();
		for (String identifier : TAB.getInstance().getPlaceholderManager().detectPlaceholders(value)) {
			if (identifier.startsWith("%rel_")) {
				relPlaceholders0.add(identifier);
			} else {
				placeholders0.add(identifier);
			}
		}
		String rawFormattedValue0 = value;
		for (String placeholder : placeholders0) {
			rawFormattedValue0 = rawFormattedValue0.replace(placeholder, "%s");
		}
		if (rawFormattedValue0.contains("%")) {
			int index = rawFormattedValue0.lastIndexOf('%');
			if (rawFormattedValue0.length() == index+1 || rawFormattedValue0.charAt(index+1) != 's') {
				StringBuilder sb = new StringBuilder(rawFormattedValue0);
				sb.insert(index+1, "%");
				rawFormattedValue0 = sb.toString();
			}
		}
		placeholders = placeholders0.toArray(new String[0]);
		relPlaceholders = relPlaceholders0.toArray(new String[0]);
		rawFormattedValue0 = EnumChatFormat.color(rawFormattedValue0);
		rawFormattedValue = applyRemoveStrings(rawFormattedValue0); //this should never be needed
		for (TabFeature listener : listeners) {
			listener.addUsedPlaceholders(placeholders0);
			listener.addUsedPlaceholders(relPlaceholders0);
		}
	}
	
	@Override
	public void addListener(TabFeature listener) {
		listeners.add(listener);
		listener.addUsedPlaceholders(Arrays.asList(placeholders));
		listener.addUsedPlaceholders(Arrays.asList(relPlaceholders));
	}
	
	@Override
	public void setTemporaryValue(String temporaryValue) {
		if (temporaryValue != null) {
			this.temporaryValue = RGBUtils.getInstance().applyFormats(temporaryValue, true);
			analyze(this.temporaryValue);
		} else {
			this.temporaryValue = null;
			analyze(rawValue);
		}
		update();
	}
	
	@Override
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = RGBUtils.getInstance().applyFormats(newValue, true);
		if (temporaryValue == null) {
			analyze(rawValue);
			update();
		}
	}
	
	@Override
	public String getCurrentRawValue() {
		return temporaryValue != null ? temporaryValue : rawValue;
	}
	
	@Override
	public String getTemporaryValue() {
		return temporaryValue;
	}
	
	@Override
	public String getOriginalRawValue() {
		return rawValue;
	}
	
	/**
	 * Returns source of this raw value or "API" if source is an API call
	 * @return source of the value
	 */
	public String getSource() {
		return temporaryValue == null ? source : "API";
	}
	
	/**
	 * Changes source value to new one
	 * @param source - new source
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public String updateAndGet() {
		update();
		return get();
	}
	
	@Override
	public boolean update() {
		long time = System.nanoTime();
		String string;
		if (placeholders.length > 0) {
			if (rawFormattedValue.equals("%s")) {
				string = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholders[0]).set(placeholders[0], owner);
			} else {
				String[] values = new String[placeholders.length];
				for (int i=0; i<placeholders.length; i++) {
					values[i] = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholders[i]).set(placeholders[i], owner);
				}
				string = String.format(rawFormattedValue, (Object[]) values);
			}
			string = EnumChatFormat.color(string);
			string = applyRemoveStrings(string);
		} else {
			string = rawFormattedValue;
		}
		if (lastReplacedValue == null || !lastReplacedValue.equals(string)) {
			lastReplacedValue = string;
			TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
			return true;
		}
		TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
		return false;
	}
	
	private String applyRemoveStrings(String text) {
		if (TAB.getInstance().getConfiguration().getRemoveStrings().isEmpty()) return text;
		String reformatted = text;
		for (String removed : TAB.getInstance().getConfiguration().getRemoveStrings()) {
			if (removed.startsWith("CONTAINS:") && reformatted.contains(removed.substring(9))) return "";
			if (removed.startsWith("STARTS:") && reformatted.startsWith(removed.substring(7))) return "";
			if (removed.startsWith("ENDS:") && reformatted.endsWith(removed.substring(5))) return "";
			if (reformatted.contains(removed)) reformatted = reformatted.replace(removed, "");
		}
		return reformatted;
	}
	
	@Override
	public String get() {
		return lastReplacedValue;
	}
	
	@Override
	public String getFormat(TabPlayer viewer) {
		String format = lastReplacedValue;
		for (String identifier : relPlaceholders) {
			RelationalPlaceholder pl = (RelationalPlaceholder) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			format = format.replace(pl.getIdentifier(), viewer == null ? "" : pl.getLastValue(viewer, owner));
		}
		return format;
	}
}