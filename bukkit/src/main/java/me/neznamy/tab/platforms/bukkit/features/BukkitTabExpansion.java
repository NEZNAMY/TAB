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

import java.util.HashMap;
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
