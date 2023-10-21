package me.neznamy.tab.shared.config;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Configuration converter that converts configuration files from practically
 * any previous version of TAB to the current format.
 */
public class Converter {

    public void convert2810to290(@NotNull ConfigurationFile animations) {
        if (animations.getValues().size() == 1 && animations.getValues().containsKey("animations")) {
            TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 2.8.10 to 2.9.0"));
            animations.setValues(animations.getConfigurationSection("animations"));
            animations.save();
        }
    }

    /**
     * Converts all configuration files from 2.9.2 into new 3.0.0 format and saves a copy
     * of old files into a new "old_configs" folder.
     *
     * @param   currentConfig
     *          currently detected config.yml file
     * @throws  IOException
     *          if an I/O operation with the files fails
     */
    public void convert292to300(@NotNull ConfigurationFile currentConfig) throws IOException {
        if (!currentConfig.hasConfigOption("change-nametag-prefix-suffix")) return;
        TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 2.9.2 to 3.0.0"));

        File folder = TAB.getInstance().getDataFolder();
        moveOldFiles();
        Files.createFile(new File(folder, "groups.yml").toPath());
        Files.createFile(new File(folder, "users.yml").toPath());
        Files.createFile(new File(folder, "config.yml").toPath());
        ConfigurationFile groups = new YamlConfigurationFile(null, new File(folder, "groups.yml"));
        ConfigurationFile users = new YamlConfigurationFile(null, new File(folder, "users.yml"));
        File oldConfigsFolder = new File(folder, "old_configs");
        File oldAnimations = new File(oldConfigsFolder, "animations.yml");
        if (oldAnimations.exists()) {
            Files.copy(oldAnimations.toPath(), new File(folder, "animations.yml").toPath());
        }
        File premiumFile = new File(oldConfigsFolder, "premiumconfig.yml");
        ConfigurationFile premiumConfig = premiumFile.exists() ? new YamlConfigurationFile(null, premiumFile) : null;
        File bossBarFile = new File(oldConfigsFolder, "bossbar.yml");
        if (!bossBarFile.exists()) throw new IllegalStateException("Failed to convert configuration to v3: File bossbar.yml does not exist");
        ConfigurationFile bossBar = new YamlConfigurationFile(null, bossBarFile);
        ConfigurationFile oldConfig = new YamlConfigurationFile(null, new File(oldConfigsFolder, "config.yml"));
        ConfigurationFile newConfig = new YamlConfigurationFile(null, new File(folder, "config.yml"));

        convertHeaderFooter(oldConfig, newConfig);
        convertTabListFormatting(oldConfig, newConfig, premiumConfig);
        convertTeamOptions(oldConfig, newConfig, premiumConfig);
        convertYellowNumber(oldConfig, newConfig);
        convertBelowName(oldConfig, newConfig);
        convertBossBar(bossBar, newConfig);
        if (premiumConfig != null) {
            convertScoreboard(newConfig, premiumConfig);
        } else {
            createDefaultScoreboard(newConfig);
        }
        convertOtherOptions(oldConfig, newConfig, premiumConfig);
        convertGroupsAndUsers(oldConfig, groups, users);
        currentConfig.setValues(newConfig.getValues());
    }

