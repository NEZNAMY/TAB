package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic text with placeholder support. If any placeholder
 * used in this text changes value, feature defining this text
 * will receive refresh call letting it know and allowing to get new value.
 */
public class Property {

    private static long counter;

    /** Internal identifier for this text for PlaceholderAPI expansion, null if it should not be exposed */
    @Nullable private final String name;

    /**
     * Feature defining this text, which will receive refresh function
     * if any of placeholders used in it change value.
     */
    @Nullable private final Refreshable listener;
    
    /** Player this text belongs to */
    @NotNull private final TabPlayer owner;
    
    /** Raw value as defined in configuration */
    @NotNull @Getter private String originalRawValue;

    /** Raw value assigned via API, null if not set */
    @Nullable @Getter private String temporaryValue;

    /**
     * Raw value using %s for each placeholder ready to be inserted
     * into String formatter, which results in about 5x lower
     * memory allocations as well as better performance.
     */
    private String rawFormattedValue;

    /** Last known value after parsing non-relational placeholders */
    private String lastReplacedValue;
    
    /** Source defining value of the text, displayed in debug command */
    @Nullable private String source;

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
     * @param   name
     *          Property name to use in expansion (nullable if not use)
     * @param   listener
     *          Feature which should receive refresh method if placeholder changes value
     * @param   owner
     *          Player this text belongs to
     * @param   rawValue
     *          Raw value using raw placeholder identifiers
     * @param   source
     *          Source of the text used in debug command
     */
    public Property(@Nullable String name, @Nullable Refreshable listener, @NotNull TabPlayer owner,
                    @NotNull String rawValue, @Nullable String source) {
        this.name = name;
        this.listener = listener;
        this.owner = owner;
        this.source = source;
        originalRawValue = rawValue;
        analyze(originalRawValue);
    }

    /**
     * Finds all placeholders used in the value and prepares it for
     * String formatter using %s for each placeholder.
     *
     * @param   value
     *          raw value to analyze
     */
    private void analyze(@NotNull String value) {
        // Identify placeholders used directly
        List<String> placeholders0 = new ArrayList<>();
        List<String> relPlaceholders0 = new ArrayList<>();
        for (String identifier : TAB.getInstance().getPlaceholderManager().detectPlaceholders(value)) {
            placeholders0.add(identifier);
            if (identifier.startsWith("%rel_")) {
                relPlaceholders0.add(identifier);
            }
        }

        // Convert all placeholders to %s for String formatter
        String rawFormattedValue0 = value;
        for (String placeholder : placeholders0) {
            rawFormattedValue0 = replaceFirst(rawFormattedValue0, placeholder);
        }

        // Make % symbol not break String formatter by adding another one to display it
        if (!placeholders0.isEmpty() && rawFormattedValue0.contains("%")) {
            int index = rawFormattedValue0.lastIndexOf('%');
            if (rawFormattedValue0.length() == index+1 || rawFormattedValue0.charAt(index+1) != 's') {
                StringBuilder sb = new StringBuilder(rawFormattedValue0);
                sb.insert(index+1, "%");
                rawFormattedValue0 = sb.toString();
            }
        }

        // Apply gradients that do not include placeholders to avoid applying them on every refresh
        rawFormattedValue0 = RGBUtils.getInstance().applyCleanGradients(rawFormattedValue0);

        // Make \n work even if used in '', which snakeyaml does not convert to newline
        if (rawFormattedValue0.contains("\\n")) {
            rawFormattedValue0 = rawFormattedValue0.replace("\\n", "\n");
        }

        // Apply static colors to not need to do it on every refresh
        rawFormattedValue = EnumChatFormat.color(rawFormattedValue0);

        // Update and save values
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

    private String replaceFirst(String original, String searchString) {
        int index = original.indexOf(searchString);
        if (index != -1) {
            return original.substring(0, index) + "%s" + original.substring(index + searchString.length());
        } else {
            return original;
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
    public void changeRawValue(@NotNull String newValue, @Nullable String newSource) {
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
    public @Nullable String getSource() {
        return temporaryValue == null ? source : "API";
    }

    /**
     * Temporarily overrides current raw value with an API call
     *
     * @param   temporaryValue
     *          temporary value to be assigned
     */
    public void setTemporaryValue(@Nullable String temporaryValue) {
        if (temporaryValue != null) {
            this.temporaryValue = temporaryValue;
            analyze(this.temporaryValue);
        } else {
            this.temporaryValue = null;
            analyze(originalRawValue);
        }
    }

    /**
     * Returns temporary value (via API) if present, raw value otherwise
     *
     * @return  current raw value
     */
    public @NotNull String getCurrentRawValue() {
        return temporaryValue != null ? temporaryValue : originalRawValue;
    }

    /**
     * Replaces all placeholders in current raw value, colorizes it and returns it.
     * Equal to calling {@link #update()} and then {@link #get()}.
     *
     * @return  updated value
     */
    public @NotNull String updateAndGet() {
        update();
        return get();
    }

    /**
     * Replaces all placeholders in current raw value, colorizes it and returns whether value changed or not
     *
     * @return  if updating changed value or not
     */
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

    /**
     * Returns last known value
     *
     * @return  last known value
     */
    public @NotNull String get() {
        return lastReplacedValue;
    }

    /**
     * Returns value for defined viewer by applying relational placeholders to last known value
     *
     * @param   viewer
     *          the viewer
     * @return  format for the viewer
     */
    public @NotNull String getFormat(@NotNull TabPlayer viewer) {
        String format = lastReplacedValue;
        // Direct placeholders
        for (String identifier : relPlaceholders) {
            RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
            format = format.replace(pl.getIdentifier(), pl.getLastValue(viewer, owner));
        }

        // Nested placeholders
        for (String identifier : TAB.getInstance().getPlaceholderManager().detectPlaceholders(format)) {
            if (!identifier.startsWith("%rel_")) continue;
            RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
            format = format.replace(pl.getIdentifier(), pl.getLastValue(viewer, owner));
            if (listener != null) listener.addUsedPlaceholder(identifier);
        }
        return EnumChatFormat.color(format);
    }

    /**
     * Returns a new unique property name.
     *
     * @return  A new unique property name.
     */
    public static String randomName() {
        return String.valueOf(counter++);
    }
}