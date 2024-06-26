package me.neznamy.tab.shared.config.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class MySQL {

    private Connection con;
    @NotNull private final String host;
    private final int port;
    @NotNull private final String database;
    @NotNull private final String username;
    @NotNull private final String password;
    private final boolean useSSL;

    public void openConnection() throws SQLException {
        if (isConnected()) return;
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("useSSL", String.valueOf(useSSL));
        properties.setProperty("characterEncoding", "UTF-8");
        con = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", host, port, database), properties);
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GREEN + "Successfully connected to MySQL"));
    }
    
    public void closeConnection() throws SQLException {
        if (isConnected()) con.close();
    }

    private boolean isConnected() throws SQLException {
        return con != null && !con.isClosed();
    }

    public void execute(@NonNull String query, @Nullable Object... vars) throws SQLException {
        try (PreparedStatement ps = prepareStatement(query, vars)) {
            ps.execute();
        }
    }

    private PreparedStatement prepareStatement(@NonNull String query, @Nullable Object... vars) throws SQLException {
        if (!isConnected()) openConnection();
        PreparedStatement ps = con.prepareStatement(query);
        int i = 0;
        if (query.contains("?")) {
            for (Object obj : vars) {
                i++;
                ps.setObject(i, obj);
            }
        }
        return ps;
    }

    public CachedRowSet getCRS(@NonNull String query, @NonNull Object... vars) throws SQLException {
        PreparedStatement ps = prepareStatement(query, vars);
        ResultSet rs = ps.executeQuery();
        CachedRowSet crs;
        try {
            crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return crs;
        } finally {
            rs.close();
            ps.close();
        }
    }
}