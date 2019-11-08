package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
	private static String urlString;
	public static void main(String[] args) {
		try {
			urlString = "https://calendarific.com/api/v2";
			appendCountry("DE");
			appendCredentials();
			appendMethod("holidays");
			appendYear(2019);
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
			}
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(in);
			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void appendMethod(String method) {
		
		if(urlString.contains("?")) {
			int pos = urlString.indexOf('?');
			urlString = urlString.substring(0, pos)+"/"+method+urlString.substring(pos);
			//Alternativ:
//			String[] parts = urlString.split("\\?");
//			urlString += parts[0]+"/"+method+"?"+parts[1];
		}else {
			urlString += "/"+method;
		}
	}
	private static void appendCountry(String country) {
		checkAndAppendConcatenator();
		urlString += "country="+country;
	}
	private static void appendYear(int year) {
		checkAndAppendConcatenator();
		urlString += "year="+year;
	}
	private static void appendYear(String year) {
		appendYear(Integer.parseInt(year));
	}
	private static void appendCredentials() {
		checkAndAppendConcatenator();
		urlString += "api_key=416952d00c2786b01c36f84da35bd28937656220"; 
	}
	private static void checkAndAppendConcatenator() {
		if(urlString.contains("?")) {
			urlString += "&";
		}else {
			urlString += "?";
		}
	}
}
