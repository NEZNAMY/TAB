package me.neznamy.tab.shared.config;

import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlConfigurationFile;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQL;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    @Getter private final ConfigurationFile config = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("config.yml"),
            new File(TAB.getInstance().getDataFolder(), "config.yml"));

    @Getter private final boolean bukkitPermissions = TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY && config.getBoolean("use-bukkit-permissions-manager", false);
    @Getter private final boolean debugMode = config.getBoolean("debug", false);
    @Getter private final boolean onlineUuidInTabList = config.getBoolean("use-online-uuid-in-tablist", true);
    @Getter private final boolean pipelineInjection = getSecretOption("pipeline-injection", true);
    @Getter private final String serverName = getSecretOption("server-name", "N/A");

    //animations.yml file
    @Getter private final ConfigurationFile animationFile = new YamlConfigurationFile(getClass().getClassLoader().getResourceAsStream("animations.yml"),
            new File(TAB.getInstance().getDataFolder(), "animations.yml"));

    //messages.yml file
    @Getter private final MessageFile messages = new MessageFile();

    //playerdata.yml, used for bossbar & scoreboard toggle saving
    private ConfigurationFile playerdata;

    @Getter private PropertyConfiguration groups;

    @Getter private PropertyConfiguration users;

    @Getter private MySQL mysql;

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
        converter.convert2810to290(animationFile);
        converter.convert292to300(config);
        converter.convert301to302(config);
        converter.convert331to332(config);
        converter.convert332to400(config);
        converter.convert403to404(config);
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
                mysql.openConnection();
                groups = new MySQLGroupConfiguration(mysql);
                users = new MySQLUserConfiguration(mysql);
                return;
            } catch (SQLException | ClassNotFoundException e) {
                TAB.getInstance().getErrorManager().criticalError("Failed to connect to MySQL", e);
            }
        }
        groups = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
        users = new YamlPropertyConfigurationFile(getClass().getClassLoader().getResourceAsStream("users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
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
    public @NotNull <T> T getSecretOption(@NotNull String path, @NotNull T defaultValue) {
        Object value = config.getObject(path);
        return value == null ? defaultValue : (T) value;
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

    public String getGroup(@NotNull List<Object> serverGroups, @Nullable String element) {
        if (serverGroups.isEmpty() || element == null) return element;
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
}