package Jira;

import java.util.Hashtable;

import main.ApiHelper;

public class Main {
	public static void main(String[] args) {
		//Die Auskommentierten Teile funktionieren aktuell aus unbekannten Gründen nicht.
		ApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
//		ApiHelper.getInstance().appendKeyValue("jql", "project = SBOS AND text ~ \"KW Bader\"");
		ApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
//		ApiHelper.getInstance().appendKeyValue("fields", "worklog, key");
		Hashtable<String, String> header = new Hashtable<String, String>();
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); //Der Auth-Header mit API-Token in base64 encoding
		ApiHelper.getInstance().sendRequest("GET", header);
	}
}
