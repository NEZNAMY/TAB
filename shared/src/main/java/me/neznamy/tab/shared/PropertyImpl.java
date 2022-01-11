package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;

/**
 * A string with placeholders
 */
public class PropertyImpl implements Property {

	//feature using this property to track used placeholders and receive refresh()
	private final TabFeature listener;
	
	//owner of the property
	private final TabPlayer owner;
	
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
		this.listener = listener;
		this.owner = owner;
		this.source = source;
		this.rawValue = (rawValue == null ? "" : rawValue);
		analyze(this.rawValue);
	}

	/**
	 * Finds all placeholders used in the value
	 * @param value - raw value to be checked
	 */
	private void analyze(String value) {
		List<String> placeholders0 = new ArrayList<>();
		List<String> relPlaceholders0 = new ArrayList<>();
		for (String identifier : TAB.getInstance().getPlaceholderManager().detectPlaceholders(value)) {
			placeholders0.add(identifier);
			if (identifier.startsWith("%rel_")) {
				relPlaceholders0.add(identifier);
			}
		}
		String rawFormattedValue0 = value;
		for (String placeholder : placeholders0) {
			rawFormattedValue0 = rawFormattedValue0.replace(placeholder, "%s");
		}
		if (placeholders0.size() > 0 && rawFormattedValue0.contains("%")) {
			int index = rawFormattedValue0.lastIndexOf('%');
			if (rawFormattedValue0.length() == index+1 || rawFormattedValue0.charAt(index+1) != 's') {
				StringBuilder sb = new StringBuilder(rawFormattedValue0);
				sb.insert(index+1, "%");
				rawFormattedValue0 = sb.toString();
			}
		}
		rawFormattedValue0 = RGBUtils.getInstance().applyFormats(rawFormattedValue0, true);
		rawFormattedValue = EnumChatFormat.color(rawFormattedValue0);
		placeholders = placeholders0.toArray(new String[0]);
		relPlaceholders = relPlaceholders0.toArray(new String[0]);
		if (listener != null) {
			listener.addUsedPlaceholders(placeholders0);
		}
		lastReplacedValue = rawFormattedValue;
		update();
	}

	@Override
	public void setTemporaryValue(String temporaryValue) {
		if (temporaryValue != null) {
			this.temporaryValue = temporaryValue;
			analyze(this.temporaryValue);
		} else {
			this.temporaryValue = null;
			analyze(rawValue);
		}
	}
	
	@Override
	public void changeRawValue(String newValue) {
		if (rawValue.equals(newValue)) return;
		rawValue = newValue;
		if (temporaryValue == null) {
			analyze(rawValue);
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
		if (placeholders.length == 0) return false;
		long time = System.nanoTime();
		String string;
		if ("%s".equals(rawFormattedValue)) {
			string = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholders[0]).set(placeholders[0], owner);
		} else {
			Object[] values = new String[placeholders.length];
			for (int i=0; i<placeholders.length; i++) {
				values[i] = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholders[i]).set(placeholders[i], owner);
			}
			string = String.format(rawFormattedValue, values);
		}
		string = EnumChatFormat.color(string);
		if (!lastReplacedValue.equals(string)) {
			lastReplacedValue = string;
			TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
			return true;
		}
		TAB.getInstance().getCPUManager().addMethodTime("Property#update", System.nanoTime()-time);
		return false;
	}

	@Override
	public String get() {
		return lastReplacedValue;
	}
	
	@Override
	public String getFormat(TabPlayer viewer) {
		String format = lastReplacedValue;
		for (String identifier : relPlaceholders) {
			RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
			format = format.replace(pl.getIdentifier(), viewer == null ? "" : pl.getLastValue(viewer, owner));
		}
		return format;
	}
}