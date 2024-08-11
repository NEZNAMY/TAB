package me.neznamy.tab.shared.config.files.config;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.Converter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Getter
public class Config {

    @NotNull private final ConfigurationFile config = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("config/config.yml"),
            new File(TAB.getInstance().getDataFolder(), "config.yml"));

    @Nullable private BelownameConfiguration belowname;
    @Nullable private BossBarConfiguration bossbar;
    @NotNull private final ConditionsSection conditions;
    @Nullable private GlobalPlayerListConfiguration globalPlayerList;
    @Nullable private HeaderFooterConfiguration headerFooter;
    @Nullable private LayoutConfiguration layout;
    @Nullable private MySQLConfiguration mysql;
    @Nullable private PerWorldPlayerListConfiguration perWorldPlayerList;
    @Nullable private PingSpoofConfiguration pingSpoof;
    @NotNull private final PlaceholderRefreshConfiguration refresh;
    @NotNull private final PlaceholderReplacementsConfiguration replacements;
    @NotNull private final PlaceholdersConfiguration placeholders;
    @Nullable private PlayerListObjectiveConfiguration playerlistObjective;
    @Nullable private ScoreboardConfiguration scoreboard;
    @Nullable private SortingConfiguration sorting;
    @Nullable private TablistFormattingConfiguration tablistFormatting;
    @Nullable private TeamConfiguration teams;

    private final boolean preventSpectatorEffect = config.getBoolean("prevent-spectator-effect.enabled", false);
    private final boolean bukkitPermissions = TAB.getInstance().getPlatform().isProxy() && config.getBoolean("use-bukkit-permissions-manager", false);
    private final boolean debugMode = config.getBoolean("debug", false);
    private final boolean onlineUuidInTabList = config.getBoolean("use-online-uuid-in-tablist", true);
    private final boolean pipelineInjection = getSecretOption("pipeline-injection", true);
    @NotNull private final String serverName = getSecretOption("server-name", "N/A");
    private final int permissionRefreshInterval = config.getInt("permission-refresh-interval", 1000);
    private final boolean enableRedisHook = config.getBoolean("enable-redisbungee-support", true);

    /** If enabled, groups are assigned via permissions instead of permission plugin */
    private final boolean groupsByPermissions = config.getBoolean("assign-groups-by-permissions", false);

    /** List of group permissions to iterate through if {@link #groupsByPermissions} is {@code true} */
    @NotNull private final List<String> primaryGroupFindingList = config.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

    public Config() throws IOException {
        Converter converter = new Converter();
        converter.convert292to300(config);
        converter.convert301to302(config);
        converter.convert332to400(config);
        converter.convert409to410(config);
        converter.convert415to500(config);

        conditions = new ConditionsSection(config);
        refresh = new PlaceholderRefreshConfiguration(config);
        replacements = new PlaceholderReplacementsConfiguration(config);
        placeholders = new PlaceholdersConfiguration(config);
        if (config.getBoolean("belowname-objective.enabled", false)) belowname = new BelownameConfiguration(config);
        if (config.getBoolean("bossbar.enabled", false)) bossbar = new BossBarConfiguration(config);
        if (config.getBoolean("global-playerlist.enabled", false)) globalPlayerList = new GlobalPlayerListConfiguration(config);
        if (config.getBoolean("header-footer.enabled", true)) headerFooter = new HeaderFooterConfiguration(config);
        if (config.getBoolean("layout.enabled", false)) layout = new LayoutConfiguration(config);
        if (config.getBoolean("mysql.enabled", false)) mysql = new MySQLConfiguration(config);
        if (config.getBoolean("per-world-playerlist.enabled", false)) perWorldPlayerList = new PerWorldPlayerListConfiguration(config);
        if (config.getBoolean("ping-spoof.enabled", false)) pingSpoof = new PingSpoofConfiguration(config);
        if (config.getBoolean("playerlist-objective.enabled", true)) playerlistObjective = new PlayerListObjectiveConfiguration(config);
        if (config.getBoolean("scoreboard.enabled", false)) scoreboard = new ScoreboardConfiguration(config);
        if (config.getBoolean("scoreboard-teams.enabled", true) || config.getBoolean("layout.enabled", false)) sorting = new SortingConfiguration(config);
        if (config.getBoolean("tablist-name-formatting.enabled", false)) tablistFormatting = new TablistFormattingConfiguration(config);
        if (config.getBoolean("scoreboard-teams.enabled", false)) teams = new TeamConfiguration(config);
    }

    /**
     * Returns value of hidden config option with specified path if it exists, defaultValue otherwise
     *
     * @param   path
     *          path to value
     * @param   defaultValue
     *          value to return if option is not present in file
     * @return  value with specified path or default value if not present
     * @param   <T>
     *          Class type of the config option
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T> T getSecretOption(@NotNull String path, @NotNull T defaultValue) {
        Object value = config.getObject(path);
        return value == null ? defaultValue : (T) value;
    }
}
