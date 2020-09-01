package Jira;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Jira.gui.MainFrame;
import Jira.scripting.AureaScript;
import Jira.scripting.Script;

public class Main {
	public static void main(String[] args) {
		if(args.length<1) {
			MainFrame.getInstance();
		} else if(args[0].equalsIgnoreCase("help")) {
			System.out.println("Anwendung:\r\nScript <Scriptname(n)>");
		} else {
			JiraApiHelper.getInstance().queryEpics(null);
			for (int i = 0; i < args.length; i++) {
				boolean isAurea = false;
				try {
					BufferedReader reader = new BufferedReader(new  FileReader(args[i]));
					String line = reader.readLine();
					//First line is the header and can be skipped
					while((line = reader.readLine())!=null) {
						if(line.toLowerCase().contains("aureascript")) {
							isAurea = true;
						}
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(isAurea) {
					AureaScript s =AureaScript.getScript(args[i]);
					s.execute();
				} else {
					Script s = Script.getScript(args[i]);
					if(s.getSteps().size()>0) {
						s.execute();
						System.out.println("Script "+args[i]+" wurde geladen und ausgeführt");
					}else {
						System.out.println("Script "+args[i]+" konnte nicht geladen werden");
					}
				}
			}
		}
	}
}
