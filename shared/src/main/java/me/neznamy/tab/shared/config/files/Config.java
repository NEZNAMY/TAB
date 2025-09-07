package me.neznamy.tab.shared.config.files;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ComponentConfiguration;
import me.neznamy.tab.shared.config.converter.LegacyConverter;
import me.neznamy.tab.shared.config.converter.ModernConverter;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQLConfiguration;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.belowname.BelowNameConfiguration;
import me.neznamy.tab.shared.features.bossbar.BossBarConfiguration;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerListConfiguration;
import me.neznamy.tab.shared.features.header.HeaderFooterConfiguration;
import me.neznamy.tab.shared.features.layout.LayoutConfiguration;
import me.neznamy.tab.shared.features.nametags.TeamConfiguration;
import me.neznamy.tab.shared.features.pingspoof.PingSpoofConfiguration;
import me.neznamy.tab.shared.features.playerlist.TablistFormattingConfiguration;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveConfiguration;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardConfiguration;
import me.neznamy.tab.shared.features.sorting.SortingConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementsConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholdersConfiguration;
import me.neznamy.tab.shared.placeholders.conditions.ConditionsSection;
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

    @Nullable private BelowNameConfiguration belowname;
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
    @NotNull private final ComponentConfiguration components;

    private final boolean preventSpectatorEffect = config.getBoolean("prevent-spectator-effect.enabled", false);
    private final boolean bukkitPermissions = TAB.getInstance().getPlatform().isProxy() && config.getBoolean("use-bukkit-permissions-manager", false);
    private final boolean debugMode = config.getBoolean("debug", false);
    private final boolean onlineUuidInTabList = config.getBoolean("use-online-uuid-in-tablist", true);
    private final boolean pipelineInjection = getSecretOption("pipeline-injection", true);
    @NotNull private final String serverName = getSecretOption("server-name", "N/A");
    private final int permissionRefreshInterval = config.getInt("permission-refresh-interval", 1000);
    private final boolean enableProxySupport = config.getBoolean("proxy-support.enabled", true);
    private final boolean packetEventsCompensation = config.getBoolean("compensate-for-packetevents-bug", false) && !TAB.getInstance().getPlatform().isSafeFromPacketEventsBug();

    /** If enabled, groups are assigned via permissions instead of permission plugin */
    private final boolean groupsByPermissions = config.getBoolean("assign-groups-by-permissions", false);

    /** List of group permissions to iterate through if {@link #groupsByPermissions} is {@code true} */
    @NotNull private final List<String> primaryGroupFindingList = config.getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

    public Config() throws IOException {
        LegacyConverter converter = new LegacyConverter();
        converter.convert292to300(config);
        converter.convert301to302(config);
        converter.convert332to400(config);
        converter.convert409to410(config);
        converter.convert412to413(config);
        converter.convert419to500(config);
        converter.convert501to502(config);
        converter.convert507to510(config);
        converter.convert521to522(config);

        ModernConverter modernConverter = new ModernConverter();
        modernConverter.convert(config);

        conditions = ConditionsSection.fromSection(config.getConfigurationSection("conditions"));
        refresh = PlaceholderRefreshConfiguration.fromSection(config.getConfigurationSection("placeholder-refresh-intervals"));
        replacements = PlaceholderReplacementsConfiguration.fromSection(config.getConfigurationSection("placeholder-output-replacements"));
        placeholders = PlaceholdersConfiguration.fromSection(config.getConfigurationSection("placeholders"));
        components = ComponentConfiguration.fromSection(config.getConfigurationSection("components"));

        if (config.getBoolean("belowname-objective.enabled", false)) belowname = BelowNameConfiguration.fromSection(config.getConfigurationSection("belowname-objective"));
        if (config.getBoolean("bossbar.enabled", false)) bossbar = BossBarConfiguration.fromSection(config.getConfigurationSection("bossbar"));
        if (config.getBoolean("global-playerlist.enabled", false)) globalPlayerList = GlobalPlayerListConfiguration.fromSection(config.getConfigurationSection("global-playerlist"));
        if (config.getBoolean("header-footer.enabled", true)) headerFooter = HeaderFooterConfiguration.fromSection(config.getConfigurationSection("header-footer"));
        if (config.getBoolean("layout.enabled", false)) layout = LayoutConfiguration.fromSection(config.getConfigurationSection("layout"));
        if (config.getBoolean("mysql.enabled", false)) mysql = MySQLConfiguration.fromSection(config.getConfigurationSection("mysql"));
        if (config.getBoolean("per-world-playerlist.enabled", false)) perWorldPlayerList = PerWorldPlayerListConfiguration.fromSection(config.getConfigurationSection("per-world-playerlist"));
        if (config.getBoolean("ping-spoof.enabled", false)) pingSpoof = PingSpoofConfiguration.fromSection(config.getConfigurationSection("ping-spoof"));
        if (config.getBoolean("playerlist-objective.enabled", true)) playerlistObjective = PlayerListObjectiveConfiguration.fromSection(config.getConfigurationSection("playerlist-objective"));
        if (config.getBoolean("scoreboard.enabled", false)) scoreboard = ScoreboardConfiguration.fromSection(config.getConfigurationSection("scoreboard"));
        if (config.getBoolean("scoreboard-teams.enabled", true) || config.getBoolean("layout.enabled", false)) sorting = SortingConfiguration.fromSection(config.getConfigurationSection("scoreboard-teams"));
        if (config.getBoolean("tablist-name-formatting.enabled", false)) tablistFormatting = TablistFormattingConfiguration.fromSection(config.getConfigurationSection("tablist-name-formatting"));
        if (config.getBoolean("scoreboard-teams.enabled", false)) teams = TeamConfiguration.fromSection(config.getConfigurationSection("scoreboard-teams"));

        if (layout != null) {
            if (perWorldPlayerList != null) {
                TAB.getInstance().getConfigHelper().startup().startupWarn(config.getFile(), "Both per world playerlist and layout features are enabled, but layout makes per world playerlist redundant. " +
                        "Layout automatically works with all connected players and replaces real player entries with" +
                                " fake players, making per world playerlist completely useless as real players are pushed out of the playerlist. " +
                        "Disable per world playerlist for the same result, but with better performance.");
            }
            if (playerlistObjective != null) {
                TAB.getInstance().getConfigHelper().startup().startupWarn(config.getFile(), "Layout feature breaks playerlist-objective feature, because it replaces real player with fake slots " +
                        "with different usernames for more reliable functionality. Disable playerlist-objective feature, as it will only look bad " +
                        "and consume resources.");
            }
            if (preventSpectatorEffect) {
                TAB.getInstance().getConfigHelper().hint(config.getFile(), "Layout feature automatically includes prevent-spectator-effect, therefore the feature can be disabled " +
                        "for better performance, as it is not needed at all (assuming it is configured to always display some layout).");
            }
            if (globalPlayerList != null) {
                TAB.getInstance().getConfigHelper().startup().startupWarn(config.getFile(), "Both global playerlist and layout features are enabled, but layout makes global playerlist redundant. " +
                        "Layout automatically works with all connected players on the proxy and replaces real player entries with" +
                                " fake players, making global playerlist completely useless. " +
                        "Disable global playerlist for the same result, but with better performance.");
            }
        }
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
    private <T> T getSecretOption(@NonNull String path, @NonNull T defaultValue) {
        Object value = config.getObject(path);
        return value == null ? defaultValue : (T) value;
    }
}