    /**
     * Creates a new "old_configs" folder and moves all files in TAB's folder into it.
     *
     * @throws  IOException
     *          if thrown by file move operation
     */
    private void moveOldFiles() throws IOException {
        File folder = TAB.getInstance().getDataFolder();
        File oldFolder = new File(folder, "old_configs");
        Files.createDirectories(oldFolder.toPath());
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (!file.isFile()) continue; //old_configs folder
            Files.move(file.toPath(), new File(folder.getPath() +
                    File.separator + "old_configs" + File.separator + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void convertTeamOptions(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig, @Nullable ConfigurationFile premiumConfig) {
        newConfig.set("scoreboard-teams.enabled", oldConfig.getBoolean("change-nametag-prefix-suffix", true));
        newConfig.set("scoreboard-teams.invisible-nametags", oldConfig.getBoolean("invisible-nametags", false));
        newConfig.set("scoreboard-teams.anti-override", oldConfig.getBoolean("anti-override.scoreboard-teams", true));
        newConfig.set("scoreboard-teams.enable-collision", oldConfig.getBoolean("enable-collision", true));
        newConfig.set("scoreboard-teams.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.nametag", Collections.singletonList("disabledworld")));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY) {
            newConfig.set("scoreboard-teams.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.nametag", Collections.singletonList("disabledserver")));
        } else {
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.enabled", oldConfig.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled", false));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.disable-on-boats", oldConfig.getBoolean("unlimited-nametag-prefix-suffix-mode.disable-on-boats", true));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.space-between-lines", oldConfig.getBoolean("unlimited-nametag-prefix-suffix-mode.space-between-lines", true));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.unlimited-nametags", Collections.singletonList("disabledworld")));
        }
        String sortingType;
        String sortingPlaceholder;
        List<String> placeholderOrder = new ArrayList<>();
        if (premiumConfig != null) {
            newConfig.set("scoreboard-teams.case-sensitive-sorting", premiumConfig.getBoolean("case-sensitive-sorting", true));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", premiumConfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename","nametag","belowname","another")));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.static-lines", premiumConfig.getConfigurationSection("unlimited-nametag-mode-static-lines"));
            sortingType = premiumConfig.getString("sorting-type", "GROUPS");
            sortingPlaceholder = premiumConfig.getString("sorting-placeholder", "%some_level_maybe?%");
            placeholderOrder = premiumConfig.getStringList("placeholder-order", Arrays.asList("value1", "value2"));
        } else {
            newConfig.set("scoreboard-teams.case-sensitive-sorting", true);
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.dynamic-lines", Arrays.asList("abovename","nametag","belowname","another"));
            newConfig.set("scoreboard-teams.unlimited-nametag-mode.static-lines", new HashMap<String, Object>() {{put("myCustomLine", 0.66);}});
            sortingType = oldConfig.getBoolean("sort-players-by-permissions", false) ? "GROUP_PERMISSIONS" : "GROUPS";
            sortingPlaceholder = "";
        }

        List<String> sortingTypes = new ArrayList<>();
        for (String type : sortingType.split("_THEN_")) {
            if (type.equalsIgnoreCase("GROUPS") || type.equalsIgnoreCase("GROUP_PERMISSIONS")) {
                List<String> sortingList = oldConfig.getStringList("group-sorting-priority-list", Arrays.asList("owner", "admin", "mod", "helper", "builder", "premium", "player", "default"));
                StringBuilder groups = new StringBuilder(("GROUP_PERMISSIONS".equals(type) ? "PERMISSIONS" : "GROUPS") + ":");
                for (String group : sortingList) {
                    groups.append(("GROUP_PERMISSIONS".equals(type) ? "tab.sort." : ""));
                    groups.append(group.replace(" ", "|"));
                    if (sortingList.indexOf(group) != sortingList.size() - 1)
                        groups.append(",");
                }
                sortingTypes.add(groups.toString());
            } else if (type.equalsIgnoreCase("PLACEHOLDER")) {
                sortingTypes.add("PLACEHOLDER:" + sortingPlaceholder + ":" + String.join(",", placeholderOrder));
            } else {
                sortingTypes.add(type + ":" + sortingPlaceholder);
            }
        }
        sortingTypes.add("PLACEHOLDER_A_TO_Z:" + TabConstants.Placeholder.PLAYER);
        newConfig.set("scoreboard-teams.sorting-types", sortingTypes);
    }

    private void convertTabListFormatting(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig, @Nullable ConfigurationFile premiumConfig) {
        newConfig.set("tablist-name-formatting.enabled", oldConfig.getBoolean("change-tablist-prefix-suffix", true));
        newConfig.set("tablist-name-formatting.anti-override", oldConfig.getBoolean("anti-override.tablist-names", true));
        newConfig.set("tablist-name-formatting.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.tablist-names", Collections.singletonList("disabledworld")));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY)
            newConfig.set("tablist-name-formatting.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.tablist-names", Collections.singletonList("disabledserver")));
        if (premiumConfig != null) {
            newConfig.set("tablist-name-formatting.align-tabsuffix-on-the-right", premiumConfig.getBoolean("align-tabsuffix-on-the-right", false));
            newConfig.set("tablist-name-formatting.character-width-overrides", premiumConfig.getConfigurationSection("character-width-overrides"));
        } else {
            newConfig.set("tablist-name-formatting.align-tabsuffix-on-the-right", false);
            newConfig.set("tablist-name-formatting.character-width-overrides", new HashMap<Integer, Integer>());
        }
    }

    private void convertYellowNumber(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig) {
        newConfig.set("yellow-number-in-tablist.enabled", !oldConfig.getString("yellow-number-in-tablist", TabConstants.Placeholder.PING).equals(""));
        newConfig.set("yellow-number-in-tablist.value", oldConfig.getString("yellow-number-in-tablist", TabConstants.Placeholder.PING));
        newConfig.set("yellow-number-in-tablist.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.yellow-number", Collections.singletonList("disabledworld")));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY)
            newConfig.set("yellow-number-in-tablist.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.yellow-number", Collections.singletonList("disabledserver")));
    }

    private void convertBelowName(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig) {
        newConfig.set("belowname-objective", oldConfig.getConfigurationSection("classic-vanilla-belowname"));
        newConfig.set("belowname-objective.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.belowname", Collections.singletonList("disabledworld")));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY)
            newConfig.set("belowname-objective.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.belowname", Collections.singletonList("disabledserver")));
    }

    private void convertBossBar(@NotNull ConfigurationFile bossBar, @NotNull ConfigurationFile newConfig) {
        newConfig.set("bossbar.enabled", bossBar.getBoolean("bossbar-enabled", false));
        newConfig.set("bossbar.toggle-command", bossBar.getString("bossbar-toggle-command", "/bossbar"));
        newConfig.set("bossbar.remember-toggle-choice", bossBar.getBoolean("remember-toggle-choice", false));
        newConfig.set("bossbar.hidden-by-default", bossBar.getBoolean("hidden-by-default", false));
        Map<Object, Map<String, Object>> bars = bossBar.getConfigurationSection("bars");
        Map<String, List<Object>> perWorldBossBars = bossBar.getConfigurationSection("per-world");
        List<Object> activeBossBars = new ArrayList<>(bossBar.getStringList("default-bars", new ArrayList<>()));
        String separator = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ? "server" : "world";
        for (Map.Entry<String, List<Object>> entry : perWorldBossBars.entrySet()) {
            for (Object bar : entry.getValue()) {
                if (!bars.containsKey(bar)) continue;
                activeBossBars.add(bar);
                if (bars.get(bar).containsKey("display-condition")) {
                    bars.get(bar).put("display-condition", bars.get(bar).get("display-condition") + ";%" + separator + "%=" + entry.getKey());
                } else {
                    bars.get(bar).put("display-condition", "%" + separator + "%=" + entry.getKey());
                }
            }
        }
        for (Object definedBossBar : bars.keySet()) {
            bars.get(definedBossBar).put("announcement-bar", !activeBossBars.contains(definedBossBar));
            bars.get(definedBossBar).remove("permission-required");
        }
        newConfig.set("bossbar.default-bars", null);
        newConfig.set("bossbar.bars", bars);
    }

    private void convertScoreboard(@NotNull ConfigurationFile newConfig, @NotNull ConfigurationFile premiumConfig) {
        String separator = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ? "server" : "world";
        newConfig.set("scoreboard", premiumConfig.getObject("scoreboard"));
        newConfig.set("scoreboard.permission-required-to-toggle", null);
        Map<String, Map<String,Object>> scoreboards = premiumConfig.getConfigurationSection("scoreboards");
        Map<String, String> perWorldScoreboards = premiumConfig.getConfigurationSection("scoreboard.per-world");
        newConfig.set("scoreboard.default-scoreboard", null);
        newConfig.set("scoreboard.per-world", null);
        for (Map.Entry<String, String> entry : perWorldScoreboards.entrySet()) {
            String world = entry.getKey();
            String sb = entry.getValue();
            if (!scoreboards.containsKey(sb)) continue;
            Map<String, Object> scoreboard = scoreboards.get(sb);
            if (scoreboard.containsKey("display-condition")) {
                scoreboard.put("display-condition", scoreboards.get(sb).get("display-condition") + ";%" + separator + "%=" + world);
            } else {
                scoreboard.put("display-condition", "%" + separator + "%=" + world);
                //move to the top, so it's actually displayed with new priority system
                scoreboards.remove(sb);
                Map<String, Map<String, Object>> reordered = new HashMap<>();
                reordered.put(sb, scoreboard);
                reordered.putAll(scoreboards);
                scoreboards = reordered;
            }
        }
        newConfig.set("scoreboard.scoreboards", scoreboards);
    }

    private void createDefaultScoreboard(@NotNull ConfigurationFile newConfig) {
        newConfig.set("scoreboard.enabled", false);
        newConfig.set("scoreboard.toggle-command", "/sb");
        newConfig.set("scoreboard.remember-toggle-choice", false);
        newConfig.set("scoreboard.hidden-by-default", false);
        newConfig.set("scoreboard.use-numbers", false);
        newConfig.set("scoreboard.static-number", 0);
        newConfig.set("scoreboard.delay-on-join-milliseconds", 0);
        newConfig.set("scoreboard.respect-other-plugins", true);
        newConfig.set("scoreboard.scoreboards.admin.display-condition", "permission:tab.scoreboard.admin");
        newConfig.set("scoreboard.scoreboards.admin.title", "Admin scoreboard");
        newConfig.set("scoreboard.scoreboards.admin.lines", Arrays.asList("%animation:MyAnimation1%", "&6Online:", "* &eOnline&7: &f%online%&7",
                "* &eCurrent World&7: &f%worldonline%", "* &eStaff&7: &f%staffonline%", " ", "&6Server Info:", "* &bTPS&7: %tps%",
                "* &bUptime&7: &f%server_uptime%", "* &bMemory&7: &f%memory-used%&7/&4%memory-max%", "%animation:MyAnimation1%"));
        newConfig.set("scoreboard.scoreboards.scoreboard1.title", "Default");
        newConfig.set("scoreboard.scoreboards.scoreboard1.lines", Arrays.asList("%animation:MyAnimation1%", "&6My Stats:", "* &eKills&7: &f%statistic_player_kills%",
                "* &eDeaths&7: &f%statistic_deaths%", "* &eHealth&7: &f%health%", " ", "&6Personal Info:", "* &bRank&7: &f%group%",
                "* &bPing&7: &f%ping%&7ms", "* &bWorld&7: &f%world%", "%animation:MyAnimation1%"));
    }

    private void convertHeaderFooter(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig) {
        newConfig.set("header-footer.enabled", oldConfig.getBoolean("enable-header-footer", true));
        newConfig.set("header-footer.header", oldConfig.getStringList("header"));
        newConfig.set("header-footer.footer", oldConfig.getStringList("footer"));
        newConfig.set("header-footer.disable-in-worlds", oldConfig.getStringList("disable-features-in-worlds.header-footer", Collections.singletonList("disabledworld")));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY)
            newConfig.set("header-footer.disable-in-servers", oldConfig.getStringList("disable-features-in-servers.header-footer", Collections.singletonList("disabledserver")));

        String separator = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ? "server" : "world";
        Map<String, Map<String, Object>> perWorldSettings = oldConfig.getConfigurationSection("per-" + separator + "-settings");
        Map<String, Object> headerFooterMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> worldEntry : new LinkedHashMap<>(perWorldSettings).entrySet()) {
            Map<String, Object> headerFooter = new LinkedHashMap<>();
            for (Map.Entry<String, Object> propertyValueEntry : new LinkedHashMap<>(worldEntry.getValue()).entrySet()) {
                if (propertyValueEntry.getKey().equalsIgnoreCase("header") || propertyValueEntry.getKey().equalsIgnoreCase("footer"))
                    headerFooter.put(propertyValueEntry.getKey(), propertyValueEntry.getValue());
            }
            headerFooterMap.put(translateWorldGroup(oldConfig, worldEntry.getKey()), headerFooter);
        }
        newConfig.set("header-footer.per-" + separator, headerFooterMap);
    }

    private void convertOtherOptions(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile newConfig, @Nullable ConfigurationFile premiumConfig) {
        newConfig.set("prevent-spectator-effect.enabled", oldConfig.getBoolean("do-not-move-spectators",false));

        Map<String,Object> placeholders = oldConfig.getConfigurationSection("placeholders");
        if (premiumConfig != null) {
            newConfig.set("placeholder-output-replacements", premiumConfig.getConfigurationSection("placeholder-output-replacements"));
            newConfig.set("conditions", premiumConfig.getConfigurationSection("conditions"));
        } else {
            newConfig.set("placeholder-output-replacements.%essentials_vanished%.yes", "&7| Vanished");
            newConfig.set("placeholder-output-replacements.%essentials_vanished%.no", "");
            newConfig.set("conditions.nick.conditions", Collections.singletonList("%player%=%essentials_nickname%"));
            newConfig.set("conditions.nick.yes", "%player%");
            newConfig.set("conditions.nick.no", "~%essentials_nickname%");
        }

        newConfig.set("placeholders", placeholders);
        newConfig.set("placeholderapi-refresh-intervals", oldConfig.getConfigurationSection("placeholderapi-refresh-intervals"));
        newConfig.set("assign-groups-by-permissions", oldConfig.getBoolean("assign-groups-by-permissions", false));
        newConfig.set("primary-group-finding-list", oldConfig.getStringList("primary-group-finding-list", Arrays.asList("Owner","Admin","Mod","Helper","default")));

        newConfig.set("debug", oldConfig.getBoolean("debug", false));

        newConfig.set("mysql.enabled", false);
        newConfig.set("mysql.host", "127.0.0.1");
        newConfig.set("mysql.port", 3306);
        newConfig.set("mysql.database", "tab");
        newConfig.set("mysql.username", "user");
        newConfig.set("mysql.password", "password");

        if (TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY) {
            newConfig.set("global-playerlist", oldConfig.getConfigurationSection("global-playerlist"));
            newConfig.set("global-playerlist.update-latency", false);
            newConfig.set("use-bukkit-permissions-manager", false);
        } else {
            newConfig.set("per-world-playerlist", oldConfig.getConfigurationSection("per-world-playerlist"));
        }
    }

    private void convertGroupsAndUsers(@NotNull ConfigurationFile oldConfig, @NotNull ConfigurationFile groups, @NotNull ConfigurationFile users) {
        groups.setValues(oldConfig.getConfigurationSection("Groups"));
        users.setValues(oldConfig.getConfigurationSection("Users"));

        String separator = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY ? "server" : "world";
        Map<String,Map<String,Object>> perWorldSettings = oldConfig.getConfigurationSection("per-" + separator + "-settings");
        Map<String,Object> groupMap = new LinkedHashMap<>();
        Map<String,Object> userMap = new LinkedHashMap<>();
        Map<String,Map<String,Object>> worldMap = new LinkedHashMap<>(perWorldSettings);
        for (Map.Entry<String, Map<String, Object>> worldEntry : worldMap.entrySet()) {
            for (Map.Entry<String, Object> entry2 : new LinkedHashMap<>(worldEntry.getValue()).entrySet()) {
                if (entry2.getKey().equalsIgnoreCase("Groups"))
                    groupMap.put(translateWorldGroup(oldConfig, worldEntry.getKey()),entry2.getValue());
                else if (entry2.getKey().equalsIgnoreCase("Users"))
                    userMap.put(translateWorldGroup(oldConfig, worldEntry.getKey()),entry2.getValue());
            }
        }
        groups.set("per-" + separator, groupMap);
        groups.set(TabConstants.DEFAULT_GROUP, groups.getConfigurationSection("_OTHER_"));
        groups.set("_OTHER_", null);
        users.set("per-" + separator, userMap);
        for (Object world : groups.getConfigurationSection("per-" + separator).keySet()) {
            String gPath = "per-" + separator + "." + world;
            if (!groups.hasConfigOption(gPath + "._OTHER_")) continue;
            groups.set(gPath + "." + TabConstants.DEFAULT_GROUP, groups.getObject(gPath + "._OTHER_"));
            groups.set(gPath + "._OTHER_", null);
        }
    }

    private String translateWorldGroup(@NotNull ConfigurationFile oldConfig, @NotNull String group) {
        String oldSeparator = oldConfig.hasConfigOption("multi-world-separator") ? oldConfig.getString("multi-world-separator") : "-";
        return group.replace(oldSeparator, ";");
    }

    public void convert301to302(@NotNull ConfigurationFile config) {
        if (!config.hasConfigOption("placeholders.remove-strings")) return;
        TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 3.0.1 to 3.0.2"));
        config.set("placeholders.remove-strings", null);
    }

    public void convert331to332(@NotNull ConfigurationFile config) {
        if (!config.hasConfigOption("scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients")) return;
        TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 3.3.1 to 3.3.2)"));
        config.set("scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients", null);
    }

    @SuppressWarnings("unchecked")
    public void convert332to400(@NotNull ConfigurationFile config) throws IOException {
        // Removed config options
        if (config.hasConfigOption("fix-pet-names")) {
            TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 3.3.2 to 4.0.0"));
            config.set("fix-pet-names", null);
            config.set("bossbar.disable-in-worlds", null);
            config.set("bossbar.disable-in-servers", null);
            config.set("scoreboard.disable-in-worlds", null);
            config.set("scoreboard.disable-in-servers", null);
            config.set("remove-ghost-players", null);
            config.set("global-playerlist.fill-profile-key", null);
        }

        // Merged refresh intervals
        Map<Object, Object> intervals = config.getConfigurationSection("placeholderapi-refresh-intervals");
        boolean updated = false;
        for (Map.Entry<?, ?> entry : new ArrayList<>(intervals.entrySet())) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                intervals.remove(entry.getKey());
                intervals.putAll(((Map<Object, Object>)value));
                updated = true;
            }
        }
        if (updated) config.save();

        // Merge layout to config
        File layoutFile = new File(TAB.getInstance().getDataFolder(), "layout.yml");
        if (layoutFile.exists()) {
            ConfigurationFile layout = new YamlConfigurationFile(null, layoutFile);
            config.set("layout", layout.getValues());
            Files.delete(layoutFile.toPath());
        }

        // Convert disabled worlds/servers into disable condition
        Consumer<Map<String, Object>> disabledConditionConverter = (map -> {
            List<String> newConditions = new ArrayList<>();
            boolean update = false;
            if (map.containsKey("disable-in-worlds") && map.get("disable-in-worlds") instanceof List) {
                update = true;
                List<String> worlds = (List<String>) map.get("disable-in-worlds");
                newConditions.addAll(worlds.stream().map(world -> "%world%=" + world).collect(Collectors.toList()));
            }
            if (map.containsKey("disable-in-servers") && map.get("disable-in-servers") instanceof List) {
                update = true;
                List<String> worlds = (List<String>) map.get("disable-in-servers");
                newConditions.addAll(worlds.stream().map(server -> "%server%=" + server).collect(Collectors.toList()));
            }
            if (update) {
                map.remove("disable-in-worlds");
                map.remove("disable-in-servers");
                map.put("disable-condition", String.join("|", newConditions));
                config.save();
            }
        });
        disabledConditionConverter.accept(config.getConfigurationSection("header-footer"));
        disabledConditionConverter.accept(config.getConfigurationSection("tablist-name-formatting"));
        disabledConditionConverter.accept(config.getConfigurationSection("scoreboard-teams"));
        disabledConditionConverter.accept(config.getConfigurationSection("scoreboard-teams.unlimited-nametag-mode"));
        disabledConditionConverter.accept(config.getConfigurationSection("yellow-number-in-tablist"));
        disabledConditionConverter.accept(config.getConfigurationSection("belowname-objective"));

        // Removed config option
        if (config.hasConfigOption("layout.hide-vanished-players")) config.set("layout.hide-vanished-players", null);
    }

    public void convert403to404(@NotNull ConfigurationFile config) {
        if (config.hasConfigOption("placeholders.register-tab-expansion")) {
            TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&ePerforming configuration conversion from 4.0.3 to 4.0.4"));
            config.set("placeholders.register-tab-expansion", null);
        }
        if (config.hasConfigOption("global-playerlist.update-latency")) {
            config.set("global-playerlist.update-latency", null);
        }
    }
}
