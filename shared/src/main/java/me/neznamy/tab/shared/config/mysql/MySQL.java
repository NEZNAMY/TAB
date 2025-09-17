package me.neznamy.tab.shared.config.mysql;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.Properties;

@RequiredArgsConstructor
public class MySQL {

    private Connection con;
    @NotNull private final MySQLConfiguration configuration;

    public void openConnection() throws SQLException {
        if (isConnected()) return;
        Properties properties = new Properties();
        properties.setProperty("user", configuration.getUsername());
        properties.setProperty("password", configuration.getPassword());
        properties.setProperty("useSSL", String.valueOf(configuration.isUseSSL()));
        properties.setProperty("characterEncoding", "UTF-8");
        con = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", configuration.getHost(), configuration.getPort(), configuration.getDatabase()), properties);
        TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Successfully connected to MySQL", TabTextColor.GREEN));
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

    @NotNull
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

    @NotNull
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