package main;

import java.io.FileWriter;
import java.io.IOException;

public class CSVOutput {
	
	public static void writeoutput (StringBuffer output) {
		try {
			FileWriter writer = new FileWriter("holidays.csv");
			writer.write(output.toString());
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
