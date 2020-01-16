package Jira;

import java.util.Hashtable;

public class Main {
	public static void main(String[] args) {
		//Die Auskommentierten Teile funktionieren aktuell aus unbekannten Gründen nicht.
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		JiraApiHelper.getInstance().appendKeyValue("jql", "project = SBOS AND text ~ \"KW Bader\"");
		JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		JiraApiHelper.getInstance().appendKeyValue("fields", "worklog, key");
		Hashtable<String, String> header = new Hashtable<String, String>();
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); //Der Auth-Header mit API-Token in base64 encoding
		JiraApiHelper.getInstance().sendRequest("GET", header);
	}
}
