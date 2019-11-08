package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// main - Klasse
public class Main {
	//in einer Klasse muss es static sein, da für alle gleich
	private static String urlString;
	// Main-Methode
	public static void main(String[] args) {
		//try-catch Konstrukt; dritte Erweiterung wäre finally; finally ist wie ein Aufräumer, was hier steht, wird auf jeden Fall gemacht
		try {
			urlString = "https://calendarific.com/api/v2"; //Adresse der anzusteuernden API; hier ist es der Endpunkt
			//Aufruf der notwendigen Login-Methoden (abhängig von der API), die weiter unten erstellt wurden
			appendCountry("DE");
			appendCredentials();
			appendMethod("holidays");
			appendYear(2019);
			
			// Aufbau der Verbindung mit HTTP-Code-Abfrage
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}
			
			InputStreamReader in = new InputStreamReader(conn.getInputStream()); // Instanziertes Objekt InputStreamReader namens in, zeigt mir, was ich an Informationen bekomme; kommt von der Klasse InputStream
			BufferedReader br = new BufferedReader(in); // liest zeilenweise und bezieht seine Daten vom InputStreamReader, Daten werden im Puffer zwischengespeichert; kann Geschwindigkeitsvorteile bringen
			String output;
			// Schleife, ob die Daten, die ich bekomme, das Ende erreicht haben oder nicht, wenn ja, dann Verbindung beenden
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();
		} catch (IOException e) // Fehlerhandling, IOException ist höchste Instanz zum Fehler abfangen
			{
			e.printStackTrace(); //das Gleiche wie System.err
		}
	}
	
	// Methoden, die für die Authentifizierung notwendig sind
	
	/* Methode, die den String für die Authentifizierung zusammensetzt, und dabei den String vor und nach dem ? zerschneidet
	 und dazwischen die anderen Methoden ausführt, so dass ? und & an der richtigen Stelle sind */
	private static void appendMethod(String method) {
		
		if(urlString.contains("?")) {
			int pos = urlString.indexOf('?');
			urlString = urlString.substring(0, pos)+"/"+method+urlString.substring(pos);
			// Alternativ über Arrays, ist aber nicht so sauber da der Rechner mehr Rechenoperationen machen muss, wegen dem '+'
			// String[] parts = urlString.split("\\?");
			// urlString += parts[0]+"/"+method+"?"+parts[1];
		}else {
			urlString += "/"+method;
		}
	}
	
	// Methode um den String für die Land-Abfrage zu erstellen
	private static void appendCountry(String country) {
		checkAndAppendConcatenator();
		urlString += "country="+country;
	}
	// Methode für das Jahr mit Parameterübergabe; hier ein int
	private static void appendYear(int year) {
		checkAndAppendConcatenator();
		urlString += "year="+year;
	}
	// Methode für das Jahr mit Parameterübergabe; hier ein String
	private static void appendYear(String year) {
		appendYear(Integer.parseInt(year));
	}
	// Methode für den API-Key
	private static void appendCredentials() {
		checkAndAppendConcatenator();
		urlString += "api_key=416952d00c2786b01c36f84da35bd28937656220"; 
	}
	// Methode, um zu prüfen, ob die URL schon ein ? besitzt
	private static void checkAndAppendConcatenator() {
		if(urlString.contains("?")) {
			urlString += "&";
		}else {
			urlString += "?";
		}
	}
}
