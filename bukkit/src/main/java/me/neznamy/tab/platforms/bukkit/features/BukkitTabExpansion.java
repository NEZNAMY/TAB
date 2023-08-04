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
    private final WeakHashMap<Player, Map<String, String>> values = new WeakHashMap<>();

    @NotNull
    private final String author = TabConstants.PLUGIN_AUTHOR;

    @NotNull
    private final String identifier = TabConstants.PLUGIN_ID;

    @NotNull
    private final String version = TabConstants.PLUGIN_VERSION;

    @Override
    public boolean persist() {
        return true;
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
        return values.get(player).get(identifier);
    }

    @Override
    public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
        values.computeIfAbsent((Player) player.getPlayer(), p -> new HashMap<>()).put(key, value);
    }
}
