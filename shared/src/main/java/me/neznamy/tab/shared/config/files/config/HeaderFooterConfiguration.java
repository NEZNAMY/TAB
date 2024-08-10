package me.neznamy.tab.shared.config.files.config;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeaderFooterConfiguration extends ConfigurationSection {

    private final String SECTION = "header-footer";
    @NotNull public final List<String> header = getStringList(SECTION + ".header", Collections.emptyList());
    @NotNull public final List<String> footer = getStringList(SECTION + ".footer", Collections.emptyList());
    @Nullable public final String disableCondition = getString(SECTION + ".disable-condition", "%world%=disabledworld");
    @NotNull public final Map<String, HeaderFooterPair> perWorld = new HashMap<>();
    @NotNull public final Map<String, HeaderFooterPair> perServer = new HashMap<>();

    public HeaderFooterConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList(
                "enabled", "header", "footer", "disable-condition", "per-world", "per-server"));
        Map<String, ?> perWorld = getMap(SECTION + ".per-world");
        if (perWorld != null) {
            for (String world : perWorld.keySet()) {
                checkForUnknownKey(SECTION + ".per-world." + world, Arrays.asList("header", "footer"));
                this.perWorld.put(world, new HeaderFooterPair(
                        getStringList(SECTION + ".per-world." + world + ".header"),
                        getStringList(SECTION + ".per-world." + world + ".footer")
                ));
            }
        }
        Map<String, ?> perServer = getMap(SECTION + ".per-server");
        if (perServer != null) {
            for (String server : perServer.keySet()) {
                checkForUnknownKey(SECTION + ".per-server." + server, Arrays.asList("header", "footer"));
                this.perServer.put(server, new HeaderFooterPair(
                        getStringList(SECTION + ".per-server." + server + ".header"),
                        getStringList(SECTION + ".per-server." + server + ".footer")
                ));
            }
        }
        printHints();
    }

    private void printHints() {
        for (Map.Entry<String, HeaderFooterPair> entry : perWorld.entrySet()) {
            String world = entry.getKey();
            if (header.equals(entry.getValue().header)) {
                hint("Per-world header for world \"" + world + "\" is identical to default header. " +
                        "This is redundant and can be removed for cleaner config.");
            }
            if (footer.equals(entry.getValue().footer)) {
                hint("Per-world footer for world \"" + world + "\" is identical to default footer. " +
                        "This is redundant and can be removed for cleaner config.");
            }
        }
        for (Map.Entry<String, HeaderFooterPair> entry : perServer.entrySet()) {
            String server = entry.getKey();
            if (header.equals(entry.getValue().header)) {
                hint("Per-server header for server \"" + server + "\" is identical to default header. " +
                        "This is redundant and can be removed for cleaner config.");
            }
            if (footer.equals(entry.getValue().footer)) {
                hint("Per-server footer for server \"" + server + "\" is identical to default footer. " +
                        "This is redundant and can be removed for cleaner config.");
            }
        }
    }

    @RequiredArgsConstructor
    public static class HeaderFooterPair {

        @Nullable public final List<String> header;
        @Nullable public final List<String> footer;
    }
}
