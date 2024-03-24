package me.neznamy.tab.shared.config.mysql;

import java.sql.SQLException;
import java.util.*;

import javax.sql.rowset.CachedRowSet;

import me.neznamy.tab.shared.config.PropertyConfiguration;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQLUserConfiguration implements PropertyConfiguration {

    private final MySQL mysql;

    private final WeakHashMap<TabPlayer, Map<String, Object>> values = new WeakHashMap<>();
    private final Map<String, WeakHashMap<TabPlayer, Map<String, Object>>> perWorld = new HashMap<>();
    private final Map<String, WeakHashMap<TabPlayer, Map<String, Object>>> perServer = new HashMap<>();

    public MySQLUserConfiguration(@NotNull MySQL mysql) throws SQLException {
        this.mysql = mysql;
        mysql.execute("create table if not exists tab_users (`user` varchar(64), `property` varchar(16), `value` varchar(1024), world varchar(64), server varchar(64))");
    }

    @Override
    public void setProperty(@NotNull String user, @NotNull String property, @Nullable String server, @Nullable String world, @Nullable String value) {
        TabPlayer p = getPlayer(user);
        String lowercaseUser = user.toLowerCase();
        try {
            if (getProperty(lowercaseUser, property, server, world) != null) {
                mysql.execute("delete from `tab_users` where `user` = ? and `property` = ? and world " + querySymbol(world == null) + " ? and server " + querySymbol(server == null) + " ?", lowercaseUser, property, world, server);
            }
            if (p != null) setProperty0(p, property, server, world, value);
            if (value != null) mysql.execute("insert into `tab_users` (`user`, `property`, `value`, `world`, `server`) values (?, ?, ?, ?, ?)", lowercaseUser, property, value, world, server);
        } catch (SQLException e) {
            TAB.getInstance().getErrorManager().mysqlQueryFailed(e);
        }
    }

    private String querySymbol(boolean isNull) {
        return isNull ? "is" : "=";
    }

    private void setProperty0(@NotNull TabPlayer user, @NotNull String property, @Nullable String server, @Nullable String world, @Nullable String value) {
        if (world != null) {
            perWorld.computeIfAbsent(world, w -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
        } else if (server != null) {
            perServer.computeIfAbsent(server, s -> new WeakHashMap<>()).computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
        } else {
            values.computeIfAbsent(user, g -> new HashMap<>()).put(property, value);
        }
    }

    @Override
    public String[] getProperty(@NotNull String user, @NotNull String property, @Nullable String server, @Nullable String world) {
        TabPlayer p = getPlayer(user);
        Object value;
        if ((value = perWorld.getOrDefault(world, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("user=%s,world=%s", user, world)};
        }
        if ((value = perServer.getOrDefault(server, new WeakHashMap<>()).getOrDefault(p, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("user=%s,server=%s", user, server)};
        }
        if ((value = values.getOrDefault(p, new HashMap<>()).get(property)) != null) {
            return new String[] {toString(value), String.format("user=%s", user)};
        }
        return new String[0];
    }

    @Override
    public void remove(@NotNull String player) {
        try {
            mysql.execute("delete from `tab_users` where `user` = ?", player);
        } catch (SQLException e) {
            TAB.getInstance().getErrorManager().mysqlQueryFailed(e);
        }
        TabPlayer user = getPlayer(player);
        if (user == null) return;
        values.remove(user);
        perWorld.keySet().forEach(world -> perWorld.get(world).remove(user));
        perServer.keySet().forEach(server -> perServer.get(server).remove(user));
    }

    @Override
    public @NotNull Map<String, Object> getGlobalSettings(@NotNull String name) {
        throw new UnsupportedOperationException("Not supported for users");
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerWorldSettings(@NotNull String name) {
        throw new UnsupportedOperationException("Not supported for users");
    }

    @Override
    public @NotNull Map<String, Map<String, Object>> getPerServerSettings(@NotNull String name) {
        throw new UnsupportedOperationException("Not supported for users");
    }

    @Override
    public @NotNull Set<String> getAllEntries() {
        throw new UnsupportedOperationException("Not supported for users");
    }

    private TabPlayer getPlayer(@NotNull String string) {
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

    public void load(@NotNull TabPlayer player) {
        TAB.getInstance().getCPUManager().runTask(() -> {

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
                TAB.getInstance().getErrorManager().mysqlQueryFailed(e);
            }
        });
    }
}