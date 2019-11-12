package main;

/**
 * Singleton, contains all methods and variables of the used API
 * @author Dennis R�nzler
 *
 */
public class ApiHelper {
	//Variable anlegen, in der die URL gespeichert werden soll
	private String urlString;
	public void setBaseString(String urlString) {
		this.urlString = urlString; 
	}
	public String getUrlString() {
		return urlString;
	}

	// Methoden, die f�r die Authentifizierung notwendig sind
	
	/* Methode, die den String f�r die Authentifizierung zusammensetzt, und dabei den String vor und nach dem ? zerschneidet
	 und dazwischen die anderen Methoden ausf�hrt, so dass ? und & an der richtigen Stelle sind */
	public void appendMethod(String method) {
		
		if(urlString.contains("?")) {
			int pos = urlString.indexOf('?');
			urlString = urlString.substring(0, pos)+"/"+method+urlString.substring(pos);
			// Alternativ �ber Arrays, ist aber nicht so sauber da der Rechner mehr Rechenoperationen machen muss, wegen dem '+'
			// String[] parts = urlString.split("\\?");
			// urlString += parts[0]+"/"+method+"?"+parts[1];
		}else {
			urlString += "/"+method;
		}
	}
	/*// Methode f�r das Jahr mit Parameter�bergabe; hier ein int
	public void appendYear(int year) {
		checkAndAppendConcatenator();
		urlString += "year="+year;
	}
		
	// Methode f�r das Jahr mit Parameter�bergabe; hier ein String
	public void appendYear(String year) {
		appendYear(Integer.parseInt(year));
	}
	
	// Methode f�r den API-Key
	public void appendCredentials() {
		checkAndAppendConcatenator();
		urlString += "api_key=416952d00c2786b01c36f84da35bd28937656220"; 
	}
	
	// Methode um den String f�r die Land-Abfrage zu erstellen
	public  void appendCountry(String country) {
		checkAndAppendConcatenator();
		urlString += "country="+country;
	}*/
	
	// Methode, um zu pr�fen, ob die URL schon ein ? besitzt
	private void checkAndAppendConcatenator() {
		if(urlString.contains("?")) {
			urlString += "&";
		}else {
			urlString += "?";
		}
	}
	/*
	 * Methode, die den urlString zusammensetzt, Verkn�pfung aller Methoden zu einer Methode
	 */
	public void appendKeyValue(String key, String value) {
		checkAndAppendConcatenator();
		urlString += key + "=" + value;
	}
	//------------- Singleton Code only below
	/**
	 * The Instance, should only exist once
	 */
	private static ApiHelper instance;
	/**
	 * Constructor should only be called by factory method 
	 */
	private ApiHelper() {
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
