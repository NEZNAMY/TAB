package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.sql.rowset.CachedRowSet;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.MySQL;

public class MySQLUserConfiguration implements PropertyConfiguration {

	private final MySQL mysql;

	private final WeakHashMap<TabPlayer, Map<String, String>> values = new WeakHashMap<>();
	private final Map<String, WeakHashMap<TabPlayer, Map<String, String>>> perWorld = new HashMap<>();
	private final Map<String, WeakHashMap<TabPlayer, Map<String, String>>> perServer = new HashMap<>();

	public MySQLUserConfiguration(MySQL mysql) throws SQLException {
		this.mysql = mysql;
		mysql.execute("create table if not exists tab_users (`user` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
	}

	@Override
	public void setProperty(String user, String property, String server, String world, String value) {
		TabPlayer p = getPlayer(user);
		user = user.toLowerCase();
		try {
			if (getProperty(user, property, server, world) != null) {
				mysql.execute("delete from `tab_users` where `user` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?", user, property, world, server);
			}
			if (p != null) setProperty0(p, property, server, world, value);
			if (value != null) mysql.execute("insert into `tab_users` (`user`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)", user, property, value, world, server);
		} catch (SQLException e) {
			TAB.getInstance().getErrorManager().printError("Failed to execute MySQL query", e);
		}
	}
	
	private String querySymbol(boolean isNull) {
		return isNull ? "is" : "=";
	}

	private void setProperty0(TabPlayer user, String property, String server, String world, String value) {
		if (server != null) {
			perServer.computeIfAbsent(server, s -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		} else if (world != null) {
			perWorld.computeIfAbsent(world, w -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		} else {
			values.computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
		}
	}

	@Override
	public String[] getProperty(String user, String property, String server, String world) {
		TabPlayer p = getPlayer(user);
		String value;
		if ((value = perServer.getOrDefault(server, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("user=%s,server=%s", user, server)};
		}
		if ((value = perWorld.getOrDefault(world, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("user=%s,world=%s", user, world)};
		}
		if ((value = values.getOrDefault(p, new HashMap<>()).get(property)) != null) {
			return new String[] {value, String.format("user=%s", user)};
		}
		return new String[0];
	}

	@Override
	public void remove(String player) {
		try {
			mysql.execute("delete from `tab_users` where `user` = ?", player);
		} catch (SQLException e) {
			TAB.getInstance().getErrorManager().printError("Failed to execute MySQL query", e);
		}
		TabPlayer user = getPlayer(player);
		if (user == null) return;
		values.remove(user);
		perWorld.keySet().forEach(world -> perWorld.get(world).remove(user));
		perServer.keySet().forEach(server -> perServer.get(server).remove(user));
	}

	private TabPlayer getPlayer(String string) {
		TabPlayer p = TAB.getInstance().getPlayer(string);
		if (p == null) {
			try {
				p = TAB.getInstance().getPlayer(UUID.fromString(string));
			} catch (IllegalArgumentException ex) {
				//not a valid uuid
			}
		}
		return p;
	}
	
	public void load(TabPlayer player) {
		TAB.getInstance().getCPUManager().runTask("Loading MySQL data", () -> {
			
			try {
				CachedRowSet crs = mysql.getCRS("select * from `tab_users` where `user` = ?", player.getName().toLowerCase());
				while (crs.next()) {
					String user = crs.getString("user");
					String property = crs.getString("property");
					String value = crs.getString("value");
					String world = crs.getString("world");
					String server = crs.getString("server");
					TAB.getInstance().debug("Loaded user line: " + String.format("%s, %s, %s, %s, %s", user, property, value, world, server));
					setProperty0(player, property, server, world, value);
				}
				CachedRowSet crs2 = mysql.getCRS("select * from `tab_users` where `user` = ?", player.getUniqueId().toString());
				while (crs2.next()) {
					String user = crs2.getString("user");
					String property = crs2.getString("property");
					String value = crs2.getString("value");
					String world = crs2.getString("world");
					String server = crs2.getString("server");
					TAB.getInstance().debug("Loaded user line: " + String.format("%s, %s, %s, %s, %s", user, property, value, world, server));
					setProperty0(player, property, server, world, value);
				}
				TAB.getInstance().debug("Loaded MySQL data of " + player.getName());
				if (crs.size() > 0 || crs2.size() > 0) {
					player.forceRefresh();
				}
			} catch (SQLException e) {
				TAB.getInstance().getErrorManager().printError("Failed to load data of " + player.getName() + " from MySQL", e);
			}
		});
	}
}