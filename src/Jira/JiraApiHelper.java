package Jira;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;

import org.json.JSONObject;

import Jira.gui.MainFrame;
import Jira.utils.CalendarUtils;
import Jira.utils.StringUtils;
import main.ApiHelper;

public class JiraApiHelper extends ApiHelper {

	public static final String FIELDS_FOR_WORKLOGS = "worklog, key,customfield_10030,customfield_10031,customfield_10033,subtasks,summary,project,customfield_10014,components,creator,assignee,customfield_10029,issuetype";
	public static final String FIELDS_FOR_WORKLOG_SUBTASKS = "worklog, key,customfield_10030,customfield_10031,customfield_10033,summary,project,parent,components,creator,assignee,customfield_10029";
	public static final String FIELDS_FOR_TASKS = "key,customfield_10030,customfield_10031,customfield_10033,subtasks,summary,project,customfield_10014,creator,assignee,dueddate,customfield_10039,customfield_10034,timeoriginalestimate,timeestimate,timespent,customfield_10029,customfield_10035,issuetype";
	public static final String FIELDS_FOR_SUBTASKS = "key,customfield_10030,customfield_10031,customfield_10033,summary,project,parent,creator,assignee,duedate,customfield_10039,customfield_10034,timeoriginalestimate,timeestimate,timespent,customfield_10029,customfield_10035";
	public static final String FIELDS_FOR_EPICS = "key,customfield_10011,customfield_10030,customfield_10031,project";
	
	private static Hashtable<String, Task>taskCache = new Hashtable<String, Task>();
		
