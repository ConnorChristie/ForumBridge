package net.chillercraft.api;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.chillercraft.forum.Main;

public class DatabaseHandler {
	private SQL core;
	private Connection connection;
	private String dblocation;
	private String username;
	private String password;
	private String database;

	public DatabaseHandler(SQL core, String dbLocation, String database, String username, String password) {
		this.core = core;
		this.dblocation = dbLocation;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	private void openConnection() throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = DriverManager.getConnection("jdbc:mysql://" + this.dblocation + "/" + this.database, this.username, this.password);
		} catch (ClassNotFoundException e) {
			Main.log.warn("ClassNotFoundException! " + e.getMessage());
		} catch (SQLException e) {
			Main.log.warn("SQLException! " + e.getMessage());
		}
	}

	public Boolean checkConnection() {
		if (this.connection == null) {
			try {
				openConnection();
				return Boolean.valueOf(true);
			} catch (MalformedURLException ex) {
				Main.log.warn("MalformedURLException! " + ex.getMessage());
			} catch (InstantiationException ex) {
				Main.log.warn("InstantiationExceptioon! " + ex.getMessage());
			} catch (IllegalAccessException ex) {
				Main.log.warn("IllegalAccessException! " + ex.getMessage());
			}
			return Boolean.valueOf(false);
		}
		return Boolean.valueOf(true);
	}

	public void closeConnection() {
		try {
			if (this.connection != null)
				this.connection.close();
		} catch (Exception e) {
			Main.log.warn("Failed to close database connection! " + e.getMessage());
		}
	}

	public Connection getConnection() throws MalformedURLException, InstantiationException, IllegalAccessException {
		if (this.connection == null) {
			openConnection();
		}
		return this.connection;
	}

	public ResultSet sqlQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			statement.setQueryTimeout(10);

			ResultSet result = statement.executeQuery(query);

			return result;
		} catch (SQLException ex) {
			Main.log.warn("Error at SQL Query: " + ex.getMessage());
		}
		return null;
	}

	public void insertQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			statement.executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet"))
				Main.log.warn("Error at SQL INSERT Query: " + ex);
		}
	}

	public void updateQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			statement.executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet"))
				Main.log.warn("Error at SQL UPDATE Query: " + ex);
		}
	}

	public void deleteQuery(String query) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			statement.executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet"))
				Main.log.warn("Error at SQL DELETE Query: " + ex);
		}
	}

	public Boolean checkTable(String table) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			Connection connection = getConnection();
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery("SELECT * FROM " + table);

			if (result == null)
				return Boolean.valueOf(false);
			if (result != null)
				return Boolean.valueOf(true);
		} catch (SQLException ex) {
			if (ex.getMessage().contains("exist")) {
				return Boolean.valueOf(false);
			}
			Main.log.warn("Error at SQL Query: " + ex.getMessage());
		}

		if (sqlQuery("SELECT * FROM " + table) == null)
			return Boolean.valueOf(true);
		return Boolean.valueOf(false);
	}

	public Boolean wipeTable(String table) throws MalformedURLException, InstantiationException, IllegalAccessException {
		try {
			if (!this.core.checkTable(table).booleanValue()) {
				Main.log.warn("Error at Wipe Table: table, " + table + ", does not exist");
				return Boolean.valueOf(false);
			}
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			String query = "DELETE FROM " + table + ";";
			statement.executeUpdate(query);

			return Boolean.valueOf(true);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet"))
				Main.log.warn("Error at SQL WIPE TABLE Query: " + ex);
		}
		return Boolean.valueOf(false);
	}

	public Boolean createTable(String query) {
		try {
			if (query == null) {
				Main.log.warn("SQL Create Table query empty.");
				return Boolean.valueOf(false);
			}
			Statement statement = this.connection.createStatement();
			statement.execute(query);
			return Boolean.valueOf(true);
		} catch (SQLException ex) {
			Main.log.warn(ex.getMessage());
		}
		return Boolean.valueOf(false);
	}
}