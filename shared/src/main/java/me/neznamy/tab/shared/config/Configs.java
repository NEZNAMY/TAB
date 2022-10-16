package me.neznamy.tab.shared.config;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Core of loading configuration files
 */
public class Configs {

    //config.yml file
    private final ConfigurationFile config = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream(TAB.getInstance().getPlatform().getConfigName()),
            new File(TAB.getInstance().getDataFolder(), "config.yml"));

    private final boolean bukkitPermissions = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY && config.getBoolean("use-bukkit-permissions-manager", false);
    private final boolean debugMode = config.getBoolean("debug", false);
    private final boolean removeGhostPlayers = getSecretOption("remove-ghost-players", false);
    private final boolean pipelineInjection = getSecretOption("pipeline-injection", true) && TAB.getInstance().getServerVersion().getMinorVersion() >= 8;

    //animations.yml file
    private final ConfigurationFile animation = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("animations.yml"),
            new File(TAB.getInstance().getDataFolder(), "animations.yml"));

    //messages.yml file
    private final MessageFile messages = new MessageFile();

    //playerdata.yml, used for bossbar & scoreboard toggle saving
    private ConfigurationFile playerdata;

    private final ConfigurationFile layout = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("layout.yml"),
            new File(TAB.getInstance().getDataFolder(), "layout.yml"));

    private PropertyConfiguration groupFile;

    private PropertyConfiguration userFile;

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
    public Configs() throws YAMLException, IOException {
        Converter converter = new Converter();
        converter.convertToV3(config);
        converter.removeOldOptions(config);
        converter.convertAnimationFile(animation);
        if (config.getBoolean("mysql.enabled", false)) {
            try {
                // Initialization to try to avoid java.sql.SQLException: No suitable driver found
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    Class.forName("com.mysql.jdbc.Driver");
                }
                mysql = new MySQL(config.getString("mysql.host", "127.0.0.1"), config.getInt("mysql.port", 3306),
                        config.getString("mysql.database", "tab"), config.getString("mysql.username", "user"), config.getString("mysql.password", "password"));
                groupFile = new MySQLGroupConfiguration(mysql);
                userFile = new MySQLUserConfiguration(mysql);
                return;
            } catch (SQLException | ClassNotFoundException e) {
                TAB.getInstance().getErrorManager().criticalError("Failed to connect to MySQL", e);
            }
        }
        groupFile = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
        userFile = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
    }

    /**
     * Returns value of hidden config option with specified path if it exists, defaultValue otherwise
     *
     * @param   path
     *          path to value
     * @param   defaultValue
     *          value to return if option is not present in file
     * @return  value with specified path or default value if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getSecretOption(String path, T defaultValue) {
        Object value = config.getObject(path);
        return value == null ? defaultValue : (T) value;
    }

    public MessageFile getMessages() {
        return messages;
    }

    public ConfigurationFile getConfig() {
        return config;
    }

    public boolean isRemoveGhostPlayers() {
        return removeGhostPlayers;
    }

    public ConfigurationFile getLayout() {
        return layout;
    }

    public ConfigurationFile getAnimationFile() {
        return animation;
    }

    public boolean isBukkitPermissions() {
        return bukkitPermissions;
    }

    public boolean isPipelineInjection() {
        return pipelineInjection;
    }

    public ConfigurationFile getPlayerDataFile() {
        if (playerdata == null) {
            File file = new File(TAB.getInstance().getDataFolder(), "playerdata.yml");
            try {
                if (file.exists() || file.createNewFile()) {
                    playerdata = new YamlConfigurationFile(null, file);
                }
            } catch (IOException e) {
                TAB.getInstance().getErrorManager().criticalError("Failed to load playerdata.yml", e);
            }
        }
        return playerdata;
    }

    public PropertyConfiguration getGroups() {
        return groupFile;
    }

    public PropertyConfiguration getUsers() {
        return userFile;
    }

    public MySQL getMysql() {
        return mysql;
    }

    public String getGroup(List<Object> serverGroups, String element) {
        if (serverGroups.isEmpty() || element == null) return element;
        for (Object worldGroup : serverGroups) {
            for (String definedWorld : worldGroup.toString().split(";")) {
                if (definedWorld.endsWith("*")) {
                    if (element.toLowerCase().startsWith(definedWorld.substring(0, definedWorld.length()-1).toLowerCase())) return worldGroup.toString();
                } else {
                    if (element.equalsIgnoreCase(definedWorld)) return worldGroup.toString();
                }
            }
        }
        return element;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}