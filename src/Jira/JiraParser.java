package Jira;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JiraParser {

	public static ArrayList<Worklog> parseSearchResults(StringBuffer json) {
		ArrayList<Worklog> retval = new ArrayList<Worklog>();
		JSONObject content = new JSONObject(json.toString());
		JSONArray issues = content.getJSONArray("issues");
		for (Object object : issues) {
			JSONObject issue = (JSONObject) object;
			retval.addAll(parseWorklogFromIssueObject(issue));
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
				retval.addAll(queryAndParseSubtask(subtask.getString("self"),epic));
			}
		}
		return retval;
	}
	public static ArrayList<Worklog> queryAndParseSubtask(String subtaskSelflink, String epic) {
		JiraApiHelper.getInstance().setBaseString(subtaskSelflink);
		JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_SUBTASKS);
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		JSONObject issue = new JSONObject(json.toString());
		ArrayList<Worklog> worklogs =  parseWorklogFromIssueObject( issue);
		for (Worklog worklog : worklogs) {
			worklog.setEpic(epic);
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
	public static ArrayList<Worklog> parseWorklogFromIssueObject(JSONObject issue) {
		ArrayList<Worklog>retval = new ArrayList<Worklog>();
		String key = issue.getString("key");
		JSONObject fields = issue.getJSONObject("fields");
		JSONObject worklog = fields.getJSONObject("worklog");
		JSONArray worklogs = worklog.getJSONArray("worklogs");
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
			epic = Epic.getEpic(epic).toString();
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
	public static String[] parseUsers(StringBuffer json) {
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
		Collections.sort(users);
		String[] retval = new String[users.size()];
		return users.toArray(retval);
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
			Epic e = new Epic();
			e.setKey(value.getString("key"));
			e.setName(value.getJSONObject("fields").getString("customfield_10011"));
			e.setProject(value.getJSONObject("fields").getJSONObject("project").getString("name"));
			Epic.addEpic(e);
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
			csvString.append(worklog.getCustomer()).append(";");
			csvString.append(worklog.getSummary()).append(";");
			csvString.append(worklog.getProject()).append(";");
			csvString.append(worklog.getEpic()).append(";");
			csvString.append(worklog.getParent()).append(";");
			csvString.append(worklog.getTimeSpentSeconds()).append("\r\n");
		}
		return csvString;
	}
}
