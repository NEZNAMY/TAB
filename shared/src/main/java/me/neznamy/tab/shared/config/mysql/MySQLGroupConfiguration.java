package me.neznamy.tab.shared.config.mysql;

import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLGroupConfiguration implements PropertyConfiguration {

    private final MySQL mysql;

    private final Map<String, Map<String, Object>> values = new HashMap<>();
    private final Map<String, Map<String, Map<String, Object>>> perWorld = new HashMap<>();
    private final Map<String, Map<String, Map<String, Object>>> perServer = new HashMap<>();

    public MySQLGroupConfiguration(MySQL mysql) throws SQLException {
        this.mysql = mysql;
        mysql.execute("create table if not exists tab_groups (`group` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
        CachedRowSet crs = mysql.getCRS("select * from tab_groups");
        while (crs.next()) {
            String group = crs.getString("group");
            if (!group.equals(TabConstants.DEFAULT_GROUP)) group = group.toLowerCase(Locale.US);
            String property = crs.getString("property");
            String value = crs.getString("value");
            String world = crs.getString("world");
            String server = crs.getString("server");
            setProperty0(group, property, server, world, value);
        }
    }

    @Override
    public void setProperty(String group, String property, String server, String world, String value) {
        String lowercaseGroup = group.equals(TabConstants.DEFAULT_GROUP) ? group : group.toLowerCase(Locale.US);
        try {
            if (getProperty(lowercaseGroup, property, server, world) != null) {
                mysql.execute("delete from `tab_groups` where `group` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?", lowercaseGroup, property, world, server);
            }
            setProperty0(lowercaseGroup, property, server, world, value);
            if (value != null) mysql.execute("insert into `tab_groups` (`group`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)", lowercaseGroup, property, value, world, server);
        } catch (SQLException e) {
            TAB.getInstance().getErrorManager().printError("Failed to execute MySQL query", e);
        }
    }
    
    private String querySymbol(boolean isNull) {
        return isNull ? "is" : "=";
    }

    private void setProperty0(String group, String property, String server, String world, String value) {
        if (world != null) {
            perWorld.computeIfAbsent(world, w -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        } else if (server != null) {
            perServer.computeIfAbsent(server, s -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        } else {
            values.computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
        }
    }

    @Override
    public String[] getProperty(String group, String property, String server, String world) {
        String lowercaseGroup = group.equals(TabConstants.DEFAULT_GROUP) ? group : group.toLowerCase(Locale.US);
        Object value;
        if ((value = perWorld.getOrDefault(world, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,world=%s", lowercaseGroup, world)};
        }
        if ((value = perWorld.getOrDefault(world, new HashMap<>()).getOrDefault(TabConstants.DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,world=%s", TabConstants.DEFAULT_GROUP, world)};
        }
        if ((value = perServer.getOrDefault(server, new HashMap<>()).getOrDefault(lowercaseGroup, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,server=%s", lowercaseGroup, server)};
        }
        if ((value = perServer.getOrDefault(server, new HashMap<>()).getOrDefault(TabConstants.DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("group=%s,server=%s", TabConstants.DEFAULT_GROUP, server)};
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
    public void remove(String group) {
        values.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, null, null));
        perWorld.keySet().forEach(world -> perWorld.get(world).getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, world, null)));
        perServer.keySet().forEach(server -> perServer.get(server).getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, server, null, null)));
    }

    @Override
    public Map<String, Object> getGlobalSettings(String name) {
        return values.getOrDefault(name, Collections.emptyMap());
    }

    @Override
    public Map<String, Map<String, Object>> getPerWorldSettings(String name) {
        return convertMap(perWorld, name);
    }

    @Override
    public Map<String, Map<String, Object>> getPerServerSettings(String name) {
        return convertMap(perServer, name);
    }

    @Override
    public Set<String> getAllEntries() {
        Set<String> set = new HashSet<>(values.keySet());
        perWorld.values().forEach(map -> set.addAll(map.keySet()));
        perServer.values().forEach(map -> set.addAll(map.keySet()));
        return set;
    }
}