package me.neznamy.tab.shared.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import me.neznamy.tab.shared.TAB;

public class MySQL {

	private Connection con;
	private String host;
	private String database;
	private String username;
	private String password;
	private int port;

	public MySQL(String host, int port, String database, String username, String password) throws SQLException {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		openConnection();
	}
	
	private void openConnection() throws SQLException {
		if (!isConnected()) {
			con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			TAB.getInstance().getPlatform().sendConsoleMessage("&a[TAB] Successfully connected to MySQL", true);
		}
	}
	
	public void closeConnection() {
		if (isConnected()) {
			try {
				con.close();
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to close MySQL connection", e);
			}
		}
	}

	private boolean isConnected() {
		try {
			return con != null && !con.isClosed();
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to check MySQL connection", e);
		}
		return false;
	}

	public void execute(String query, Object... vars) throws SQLException {
		if (isConnected()) {
			PreparedStatement ps = null;
			try {
				ps = prepareStatement(query, vars);
				ps.execute();
			} finally {
				ps.close();
			}
			
		} else {
			openConnection();
			execute(query, vars);
		}
	}

	private PreparedStatement prepareStatement(String query, Object... vars) throws SQLException {
		if (isConnected()) {
			PreparedStatement ps = con.prepareStatement(query);
			int i = 0;
			if (query.contains("?") && vars.length != 0) {
				for (Object obj : vars) {
					i++;
					ps.setObject(i, obj);
				}
			}
			return ps;
		} else {
			openConnection();
			return prepareStatement(query, vars);
		}
	}

	public CachedRowSet getCRS(String query, Object... vars) throws SQLException {
		if (isConnected()) {
			PreparedStatement ps = prepareStatement(query, vars);
			ResultSet rs = ps.executeQuery();
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
			rs.close();
			ps.close();
			crs.close();
			return crs;
		} else {
			openConnection();
			return getCRS(query, vars);
		}
	}
}