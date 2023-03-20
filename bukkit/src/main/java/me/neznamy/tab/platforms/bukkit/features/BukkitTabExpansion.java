package me.neznamy.tab.platforms.bukkit.features;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TAB's expansion for PlaceholderAPI
 */
public class BukkitTabExpansion extends PlaceholderExpansion implements TabExpansion {

    /** Map holding all values for all players for easy and high-performance access */
    private final WeakHashMap<Player, Map<String, String>> values = new WeakHashMap<>();

    @Getter private final String author = TabConstants.PLUGIN_AUTHOR;
    @Getter private final String identifier = TabConstants.PLUGIN_ID;
    @Getter private final String version = TabConstants.PLUGIN_VERSION;

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.startsWith("replace_")) {
            String text = "%" + identifier.substring(8) + "%";
            String textBefore;
            do {
                textBefore = text;
                for (String placeholder : TAB.getInstance().getPlaceholderManager().detectPlaceholders(text)) {
                    text = text.replace(placeholder, TabAPI.getInstance().getPlaceholderManager().findReplacement(placeholder,
                            PlaceholderAPI.setPlaceholders(player, placeholder)));
                }
            } while (!textBefore.equals(text));
            return text;
        }
        if (identifier.startsWith("placeholder_")) {
            TabAPI.getInstance().getPlaceholderManager().addUsedPlaceholder("%" + identifier.substring(12) + "%", TabAPI.getInstance().getPlaceholderManager());
        }
        return values.get(player).get(identifier);
    }

    @Override
    public void setValue(TabPlayer player, String key, String value) {
        values.computeIfAbsent((Player) player.getPlayer(), p -> new HashMap<>()).put(key, value);
    }
}
