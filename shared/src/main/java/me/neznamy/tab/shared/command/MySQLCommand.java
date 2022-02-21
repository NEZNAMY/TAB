package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.MySQL;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import org.yaml.snakeyaml.error.YAMLException;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MySQLCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    protected MySQLCommand() {
        super("mysql", null);
    }

    @Override
    public void execute(TabPlayer sender, String[] args) {
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

    private void download(TabPlayer sender) {
        MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
        if (mysql == null) {
            sendMessage(sender, getMessages().getMySQLFailNotEnabled());
            return;
        }
        TAB.getInstance().getCPUManager().runTask(() -> {
            try {
                YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
                YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
                CachedRowSet crs = mysql.getCRS("select * from tab_groups");
                while (crs.next()) {
                    groupFile.setProperty(crs.getString("group"), crs.getString("property"),
                            crs.getString("server"), crs.getString("world"), crs.getString("value"));
                }
                crs = mysql.getCRS("select * from tab_users");
                while (crs.next()) {
                    userFile.setProperty(crs.getString("user"), crs.getString("property"),
                            crs.getString("server"), crs.getString("world"), crs.getString("value"));
                }
                sendMessage(sender, getMessages().getMySQLDownloadSuccess());
            } catch (YAMLException | IOException | SQLException e) {
                sendMessage(sender, getMessages().getMySQLFailError());
                TAB.getInstance().getErrorManager().criticalError("MySQL download failed", e);
            }
        });
    }

    private void upload(TabPlayer sender) {
        MySQL mysql = TAB.getInstance().getConfiguration().getMysql();
        if (mysql == null) {
            sendMessage(sender, getMessages().getMySQLFailNotEnabled());
            return;
        }
        TAB.getInstance().getCPUManager().runTask(() -> {
            try {
                YamlPropertyConfigurationFile groupFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("groups.yml"), new File(TAB.getInstance().getDataFolder(), "groups.yml"));
                YamlPropertyConfigurationFile userFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("users.yml"), new File(TAB.getInstance().getDataFolder(), "users.yml"));
                upload(groupFile, TAB.getInstance().getConfiguration().getGroups());
                upload(userFile, TAB.getInstance().getConfiguration().getUsers());
                sendMessage(sender, getMessages().getMySQLUploadSuccess());
            } catch (YAMLException | IOException e) {
                sendMessage(sender, getMessages().getMySQLFailError());
                TAB.getInstance().getErrorManager().criticalError("MySQL download failed", e);
            }
        });
    }

    private void upload(YamlPropertyConfigurationFile file, PropertyConfiguration mysqlTable) {
        for (String name : file.getAllEntries()) {
            for (Map.Entry<String, String> property : file.getGlobalSettings(name).entrySet()) {
                mysqlTable.setProperty(name, property.getKey(), null, null, property.getValue());
            }
            for (Map.Entry<String, Map<String, String>> world : file.getPerWorldSettings(name).entrySet()) {
                if (world.getValue() == null) continue;
                for (Map.Entry<String, String> property : world.getValue().entrySet()) {
                    mysqlTable.setProperty(name, property.getKey(), null, world.getKey(), property.getValue());
                }
            }
            for (Map.Entry<String, Map<String, String>> server : file.getPerServerSettings(name).entrySet()) {
                if (server.getValue() == null) continue;
                for (Map.Entry<String, String> property : server.getValue().entrySet()) {
                    mysqlTable.setProperty(name, property.getKey(), server.getKey(), null, property.getValue());
                }
            }
        }
    }

    @Override
    public List<String> complete(TabPlayer sender, String[] arguments) {
        return getStartingArgument(Arrays.asList("download", "upload"), arguments[0]);
    }
}
