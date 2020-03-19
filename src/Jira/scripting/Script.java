package Jira.scripting;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

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
	
	public static Script load(String name) {
		File f = new File(name);
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
			File[] files = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					name.endsWith(name+".scr");
					return false;
				}
			});
			if(files.length>0) {
				c = load(name);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return c;
	}
	private Script() {
		
	}
}
