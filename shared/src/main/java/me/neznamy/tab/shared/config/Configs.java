package me.neznamy.tab.shared.config;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.files.Animations;
import me.neznamy.tab.shared.config.files.Config;
import me.neznamy.tab.shared.config.mysql.MySQL;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import me.neznamy.tab.shared.config.skin.SkinManager;
import me.neznamy.tab.shared.data.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Core of loading configuration files
 */
@Getter
public class Configs {

    /** config.yml file */
    private final Config config = new Config();

    /** animations.yml file */
    private final Animations animations = new Animations();

    //messages.yml file
    private final MessageFile messages = new MessageFile();

    //playerdata.yml, used for bossbar & scoreboard toggle saving
    private final ConfigurationFile playerData = new YamlConfigurationFile(
            new ByteArrayInputStream(new byte[0]),
            new File(TAB.getInstance().getDataFolder(), "playerdata.yml")
    );

    /** Skin cache file for Layout and Head components */
    private final ConfigurationFile skinCache = new YamlConfigurationFile(
            new ByteArrayInputStream(new byte[0]),
            new File(TAB.getInstance().getDataFolder(), "skincache.yml")
    );

    private final SkinManager skinManager = new SkinManager(skinCache);

    private PropertyConfiguration groups;

    private PropertyConfiguration users;

    private MySQL mysql;

    /**
     * Constructs new instance and loads configuration files.
     * If needed, converts old configuration files as well.
     *
     * @throws  IOException
     *          if File I/O operation fails
     * @throws  YAMLException
     *          if files contain syntax errors
     */
    public Configs() throws IOException {
        File errorLog = TAB.getInstance().getErrorManager().getErrorLog();
        if (errorLog.length() > TabConstants.MAX_LOG_SIZE) {
            TAB.getInstance().getConfigHelper().startup().startupWarn(errorLog, "The file has reached its size limit (16MB). No new errors will be logged. " +
                    "Take a look at the existing logged errors, as they may have caused the plugin to not work properly " +
                    "in the past and if not fixed, will most likely cause problems in the future as well. If you are using latest version " +
                    "of the plugin, consider reporting them.");
        }
        if (config.getMysql() != null) {
            try {
                mysql = new MySQL(config.getMysql());
                mysql.openConnection();
                groups = new MySQLGroupConfiguration(mysql);
                users = new MySQLUserConfiguration(mysql);
                return;
            } catch (SQLException e) {
                TAB.getInstance().getErrorManager().mysqlConnectionFailed(e);
            }
        }
        groups = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
        users = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
    }

    /**
     * Returns world/server group name which specified element belongs to.
     * If nothing is found, element itself is returned.
     *
     * @param   serverGroups
     *          Defined groups
     * @param   element
     *          Element to find
     * @return  Group containing the element or element itself if not found
     */
    @NotNull
    public String getGroup(@NonNull Collection<String> serverGroups, @Nullable String element) {
        if (element == null) return "null";
        if (serverGroups.isEmpty()) return element;
        for (Object worldGroup : serverGroups) {
            for (String definedWorld : worldGroup.toString().split(";")) {
                if (definedWorld.endsWith("*")) {
                    if (element.toLowerCase().startsWith(definedWorld.substring(0, definedWorld.length()-1).toLowerCase())) return worldGroup.toString();
                } else if (definedWorld.startsWith("*")) {
                    if (element.toLowerCase().endsWith(definedWorld.substring(1).toLowerCase())) return worldGroup.toString();
                }  else {
                    if (element.equalsIgnoreCase(definedWorld)) return worldGroup.toString();
                }
            }
        }
        return element;
    }

    /**
     * Returns world/server group name which specified element belongs to.
     * If nothing is found, element itself is returned.
     * This also hooks into global playerlist to see groups defined there.
     *
     * @param   serverGroups
     *          Defined groups
     * @param   server
     *          Server to find
     * @return  Group containing the element or element itself if not found
     */
    @NotNull
    public String getServerGroup(@NonNull Collection<String> serverGroups, @Nullable Server server) {
        String globalGroup = tryServerGroup(serverGroups, server);
        if (globalGroup != null) return globalGroup;

        // Use existing logic to check config key for server group (separated by ';')
        return getGroup(serverGroups, server == null ? null : server.getName());
    }

    @Nullable
    private String tryServerGroup(@NonNull Collection<String> serverGroups, @Nullable Server server) {
        if (serverGroups.isEmpty() || server == null) return null;
        if (serverGroups.contains(server.getName())) return server.getName();
        if (server.getServerGroup() != null) {
            if (serverGroups.contains(server.getServerGroup().getName())) return server.getServerGroup().getName();
        }
        return null;
    }
}