package Jira;

import java.util.ArrayList;

import Jira.utils.StringUtils;

public class Epic {
	private String name;
	private String key;
	private String project;
	private static ArrayList<Epic> list = new ArrayList<Epic>();
	public static int getEpicCount() {
		if(list == null) {
			return 0;
		}
		return list.size();
	}
	public static String[] getEpicStringsByProject(String project) {
		if(StringUtils.isEmpty(project)) {
			String[] retval = new String[list.size()];
			for (int i = 0; i < retval.length; i++) {
				retval[i] = list.get(i).toString();
			} 
			return retval;
		}
		ArrayList<String> sublist = new ArrayList<String>();
		for (Epic epic : list) {
			if(epic.getProject().equals(project)) {
				sublist.add(epic.toString());
			}
		}
		String[] retval = new String[sublist.size()];
		retval =sublist.toArray(retval); 
		return retval;
	}
	public static Epic getEpic(String keyOrName) {
		for (Epic epic : list) {
			if(epic.getKey().equalsIgnoreCase(keyOrName)) {
				return epic;
			}	
			if(epic.getName().equalsIgnoreCase(keyOrName)) {
				return epic;
			}
		}
		return null;
	}
	public static  void addEpic(Epic e) {
		if(!list.contains(e)) {
			list.add(e);
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String toString() {
		return getKey()+" - "+getName();
	}
	
}
