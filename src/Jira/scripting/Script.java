package Jira.scripting;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import Jira.JiraApiHelper;
import Jira.JiraParser;
import Jira.Worklog;
import Jira.utils.StringUtils;

@XmlRootElement
@XmlSeeAlso(ScriptStep.class)
public class Script {
	private String name = "";
	private ArrayList<ScriptStep> steps = new ArrayList<ScriptStep>();
	
	public ArrayList<ScriptStep> getSteps() {
		return steps;
	}

	public void setSteps(ArrayList<ScriptStep> steps) {
		this.steps = steps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void save() {
		File f = new File(name+".scr");
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Script.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public void execute() {
		for (ScriptStep step :getSteps()) {
			if(!StringUtils.isEmpty(step.getSavePath())) {
				ArrayList<Worklog> worklogList;
				JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
				JiraApiHelper.getInstance().appendKeyValue("jql",step.getSearchString());
				JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
				JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
				JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
				Hashtable<String, String> header = new Hashtable<String, String>();
				// Der Auth-Header mit API-Token in base64 encoding
				header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
				StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
				worklogList = JiraParser.parseSearchResults(json);
				int startAt = 0;
				while ((startAt = JiraParser.nextStartAt(json)) != -1) {
					JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
					JiraApiHelper.getInstance().appendKeyValue("jql", step.getSearchString());
					JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
					JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
					JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
					JiraApiHelper.getInstance().appendKeyValue("startAt", String.valueOf(startAt));
					json = JiraApiHelper.getInstance().sendRequest("GET", header);
					worklogList.addAll(JiraParser.parseSearchResults(json));
				}
				worklogList = JiraApiHelper.applyFiltersToWorklogList(worklogList, step.getCalculatedStartDate(), step.getCalculatedEndDate(), step.getUser());
				String filename =step.getSavePath();
				if(!filename.endsWith(".csv")) {
					filename+=".csv";
				}
				File f = new File(filename);
				JiraParser.writeWorklogsToFile(worklogList, f);
			}
		}
	}
	
	public static Script load(String name) {
		File f;
		if(name.endsWith(".scr")) {
			f = new File(name);
		}else {
			f = new File(name+".scr");
		}
		Script c = new Script();
		c.setName(name);
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Script.class);
			c  = (Script)context.createUnmarshaller().unmarshal(f);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public static Script getScript(String name) {
		Script c = new Script();
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
	private Script() {
		
	}
}
