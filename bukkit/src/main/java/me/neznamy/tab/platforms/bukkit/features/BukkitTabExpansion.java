package me.neznamy.tab.platforms.bukkit.features;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TAB's expansion for PlaceholderAPI
 */
@Getter
public class BukkitTabExpansion extends PlaceholderExpansion implements TabExpansion {

    /** Map holding all values for all players for easy and high-performance access */
    @NotNull
    private final WeakHashMap<TabPlayer, Map<String, String>> values = new WeakHashMap<>();

    /** List of all placeholders offered by the plugin for command suggestions */
    @NotNull
    private final List<String> placeholders = Arrays.asList(
            "%tab_tabprefix%",
            "%tab_tabsuffix%",
            "%tab_tagprefix%",
            "%tab_tagsuffix%",
            "%tab_customtabname%",
            "%tab_customtagname%",
            "%tab_belowname%",
            "%tab_abovename%",
            "%tab_tabprefix_raw%",
            "%tab_tabsuffix_raw%",
            "%tab_tagprefix_raw%",
            "%tab_tagsuffix_raw%",
            "%tab_customtabname_raw%",
            "%tab_customtagname_raw%",
            "%tab_belowname_raw%",
            "%tab_abovename_raw%",
            "%tab_scoreboard_name%",
            "%tab_scoreboard_visible%",
            "%tab_bossbar_visible%",
            "%tab_nametag_preview%",
            "%tab_nametag_visibility%",
            "%tab_replace_<placeholder>%",
            "%tab_placeholder_<placeholder>%"
    );

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return TabConstants.PLUGIN_AUTHOR;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return TabConstants.PLUGIN_ID;
    }

    @Override
    @NotNull
    public String getVersion() {
        return TabConstants.PLUGIN_VERSION;
    }

    @Override
    @NotNull
    public List<String> getPlaceholders() {
        return placeholders;
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String identifier) {
        if (identifier.startsWith("replace_")) {
            String text = "%" + identifier.substring(8) + "%";
            String textBefore;
            do {
                textBefore = text;
                for (String placeholder : TAB.getInstance().getPlaceholderManager().detectPlaceholders(text)) {
                    text = text.replace(placeholder, TAB.getInstance().getPlaceholderManager().findReplacement(placeholder,
                            PlaceholderAPI.setPlaceholders(player, placeholder)));
                }
            } while (!textBefore.equals(text));
            return text;
        }
        if (identifier.startsWith("placeholder_")) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%" + identifier.substring(12) + "%", TAB.getInstance().getPlaceholderManager());
        }
        if (player == null) return "<Player cannot be null>";
        TabPlayer p = TAB.getInstance().getPlayer(player.getUniqueId());
        if (p == null || !p.isLoaded()) return "<Player is not loaded>";
        return values.get(p).get(identifier);
    }

    @Override
    public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put(key, value);
    }
}
