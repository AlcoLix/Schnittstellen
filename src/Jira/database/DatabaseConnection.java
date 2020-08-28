package Jira.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private Connection con;
	public static void main(String[] args) {
		getInstance().close();
	}
	
	public void close() {
		try {
			con.close();
			System.out.println("connection to DB closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Singleton Block
	public static DatabaseConnection getInstance() {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}
	private static DatabaseConnection instance;
	private DatabaseConnection() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://localhost;database=JiraAurea;user=sa;password=Part1234";  
			con = DriverManager.getConnection(connectionUrl);
			System.out.println("connection to DB created");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}  
	}
}
