package me.neznamy.tab.platforms.bukkit.features;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.TabExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TAB's expansion for PlaceholderAPI
 */
public class BukkitTabExpansion extends PlaceholderExpansion implements TabExpansion {

    /** Map holding all placeholder values for all players */
    private final Map<TabPlayer, Map<String, String>> values = new WeakHashMap<>();

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return "NEZNAMY";
    }

    @Override
    public @NotNull String getIdentifier(){
        return "tab";
    }

    @Override
    public @NotNull String getVersion() {
        return TabConstants.PLUGIN_VERSION;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier){
        if (player == null) return "";
        TabPlayer p = TAB.getInstance().getPlayer(player.getUniqueId());
        if (identifier.startsWith("replace_")) {
            String placeholder = "%" + identifier.substring(8) + "%";
            String output = PlaceholderAPI.setPlaceholders(player, placeholder);
            return TAB.getInstance().getPlaceholderManager().findReplacement(placeholder, output);
        }
        if (identifier.startsWith("placeholder_")) {
            TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList("%" + identifier.substring(12) + "%"));
        }
        return values.computeIfAbsent(p, pl -> new HashMap<>()).get(identifier);
    }

    @Override
    public void setScoreboardVisible(TabPlayer player, boolean visible) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put("scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    @Override
    public void setScoreboardName(TabPlayer player, String name) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put("scoreboard_name", name);
    }

    @Override
    public void setBossBarVisible(TabPlayer player, boolean visible) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put("bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean previewing) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put("ntpreview", previewing ? "Enabled" : "Disabled");
    }

    @Override
    public void setPlaceholderValue(TabPlayer player, String placeholder, String value) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put("placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    @Override
    public void setPropertyValue(TabPlayer player, String property, String value) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put(property, value);
    }

    @Override
    public void setRawPropertyValue(TabPlayer player, String property, String value) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put(property + "_raw", value);
    }
}
