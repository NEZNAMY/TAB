package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.MySQL;

public class MySQLGroupConfiguration implements PropertyConfiguration {

	private static final String DEFAULT_GROUP = "_DEFAULT_";
	private final MySQL mysql;

	private final Map<String, Map<String, String>> values = new HashMap<>();
	private final Map<String, Map<String, Map<String, String>>> perWorld = new HashMap<>();
	private final Map<String, Map<String, Map<String, String>>> perServer = new HashMap<>();

	public MySQLGroupConfiguration(MySQL mysql) throws SQLException {
		this.mysql = mysql;
		mysql.execute("create table if not exists tab_groups (`group` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
		CachedRowSet crs = mysql.getCRS("select * from tab_groups");
		while (crs.next()) {
			String group = crs.getString("group");
			String property = crs.getString("property");
			String value = crs.getString("value");
			String world = crs.getString("world");
			String server = crs.getString("server");
			TAB.getInstance().debug("Loaded group: " + String.format("%s, %s, %s, %s, %s", group, property, value, world, server));
			setProperty0(group, property, server, world, value);
		}
	}

	@Override
	public void setProperty(String group, String property, String server, String world, String value) {
		try {
			if (getProperty(group, property, server, world) != null) {
				mysql.execute("delete from `tab_groups` where `group` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?", group, property, world, server);
			}
			setProperty0(group, property, server, world, value);
			if (value != null) mysql.execute("insert into `tab_groups` (`group`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)", group, property, value, world, server);
		} catch (SQLException e) {
			TAB.getInstance().getErrorManager().printError("Failed to execute MySQL query", e);
		}
	}
	
	private String querySymbol(boolean isNull) {
		return isNull ? "is" : "=";
	}

	private void setProperty0(String group, String property, String server, String world, String value) {
		if (server != null) {
			perServer.computeIfAbsent(server, s -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
		} else if (world != null) {
			perWorld.computeIfAbsent(world, w -> new HashMap<>()).computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
		} else {
			values.computeIfAbsent(group, g -> new HashMap<>()).put(property, value);
		}
	}

	@Override
	public String[] getProperty(String group, String property, String server, String world) {
		String value;
		if ((value = perServer.getOrDefault(server, new HashMap<>()).getOrDefault(group, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s,server=%s", group, server)};
		}
		if ((value = perServer.getOrDefault(server, new HashMap<>()).getOrDefault(DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s,server=%s", DEFAULT_GROUP, server)};
		}
		if ((value = perWorld.getOrDefault(world, new HashMap<>()).getOrDefault(group, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s,world=%s", group, world)};
		}
		if ((value = perWorld.getOrDefault(world, new HashMap<>()).getOrDefault(DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s,world=%s", DEFAULT_GROUP, world)};
		}
		if ((value = values.getOrDefault(group, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s", group)};
		}
		if ((value = values.getOrDefault(DEFAULT_GROUP, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("group=%s", DEFAULT_GROUP)};
		}
		return new String[0];
	}

	@Override
	public void remove(String group) {
		values.getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, null, null));
		perWorld.keySet().forEach(world -> perWorld.get(world).getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, null, world, null)));
		perServer.keySet().forEach(server -> perServer.get(server).getOrDefault(group, new HashMap<>()).keySet().forEach(property -> setProperty(group, property, server, null, null)));
	}
}