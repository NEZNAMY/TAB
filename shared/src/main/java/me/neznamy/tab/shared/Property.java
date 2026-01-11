package me.neznamy.tab.shared;

import lombok.Getter;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
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

    /** Thread-local StringBuilder pool for efficient string building without allocations */
    private static final ThreadLocal<StringBuilder> STRING_BUILDER_POOL = ThreadLocal.withInitial(() -> new StringBuilder(256));

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
     * Represents either a literal string or a placeholder in the text.
     */
    private static abstract class Element {
        abstract void appendTo(StringBuilder sb, TabPlayer owner);
    }

    /**
     * A literal string element that gets appended as-is.
     */
    private static class LiteralElement extends Element {
        private final String text;

        LiteralElement(String text) {
            this.text = text;
        }

        @Override
        void appendTo(StringBuilder sb, TabPlayer owner) {
            sb.append(text);
        }
    }

    /**
     * A placeholder element that gets resolved at runtime.
     */
    private static class PlaceholderElement extends Element {
        private final String identifier;

        PlaceholderElement(String identifier) {
            this.identifier = identifier;
        }

        @Override
        void appendTo(StringBuilder sb, TabPlayer owner) {
            String value = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(identifier, owner);
            sb.append(value);
        }
    }

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
        List<String> placeholders0 = new ArrayList<>();
        List<String> relPlaceholders0 = new ArrayList<>();
        for (String identifier : PlaceholderManagerImpl.detectPlaceholders(value)) {
            placeholders0.add(identifier);
            if (identifier.startsWith("%rel_")) {
                relPlaceholders0.add(identifier);
            }
        }

        // Parse text into elements (literal strings and placeholders)
        List<Element> elementList = new ArrayList<>();
        if (placeholders0.isEmpty()) {
            // No placeholders - entire text is one literal
            elementList.add(new LiteralElement(EnumChatFormat.color(value)));
        } else {
            String remaining = value;
            for (String placeholder : placeholders0) {
                int index = remaining.indexOf(placeholder);
                if (index != -1) {
                    // Add literal text before placeholder if not empty
                    if (index > 0) {
                        elementList.add(new LiteralElement(EnumChatFormat.color(remaining.substring(0, index))));
                    }
                    // Add placeholder element
                    elementList.add(new PlaceholderElement(placeholder));
                    // Move past the placeholder
                    remaining = remaining.substring(index + placeholder.length());
                }
            }
            // Add remaining literal text if any
            if (!remaining.isEmpty()) {
                elementList.add(new LiteralElement(EnumChatFormat.color(remaining)));
            }
        }

        // Update and save values
        elements = elementList.toArray(new Element[0]);
        placeholders = placeholders0.toArray(new String[0]);
        relPlaceholders = relPlaceholders0.toArray(new String[0]);

        if (listener != null) {
            listener.addUsedPlaceholders(placeholders0);
        }
        lastReplacedValue = "";
        update();
        if (name != null) {
            TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
            expansion.setPropertyValue(owner, name, lastReplacedValue);
            expansion.setRawPropertyValue(owner, name, getCurrentRawValue());
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
        if (elements.length == 0) return false;

        // Single literal element - fast path
        if (elements.length == 1 && elements[0] instanceof LiteralElement) {
            String string = ((LiteralElement) elements[0]).text;
            if (!lastReplacedValue.equals(string)) {
                lastReplacedValue = string;
                mayContainRelPlaceholders = lastReplacedValue.indexOf('%') != -1;
                if (name != null) {
                    TAB.getInstance().getPlaceholderManager().getTabExpansion().setPropertyValue(owner, name, lastReplacedValue);
                }
                return true;
            }
            return false;
        }

        // Get thread-local StringBuilder and prepare it
        StringBuilder sb = STRING_BUILDER_POOL.get();

        // Limit maximum capacity to prevent memory bloat
        if (sb.capacity() > 2048) {
            sb = new StringBuilder(256);
            STRING_BUILDER_POOL.set(sb);
        }

        sb.setLength(0);

        // Build string by processing each element
        for (Element element : elements) {
            element.appendTo(sb, owner);
        }

        // Colorize once at the end
        String string = EnumChatFormat.color(sb.toString());

        if (!lastReplacedValue.equals(string)) {
            lastReplacedValue = string;
            mayContainRelPlaceholders = lastReplacedValue.indexOf('%') != -1;
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
        if (!mayContainRelPlaceholders) return lastReplacedValue;
        String format = lastReplacedValue;
        // Direct placeholders
        for (String identifier : relPlaceholders) {
            RelationalPlaceholderImpl pl = (RelationalPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
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
            value = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier).set(identifier, owner);
        }
        return EnumChatFormat.color(value);
    }
}