	/**
	 * sendet den Request mit den eingewstellten Parametern. der urlString muss gesetzt sein!
	 * @param type GET oder POST
	 * @param header key-value Paare für die Header-PArameter (z.B. Authentifizierung)
	 * @return einen String Buffer mit dem JSON
	 */
	public StringBuffer sendRequest(String type, Hashtable<String, String>header) {
		return sendRequest(type, header, null);
	}
	/**
	 * sendet den Request mit den eingestellten Parametern. der urlString muss gesetzt sein!
	 * @param type GET oder POST
	 * @param header key-value Paare für die Header-PArameter (z.B. Authentifizierung)
	 * @param body der body für POST anfragen
	 * @return einen String Buffer mit dem JSON
	 */
	public StringBuffer sendRequest(String type, Hashtable<String, String>header, String body) {
		StringBuffer output = new StringBuffer();
//		System.out.println(ApiHelper.getInstance().getUrlString().toString());
		//try-catch Konstrukt; dritte Erweiterung wäre finally; finally ist wie ein Aufräumer, was hier steht, wird auf jeden Fall gemacht
		try {
		// Aufbau der Verbindung mit HTTP-Code-Abfrage
				URL url = new URL(ApiHelper.getInstance().getUrlString().toString());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(type);
				conn.setRequestProperty("Accept", "application/json");
				if(header!=null) {
					for (String key : header.keySet()) {
						conn.setRequestProperty(key, header.get(key));	
					}
				}
				if (body != null) {
					conn.setDoOutput(true);
		            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		            conn.setRequestProperty("Content-Type", "application/json");
		            conn.setRequestProperty("User-Agent", "Java client");
					conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
					new DataOutputStream(conn.getOutputStream()).write(body.getBytes("UTF8"));
				}
				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
				}
				InputStreamReader in = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8); // Instanziertes Objekt InputStreamReader namens in, zeigt mir, was ich an Informationen bekomme; kommt von der Klasse InputStream
				BufferedReader br = new BufferedReader(in); // liest zeilenweise und bezieht seine Daten vom InputStreamReader, Daten werden im Puffer zwischengespeichert; kann Geschwindigkeitsvorteile bringen
				String line;
				// Schleife, ob die Daten, die ich bekomme, das Ende erreicht haben oder nicht, wenn ja, dann Verbindung beenden
				while ((line = br.readLine()) != null) {
//					System.out.println(line);
					output.append(line).append("\r\n");
				}
				conn.disconnect();
				MainFrame.addDebugLine(ApiHelper.getInstance().getUrlString().toString());
			} catch (IOException e) // Fehlerhandling, IOException ist höchste Instanz zum Fehler abfangen
				{
				e.printStackTrace(); //das Gleiche wie System.err
			}
		return output;
	}
	
	public StringBuffer sendRequest() {
		return this.sendRequest("GET", null);
	}

	/**
	 * 
	 * @param project can be null
	 * @return all epics of the project or all epics if project is null
	 */
	public String[] queryEpics(String project) {
		if(Epic.getEpicCount() == 0) {
			//Initialize the epics
			setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
			appendKeyValue("validateQuery", "warn");
			appendKeyValue("fields", FIELDS_FOR_EPICS);
			appendKeyValue("jql", "type = Epic");
			Hashtable<String, String> header = new Hashtable<String, String>();
			// Der Auth-Header mit API-Token in base64 encoding
			header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
			StringBuffer json;
			json = JiraApiHelper.getInstance().sendRequest("GET", header);
			JiraParser.parseEpics(json);
			int startAt = 0;
			while((startAt = JiraParser.nextStartAt(json))!=-1) {
				setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
				appendKeyValue("validateQuery", "warn");
				appendKeyValue("fields", FIELDS_FOR_EPICS);
				appendKeyValue("jql", "type = Epic");
				appendKeyValue("maxResults", "100");
				appendKeyValue("startAt", String.valueOf(startAt));
				json = JiraApiHelper.getInstance().sendRequest("GET", header);
				JiraParser.parseEpics(json);
			}
		} 
		//When the epics are initialized, return the saved values
		return Epic.getEpicStringsByProject(project);
	}
	public StringBuffer getWorklogsArray2Issue(String IssueKey) {
		setBaseString("https://partsolution.atlassian.net/rest/api/latest/issue/"+IssueKey+"/worklog");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json;
		json = JiraApiHelper.getInstance().sendRequest("GET", header);
		return json;
	}
	public Task queryIssue2ID(String id) {
		if(taskCache.containsKey(id)) {
			return taskCache.get(id);
		}
		setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		appendKeyValue("validateQuery", "warn");
		appendKeyValue("fields", FIELDS_FOR_TASKS);
		appendKeyValue("jql", "id = "+id);
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json;
		json = JiraApiHelper.getInstance().sendRequest("GET", header);
		Task task = JiraParser.parseTaskFromIssueObject(new JSONObject(json.toString()).getJSONArray("issues").getJSONObject(0));
		taskCache.put(id, task);
		return task;
	}
	public String[] queryUsers() {
		setBaseString("https://partsolution.atlassian.net/rest/api/latest/group/member");
		appendKeyValue("groupname", "jira-software-users");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		ArrayList<String> users = JiraParser.parseUsers(json);
		int startAt = 0;
		while((startAt = JiraParser.nextStartAt(json))!=-1) {
			setBaseString("https://partsolution.atlassian.net/rest/api/latest/group/member");
			appendKeyValue("groupname", "jira-software-users");
			appendKeyValue("startAt", String.valueOf(startAt));
			json = JiraApiHelper.getInstance().sendRequest("GET", header);
			users.addAll( JiraParser.parseUsers(json));
		}
		Collections.sort(users);
		String[] retval = new String[users.size()];
		return users.toArray(retval);
	}
	/*
	 * Methode, die den urlString zusammensetzt, Verknüpfung aller Methoden zu einer Methode
	 */
	public void appendKeyValue(String key, String value) {
		checkAndAppendConcatenator();
		key = performHtmlEncode(key);
		value = performHtmlEncode(value);
		urlString.append(key).append("=").append(value);
	}
	private String performHtmlEncode(String s) {
		String retval = s.replace(" ", "%20"); //in der url darf kein leerzeichen vorkommen, stattdessen muss %20 gesendet werden
		retval = retval.replace("ü", "%C3%BC");
		retval = retval.replace("Ü", "%C3%9C");
		retval = retval.replace("ä", "%C3%A4");
		retval = retval.replace("Ä", "%C3%84");
		retval = retval.replace("ö", "%C3%B6");
		retval = retval.replace("Ö", "%C3%96");
		retval = retval.replace("<", "%3C");
		retval = retval.replace(">", "%3E");
		retval = retval.replace("ß", "%C3%9F");
		return retval;
	}

	/**
	 * since the api filters the tickets, not the worklogs, we need to apply our
	 * filter settings to the worklogList
	 * 
	 * @param list
	 * @param fromDate
	 * @param toDate
	 * @param user
	 */
	public static ArrayList<Worklog> applyFiltersToWorklogList(ArrayList<Worklog> list, Date fromDate, Date toDate, String user){
		ArrayList<Worklog> retval = new ArrayList<Worklog>(list.size());
		for (Worklog worklog : list) {
			boolean keep = true;
			if (fromDate != null) {
				Date d = CalendarUtils.startOfDay(fromDate);
				if (worklog.getDate().before(d)&&!CalendarUtils.isSameDay(d, worklog.getDate())) {
					keep = false;
				}
			}
			if (toDate != null) {
				Date d = CalendarUtils.endOfDay(toDate);
				if (worklog.getDate().after(d)&&!CalendarUtils.isSameDay(d, worklog.getDate())) {
					keep = false;
				}
			}
			if (!StringUtils.isEmpty(user)) {
				if (!worklog.getUser().equalsIgnoreCase(user)) {
					keep = false;
				}
			}
			if (keep) {
				retval.add(worklog);
			}
		}
		return retval;
	}
	
	//------------- Singleton Code only below
	/**
	 * The Instance, should only exist once
	 * Inherited from Superclass
	 */
	//private static JiraApiHelper instance;
	/**
	 * Constructor should only be called by factory method
	 */
	protected JiraApiHelper() {
	}
	/**
	 * Factory Method. Constructor is private to ensure only this method is used to obtain instance
	 * @return The Singleton instance
	 */
	public static JiraApiHelper getInstance() {
		if (instance == null) {
			instance = new JiraApiHelper();	
		}
		return (JiraApiHelper) instance;
	}
}
