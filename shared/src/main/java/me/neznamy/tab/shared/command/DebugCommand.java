package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab debug" subcommand
 */
public class DebugCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public DebugCommand() {
        super("debug", TabConstants.Permission.COMMAND_DEBUG);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        TabPlayer analyzed = null;
        if (args.length > 0) {
            analyzed = TAB.getInstance().getPlayer(args[0]);
            if (analyzed == null) {
                sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
                return;
            }
        }
        if (analyzed == null && sender != null) {
            analyzed = sender;
        }
        debug(sender, analyzed);
    }

    /**
     * Performs debug on player and displays output
     *
     * @param   sender
     *          command sender or null if console
     * @param   analyzed
     *          player to be analyzed
     */
    private void debug(@Nullable TabPlayer sender, @Nullable TabPlayer analyzed) {
        TAB tab = TAB.getInstance();
        String separator = "&7&m>-------------------------------<";
        sendMessage(sender, "&3[TAB] &a&lShowing debug information");
        sendMessage(sender, separator);
        sendMessage(sender, "&6Server version: &b" + tab.getPlatform().getServerVersionInfo());
        sendMessage(sender, "&6Plugin version: &b" + TabConstants.PLUGIN_VERSION);
        sendMessage(sender, "&6Permission plugin: &b" + TAB.getInstance().getGroupManager().getPermissionPlugin());
        sendMessage(sender, "&6Permission group choice logic: &b" + getGroupChoiceLogic());
        sendMessage(sender, "&6Sorting system: &b" + getSortingType());
        sendMessage(sender, "&6Storage type: &b" + (tab.getConfiguration().getGroups() instanceof ConfigurationFile ? "File" : "MySQL"));
        sendMessage(sender, separator);
        if (analyzed == null) return;
        if (!analyzed.isLoaded()) {
            sendMessage(sender, "&cThe specified player is not loaded. This is either because player failed to load" +
                    " due to an error (see TAB's folder for errors.log file) or the plugin is overloaded (see /tab cpu).");
            return;
        }
        sendMessage(sender, "&ePlayer: &a" + analyzed.getName());
        if (analyzed instanceof ProxyTabPlayer) {
            char versionRequired = TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.charAt(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.length()-1);
            sendMessage(sender, "&eBridge connection: " + (((ProxyTabPlayer)analyzed).isBridgeConnected() ?
                    "&aConnected" : "&cNot connected (requires Bridge version " + versionRequired + ".x.x installed)"));
        }
        sendMessage(sender, getGroup(analyzed));
        sendMessage(sender, getTeamName(analyzed));
        sendMessage(sender, getTeamNameNote(analyzed));
        if (tab.getFeatureManager().isFeatureEnabled(TabConstants.Feature.PLAYER_LIST)) {
            PlayerList playerlist = tab.getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
            boolean disabledPlayerlist = playerlist.getDisableChecker().isDisabledPlayer(analyzed);
            showProperty(sender, analyzed, TabConstants.Property.TABPREFIX, disabledPlayerlist);
            showProperty(sender, analyzed, TabConstants.Property.TABSUFFIX, disabledPlayerlist);
            showProperty(sender, analyzed, TabConstants.Property.CUSTOMTABNAME, disabledPlayerlist);
        } else {
            sendMessage(sender, "&atabprefix: &cDisabled");
            sendMessage(sender, "&atabsuffix: &cDisabled");
            sendMessage(sender, "&acustomtabname: &cDisabled");
        }
        if (tab.getNameTagManager() != null) {
            boolean disabledNametags = tab.getNameTagManager().getDisableChecker().isDisabledPlayer(analyzed);
            showProperty(sender, analyzed, TabConstants.Property.TAGPREFIX, disabledNametags);
            showProperty(sender, analyzed, TabConstants.Property.TAGSUFFIX, disabledNametags);
            NameTagX nameTagX = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
            if (nameTagX != null) {
                boolean disabledUnlimited = nameTagX.isPlayerDisabled(analyzed);
                for (String line : getExtraLines()) {
                    showProperty(sender, analyzed, line, disabledUnlimited);
                }
            }
        } else {
            sendMessage(sender, "&atagprefix: &cDisabled");
            sendMessage(sender, "&atagsuffix: &cDisabled");
        }
        sendMessage(sender, separator);
    }

    /**
     * Returns group choice logic
     *
     * @return  group choice logic
     */
    private @NotNull String getGroupChoiceLogic() {
        if (TAB.getInstance().getGroupManager().isGroupsByPermissions()) {
            return "Permissions";
        }
        return "Primary group";

    }

    /**
     * Returns sorting type
     *
     * @return  sorting type
     */
    private @NotNull String getSortingType() {
        Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
        if (sorting != null) {
            return sorting.typesToString();
        } else {
            return "&cDISABLED";
        }
    }

    /**
     * Returns all info about player's group
     *
     * @param   analyzed
     *          player to check group of
     * @return  all info about player's group
     */
    private @NotNull String getGroup(@NotNull TabPlayer analyzed) {
        if (TAB.getInstance().getGroupManager().isGroupsByPermissions()) {
            return "&eHighest group permission: &8tab.group.&a" + analyzed.getGroup();
        }
        return "&ePrimary permission group: &a" + analyzed.getGroup();
    }

    /**
     * Returns team name of specified player
     *
     * @param   analyzed
     *          player to get team name of
     * @return  team name of specified player
     */
    private @NotNull String getTeamName(@NotNull TabPlayer analyzed) {
        Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
        if (sorting == null) return "";
        if (TAB.getInstance().getNameTagManager() != null &&
                TAB.getInstance().getNameTagManager().getDisableChecker().isDisabledPlayer(analyzed)) {
            return "&eTeam name: &cSorting is disabled in player's world/server";
        }
        return "&eTeam name: &a" + (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.LAYOUT)
                ? sorting.getFullTeamName(analyzed) : sorting.getShortTeamName(analyzed));
    }

    /**
     * Returns team name note of specified player
     *
     * @param   analyzed
     *          player to get team name note of
     * @return  team name note of specified player
     */
    private @NotNull String getTeamNameNote(@NotNull TabPlayer analyzed) {
        Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
        if (sorting == null) return "";
        if (TAB.getInstance().getNameTagManager() != null &&
                TAB.getInstance().getNameTagManager().getDisableChecker().isDisabledPlayer(analyzed)) {
            return "";
        }
        return "&eSorting note: &r" + sorting.getTeamNameNote(analyzed);
    }

    /**
     * Returns list of extra properties if unlimited nametag mode is enabled
     *
     * @return  list of extra properties
     */
    public @NotNull List<String> getExtraLines() {
        if (!TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS)) return Collections.emptyList();
        List<String> lines = new ArrayList<>(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.unlimited-nametag-mode.dynamic-lines"));
        Map<String, Number> staticLines = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines");
        lines.addAll(staticLines.keySet());
        lines.remove(TabConstants.Property.NAMETAG);
        lines.add(TabConstants.Property.CUSTOMTAGNAME);
        return lines;
    }

    /**
     * Shows value and source of player's property
     *
     * @param   sender
     *          command sender or null if console
     * @param   analyzed
     *          analyzed player
     * @param   property
     *          property name
     * @param   disabled
     *          if feature the property belongs to is disabled or not
     */
    private void showProperty(@Nullable TabPlayer sender, @NotNull TabPlayer analyzed, @NotNull String property, boolean disabled) {
        if (disabled) {
            sendMessage(sender, "&a" + property + ": &cDisabled in player's world/server");
        } else {
            Property pr = analyzed.getProperty(property);
            String rawValue = EnumChatFormat.decolor(pr.getCurrentRawValue());
            String value = String.format((EnumChatFormat.color("&a%s: &e\"&r%s&r&e\" &7(Source: %s)")), property, rawValue, pr.getSource());
            sendRawMessage(sender, value);
        }
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        return arguments.length == 1 ? getOnlinePlayers(arguments[0]) : new ArrayList<>();
    }
}