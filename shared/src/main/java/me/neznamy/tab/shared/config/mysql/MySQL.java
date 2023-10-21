package me.neznamy.tab.shared.config.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class MySQL {

    private Connection con;
    @NonNull private final String host;
    private final int port;
    @NonNull private final String database;
    @NonNull private final String username;
    @NonNull private final String password;

    public void openConnection() throws SQLException {
        if (isConnected()) return;
        con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        TAB.getInstance().getPlatform().logInfo(IChatBaseComponent.fromColoredText("&aSuccessfully connected to MySQL"));
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
        if (query.contains("?") && vars.length != 0) {
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