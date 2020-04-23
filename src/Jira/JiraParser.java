package Jira;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Jira.utils.StringUtils;

public class JiraParser {

	public static ArrayList<Worklog> parseWorklogSearchResults(StringBuffer json) {
		ArrayList<Worklog> retval = new ArrayList<Worklog>();
		JSONObject content = new JSONObject(json.toString());
		JSONArray issues = content.getJSONArray("issues");
		for (Object object : issues) {
			JSONObject issue = (JSONObject) object;
			retval.addAll(parseWorklogsFromIssueObject(issue));
			//query the worklogs of the subtasks, if any
			JSONObject fields = issue.getJSONObject("fields");
			JSONArray subtasks = fields.getJSONArray("subtasks");
			String epic = "";
			try { 
				epic = fields.getString("customfield_10014");
				if(Epic.getEpic(epic)!=null) {
					epic = Epic.getEpic(epic).toString();
				}
			} catch (JSONException e) {
				
			}
			for (Object sub : subtasks) {
				JSONObject subtask = (JSONObject) sub;
				retval.addAll(queryAndParseWorklogsFromSubtask(subtask.getString("self"),epic));
			}
		}
		return retval;
	}
	public static ArrayList<Task> parseTaskSearchResults(StringBuffer json) {
		ArrayList<Task> retval = new ArrayList<Task>();
		JSONObject content = new JSONObject(json.toString());
		JSONArray issues = content.getJSONArray("issues");
		for (Object object : issues) {
			JSONObject issue = (JSONObject) object;
			retval.add(parseTaskFromIssueObject(issue));
			//query the worklogs of the subtasks, if any
			JSONObject fields = issue.getJSONObject("fields");
			JSONArray subtasks = fields.getJSONArray("subtasks");
			String epic = "";
			try { 
				epic = fields.getString("customfield_10014");
				epic = Epic.getEpic(epic).toString();
			} catch (JSONException e) {
				
			}
			for (Object sub : subtasks) {
				JSONObject subtask = (JSONObject) sub;
				retval.add(queryAndParseTasksFromSubtask(subtask.getString("self"),epic));
			}
		}
		return retval;
	}
	public static Task queryAndParseTasksFromSubtask(String subtaskSelflink, String epic) {
		JiraApiHelper.getInstance().setBaseString(subtaskSelflink);
		JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_SUBTASKS);
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		JSONObject issue = new JSONObject(json.toString());
		Task t =  parseTaskFromIssueObject(issue);
		t.setEpic(epic);
		//Fallback, if the Ordernumber is only set in the Epic
		String ordernumber = "";
		if(StringUtils.isEmpty(t.getOrdernumber())&&!StringUtils.isEmpty(epic)) {
			ordernumber = Epic.getEpic(epic).getOrdernumber();
		}
		String orderposition = "";
		if(StringUtils.isEmpty(t.getOrderposition())&&!StringUtils.isEmpty(epic)) {
			orderposition = Epic.getEpic(epic).getOrderposition();
		}
		if(StringUtils.isEmpty(t.getOrdernumber())) {
			t.setOrdernumber(ordernumber);
		}
		if(StringUtils.isEmpty(t.getOrderposition())) {
			t.setOrderposition(orderposition);
		}
		return t;
	}
	
	public static ArrayList<Worklog> queryAndParseWorklogsFromSubtask(String subtaskSelflink, String epic) {
		JiraApiHelper.getInstance().setBaseString(subtaskSelflink);
		JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_WORKLOG_SUBTASKS);
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		JSONObject issue = new JSONObject(json.toString());
		ArrayList<Worklog> worklogs =  parseWorklogsFromIssueObject( issue);
		for (Worklog worklog : worklogs) {
			worklog.setEpic(epic);
			//Fallback, if the Ordernumber is only set in the Epic
			String ordernumber = "";
			if(StringUtils.isEmpty(worklog.getOrdernumber())&&!StringUtils.isEmpty(epic)) {
				ordernumber = Epic.getEpic(epic).getOrdernumber();
			}
			String orderposition = "";
			if(StringUtils.isEmpty(worklog.getOrderposition())&&!StringUtils.isEmpty(epic)) {
				orderposition = Epic.getEpic(epic).getOrderposition();
			}
			if(StringUtils.isEmpty(worklog.getOrdernumber())) {
				worklog.setOrdernumber(ordernumber);
			}
			if(StringUtils.isEmpty(worklog.getOrderposition())) {
				worklog.setOrderposition(orderposition);
			}
		}
		return worklogs;
	}
	public static String parseNameForSingleTicket(StringBuffer json) {
		JSONObject issue = new JSONObject(json.toString());
		JSONObject fields = issue.getJSONObject("fields");
		String name ="";
		try { 
			name = fields.getString("summary");
		} catch (JSONException e) {
			
		}
		return name;
	}
	public static Task parseTaskFromIssueObject(JSONObject issue) {
		String key = issue.getString("key");
		JSONObject fields = issue.getJSONObject("fields");
		boolean billing = false;
		try {
			billing = fields.getJSONArray("customfield_10029").getJSONObject(0).getString("value").equalsIgnoreCase("Billable");
		} catch (JSONException e) {
			
		}
		String ordernumber ="";
		try {
			ordernumber= fields.getString("customfield_10030");
		} catch (JSONException e) {
			
		}
		String summary ="";
		try { 
			summary= fields.getString("summary");
		} catch (JSONException e) {
			
		}
		String orderposition = "";
		try { 
			orderposition = fields.getString("customfield_10031");
		} catch (JSONException e) {
			
		}
		String customer = "";
		try {
			JSONArray customerList = fields.getJSONArray("customfield_10033");
			for (int i = 0; i < customerList.length(); i++) {
				customer += customerList.getString(i);
				if (i+1<customerList.length()) {
					customer += ", ";
				}
			}
		} catch (JSONException e) {
			
		}
		String epic = "";
		try { 
			epic = fields.getString("customfield_10014");
			if(Epic.getEpic(epic) != null) {
				epic = Epic.getEpic(epic).toString();
				if(StringUtils.isEmpty(ordernumber)) {
					//Fallback, if the Ordernumber is only set in the Epic
					ordernumber = Epic.getEpic(epic).getOrdernumber();
				}
				if(StringUtils.isEmpty(orderposition)) {
					orderposition = Epic.getEpic(epic).getOrderposition();
				}
			}
		} catch (JSONException e) {
			
		}
		String project = "";
		try { 
			project = fields.getJSONObject("project").getString("name");
		} catch (JSONException e) {
			
		}
		String parent = "";
		try { 
			parent = fields.getJSONObject("parent").getString("key");
		} catch (JSONException e) {
			
		}
		Date duedate = null;
		try {
			String due = fields.getString("duedate");
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, Integer.parseInt(due.substring(0, 4)));
			//-1 because in the json, the first month is 1, in Calendar it is 0
			c.set(Calendar.MONTH, Integer.parseInt(due.substring(5, 7))-1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(due.substring(8, 10)));
			duedate = c.getTime();
		} catch (JSONException e) {
			
		}
		Date plandate = null;
		try {
			//Somehow there are two different fields for the planned date.
			String plan="";
			if(fields.has("customfield_10039")){
				plan = fields.getString("customfield_10039");
			}
			if(StringUtils.isEmpty(plan)){
				plan = fields.getString("customfield_10034");
			}
			if(!StringUtils.isEmpty(plan)){
				Calendar c = Calendar.getInstance();
				c.clear();
				c.set(Calendar.YEAR, Integer.parseInt(plan.substring(0, 4)));
				//-1 because in the json, the first month is 1, in Calendar it is 0
				c.set(Calendar.MONTH, Integer.parseInt(plan.substring(5, 7))-1);
				c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(plan.substring(8, 10)));
				plandate = c.getTime();
			}
		} catch (JSONException e) {
			
		}
		String creator = "";
		try { 
			creator = fields.getJSONObject("creator").getString("displayName");
		} catch (JSONException e) {
			
		}
		String responsible = "";
		try { 
			responsible = fields.getJSONObject("customfield_10035").getString("displayName");
		} catch (JSONException e) {
			
		}
		String assignee = "";
		try { 
			assignee = fields.getJSONObject("assignee").getString("displayName");
		} catch (JSONException e) {
			
		}
		long timespentseconds = 0;
		String timespent = "";
		try { 
			timespentseconds = fields.getLong("timespent");
			timespent = getWordtimeForLong(timespentseconds);
		} catch (JSONException e) {
			
		}
		long originalEstimateSeconds = 0;
		String originalEstimate = "";
		try { 
			originalEstimateSeconds = fields.getLong("timespent");
			originalEstimate = getWordtimeForLong(originalEstimateSeconds);
		} catch (JSONException e) {
			
		}
		long remainingEstimateSeconds = 0;
		String remainingEstimate = "";
		try { 
			remainingEstimateSeconds = fields.getLong("timespent");
			remainingEstimate = getWordtimeForLong(remainingEstimateSeconds);
		} catch (JSONException e) {
			
		}
		Task t = new Task();
		t.setAssignee(assignee);
		t.setBillable(billing);
		t.setDueDate(duedate);
		t.setEpic(epic);
		t.setIssueKey(key);
		t.setOrdernumber(ordernumber);
		t.setOrderposition(orderposition);
		t.setParent(parent);
		t.setPlannedDate(plandate);
		t.setProject(project);
		t.setResponsible(responsible);
		t.setSummary(summary);
		t.setTimeEstimate(originalEstimate);
		t.setTimeEstmateSeconds(originalEstimateSeconds);
		t.setTimeSpent(timespent);
		t.setTimeSpentSeconds(timespentseconds);
		t.setTimeEstimateRemaining(remainingEstimate);
		t.setTimeEstmateRemainingSeconds(remainingEstimateSeconds);
		return t;
	}
	private static String getWordtimeForLong(long timeInSeconds) {
		StringBuffer retval = new StringBuffer();
		long temp = timeInSeconds % 60;
		timeInSeconds /= 60;
		if(temp!=0) {
			retval.append(temp).append("s");
		}
		temp = timeInSeconds % 60;
		timeInSeconds /= 60;
		if(temp!=0) {
			retval.insert(0,"m ").insert(0, temp);
		}
		temp = timeInSeconds % 60;
		timeInSeconds /= 60;
		if(temp!=0) {
			retval.insert(0,"h ").insert(0, temp);
		}
		temp = timeInSeconds % 8;
		timeInSeconds /= 8;
		if(temp!=0) {
			retval.insert(0,"d ").insert(0, temp);
		}
		temp = timeInSeconds % 5;
		timeInSeconds /= 5;
		if(temp!=0) {
			retval.insert(0,"w ").insert(0, temp);
		}
		return retval.toString();
	}
	public static ArrayList<Worklog> parseWorklogsFromIssueObject(JSONObject issue) {
		ArrayList<Worklog>retval = new ArrayList<Worklog>();
		String key = issue.getString("key");
		JSONObject fields = issue.getJSONObject("fields");
		JSONObject worklog = fields.getJSONObject("worklog");
		if(worklog.getInt("total")>worklog.getInt("maxResults")) {
			StringBuffer json = JiraApiHelper.getInstance().getWorklogsArray2Issue(key);
			worklog = new JSONObject(json.toString());
		}
		JSONArray worklogs = worklog.getJSONArray("worklogs");
		boolean billing = false;
		try {
			billing = fields.getJSONArray("customfield_10029").getJSONObject(0).getString("value").equalsIgnoreCase("Billable");
		} catch (JSONException e) {
			
		}
		String ordernumber ="";
		try {
			ordernumber= fields.getString("customfield_10030");
		} catch (JSONException e) {
			
		}
		String summary ="";
		try { 
			summary= fields.getString("summary");
		} catch (JSONException e) {
			
		}
		String orderposition = "";
		try { 
			orderposition = fields.getString("customfield_10031");
		} catch (JSONException e) {
			
		}
		String customer = "";
		try {
			JSONArray customerList = fields.getJSONArray("customfield_10033");
			for (int i = 0; i < customerList.length(); i++) {
				customer += customerList.getString(i);
				if (i+1<customerList.length()) {
					customer += ", ";
				}
			}
		} catch (JSONException e) {
			
		}
		String epic = "";
		try { 
			epic = fields.getString("customfield_10014");
			if(Epic.getEpic(epic) != null) {
				epic = Epic.getEpic(epic).toString();
				if(StringUtils.isEmpty(ordernumber)) {
					//Fallback, if the Ordernumber is only set in the Epic
					ordernumber = Epic.getEpic(epic).getOrdernumber();
				}
				if(StringUtils.isEmpty(orderposition)) {
					orderposition = Epic.getEpic(epic).getOrderposition();
				}
			}
		} catch (JSONException e) {
			
		}
		String project = "";
		try { 
			project = fields.getJSONObject("project").getString("name");
		} catch (JSONException e) {
			
		}
		String parent = "";
		try { 
			parent = fields.getJSONObject("parent").getString("key");
		} catch (JSONException e) {
			
		}
		
		for (Object object2 : worklogs) {
			Worklog jiraWorklog = new Worklog();
			JSONObject log = (JSONObject) object2;
			String name = log.getJSONObject("author").getString("displayName");
			String comment = "";
			try { 
				comment= log.getString("comment");
			} catch (JSONException e) {
			}
			String timeSpent = log.getString("timeSpent");
			long timeSpentSeconds = log.getLong("timeSpentSeconds");
			String started = log.getString("started");
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, Integer.parseInt(started.substring(0, 4)));
			//-1 because in the json, the first month is 1, in Calendar it is 0
			c.set(Calendar.MONTH, Integer.parseInt(started.substring(5, 7))-1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(started.substring(8, 10)));
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(started.substring(11, 13)));
			c.set(Calendar.MINUTE, Integer.parseInt(started.substring(14, 16)));
			c.set(Calendar.SECOND, Integer.parseInt(started.substring(17, 19)));
			Date date = c.getTime();
			jiraWorklog.setComment(comment);
			jiraWorklog.setDate(date);
			jiraWorklog.setIssueKey(key);
			jiraWorklog.setTimeSpent(timeSpent);
			jiraWorklog.setUser(name);
			jiraWorklog.setTimeSpentSeconds(timeSpentSeconds);
			jiraWorklog.setBillable(billing);
			jiraWorklog.setOrdernumber(ordernumber);
			jiraWorklog.setOrderposition(orderposition);
			jiraWorklog.setCustomer(customer);
			jiraWorklog.setSummary(summary);
			jiraWorklog.setProject(project);
			jiraWorklog.setEpic(epic);
			jiraWorklog.setParent(parent);
			retval.add(jiraWorklog);
		}
		return retval;
	}
	public static ArrayList<String> parseUsers(StringBuffer json) {
		//Pagination must be added in case there are more than 50 users
		JSONObject content = new JSONObject(json.toString());
		ArrayList<String> users = new ArrayList<String>(content.getInt("total"));
		JSONArray values = content.getJSONArray("values");
		for (Object object : values) {
			JSONObject value = (JSONObject) object;
			if(value.getString("accountType").equalsIgnoreCase("atlassian")) {
				users.add(value.getString("displayName"));
			}
		}
		return users;
	}
	public static String[] parseProjects(StringBuffer json) {
		JSONObject content = new JSONObject(json.toString());
		ArrayList<String> projects = new ArrayList<String>(content.getInt("total"));
		JSONArray values = content.getJSONArray("values");
		for (Object object : values) {
			JSONObject value = (JSONObject) object;
			projects.add(value.getString("name"));
		}
		Collections.sort(projects);
		String[] retval = new String[projects.size()];
		return projects.toArray(retval);
	}
	/**
	 * parses the Epics and adds them to the static List in Epic
	 * @param json the Epics in JSON format
	 */
	public static void parseEpics(StringBuffer json) {
		JSONObject content = new JSONObject(json.toString());
		JSONArray values = content.getJSONArray("issues");
		for (Object object : values) {
			JSONObject value = (JSONObject) object;
			//filters out next gen projects
			if(value.getJSONObject("fields").has("customfield_10011")) {
				Epic e = new Epic();
				e.setKey(value.getString("key"));
				e.setName(value.getJSONObject("fields").getString("customfield_10011"));
				e.setProject(value.getJSONObject("fields").getJSONObject("project").getString("name"));
				String ordernumber ="";
				try { 
					ordernumber= value.getJSONObject("fields").getString("customfield_10030");
				} catch (JSONException ex) {
					
				}
				e.setOrdernumber(ordernumber);
				String orderposition = "";
				try { 
					orderposition = value.getJSONObject("fields").getString("customfield_10031");
				} catch (JSONException ex) {
					
				}
				e.setOrderposition(orderposition);
				Epic.addEpic(e);
			}
		}
	}
	/**
	 * In case there are more results available than are to be returned per call 
	 * @param json the result from the last call
	 * @return the next startAt value or -1 if the end is reached
	 */
	public static int nextStartAt(StringBuffer json) {
		JSONObject content = new JSONObject(json.toString());
		int total = (content.getInt("total"));
		int max = (content.getInt("maxResults"));
		int start = (content.getInt("startAt"));
		if(start+max<total) {
			return start+max; 
		}
		return -1;
	}
	public static StringBuffer parseWorklogsToCsvString(ArrayList<Worklog> worklogs) {
		StringBuffer csvString = new StringBuffer();
		csvString.append("Mitarbeiter").append(";");
		csvString.append("Datum").append(";");
		csvString.append("Uhrzeit").append(";");
		csvString.append("Zeit").append(";");
		csvString.append("Ticket").append(";");
		csvString.append("Bemerkung").append(";");
		csvString.append("Auftrag").append(";");
		csvString.append("Position").append(";");
		csvString.append("Abrechenbar").append(";");
		csvString.append("Kunde").append(";");
		csvString.append("Ticketname").append(";");
		csvString.append("Projekt").append(";");
		csvString.append("Epic").append(";");
		csvString.append("Mutterticket").append(";");
		csvString.append("Zeit in Sekunden").append("\r\n");
		for (Worklog worklog : worklogs) {
			csvString.append(worklog.getUser()).append(";");
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			csvString.append(format.format(worklog.getDate())).append(";");
			format = new SimpleDateFormat("HH:mm:ss");
			csvString.append(format.format(worklog.getDate())).append(";");
			csvString.append(worklog.getTimeSpent()).append(";");
			csvString.append(worklog.getIssueKey()).append(";");
			csvString.append("\"").append(worklog.getComment().replaceAll("\r\n", " ")).append("\"").append(";");
			csvString.append(worklog.getOrdernumber()).append(";");
			csvString.append(worklog.getOrderposition()).append(";");
			csvString.append(worklog.isBillable()).append(";");
			csvString.append(worklog.getCustomer()).append(";");
			csvString.append(worklog.getSummary()).append(";");
			csvString.append(worklog.getProject()).append(";");
			csvString.append(worklog.getEpic()).append(";");
			csvString.append(worklog.getParent()).append(";");
			csvString.append(worklog.getTimeSpentSeconds()).append("\r\n");
		}
		return csvString;
	}

	public static StringBuffer parseTasksToCsvString(ArrayList<Task> tasks) {
		StringBuffer csvString = new StringBuffer();
		csvString.append("Ticket").append(";");
		csvString.append("Verantwortlicher").append(";");
		csvString.append("Bearbeiter").append(";");
		csvString.append("Fälligkeit").append(";");
		csvString.append("geplante Fertigstellung").append(";");
		csvString.append("Auftrag").append(";");
		csvString.append("Position").append(";");
		csvString.append("Abrechenbar").append(";");
		csvString.append("Ticketname").append(";");
		csvString.append("Projekt").append(";");
		csvString.append("Epic").append(";");
		csvString.append("Mutterticket").append(";");
		csvString.append("geplante Zeit").append(";");
		csvString.append("geplante Zeit in Sekunden").append(";");
		csvString.append("gebuchte Zeit").append(";");
		csvString.append("gebuchte Zeit in Sekunden").append(";");
		csvString.append("verbleibende Zeit").append(";");
		csvString.append("verbleibende Zeit in Sekunden").append("\r\n");
		for (Task task : tasks) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			csvString.append(task.getIssueKey()).append(";");
			csvString.append(task.getResponsible()).append(";");
			csvString.append(task.getAssignee()).append(";");
			if(task.getDueDate()!=null) {
				csvString.append(format.format(task.getDueDate())).append(";");
			}else {
				csvString.append(";");
			}
			if(task.getPlannedDate()!=null) {
				csvString.append(format.format(task.getPlannedDate())).append(";");
			}else {
				csvString.append(";");
			}
			csvString.append(task.getOrdernumber()).append(";");
			csvString.append(task.getOrderposition()).append(";");
			csvString.append(task.isBillable()).append(";");
			csvString.append(task.getSummary()).append(";");
			csvString.append(task.getProject()).append(";");
			csvString.append(task.getEpic()).append(";");
			csvString.append(task.getParent()).append(";");
			csvString.append(task.getTimeEstimate()).append(";");
			csvString.append(task.getTimeEstmateSeconds()).append(";");
			csvString.append(task.getTimeSpent()).append(";");
			csvString.append(task.getTimeSpentSeconds()).append(";");
			csvString.append(task.getTimeEstimateRemaining()).append(";");
			csvString.append(task.getTimeEstmateRemainingSeconds()).append("\r\n");
		}
		return csvString;
	}
	
	public static void writeWorklogsToFile(ArrayList<Worklog> worklogs, File f) {
		try {
			StringBuffer buf = JiraParser.parseWorklogsToCsvString(worklogs);
			FileWriter writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
			writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void writeTasksToFile(ArrayList<Task> Tasks, File f) {
		try {
			StringBuffer buf = JiraParser.parseTasksToCsvString(Tasks);
			FileWriter writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
			writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
