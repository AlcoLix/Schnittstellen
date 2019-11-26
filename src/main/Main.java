package main;

// main - Klasse
public class Main {
	/*
	// in einer Klasse muss es static sein, da für alle gleich
	private static String urlString;
	*/
	// Main-Methode
	public static void main(String[] args) {
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
			
			StringBuffer output =  ApiHelper.getInstance().sendRequest();
			StringBuffer parsedoutput = Parser.parse(output);
			CSVOutput.writeoutput(parsedoutput);
	}
}
