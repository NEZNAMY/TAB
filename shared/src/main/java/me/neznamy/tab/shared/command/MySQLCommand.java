package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.mysql.MySQL;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MySQLCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    protected MySQLCommand() {
        super("mysql", null);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length == 0) {
            sendMessages(sender, getMessages().getMySQLHelpMenu());
            return;
        }
        if (args[0].equalsIgnoreCase("download")) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_MYSQL_DOWNLOAD)) {
                download(sender);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else if (args[0].equalsIgnoreCase("upload")) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_MYSQL_UPLOAD)) {
                upload(sender);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else {
            for (String message : getMessages().getMySQLHelpMenu()) {
                sendMessage(sender, message);
            }
        }
    }

    private void download(@Nullable TabPlayer sender) {
        MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
        if (mysql == null) {
            sendMessage(sender, getMessages().getMySQLFailNotEnabled());
            return;
        }
        TAB.getInstance().getCPUManager().getMysqlThread().execute(() -> {
            try {
                YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
                YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
                CachedRowSet crs = mysql.getCRS("select * from tab_groups");
                while (crs.next()) {
                    groupFile.setProperty(crs.getString("group"), crs.getString("property"),
                            Server.byName(crs.getString("server")), World.byName(crs.getString("world")), crs.getString("value"));
                }
                crs = mysql.getCRS("select * from tab_users");
                while (crs.next()) {
                    userFile.setProperty(crs.getString("user"), crs.getString("property"),
                            Server.byName(crs.getString("server")), World.byName(crs.getString("world")), crs.getString("value"));
                }
                sendMessage(sender, getMessages().getMySQLDownloadSuccess());
            } catch (YAMLException | IOException | SQLException e) {
                sendMessage(sender, getMessages().getMySQLFailError());
                TAB.getInstance().getErrorManager().criticalError("MySQL download failed", e);
            }
        });
    }

    private void upload(@Nullable TabPlayer sender) {
        MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
        if (mysql == null) {
            sendMessage(sender, getMessages().getMySQLFailNotEnabled());
            return;
        }
        TAB.getInstance().getCPUManager().getMysqlThread().execute(() -> {
            try {
                YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("config/groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
                YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("config/users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
                mysql.execute("DELETE FROM tab_groups");
                mysql.execute("DELETE FROM tab_users");
                upload(groupFile, TAB.getInstance().getConfiguration().getGroups());
                upload(userFile, TAB.getInstance().getConfiguration().getUsers());
                sendMessage(sender, getMessages().getMySQLUploadSuccess());
            } catch (YAMLException | IOException | SQLException e) {
                sendMessage(sender, getMessages().getMySQLFailError());
                TAB.getInstance().getErrorManager().criticalError("MySQL upload failed", e);
            }
        });
    }

    private void upload(@NotNull YamlPropertyConfigurationFile file, @NotNull PropertyConfiguration mysqlTable) {
        for (String name : file.getAllEntries()) {
            for (Map.Entry<String, Object> property : file.getGlobalSettings(name).entrySet()) {
                mysqlTable.setProperty(name, property.getKey(), null, null, toString(property.getValue()));
            }
            for (Map.Entry<String, Map<String, Object>> world : file.getPerWorldSettings(name).entrySet()) {
                if (world.getValue() == null) continue;
                for (Map.Entry<String, Object> property : world.getValue().entrySet()) {
                    mysqlTable.setProperty(name, property.getKey(), null, World.byName(world.getKey()), toString(property.getValue()));
                }
            }
            for (Map.Entry<String, Map<String, Object>> server : file.getPerServerSettings(name).entrySet()) {
                if (server.getValue() == null) continue;
                for (Map.Entry<String, Object> property : server.getValue().entrySet()) {
                    mysqlTable.setProperty(name, property.getKey(), Server.byName(server.getKey()), null, toString(property.getValue()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String toString(@NotNull Object obj) {
        if (obj instanceof List) {
            return ((List<Object>)obj).stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
        return obj.toString();
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        return getStartingArgument(Arrays.asList("download", "upload"), arguments[0]);
    }
}
