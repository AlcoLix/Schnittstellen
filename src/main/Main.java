package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// main - Klasse
public class Main {
	/*
	// in einer Klasse muss es static sein, da für alle gleich
	private static String urlString;
	*/
	// Main-Methode
	public static void main(String[] args) {
		//try-catch Konstrukt; dritte Erweiterung wäre finally; finally ist wie ein Aufräumer, was hier steht, wird auf jeden Fall gemacht
		try {
			ApiHelper.getInstance().setBaseString("https://calendarific.com/api/v2");  //Adresse der anzusteuernden API; hier ist es der Endpunkt
			//Aufruf der notwendigen Login-Methoden (abhängig von der API), die weiter unten erstellt wurden
//			ApiHelper.getInstance().appendCountry("DE");
//			ApiHelper.getInstance().appendCredentials();
			ApiHelper.getInstance().appendMethod("holidays");
//			ApiHelper.getInstance().appendYear(2019);
			ApiHelper.getInstance().appendKeyValue("year", "2019");
			ApiHelper.getInstance().appendKeyValue("api_key", "416952d00c2786b01c36f84da35bd28937656220");
			ApiHelper.getInstance().appendKeyValue("country", "DE");
			
			//optionale Parameter
			ApiHelper.getInstance().appendKeyValue("location", "de-by"); //us-ny
			//ApiHelper.getInstance().appendKeyValue("day", "6");
			//ApiHelper.getInstance().appendKeyValue("month", "12");
			ApiHelper.getInstance().appendKeyValue("type", "local");
			
			// Aufbau der Verbindung mit HTTP-Code-Abfrage
			URL url = new URL(ApiHelper.getInstance().getUrlString());
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
}
