package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.MySQL;
import me.neznamy.tab.shared.config.PropertyConfiguration;

public class MySQLUserConfiguration implements PropertyConfiguration {

	private MySQL mysql;

	private Map<String, Map<String, String>> values = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> perWorld = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> perServer = new HashMap<>();

	public MySQLUserConfiguration(MySQL mysql) throws SQLException {
		this.mysql = mysql;
		mysql.execute("create table if not exists tab_users (`user` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
		CachedRowSet crs = mysql.getCRS("select * from tab_users");
		while (crs.next()) {
			String user = crs.getString("user");
			String property = crs.getString("property");
			String value = crs.getString("value");
			String world = crs.getString("world");
			String server = crs.getString("server");
			TAB.getInstance().debug("Loaded user: " + String.format("%s, %s, %s, %s, %s", user, property, value, world, server));
			setProperty0(user, property, server, world, value);
		}
	}

	@Override
	public void setProperty(String user, String property, String server, String world, String value) {
		try {
			if (getProperty(user, property, server, world) != null) {
				mysql.execute("delete from `tab_users` where `user` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?", user, property, world, server);
			}
			setProperty0(user, property, server, world, value);
			if (value != null) mysql.execute("insert into `tab_users` (`user`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)", user, property, value, world, server);
		} catch (SQLException e) {
			TAB.getInstance().getErrorManager().printError("Failed to execute MySQL query", e);
		}
	}
	
	private String querySymbol(boolean isNull) {
		return isNull ? "is" : "=";
	}

	private void setProperty0(String user, String property, String server, String world, String value) {
		if (server != null) {
			perServer.computeIfAbsent(server, s -> new HashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		} else if (world != null) {
			perWorld.computeIfAbsent(world, w -> new HashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		} else {
			values.computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		}
	}

	@Override
	public String[] getProperty(String user, String property, String server, String world) {
		String value = null;
		if ((value = perServer.getOrDefault(server, new HashMap<>()).getOrDefault(user, new HashMap<>()).get(property)) != null) {
			return new String[] {value, "user=" + user + ",server=" + server};
		}
		if ((value = perWorld.getOrDefault(world, new HashMap<>()).getOrDefault(user, new HashMap<>()).get(property)) != null) {
			return new String[] {value, "user=" + user + ",world=" + world};
		}
		if ((value = values.getOrDefault(user, new HashMap<>()).get(property)) != null) {
			return new String[] {value, "user=" + user};
		}
		return null;
	}

	@Override
	public void remove(String user) {
		values.getOrDefault(user, new HashMap<>()).keySet().forEach(property -> setProperty(user, property, null, null, null));
		perWorld.keySet().forEach(world -> perWorld.get(world).getOrDefault(user, new HashMap<>()).keySet().forEach(property -> setProperty(user, property, null, world, null)));
		perServer.keySet().forEach(server -> perServer.get(server).getOrDefault(user, new HashMap<>()).keySet().forEach(property -> setProperty(user, property, server, null, null)));
	}
}