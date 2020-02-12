package Jira;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

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
			String key = issue.getString("key");
			JSONObject fields = issue.getJSONObject("fields");
			JSONObject worklog = fields.getJSONObject("worklog");
			JSONArray worklogs = worklog.getJSONArray("worklogs");
			String ordernumber ="";
			try { 
				ordernumber= fields.getString("customfield_10030");
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
				c.set(Calendar.MONTH, Integer.parseInt(started.substring(5, 7)));
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
				retval.add(jiraWorklog);
			}
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
		//Pagination must be added in case there are more than 50 users
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
	public static StringBuffer parseWorklogsToCsvString(ArrayList<Worklog> worklogs) {
		StringBuffer csvString = new StringBuffer();
		csvString.append("Mitarbeiter").append(";");
		csvString.append("Datum").append(";");
		csvString.append("Zeit").append(";");
		csvString.append("Ticket").append(";");
		csvString.append("Bemerkung").append(";");
		csvString.append("Auftrag").append(";");
		csvString.append("Position").append(";");
		csvString.append("Kunde").append(";");
		csvString.append("Zeit in Sekunden").append("\r\n");
		for (Worklog worklog : worklogs) {
			csvString.append(worklog.getUser()).append(";");
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			csvString.append(format.format(worklog.getDate())).append(";");
			csvString.append(worklog.getTimeSpent()).append(";");
			csvString.append(worklog.getIssueKey()).append(";");
			csvString.append("\"").append(worklog.getComment().replaceAll("\r\n", " ")).append("\"").append(";");
			csvString.append(worklog.getOrdernumber()).append(";");
			csvString.append(worklog.getOrderposition()).append(";");
			csvString.append(worklog.getCustomer()).append(";");
			csvString.append(worklog.getTimeSpentSeconds()).append("\r\n");
		}
		return csvString;
	}
}
