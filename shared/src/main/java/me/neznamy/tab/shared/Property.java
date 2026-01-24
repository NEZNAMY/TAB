package me.neznamy.tab.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A dynamic text with placeholder support. If any placeholder
 * used in this text changes value, feature defining this text
 * will receive refresh call letting it know and allowing to get new value.
 */
public class Property {

    /** Internal identifier for this text for PlaceholderAPI expansion, null if it should not be exposed */
    @Getter
    @Nullable
    private final String name;

    /**
     * Feature defining this text, which will receive refresh function
     * if any of placeholders used in it change value.
     */
    @Nullable private final RefreshableFeature listener;

    /** Player this text belongs to */
    @NotNull private final TabPlayer owner;

    /** Raw value as defined in configuration */
    @NotNull @Getter private String originalRawValue;

    /** Raw value assigned via API, null if not set */
    @Nullable @Getter private String temporaryValue;

    /**
     * Parsed elements representing either literal text or placeholders.
     * This array contains the text broken down into processable chunks.
     */
    private Element[] elements;

    /** Last known value after parsing non-relational placeholders */
    private String lastReplacedValue;

    /** Flag tracking whether last replaced value may contain relational placeholders or not */
    private boolean mayContainRelPlaceholders;

    /** Source defining value of the text, displayed in debug command */
    @Nullable private String source;

    /** Relational placeholders in the text in the same order they are used */
    private RelationalPlaceholderImpl[] relPlaceholders;

    /** String builder to avoid reallocation on every update call */
    @NotNull
    private final StringBuilder stringBuilder = new StringBuilder();

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
     */
    public Property(@Nullable RefreshableFeature listener, @NotNull TabPlayer owner,
                    @NotNull String rawValue) {
        this(null, listener, owner, rawValue, null);
    }

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
    public Property(@Nullable String name, @Nullable RefreshableFeature listener, @NotNull TabPlayer owner,
                    @NotNull String rawValue, @Nullable String source) {
        this.name = name;
        this.listener = listener;
        this.owner = owner;
        this.source = source;
        originalRawValue = rawValue;
        analyze(originalRawValue);
    }

    /**
     * Finds all placeholders used in the value and splits text into
     * elements (literals and placeholders) for fast replacement.
     *
     * @param   value
     *          raw value to analyze
     */
    private void analyze(@NotNull String value) {
        // Identify placeholders used directly
        List<TabPlaceholder> placeholders = new ArrayList<>();
        List<RelationalPlaceholderImpl> relPlaceholders0 = new ArrayList<>();
        for (String identifier : PlaceholderManagerImpl.detectPlaceholders(value)) {
            TabPlaceholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
            placeholders.add(placeholder);
            if (placeholder instanceof RelationalPlaceholderImpl) {
                relPlaceholders0.add((RelationalPlaceholderImpl) placeholder);
            }
        }

        // Parse text into elements (literal strings and placeholders)
        List<Element> elementList = new ArrayList<>();
        if (placeholders.isEmpty()) {
            // No placeholders - entire text is one literal
            elementList.add(new LiteralElement(EnumChatFormat.color(value)));
        } else {
            String remaining = value;
            for (TabPlaceholder placeholder : placeholders) {
                int index = remaining.indexOf(placeholder.getIdentifier());
                if (index != -1) {
                    // Add literal text before placeholder if not empty
                    if (index > 0) {
                        elementList.add(new LiteralElement(EnumChatFormat.color(remaining.substring(0, index))));
                    }
                    // Add placeholder element
                    elementList.add(new PlaceholderElement(placeholder));
                    // Move past the placeholder
                    remaining = remaining.substring(index + placeholder.getIdentifier().length());
                }
            }
            // Add remaining literal text if any
            if (!remaining.isEmpty()) {
                elementList.add(new LiteralElement(EnumChatFormat.color(remaining)));
            }
        }

        // Update and save values
        elements = elementList.toArray(new Element[0]);
        relPlaceholders = relPlaceholders0.toArray(new RelationalPlaceholderImpl[0]);

        if (listener != null) {
            listener.addUsedPlaceholders(placeholders);
        }
        lastReplacedValue = "";
        update();
        if (name != null) {
            owner.expansionData.setPropertyValue(name, lastReplacedValue);
            owner.expansionData.setRawPropertyValue(name, getCurrentRawValue());
        }
    }

    /**
     * Changes raw value to new provided value and performs all
     * operations related to it.
     *
     * @param   newValue
     *          new raw value to use
     * @return  Whether raw value changed or not
     */
    public boolean changeRawValue(@NotNull String newValue) {
        return changeRawValue(newValue, null);
    }

    /**
     * Changes raw value to new provided value and performs all
     * operations related to it. Changes source as well.
     *
     * @param   newValue
     *          new raw value to use
     * @param   newSource
     *          new source of the text
     * @return  Whether raw value changed or not
     */
    public boolean changeRawValue(@NotNull String newValue, @Nullable String newSource) {
        if (originalRawValue.equals(newValue)) return false;
        originalRawValue = newValue;
        source = newSource;
        if (temporaryValue == null) {
            analyze(originalRawValue);
        }
        return true;
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
        String string;
        if (elements.length == 1) {
            // Single element - fast path
            string = elements[0].get(owner);
        } else {
            stringBuilder.setLength(0);
            for (Element element : elements) {
                stringBuilder.append(element.get(owner));
            }
            string = stringBuilder.toString();
        }

        if (!lastReplacedValue.equals(string)) {
            lastReplacedValue = string;
            mayContainRelPlaceholders = lastReplacedValue.indexOf('%') != -1;
            if (name != null) {
                owner.expansionData.setPropertyValue(name, lastReplacedValue);
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
        if (!mayContainRelPlaceholders) return lastReplacedValue;
        String format = lastReplacedValue;
        // Direct placeholders
        for (RelationalPlaceholderImpl pl : relPlaceholders) {
            format = format.replace(pl.getIdentifier(), EnumChatFormat.color(pl.getLastValue(viewer, owner)));
        }

        // Nested placeholders
        for (String identifier : PlaceholderManagerImpl.detectPlaceholders(format)) {
            if (!identifier.startsWith("%rel_")) continue;
            RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
            format = format.replace(pl.getIdentifier(), EnumChatFormat.color(pl.getLastValue(viewer, owner)));
            if (listener != null) listener.addUsedPlaceholder(identifier);
        }
        return format;
    }

    /**
     * Returns original raw value with placeholders replaced by their values.
     *
     * @return  original raw value with placeholders replaced
     */
    @NotNull
    public String getOriginalReplacedValue() {
        String value = originalRawValue;
        for (String identifier : PlaceholderManagerImpl.detectPlaceholders(value)) {
            value = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).parse(owner);
        }
        return EnumChatFormat.color(value);
    }

    /**
     * Represents either a literal string or a placeholder in the text.
     */
    private abstract static class Element {

        @NotNull
        abstract String get(@NotNull TabPlayer owner);
    }

    /**
     * A literal string element that gets appended as-is.
     */
    @RequiredArgsConstructor
    private static class LiteralElement extends Element {

        @NotNull
        private final String text;

        @Override
        @NotNull
        String get(@NotNull TabPlayer owner) {
            return text;
        }
    }

    /**
     * A placeholder element that gets resolved at runtime.
     */
    @RequiredArgsConstructor
    private static class PlaceholderElement extends Element {

        @NotNull
        private final TabPlaceholder placeholder;

        @Override
        @NotNull
        String get(@NotNull TabPlayer owner) {
            return EnumChatFormat.color(placeholder.parse(owner));
        }
    }
}