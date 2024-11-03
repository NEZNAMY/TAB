package me.neznamy.tab.shared.features.header;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class storing header/footer configuration.
 */
@Getter
@RequiredArgsConstructor
public class HeaderFooterConfiguration {

    @NotNull private final List<String> header;
    @NotNull private final List<String> footer;
    @NotNull private final String disableCondition;
    @NotNull private final Map<String, HeaderFooterPair> perWorld;
    @NotNull private final Map<String, HeaderFooterPair> perServer;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static HeaderFooterConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "header", "footer", "disable-condition", "per-world", "per-server"));

        List<String> header = section.getStringList("header", Collections.emptyList());
        List<String> footer = section.getStringList("footer", Collections.emptyList());

        return new HeaderFooterConfiguration(
                header,
                footer,
                section.getString("disable-condition", "%world%=disabledworld"),
                getPairs(section.getConfigurationSection("per-world"), "world", header, footer),
                getPairs(section.getConfigurationSection("per-server"), "server", header, footer)
        );
    }

    @NotNull
    private static Map<String, HeaderFooterPair> getPairs(@NotNull ConfigurationSection section, @NotNull String type,
                                                          @NotNull List<String> header, @NotNull List<String> footer) {
        Map<String, HeaderFooterPair> pairs = new HashMap<>();
        for (Object key : section.getKeys()) {
            String asString = key.toString();
            HeaderFooterPair pair = HeaderFooterPair.fromSection(section.getConfigurationSection(asString));
            pairs.put(asString, pair);

            if (header.equals(pair.getHeader())) {
                section.hint("Per-" + type + " header for " + type + " \"" + key + "\" is identical to default header. " +
                        "This is redundant and can be removed for cleaner config.");
            }
            if (footer.equals(pair.getFooter())) {
                section.hint("Per-" + type + " footer for " + type + " \"" + key + "\" is identical to default footer. " +
                        "This is redundant and can be removed for cleaner config.");
            }
        }
        return pairs;
    }

    /**
     * Class storing a header-footer pair.
     */
    @Getter
    @RequiredArgsConstructor
    public static class HeaderFooterPair {

        @Nullable private final List<String> header;
        @Nullable private final List<String> footer;

        /**
         * Returns instance of this class created from given configuration section. If there are
         * issues in the configuration, console warns are printed.
         *
         * @param   section
         *          Configuration section to load from
         * @return  Loaded instance from given configuration section
         */
        public static HeaderFooterPair fromSection(@NotNull ConfigurationSection section) {
            // Check keys
            section.checkForUnknownKey(Arrays.asList("header", "footer"));

            return new HeaderFooterPair(section.getStringList("header"), section.getStringList("footer"));
        }
    }
}
