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
import java.text.SimpleDateFormat;
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
//	MERGE tableA AS t
//	USING (VALUES 
//	        ('datakeyA1', 'datakeyA2', 'somevaluetoinsertorupdate'), 
//	        ('datakeyB1', 'datakeyB2', 'somevaluetoinsertorupdate'),
//	        ('datakeyC1', 'datakeyC2', 'somevaluetoinsertorupdate')
//	    ) AS s (Key1, Key2, Val)
//	        ON s.Key1 = t.Key1
//	        AND s.Key2 = t.Key2
//	WHEN MATCHED THEN 
//	    UPDATE 
//	    SET    Val = s.Val
//	WHEN NOT MATCHED THEN 
//	    INSERT (Key1, Key2, Val)
//	    VALUES (s.Key1, s.Key2, s.Val);
	public void sendInsertOrUpdate4Aurea(ArrayList<AureaWorklog> aureaWorklogs) {
//		StringBuffer sql = new StringBuffer("INSERT INTO 'TRANSFER' (worklogID, \"user\", userID, customer, customerID, ordernumber, orderposition, date, paymentType, paymentMethod, startTime, endTime, issueKey, comment, summary, project, team, timeSpent, timeSpentSeconds, billable, parent, epic, worklogcreate, worklogupdate, displayText) VALUES ");
		StringBuffer sql = new StringBuffer("MERGE \"Transfer\" AS t USING (VALUES ");
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		for (AureaWorklog worklog : aureaWorklogs) {
			sql.append("(").append("'").append(worklog.getWorklogID()).append("'");
			sql.append(",").append("'").append(worklog.getUser()).append("'");
			sql.append(",").append("'").append(worklog.getUserID()).append("'");
			sql.append(",").append("'").append(worklog.getCustomer()).append("'");
			sql.append(",").append("'").append(worklog.getCustomerID()).append("'");
			sql.append(",").append("'").append(worklog.getOrdernumber()).append("'");
			sql.append(",").append("'").append(worklog.getOrderposition()).append("'");
			sql.append(",").append("'").append(format.format(worklog.getDate())).append("'");
			sql.append(",").append("'").append(worklog.getPaymentType()).append("'");
			sql.append(",").append("'").append(worklog.getPaymentMethod()).append("'");
			sql.append(",").append("'").append(format.format(worklog.getStartTime())).append("'");
			sql.append(",").append("'").append(format.format(worklog.getEndTime())).append("'");
			sql.append(",").append("'").append(worklog.getIssueKey()).append("'");
			sql.append(",").append("'").append(worklog.getComment()).append("'");
			sql.append(",").append("'").append(worklog.getSummary()).append("'");
			sql.append(",").append("'").append(worklog.getProject()).append("'");
			sql.append(",").append("'").append(worklog.getTeam()).append("'");
			sql.append(",").append("'").append(worklog.getTimeSpent()).append("'");
			sql.append(",").append("'").append(worklog.getTimeSpentSeconds()).append("'");
			sql.append(",").append("'").append(worklog.isBillable()?"Y":"N").append("'");
			sql.append(",").append("'").append(worklog.getParent()).append("'");
			sql.append(",").append("'").append(worklog.getEpic()).append("'");
			sql.append(",").append("'").append(format.format(worklog.getCreate())).append("'");
			sql.append(",").append("'").append(format.format(worklog.getUpdate())).append("'");
			sql.append(",").append("'").append(worklog.getDisplayText()).append("'").append("),");
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(") AS s (worklogID, \"user\", userID, customer, customerID, ordernumber, orderposition, date, paymentType, paymentMethod, startTime, endTime, issueKey, comment, summary, project, team, timeSpent, timeSpentSeconds, billable, parent, epic, worklogcreate, worklogupdate, displayText)");
		sql.append(" ON s.worklogID =t.worklogID WHEN MATCHED THEN UPDATE SET \"user\" = s.\"user\",userID = s.userID,customer = s.customer,customerID = s.customerID,ordernumber = s.ordernumber,orderposition = s.orderposition,date = s.date,paymentType = s.paymentType,paymentMethod = s.paymentMethod,startTime = s.startTime,endTime = s.endTime,issueKey = s.issueKey,comment = s.comment,summary = s.summary,project = s.project,team = s.team,timeSpent = s.timeSpent,timeSpentSeconds = s.timeSpentSeconds,billable = s.billable,parent = s.parent,epic = s.epic,worklogcreate = s.worklogcreate, worklogupdate = s.worklogupdate, displayText =  s.displayText");
		sql.append(" WHEN NOT MATCHED THEN INSERT (worklogID, \"user\", userID, customer, customerID, ordernumber, orderposition, date, paymentType, paymentMethod, startTime, endTime, issueKey, comment, summary, project, team, timeSpent, timeSpentSeconds, billable, parent, epic, worklogcreate, worklogupdate, displayText) VALUES");
		sql.append(" (s.worklogID, s.\"user\", s.userID, s.customer, s.customerID, s.ordernumber, s.orderposition, s.date, s.paymentType, s.paymentMethod, s.startTime, s.endTime, s.issueKey, s.comment, s.summary, s.project, s.team, s.timeSpent, s.timeSpentSeconds, s.billable, s.parent, s.epic, s.worklogcreate, s.worklogupdate, s.displayText);");
		
		try {
			Statement st = con.createStatement();
			st.execute(sql.toString());
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
				st.execute("Create Table \"Transfer\"(worklogID varchar(255), \"user\" varchar(255), userID varchar(255), customer varchar(255), customerID varchar(255), ordernumber varchar(255), orderposition varchar(255), date datetime, paymentType varchar(255), paymentMethod varchar(255), startTime datetime, endTime datetime, issueKey varchar(255), comment varchar(255), summary varchar(255), project varchar(255), team varchar(255), timeSpent varchar(255), timeSpentSeconds int, billable char(1), parent varchar(255), epic varchar(255), worklogcreate datetime, worklogupdate datetime, displayText varchar(255), PRIMARY KEY(worklogID))");		
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
			st.execute("select count(*) FROM sys.views where name = 'TransferView'");
			rs = st.getResultSet();
			if(rs != null) {
				rs.next();
				count = rs.getInt(1);
			}
			if(count<1) {
				st.execute("CREATE VIEW TransferView " + 
						"AS " + 
						"SELECT worklogID, userID, [user], customer, customerID, CASE WHEN LEN(ordernumber) = 0 THEN '' ELSE Concat(ordernumber, '- ', orderposition) END AS [order], paymentType, paymentMethod, issueKey, team, " + 
						"displayText, updatedAt, createdAt, FORMAT(startTime, N'HH:mm') AS startTime, FORMAT(endTime, N'HH:mm') AS endTime, FORMAT(date, N'dd.MM.yyyy') AS date " + 
						"FROM dbo.Transfer");
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
