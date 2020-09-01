package Jira.scripting;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import Jira.AureaWorklog;
import Jira.JiraApiHelper;
import Jira.JiraParser;
import Jira.database.DatabaseConnection;

@XmlRootElement
public class AureaScript {

	private String name = "";
	private int daysEarlier;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public int getDaysEarlier() {
		return daysEarlier;
	}
	public void setDaysEarlier(int daysEarlier) {
		this.daysEarlier = daysEarlier;
	}
	
	public void execute() {
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.add(Calendar.DAY_OF_MONTH, -getDaysEarlier());
		
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/worklog/updated");
		JiraApiHelper.getInstance().appendKeyValue("since=", String.valueOf(c.getTimeInMillis()));
		JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json;
		json = JiraApiHelper.getInstance().sendRequest("GET", header);
		ArrayList<AureaWorklog> aureaList = JiraParser.parseAureaSearchResults(json);
		String nextPage;
		while((nextPage = JiraParser.nextPage(json)) != null) {
			JiraApiHelper.getInstance().setBaseString(nextPage);
			json = JiraApiHelper.getInstance().sendRequest("GET", header);
			aureaList.addAll(JiraParser.parseAureaSearchResults(json));
		}
		File f = new File("latest.csv");
		JiraParser.writeAureaToFile(aureaList, f);
		DatabaseConnection.getInstance().connect();
		DatabaseConnection.getInstance().sendInsertOrUpdate4Aurea(aureaList);
	}
	
	public void save() {
		File f = new File(name+".scr");
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(AureaScript.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static AureaScript load(String name) {
		File f;
		if(name.endsWith(".scr")) {
			f = new File(name);
		}else {
			f = new File(name+".scr");
		}
		AureaScript c = new AureaScript();
		c.setName(name);
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(AureaScript.class);
			c  = (AureaScript)context.createUnmarshaller().unmarshal(f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public static AureaScript getScript(String name) {
		AureaScript c = new AureaScript();
		c.setName(name);
		try {
			File f = new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			File[] files = new File[0];
			if(f.isDirectory()&&f.exists()) {
				files = f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if(name.endsWith(".scr")) {
							return filename.endsWith(name);
						}
						return filename.endsWith(name+".scr");
					}
				});
			}
			if(files.length>0) {
				c = load(name);
			} else {
				 files = f.getParentFile().listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if(name.endsWith(".scr")) {
							return filename.endsWith(name);
						}
						return filename.endsWith(name+".scr");
					}
				});
				 if(files.length>0) {
					c = load(name);
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return c;
	}
	private AureaScript() {
		
	}
}
