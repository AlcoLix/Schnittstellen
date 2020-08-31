package Jira.database;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import Jira.AureaWorklog;

public class DatabaseConnection {
	private Connection con;
	public static void main(String[] args) {
		System.out.println("testing the connectionString");
		getInstance().connect();
		getInstance().close();
	}
	public void sendInsertOrUpdate4Aurea(ArrayList<AureaWorklog> aureaWorklogs) {
		for (AureaWorklog worklog : aureaWorklogs) {
			
			//INSERT INTO `ALLOWANCE` (`EmployeeID`, `Year`, `Month`, `OverTime`,`Medical`,
			//		`Lunch`, `Bonus`, `Allowance`) values (10000001, 2014, 4, 10.00, 10.00,
			//		10.45, 10.10, 40.55) ON DUPLICATE KEY UPDATE `EmployeeID` = 10000001
		}
		try {
			Statement st = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		    System.err.println("SQLState: " +
		        ((SQLException)e).getSQLState());
		
		    System.err.println("Error Code: " +
		        ((SQLException)e).getErrorCode());
		
		    System.err.println("Message: " + e.getMessage());
		
		    Throwable t = e.getCause();
		    while(t != null) {
		        System.out.println("Cause: " + t);
		        t = t.getCause();
		    }
		}
	}
	public boolean isConnected() {
		if(con == null) {
			System.out.println("connection to DB is closed");
			return false;
		}
		try {
			if(con.isClosed()) {
				System.out.println("connection to DB is closed");
			}
			return !con.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("connection to DB is closed");
		return false;
	}
	public void connect() {
		if(isConnected()) {
			return;
		}
		try {
			Properties prop = new Properties();
			if(new File("db.prop").exists()) {
				prop.load(new FileReader("db.prop"));
			}else {
				prop.put("user","sa");
				prop.put("pw","Part1234");
				prop.put("ip","localhost");
				prop.put("db", "JiraAurea");
				prop.store(new FileWriter("db.prop"), "");
			}
			String user = prop.getProperty("user","sa");
			String pw = prop.getProperty("pw","Part1234");
			String ip = prop.getProperty("ip","localhost");
			String db = prop.getProperty("db", "JiraAurea");
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://"+ip+";database="+db+";user="+user+";password="+pw;  
			con = DriverManager.getConnection(connectionUrl);
			System.out.println("connection to DB created");
			checkDBSetup();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	private void checkDBSetup() {
		try {
			Statement st = con.createStatement();
			st.execute("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Transfer'");
			ResultSet rs = st.getResultSet();
			int count = 0;
			if(rs != null) {
				rs.next();
				count = rs.getInt(1);
			}
			if(count<1) {
				st.execute("Create Table \"Transfer\"(worklogID varchar(255), \"user\" varchar(255), userID varchar(255), customer varchar(255), customerID varchar(255), ordernumber varchar(255), orderposition varchar(255), date datetime, paymentType varchar(255), paymentMethod varchar(255), startTime datetime, endTime datetime, issueKey varchar(255), comment varchar(255), summary varchar(255), project varchar(255), team varchar(255), timeSpent varchar(255), timeSpentSeconds int, billable char(1), parent varchar(255), epic varchar(255), worklogcreate datetime, worklogupdate datetime, PRIMARY KEY(worklogID))");		
				st.execute("ALTER TABLE \"Transfer\" " + 
						"add createdAt datetime " + 
						"CONSTRAINT DF_Transfer_createdat DEFAULT GETDATE() " + 
						"ALTER TABLE \"Transfer\" " + 
						"add updatedAt datetime " + 
						"CONSTRAINT DF_Transfer_updatedAt DEFAULT GETDATE() ");
				st.execute("create trigger trg_Transfer_update on \"Transfer\" for update as " + 
						"begin " + 
						"  update \"Transfer\" " + 
						"	set updatedAt = getDate() " + 
						"	from \"Transfer\" inner join deleted d " + 
						"	on \"Transfer\".worklogID=d.worklogID " + 
						"end ");
		    	st.close();
			}
		} catch (SQLException e) {
			e.printStackTrace(System.err);
		    System.err.println("SQLState: " +
		        ((SQLException)e).getSQLState());
		
		    System.err.println("Error Code: " +
		        ((SQLException)e).getErrorCode());
		
		    System.err.println("Message: " + e.getMessage());
		
		    Throwable t = e.getCause();
		    while(t != null) {
		        System.out.println("Cause: " + t);
		        t = t.getCause();
		    }
		}
	}
	public void close() {
		if(!isConnected()) {
			return;
		}
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
		
	}
}
