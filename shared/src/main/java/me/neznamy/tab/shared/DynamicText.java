package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;

/**
 * A dynamic text with placeholder support. If any placeholder
 * used in this text changes value, feature defining this text
 * will receive refresh call letting it know and allowing to get new value.
 */
public class DynamicText implements Property {

    /** Internal identifier for this text for PlaceholderAPI expansion, null if it should not be exposed */
    private final String name;

    /**
     * Feature defining this text, which will receive refresh function
     * if any of placeholders used in it change value.
     */
    private final Refreshable listener;
    
    /** Player this text belongs to */
    private final TabPlayer owner;
    
    /** Raw value as defined in configuration */
    @Getter private String originalRawValue;

    /** Raw value assigned via API, null if not set */
    @Getter private String temporaryValue;

    /**
     * Raw value using %s for each placeholder ready to be inserted
     * into String formatter, which results in about 5x lower
     * memory allocations as well as better performance.
     */
    private String rawFormattedValue;

    /** Last known value after parsing non-relational placeholders */
    private String lastReplacedValue;
    
    /** Source defining value of the text, displayed in debug command */
    private String source;

    /**
     * All placeholders used in the text in the same order they are used,
     * it may contain duplicates if placeholder is used more than once.
     * Contains relational placeholders as well, which will get formatted
     * to their identifier.
     */
    private String[] placeholders;
    
    /** Relational placeholders in the text in the same order they are used */
    private String[] relPlaceholders;

    /**
     * Constructs new instance with given parameters and prepares
     * the formatter for use by detecting placeholders and reformatting the text.
     *
     * @param   listener
     *          Feature which should receive refresh method if placeholder changes value
     * @param   owner
     *          Player this text belongs to
     * @param   rawValue
     *          Raw value using raw placeholder identifiers
     * @param   source
     *          Source of the text used in debug command
     */
    public DynamicText(String name, Refreshable listener, TabPlayer owner, String rawValue, String source) {
        this.name = name;
        this.listener = listener;
        this.owner = owner;
        this.source = source;
        this.originalRawValue = (rawValue == null ? "" : rawValue);
        analyze(this.originalRawValue);
    }

    /**
     * Finds all placeholders used in the value and prepares it for
     * String formatter using %s for each placeholder.
     *
     * @param   value
     *          raw value to analyze
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
        rawFormattedValue0 = RGBUtils.getInstance().applyCleanGradients(rawFormattedValue0);
        rawFormattedValue = EnumChatFormat.color(rawFormattedValue0);
        placeholders = placeholders0.toArray(new String[0]);
        relPlaceholders = relPlaceholders0.toArray(new String[0]);
        if (listener != null) {
            listener.addUsedPlaceholders(placeholders0);
        }
        lastReplacedValue = rawFormattedValue;
        update();
        if (name != null) {
            TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
            expansion.setPropertyValue(owner, name, lastReplacedValue);
            expansion.setRawPropertyValue(owner, name, getCurrentRawValue());
        }
    }

    /**
     * Changes raw value to new provided value and performs all
     * operations related to it. Changes source as well.
     *
     * @param   newValue
     *          new raw value to use
     * @param   newSource
     *          new source of the text
     */
    public void changeRawValue(String newValue, String newSource) {
        if (originalRawValue.equals(newValue)) return;
        originalRawValue = newValue;
        source = newSource;
        if (temporaryValue == null) {
            analyze(originalRawValue);
        }
    }

    /**
     * Returns source of the raw value or {@code "API"} if it comes from an API call
     *
     * @return  source of the value
     */
    public String getSource() {
        return temporaryValue == null ? source : "API";
    }

    @Override
    public void setTemporaryValue(String temporaryValue) {
        if (temporaryValue != null) {
            this.temporaryValue = temporaryValue;
            analyze(this.temporaryValue);
        } else {
            this.temporaryValue = null;
            analyze(originalRawValue);
        }
    }
    
    @Override
    public String getCurrentRawValue() {
        return temporaryValue != null ? temporaryValue : originalRawValue;
    }

    @Override
    public String updateAndGet() {
        update();
        return get();
    }
    
    @Override
    public boolean update() {
        if (placeholders.length == 0) return false;
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
            if (name != null) {
                TAB.getInstance().getPlaceholderManager().getTabExpansion().setPropertyValue(owner, name, lastReplacedValue);
            }
            return true;
        }
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
        return EnumChatFormat.color(format);
    }
}