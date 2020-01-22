package Jira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

import main.ApiHelper;

public class JiraApiHelper extends ApiHelper {

	/**
	 * sendet den Request mit den eingewstellten Parametern. der urlString muss gesetzt sein!
	 * @param type GET oder POST
	 * @param header key-value Paare für die Header-PArameter (z.B. Authentifizierung)
	 * @return einen String Buffer mit dem JSON
	 */
	public StringBuffer sendRequest(String type, Hashtable<String, String>header) {
		StringBuffer output = new StringBuffer();
		System.out.println(ApiHelper.getInstance().getUrlString().toString());
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
				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
				}
				InputStreamReader in = new InputStreamReader(conn.getInputStream()); // Instanziertes Objekt InputStreamReader namens in, zeigt mir, was ich an Informationen bekomme; kommt von der Klasse InputStream
				BufferedReader br = new BufferedReader(in); // liest zeilenweise und bezieht seine Daten vom InputStreamReader, Daten werden im Puffer zwischengespeichert; kann Geschwindigkeitsvorteile bringen
				String line;
				// Schleife, ob die Daten, die ich bekomme, das Ende erreicht haben oder nicht, wenn ja, dann Verbindung beenden
				while ((line = br.readLine()) != null) {
					System.out.println(line);
					output.append(line).append("\r\n");
				}
				conn.disconnect();
			} catch (IOException e) // Fehlerhandling, IOException ist höchste Instanz zum Fehler abfangen
				{
				e.printStackTrace(); //das Gleiche wie System.err
			}
		return output;
	}
	
	public StringBuffer sendRequest() {
		return this.sendRequest("GET", null);
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
