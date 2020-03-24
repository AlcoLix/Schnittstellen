package Jira;

import Jira.gui.MainFrame;
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
