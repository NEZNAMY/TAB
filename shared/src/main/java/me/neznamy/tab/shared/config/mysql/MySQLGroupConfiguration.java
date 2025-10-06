package me.neznamy.tab.shared.config.mysql;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLGroupConfiguration implements PropertyConfiguration {

    private final MySQL mysql;

    private final Map<String, Map<String, Object>> values = new HashMap<>();
    private final Map<String, Map<String, Map<String, Object>>> perWorld = new HashMap<>();
    private final Map<String, Map<String, Map<String, Object>>> perServer = new HashMap<>();

    public MySQLGroupConfiguration(@NonNull MySQL mysql) throws SQLException {
        this.mysql = mysql;
        mysql.execute("create table if not exists tab_groups (`group` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
        CachedRowSet crs = mysql.getCRS("select * from tab_groups");
        if (crs.size() == 0) {
            TAB.getInstance().getConfigHelper().startup().startupWarn("[MySQL] Using MySQL to store groups and users, however, the database is empty. " +
                    "You can get started by uploading existing data in files using \"/" + TAB.getInstance().getPlatform().getCommand() + " mysql upload\". Further modifications can " +
                    "be done using property commands (/" + TAB.getInstance().getPlatform().getCommand() + " <group / player> <name> <property> <value...>).");
            return;
        }
        while (crs.next()) {
            String group = crs.getString("group");
            if (!group.equals(TabConstants.DEFAULT_GROUP)) group = group.toLowerCase(Locale.US);
            String property = crs.getString("property");
            String value = crs.getString("value");
            String world = crs.getString("world");
            String server = crs.getString("server");
            setProperty0(group, property, Server.byName(server), World.byName(world), value);
            checkProperty("MySQL", "group", group, property, server, world, true);
        }
    }

    @Override
    @SneakyThrows
    public void setProperty(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
        String lowercaseGroup = group.equals(TabConstants.DEFAULT_GROUP) ? group : group.toLowerCase(Locale.US);
        if (getProperty(lowercaseGroup, property, server, world) != null) {
            mysql.execute("delete from `tab_groups` where `group` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?",
                    lowercaseGroup, property, world == null ? null : world.getName(), server == null ? null : server.getName());
        }
        setProperty0(lowercaseGroup, property, server, world, value);
        if (value != null) mysql.execute("insert into `tab_groups` (`group`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)",
                lowercaseGroup, property, value, world == null ? null : world.getName(), server == null ? null : server.getName());
    }
    
    private String querySymbol(boolean isNull) {
        return isNull ? "is" : "=";
    }

    private void setProperty0(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world, @Nullable String value) {
        if (world != null) {
            perWorld.computeIfAbsent(world.getName(), w -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        } else if (server != null) {
            perServer.computeIfAbsent(server.getName(), s -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        } else {
            values.computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        }
    }

    @Override
    public String[] getProperty(@NonNull String group, @NonNull String property, @Nullable Server server, @Nullable World world) {
        String lowercaseGroup = group.equals(TabConstants.DEFAULT_GROUP) ? group : group.toLowerCase(Locale.US);
        String worldName = world == null ? null : world.getName();
        String serverName = server == null ? null : server.getName();
        Object value;
        if ((value = perWorld.getOrDefault(worldName, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,world=%s", lowercaseGroup, worldName)};
        }
        if ((value = perWorld.getOrDefault(worldName, new HashMap<>()).getOrDefault(TabConstants.DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,world=%s", TabConstants.DEFAULT_GROUP, worldName)};
        }
        if ((value = perServer.getOrDefault(serverName, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,server=%s", lowercaseGroup, serverName)};
        }
        if ((value = perServer.getOrDefault(serverName, new HashMap<>()).getOrDefault(TabConstants.DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,server=%s", TabConstants.DEFAULT_GROUP, serverName)};
        }
        if ((value = values.getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s", lowercaseGroup)};
        }
        if ((value = values.getOrDefault(TabConstants.DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s", TabConstants.DEFAULT_GROUP)};
        }
        return new String[0];
    }

    @Override
    public void remove(@NonNull String group) {
        values.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, null, null));
        perWorld.forEach((world, stringMapMap) -> stringMapMap.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, World.byName(world), null)));
        perServer.forEach((server, stringMapMap) -> stringMapMap.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, Server.byName(server), null, null)));
    }

    @Override
    @NotNull
    public Map<String, Object> getGlobalSettings(@NonNull String name) {
        return values.getOrDefault(name, Collections.emptyMap());
    }

    @Override
    @NotNull
    public Map<String, Map<String, Object>> getPerWorldSettings(@NonNull String name) {
        return convertMap(perWorld, name);
    }

    @Override
    @NotNull
    public Map<String, Map<String, Object>> getPerServerSettings(@NonNull String name) {
        return convertMap(perServer, name);
    }

    @Override
    @NotNull
    public Set<String> getAllEntries() {
        Set<String> set = new HashSet<>(values.keySet());
        perWorld.values().forEach(map -> set.addAll(map.keySet()));
        perServer.values().forEach(map -> set.addAll(map.keySet()));
        return set;
    }
}