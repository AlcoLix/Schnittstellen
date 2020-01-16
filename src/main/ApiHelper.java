package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Singleton, contains all methods and variables of the used API
 * @author Dennis Rünzler
 *
 */
public class ApiHelper {
	//Variable anlegen, in der die URL gespeichert werden soll
	protected StringBuffer urlString;
	
	/*public void setBaseString(String urlString) {
		this.urlString = urlString; 
	}
	public String getUrlString() {
		return urlString;
	}*/
	
	
	public void setBaseString(String url) {
		urlString = new StringBuffer(url);
	}
	public StringBuffer getUrlString() {
		return urlString;
	}

	// Methoden, die für die Authentifizierung notwendig sind
	
	/* Methode, die den String für die Authentifizierung zusammensetzt, und dabei den String vor und nach dem ? zerschneidet
	 und dazwischen die anderen Methoden ausführt, so dass ? und & an der richtigen Stelle sind */
	public void appendMethod(String method) {
		
		if(urlString.indexOf("?")!=-1) {
			int pos = urlString.indexOf("?");
//			urlString = urlString.substring(0, pos)+"/"+method+urlString.substring(pos);
			urlString.insert(pos, "/"+method);
			// Alternativ über Arrays, ist aber nicht so sauber da der Rechner mehr Rechenoperationen machen muss, wegen dem '+'
			// String[] parts = urlString.split("\\?");
			// urlString += parts[0]+"/"+method+"?"+parts[1];
		}else {
			urlString.append("/").append(method);
		}
	}
	/*// Methode für das Jahr mit Parameterübergabe; hier ein int
	public void appendYear(int year) {
		checkAndAppendConcatenator();
		urlString += "year="+year;
	}
		
	// Methode für das Jahr mit Parameterübergabe; hier ein String
	public void appendYear(String year) {
		appendYear(Integer.parseInt(year));
	}
	
	// Methode für den API-Key
	public void appendCredentials() {
		checkAndAppendConcatenator();
		urlString += "api_key=416952d00c2786b01c36f84da35bd28937656220"; 
	}
	
	// Methode um den String für die Land-Abfrage zu erstellen
	public  void appendCountry(String country) {
		checkAndAppendConcatenator();
		urlString += "country="+country;
	}*/
	
	// Methode, um zu prüfen, ob die URL schon ein ? besitzt
	protected void checkAndAppendConcatenator() {
		if(urlString.indexOf("?")!=-1) {
			urlString.append("&");
		}else {
			urlString.append("?");
		}
	}
	
	/*
	 * Methode, die den urlString zusammensetzt, Verknüpfung aller Methoden zu einer Methode
	 */
	public void appendKeyValue(String key, String value) {
		checkAndAppendConcatenator();
		urlString.append(key).append("=").append(value);
	}
	
	public StringBuffer sendRequest() {
		StringBuffer output = new StringBuffer();
	
	//try-catch Konstrukt; dritte Erweiterung wäre finally; finally ist wie ein Aufräumer, was hier steht, wird auf jeden Fall gemacht
	try {
		// Aufbau der Verbindung mit HTTP-Code-Abfrage
				URL url = new URL(ApiHelper.getInstance().getUrlString().toString());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
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
	//------------- Singleton Code only below
	/**
	 * The Instance, should only exist once
	 */
	protected static ApiHelper instance;
	/**
	 * Constructor should only be called by factory method
	 * protected so it can be used as superclass 
	 */
	protected ApiHelper() {
	}
	/**
	 * Factory Method. Constructor is private to ensure only this method is used to obtain instance
	 * @return The Singleton instance
	 */
	public static ApiHelper getInstance() {
		if (instance == null) {
			instance = new ApiHelper();	
		}
		return instance;
	}
}
